package com.example.tetris.control;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tetris.Config;
import com.example.tetris.MainActivity;
import com.example.tetris.R;
import com.example.tetris.model.Boxes;
import com.example.tetris.model.Maps;
import com.example.tetris.model.Music;
import com.example.tetris.model.Score;

/**
 * Created by Administrator on 2018/3/11.
 */

public class GameControl implements View.OnClickListener{
    MainActivity mGame;
    // 地图模型
    Maps mapsModel;
    // 方块模型
    public Boxes boxesModel;
    // 分数模型
    Score scoreModel;
    // 音乐模型
    Music musicModel;
    // 方块大小
    public int boxSize;
    // 游戏暂停状态
    public static boolean isPause;
    // 游戏结束状态
    public boolean isOver;
    // 游戏胜利状态
    public boolean isVictory;

    // 自动下落的线程
    public Thread downThread;
    // 与activity进行通讯
    Handler handler;

    AlertDialog.Builder builder;

    // 装载控件数组用于换色
    TextView[] textViews;
    LinearLayout[] linearLayouts;

    // 上下文
    private Context mContext;

    public LinearLayout LL_main;
    public LinearLayout LL_1;
    public LinearLayout LL_2;
    public RelativeLayout RL_1;
    public RelativeLayout RL_2;
    public TextView tv_RECORD;
    public TextView tv_record;
    public TextView tv_LEVEL;
    public TextView tv_level;
    public TextView tv_SCORE;
    public TextView tv_score;
    public TextView tv_NEXT;

    public GameControl(Handler handler, Context context) {
        this.handler = handler;
        this.mContext = context;
        mGame = (MainActivity) context;
        initData();
    }

