package com.example.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // 游戏区域行列数
    int mGameRow = 20;
    int mGameCol = 10;
    // 游戏区域宽高
    int xWidth, xHeight;
    // 游戏区域控件
    View view;

    // 辅助线画笔
    Paint linePaint;
    // 方块画笔
    Paint boxPaint;
    // 表示俄罗斯方块当前旋转状态，有四种
    int boxState = 1;
    // 表示当前是在移动还是在旋转，有两种
    int moveOrotate;
    // 地图
    boolean[][] maps;
    // 方块
    Point[] boxs;
    // 方块大小
    int boxSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initView();
        initListener();
    }

    /**
     * 初始化数据
     */
    public void initData() {
        // 获得屏幕宽度
        int width = getScreenWidth(this);
        // 设置游戏区域宽度 = 屏幕宽度 * 3/5
        xWidth = width * 3/5;
        // 设置游戏区域宽度 = 宽度 * 2，15是是具体情况而定的。
        xHeight = xWidth * 2 - 15;
        // 初始化地图
        maps = new boolean[mGameCol][mGameRow];
        // 当前下落方块
        boxs = new Point[]{new Point(3, 0), new Point(3, 1), new Point(4, 1), new Point(5, 1)};
        // 初始化方块大小（即为10）
        boxSize = xWidth / maps.length;
        Log.d("woca", "initData: " + boxSize);
    }
    /**
     * 初始化视图
     */
    public void initView() {
        // 初始化画笔
        linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        boxPaint = new Paint();
        boxPaint.setColor(Color.BLACK);
        //一般都会把抗锯齿打开
        linePaint.setAntiAlias(true);
        boxPaint.setAntiAlias(true);
        // 得到父容器
        FrameLayout layoutGame = (FrameLayout) findViewById(R.id.Fr_Game);
        // 实例化游戏区域
        view = new View(this) {
            // 重写游戏区域绘制
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                // 方块绘制
                for (int i = 0; i < boxs.length; i++) {
                    canvas.drawRect(boxs[i].x*boxSize,
                            boxs[i].y*boxSize,
                            boxs[i].x*boxSize+boxSize,
                            boxs[i].y*boxSize+boxSize, boxPaint);
                }

                // 地图辅助线
                for (int x = 0; x < maps.length + 1; x++) {
                    canvas.drawLine(x*boxSize, 0, x*boxSize, view.getHeight(), linePaint);
                }
                for (int y = 0; y < maps[0].length + 1; y++) {
                    canvas.drawLine(0, y*boxSize, view.getWidth(), y*boxSize, linePaint);
                }
            }
        };
        // 设置游戏区域大小
        view.setLayoutParams(new FrameLayout.LayoutParams(xWidth, xHeight));
        // 设置游戏背景颜色
        //view.setBackgroundColor(Color.GRAY);
        // 添加到父容器
        layoutGame.addView(view);
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
     * 初始化监听
     */
    public void initListener() {
        findViewById(R.id.btn_Left).setOnClickListener(this);
        findViewById(R.id.btn_Top).setOnClickListener(this);
        findViewById(R.id.btn_Right).setOnClickListener(this);
        findViewById(R.id.btn_Down).setOnClickListener(this);
        findViewById(R.id.btn_Start).setOnClickListener(this);
        findViewById(R.id.btn_Pause).setOnClickListener(this);

    }

    /**
     * 捕捉点击事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Left:
                moveOrotate = 0;
                move(-1, 0);
                // 左
                break;
            case R.id.btn_Top:
                moveOrotate = 1;
                rotate();
                // 旋转完要对位置进行修正
                correct();
                // 上
                break;
            case R.id.btn_Right:
                moveOrotate = 0;
                move(1, 0);
                // 右
                break;
            case R.id.btn_Down:
                moveOrotate = 0;
                move(0, 1);
                // 下
                break;
            case R.id.btn_Start:
                // 开始
                break;
            case R.id.btn_Pause:
                // 暂停
                break;
        }

        // 每次点击刷新视图，重绘view
        view.invalidate();
    }

    /**
     * 移动的方法
     * @param dx 横坐标偏移量
     * @param dy 纵坐标偏移量
     * @return
     */
    public boolean move(int dx, int dy) {
        // 左右下的移动如果出界则不准移动
        if (moveOrotate == 0) {
            // 把方块预移动的点传入判断边界
            for (int i = 0; i < boxs.length; i++) {
                if (checkBoundary(boxs[i].x + dx, boxs[i].y + dy) > 0) {
                    return false;
                }
            }
        }
        // 遍历当前下落方块的所有单元块
        for (int i = 0; i < boxs.length; i++) {
            boxs[i].x += dx;
            boxs[i].y += dy;
        }
        return true;
    }

    /**
     * 旋转
     * @return
     */
    public boolean rotate() {
        // 遍历当前下落方块的所有单元块，每个都绕中心点顺时针旋转九十度
        for (int i = 0; i < boxs.length; i++) {
            // 旋转算法（笛卡尔公式）顺时针旋转boxs[0]这个点转九十度
            int checkX = -boxs[i].y + boxs[0].y + boxs[0].x;
            int checkY = boxs[i].x - boxs[0].x + boxs[0].y;
            boxs[i].x = checkX;
            boxs[i].y = checkY;
        }
        return true;
    }

    /**
     * 判断出界
     * @return 大于0的为出界，等于0的为未出界
     */
    public int checkBoundary(int x, int y) {
        if (x < 0) {
            return 1;
        } else if (y < 0) {
            return 2;
        } else if (x >= maps.length) {
            return 3;
        } else if (y >= maps[0].length) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * 对笛卡尔旋转进行位置的修正，包括边界处旋转的修正
     */
    public void correct() {
        if (boxState == 1) {
            move(1, 0);
            boxState = 2;
        } else if (boxState == 2) {
            move(0, 1);
            boxState = 3;
        } else if (boxState == 3) {
            move(-1, 0);
            boxState = 4;
        } else if (boxState == 4) {
            move(0, -1);
            boxState = 1;
        }
        // 对边界处旋转的修正
        correctBoundary();
    }

    /**
     * 判断旋转是否出界，出界则修正，不出界则终止函数
     */
    public void correctBoundary() {
        // 先找到出界原因
        int tmp = 0;
        for (int i = 0; i < boxs.length; i++) {
            if (checkBoundary(boxs[i].x, boxs[i].y) > 0) {
                tmp = checkBoundary(boxs[i].x, boxs[i].y);
                break;
            }
        }
        // 然后针对出界原因修正
        if (tmp == 1) {
            for (int i = 0; i < boxs.length; i++) {
                boxs[i].x += 1;
            }
        } else if (tmp == 2) {
            for (int i = 0; i < boxs.length; i++) {
                boxs[i].y += 1;
            }
        } else if (tmp == 3) {
            for (int i = 0; i < boxs.length; i++) {
                boxs[i].x -= 1;
            }
        } else if (tmp == 4) {
            for (int i = 0; i < boxs.length; i++) {
                boxs[i].y -= 1;
            }
        } else {
            // 不出界，无需修正，终止函数
            return;
        }
        // 然后再判断是否还需要修正
        correctBoundary();
    }
}