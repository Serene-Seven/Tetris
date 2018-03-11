package com.example.tetris.control;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.tetris.Config;
import com.example.tetris.R;
import com.example.tetris.model.Boxes;
import com.example.tetris.model.Maps;

/**
 * Created by Administrator on 2018/3/11.
 */

public class GameControl {
    // 地图模型
    Maps mapsModel;
    // 方块模型
    Boxes boxesModel;
    // 方块大小
    public int boxSize;
    // 游戏暂停状态
    public boolean isPause;
    // 游戏结束状态
    public boolean isOver;

    // 自动下落的线程
    public Thread downThread;
    // 与activity进行通讯
    Handler handler;

    public GameControl(Handler handler, Context context) {
        this.handler = handler;
        initData(context);
    }

    /**
     * 初始化数据
     */
    public void initData(Context context) {
        // 获得屏幕宽度
        int width = getScreenWidth(context);
        // 设置游戏区域宽度 = 屏幕宽度 * 3/5
        Config.yWith = width * 3/5;
        // 设置游戏区域宽度 = 宽度 * 2，15是是具体情况而定的。
        Config.xHeight = Config.yWith * 2 - 15;
        // 初始化方块大小（即为10）
        boxSize = Config.yWith / Config.mGameCol;
        // 实例化地图模型
        mapsModel = new Maps(boxSize, Config.yWith, Config.xHeight);
        // 实例化方块模型
        boxesModel = new Boxes(boxSize);
    }

    /**
     * 获得屏幕宽度
     * @param context
     * @return 屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * 开始游戏
     */
    public void startGame(){
        // 清楚地图
        mapsModel.cleanMaps();
        // 暂停取消
        isPause = false;
        // 结束取消
        isOver = false;
        // 生成新的方块
        boxesModel.newBoxes();
        // 自动下落
        if (downThread == null) {
            downThread = new Thread() {
                @Override
                public void run() {
                    super.run();
                    while (true) {
                        try {
                            // 休眠500毫秒
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // 休眠完，自动执行一次下落
                        // 下落前判断是否处于暂停状态或者结束状态，如果是，跳过下落和重绘
                        if (isOver || isPause) {
                            continue;
                        }
                        moveBottom();
                        // 通知主线程刷新view
                        Message msg = new Message();
                        msg.obj = "invalidate";
                        handler.sendMessage(msg);
                    }
                }
            };
            downThread.start();
        }
    }

    /**
     * 下落
     */
    public boolean moveBottom() {
        if (isPause || isOver || boxesModel.boxes == null) {
            return false;
        }
        // 下落一格成功，不做处理
        if (boxesModel.move(1,0, mapsModel, isOver, isPause)) {
            return true;
        } else {
            // 不能再移动了，堆积处理，对应地图map的位置设为True
            for (int i = 0; i < boxesModel.boxes.length; i++) {
                mapsModel.maps[boxesModel.boxes[i].x][boxesModel.boxes[i].y] = true;
            }
            // 堆积完，判断消行处理
            mapsModel.cleanLine();
            // 生成新的方块
            boxesModel.newBoxes();
            // 每生成一个新的就可以判断游戏是否结束了
            isOver = ifOverGame();
            return false;
        }
    }

    /**
     * 判断游戏是否结束
     */
    public boolean ifOverGame() {
        if (boxesModel.boxes == null) {
            return false;
        }
        for (int i = 0; i < boxesModel.boxes.length; i++) {
            if (mapsModel.maps[boxesModel.boxes[i].x][boxesModel.boxes[i].y]) {
                return true;
            }
        }
        return false;
    }

    /**
     * 暂停游戏
     */
    public void pauseGame(){
        if (isOver || boxesModel.boxes == null) {
            // boxs为空表示游戏还没开始呢
            return;
        }
        if (isPause) {
            isPause = false;
            Message msg = new Message();
            msg.obj = "pause";
            handler.sendMessage(msg);
        } else {
            isPause = true;
            Message msg = new Message();
            msg.obj = "continue";
            handler.sendMessage(msg);
        }
    }

    /**
     * 控制绘制
     * @param canvas
     */
    public void draw(Canvas canvas) {
        // 绘制地图
        mapsModel.drawMaps(canvas);
        // 绘制方块
        boxesModel.drawBoxes(canvas);
        // 绘制地图辅助线
        mapsModel.drawLines(canvas);
        // 绘制状态
        mapsModel.drawState(canvas, isOver, isPause);
    }

    public void onClick(int id, Context context) {
        switch (id) {
            case R.id.btn_Left:
                // 左
                boxesModel.move(0, -1, mapsModel, isOver, isPause);
                break;
            case R.id.btn_Top:
                // 上
                boxesModel.rotate(mapsModel, isOver, isPause);
                break;
            case R.id.btn_Right:
                // 右
                boxesModel.move(0, 1, mapsModel, isOver, isPause);
                break;
            case R.id.btn_Down:
                // 下
                moveBottom();
                break;
            case R.id.btn_Start:
                // 开始
                startGame();
                break;
            case R.id.btn_Pause:
                // 暂停
                pauseGame();
                break;
            case R.id.btn_QuickDown:
                // 快速下落
                while (true) {
                    // 如果下落失败，说明快速下落成功
                    if (!moveBottom()) {
                        break;
                    }
                }
                break;
            case R.id.btn_Exit:
                // 弹出通知窗口询问是否退出，没有暂停先暂停
                if (!isPause) {
                    pauseGame();
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("QAQ真的要走吗！")
                        .setPositiveButton("再玩会",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        arg0.dismiss();
                                        pauseGame();
                                    }
                                })
                        .setNegativeButton("溜了溜了",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0,
                                                        int arg1) {
                                        Message msg = new Message();
                                        msg.obj = "exit";
                                        handler.sendMessage(msg);
                                    }
                                })
                        .setCancelable(false)
                        .create().show();
                break;
        }
    }

    /**
     * 绘制下一块的预览区域
     * @param canvas
     */
    public void drawNext(Canvas canvas, int width) {
        boxesModel.drawNext(canvas, width);
    }
}