    /**
     * 初始化数据
     */
    public void initData() {
        // 获得屏幕宽度
        int width = getScreenWidth(mContext);
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
        // 实例化分数模型（在这里面获取游戏最高记录)
        scoreModel = new Score(mContext);
        // 实例化音乐模型
        musicModel = new Music(mContext);

        LL_main = mGame.findViewById(R.id.LL_main);
        LL_1 = mGame.findViewById(R.id.LL_1);
        LL_2 = mGame.findViewById(R.id.LL_2);
        RL_1 = mGame.findViewById(R.id.RL_1);
        RL_2 = mGame.findViewById(R.id.RL_2);
        tv_RECORD = mGame.findViewById(R.id.tv_RECORD);
        tv_record = mGame.findViewById(R.id.tv_record);
        tv_LEVEL = mGame.findViewById(R.id.tv_LEVEL);
        tv_level = mGame.findViewById(R.id.tv_level);
        tv_SCORE = mGame.findViewById(R.id.tv_SCORE);
        tv_score = mGame.findViewById(R.id.tv_score);
        tv_NEXT = mGame.findViewById(R.id.tv_NEXT);
        textViews = new TextView[]{tv_RECORD, tv_record,
                tv_LEVEL, tv_level, tv_SCORE, tv_score, tv_NEXT};
        linearLayouts = new LinearLayout[]{LL_main, LL_1, LL_2};

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
        // 播放BGM
        musicModel.playBGM();
        // 播放开始音效
        musicModel.playSound(1);
        // 清楚地图
        mapsModel.cleanMaps();
        // 暂停取消
        isPause = false;
        // 结束取消
        isOver = false;
        // 胜利取消
        isVictory = false;
        // 分数，等级清零
        scoreModel.initScore();
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
                            // 休眠的毫秒
                            sleep(Config.sleepTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // 休眠完，自动执行一次下落
                        // 下落前判断是否处于暂停状态或者结束状态，如果是，跳过下落和重绘
                        if (isOver || isPause) {
                            continue;
                        }
                        moveBottom();
                        // 子线程不能弹出弹窗，所以让handler发送
                        if (isOver) {
                            // 通知主线程弹出游戏结束弹窗
                            Message msg = new Message();
                            msg.obj = "setOver";
                            handler.sendMessage(msg);
                            continue;
                        }
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
        // 对钻戒的move单独处理
        if (isVictory) {
            boxesModel.moveDiamondRing(1,0);
            // 每下落一格后就判断到达中间了没有
            if (boxesModel.boxDiamondRing[0][0] == 8) {
                // 钻戒只能下落到第九行，这样比较居中美观
                isPause = true;
            }
            return true;
        }
        // 下落一格成功（不会被卡住），不做处理
        if (boxesModel.move(1,0, mapsModel, isPause)) {
            return true;
        } else {
            // 不能再移动了，堆积处理，对应地图map的位置设为True
            for (int i = 0; i < boxesModel.boxes.length; i++) {
                mapsModel.maps[boxesModel.boxes[i].x][boxesModel.boxes[i].y] = true;
                mapsModel.colors[boxesModel.boxes[i].x][boxesModel.boxes[i].y] = boxesModel.boxColor;
            }
            // 堆积完，判断消行处理
            // 返回消除的行数
            int lines = mapsModel.cleanLine();
            if (lines > 0) {
                // 如果消除的行数大于0，播放消除音效
                musicModel.playSound(2);
            }
            // 根据消除的行数进行加分
            scoreModel.addScore(lines);
            // 消除完判断分数，如果达到十万分，那么游戏胜利
            if (Score.score >= 100000) {
                setVictory();
                return false;
            }
            // 生成新的方块
            boxesModel.newBoxes();
            // 每生成一个新的就可以判断游戏是否结束了
            isOver = ifOverGame();
            // 如果游戏结束
            if (isOver) {
                // 判断更新记录
                scoreModel.updateRecord();
                // 播放失败音效
                musicModel.playSound(4);
                setOver();
            }
            return false;
        }
    }

    /**
     * 游戏胜利的处理
     */
    public void setVictory(){
        isVictory = true;
        // 如果游戏胜利，清空界面，下一块预览变成问号
        mapsModel.cleanMaps();
        // 画问号和钻戒
        boxesModel.newDiamondRing();
        // 控制下落速度
        Config.sleepTime = 800;
        // 此时分数已经是10W分，等级已经是10，记录也是10W。
        scoreModel.updateRecord();
        // 播放胜利音效
        musicModel.playSound(3);
    }

    /**
     * 游戏失败的处理
     */
    public void setOver(){
        // 弹出通知栏通知游戏结束
        builder = new AlertDialog.Builder(mContext);
        builder.setTitle("游戏结束！")
                .setPositiveButton("不服！再来",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                startGame();
                            }
                        })
                .setCancelable(false)
                .create().show();
    }

    /**
     * 开始游戏的通知栏
     */
    public void setStart() {
        builder = new AlertDialog.Builder(mContext);
        builder.setTitle("真的要开始新游戏吗？")
                .setPositiveButton("快开",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                if (isPause && !isVictory) {
                                    pauseGame();
                                }
                                startGame();
                            }
                        })
                .setNegativeButton("按错了",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                if (isPause && !isVictory) {
                                    pauseGame();
                                }
                            }
                        })
                .setCancelable(false)
                .create().show();
    }

    /**
     * 退出游戏的通知栏
     */
    public void setExit() {
        //弹出通知窗口询问是否退出
        builder = new AlertDialog.Builder(mContext);
        builder.setTitle("QAQ真的要走吗！")
                .setPositiveButton("再玩会",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0,
                                                int arg1) {
                                if (isPause && !isVictory) {
                                    pauseGame();
                                }
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
        if (boxesModel.boxes == null) {
            // boxs为空表示游戏还没开始呢
            return;
        }
        isPause = !isPause;
        // 通知主界面重绘
        Message msg = new Message();
        msg.obj = "pause";
        // 0表示现在是暂停状态
        msg.arg1 = (isPause ? 0 : 1);
        handler.sendMessage(msg);
    }

    /**
     * 辅助线开关
     * @param
     */
    public void setGuideLine(){
        Config.isGuideLine = !Config.isGuideLine ;
        // 通知主界面重绘
        Message msg = new Message();
        msg.obj = "guideLine";
        // 0表示现在是辅助线开
        msg.arg1 = (Config.isGuideLine ? 0 : 1);
        handler.sendMessage(msg);
        String tmp = (Config.isGuideLine ? "开" : "关");
        Toast.makeText(mContext, "辅助线：" + tmp, Toast.LENGTH_SHORT).show();
    }

    /**
     * 音效开关
     * @param
     */
    private void setMusic() {
        Config.isMusic = !Config.isMusic;
        if (Config.isMusic && boxesModel.boxes != null) {
            // 如果允许播放音乐并且游戏已经开始了
            musicModel.playBGM();
        } else {
            musicModel.stopBGM();
        }
        // 通知主界面重绘
        Message msg = new Message();
        msg.obj = "setMusic";
        // 0表示现在是音乐开
        msg.arg1 = (Config.isMusic ? 0 : 1);
        handler.sendMessage(msg);
        String tmp = (Config.isMusic ? "开" : "关");
        Toast.makeText(mContext, "音效：" + tmp, Toast.LENGTH_SHORT).show();
    }

    /**
     * 控制绘制
     * @param canvas
     */
    public void draw(Canvas canvas) {
        // 绘制地图
        mapsModel.drawMaps(canvas);
        // 绘制方块
        if (isVictory) {
            boxesModel.drawDiamondRing(canvas);
        } else {
            boxesModel.drawBoxes(canvas);
        }
        // 绘制地图辅助线
        mapsModel.drawLines(canvas);
        // 绘制状态
        mapsModel.drawState(canvas, isPause, isVictory);
    }

    public void onClick(int id) {
        // 播放按钮对应音效
        musicModel.playSound(id);
        switch (id) {
            case R.id.btn_Left:
                // 左
                if (isVictory) return;
                boxesModel.move(0, -1, mapsModel, isPause);
                break;
            case R.id.btn_Top:
                // 上
                if (isVictory) return;
                boxesModel.rotate(mapsModel, isPause);
                break;
            case R.id.btn_Right:
                // 右
                if (isVictory) return;
                boxesModel.move(0, 1, mapsModel, isPause);
                break;
            case R.id.btn_Down:
                // 下
                if (isVictory) return;
                moveBottom();
                break;
            case R.id.btn_Start:
                // 当游戏处在胜利状态或暂停状态时，不暂停，其他的都要暂停游戏
                if (!isPause && !isVictory) {
                    pauseGame();
                }
                // 开始
                setStart();
                break;
            case R.id.btn_Pause:
                // 暂停
                if (isVictory) return;
                pauseGame();
                break;
            case R.id.btn_QuickDown:
                // 快速下落
                if (isVictory) return;
                while (true) {
                    // 如果下落失败，说明快速下落成功
                    if (!moveBottom()) {
                        break;
                    }
                }
                break;
            case R.id.btn_Exit:
                // 当游戏处在胜利状态或暂停状态时，不暂停，其他的都要暂停游戏
                if (!isPause && !isVictory) {
                    pauseGame();
                }
                setExit();
                break;
            case R.id.btn_Line:
                setGuideLine();
                break;
            case R.id.btn_Music:
                setMusic();
                break;
            case R.id.btn_Chang:
                setColor();
                break;
        }
    }

    /**
     * 绘制下一块的预览区域
     * @param canvas
     */
    public void drawNext(Canvas canvas, int width) {
        if (isVictory) {
            boxesModel.drawMark(canvas, width);
        } else {
            boxesModel.drawNext(canvas, width);
        }
    }

    /**
     * 换肤功能，设置颜色后切换对应颜色
     */
    public void setColor() {
        if (!isPause) pauseGame();
        Dialog dialogColor = new Dialog(mGame, R.style.dialog);
        // 设置布局
        View viewColor = mGame.getLayoutInflater().inflate(R.layout.dialog_color_select, null);
        // 给该布局上的点击事件绑定监听事件
        viewColor.findViewById(R.id.LL_PINK).setOnClickListener(this);
        viewColor.findViewById(R.id.LL_BLUE).setOnClickListener(this);
        viewColor.findViewById(R.id.LL_PURPLE).setOnClickListener(this);
        viewColor.findViewById(R.id.LL_BLACK).setOnClickListener(this);
        viewColor.findViewById(R.id.LL_YELLOW).setOnClickListener(this);

        // 设置dialog加载布局，没有title
        dialogColor.setContentView(viewColor, new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT));

        Window window = dialogColor.getWindow();
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = mGame.getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wl.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        // 设置显示位置
        dialogColor.onWindowAttributesChanged(wl);
        dialogColor.show();
        dialogColor.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isPause) pauseGame();
            }
        });
    }


    /**
     * 捕捉点击事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LL_PINK:
                for (LinearLayout linearLayout : linearLayouts) {
                    linearLayout.setBackgroundResource(R.drawable.pink_boder);
                }
                for (TextView textView : textViews) {
                    textView.setTextColor(Color.parseColor("#ff9999"));
                }
                RL_1.setBackgroundResource(R.drawable.pink_boder2);
                RL_2.setBackgroundResource(R.drawable.pink_boder);
                break;
            case R.id.LL_BLUE:
                for (LinearLayout linearLayout : linearLayouts) {
                    linearLayout.setBackgroundResource(R.drawable.blue_boder);
                }
                for (TextView textView : textViews) {
                    textView.setTextColor(Color.parseColor("#6699FF"));
                }
                RL_1.setBackgroundResource(R.drawable.blue_boder2);
                RL_2.setBackgroundResource(R.drawable.blue_boder);
                break;
            case R.id.LL_PURPLE:
                for (LinearLayout linearLayout : linearLayouts) {
                    linearLayout.setBackgroundResource(R.drawable.purple_boder);
                }
                for (TextView textView : textViews) {
                    textView.setTextColor(Color.parseColor("#CC00FF"));
                }
                RL_1.setBackgroundResource(R.drawable.purple_boder2);
                RL_2.setBackgroundResource(R.drawable.purple_boder);
                break;
            case R.id.LL_BLACK:
                for (LinearLayout linearLayout : linearLayouts) {
                    linearLayout.setBackgroundResource(R.drawable.black_boder);
                }
                for (TextView textView : textViews) {
                    textView.setTextColor(Color.parseColor("#000000"));
                }
                RL_1.setBackgroundResource(R.drawable.black_boder2);
                RL_2.setBackgroundResource(R.drawable.black_boder);
                break;
            case R.id.LL_YELLOW:
                for (LinearLayout linearLayout : linearLayouts) {
                    linearLayout.setBackgroundResource(R.drawable.yellow_boder);
                }
                for (TextView textView : textViews) {
                    textView.setTextColor(Color.parseColor("#FFCC00"));
                }
                RL_1.setBackgroundResource(R.drawable.yellow_boder2);
                RL_2.setBackgroundResource(R.drawable.yellow_boder);
                break;
        }
    }
}