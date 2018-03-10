package com.example.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    // 游戏区域行列数，x对应行数，y对应列数
    int mGameRow = 20;
    int mGameCol = 10;
    // 游戏区域宽高
    int xWidth, xHeight;
    // 游戏区域控件
    View view;

    // 地图画笔
    Paint mapPaint;
    // 辅助线画笔
    Paint linePaint;
    // 方块画笔
    Paint boxPaint;
    // 表示俄罗斯方块当前旋转状态，有四种
    int boxState = 1;
    // 地图，布尔值判断该单元块是否占据方块
    boolean[][] maps;
    // 当前下落的方块
    Point[] boxs;
    // 方块的种类
    final int TYPE_BOX = 7;
    // 方块的类型
    int boxType;
    // 方块大小
    int boxSize;
    // 记录旋转前的状态
    Point[] historyBox;

    // 自动下落的线程
    public Thread downThread;
    // 与线程的逻辑通信
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 接收到通知，刷新view
            view.invalidate();
        }
    };

    // 游戏暂停状态
    public boolean isPause;
    // 游戏结束状态
    public boolean isOver;

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
        // 初始化地图，20行10列
        maps = new boolean[mGameRow][mGameCol];
        // 初始化方块大小（即为10）
        boxSize = xWidth / mGameCol;
        // 初始化历史矩阵
        historyBox = new Point[]{new Point(), new Point(), new Point(), new Point()};
    }

    /**
     * 新的方块
     */
    public void newBoxs() {
        // 随机生成一个新的方块，[0,6]
        Random random = new Random();
        boxType = random.nextInt(TYPE_BOX);
        switch (boxType) {
            // 田
            case 0:
                boxs = new Point[] {new Point(0, 4), new Point(0, 5),
                        new Point(1, 4), new Point(1, 5)};
                boxState = 2;
                break;
            // 1__
            case 1:
                boxs = new Point[] {new Point(1, 3), new Point(0, 3),
                        new Point(1, 4), new Point(1, 5)};
                boxState = 1;
                break;
            // __1
            case 2:
                boxs = new Point[] {new Point(1, 5), new Point(0, 5),
                        new Point(1, 3), new Point(1, 4)};
                boxState = 4;
                break;
            // -i_
            case 3:
                boxs = new Point[] {new Point(0, 4), new Point(0, 3),
                        new Point(1, 4), new Point(1, 5)};
                boxState = 3;
                break;
            // _i-
            case 4:
                boxs = new Point[] {new Point(1, 4), new Point(0, 4),
                        new Point(1, 3), new Point(0, 5)};
                boxState = 4;
                break;
            // 凸
            case 5:
                boxs = new Point[] {new Point(1, 4), new Point(0, 4),
                        new Point(1, 3), new Point(1, 5)};
                boxState = 4;
                break;
            // ——
            case 6:
                boxs = new Point[] {new Point(0, 4), new Point(0, 3),
                        new Point(0, 5), new Point(0, 6)};
                boxState = 4;
                break;
        }
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
        mapPaint = new Paint();
        mapPaint.setColor(Color.parseColor("#FFFFCCCC"));
        //一般都会把抗锯齿打开
        linePaint.setAntiAlias(true);
        boxPaint.setAntiAlias(true);
        mapPaint.setAntiAlias(true);
        // 得到父容器
        FrameLayout layoutGame = findViewById(R.id.Fr_Game);
        // 实例化游戏区域
        view = new View(this) {
            // 重写游戏区域绘制
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                // 绘制地图，即把已经下落完的那些方块绘制出来
                for (int x = 0; x < maps.length; x++) {
                    for (int y = 0; y < maps[0].length; y++) {
                        if (maps[x][y]) {
                            canvas.drawRect(y*boxSize, x*boxSize,
                                    y*boxSize+boxSize, x*boxSize+boxSize, mapPaint);
                        }
                    }
                }
                // 方块绘制
                if (boxs != null) {
                    for (int i = 0; i < boxs.length; i++) {
                        canvas.drawRect(boxs[i].y*boxSize,
                                boxs[i].x*boxSize,
                                boxs[i].y*boxSize+boxSize,
                                boxs[i].x*boxSize+boxSize, boxPaint);
                    }
                }
                // 地图辅助线
                for (int x = 0; x < maps.length + 1; x++) {
                    // 横线每一条都从X为0开始
                    canvas.drawLine(0, x*boxSize, view.getWidth(), x*boxSize, linePaint);
                }
                for (int y = 0; y < maps[0].length + 1; y++) {
                    // 竖线每一条都从Y为0开始
                    canvas.drawLine(y*boxSize, 0, y*boxSize, view.getHeight(), linePaint);
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
        findViewById(R.id.btn_QuickDown).setOnClickListener(this);
    }

    /**
     * 捕捉点击事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Left:
                // 左
                move(0, -1);
                break;
            case R.id.btn_Top:
                // 上
                rotate();
                // 旋转完位置并不完美，要修正笛卡尔旋转的位置,不同方块矫正方法不同，要进行判断
                if (boxType == 3) {
                    // 对-i_这种有两种旋转状态的方块的旋转修正
                    correctThree();
                } else if (boxType == 4) {
                    // 对_i-这种有两种旋转状态的方块的旋转修正
                    correctFour();
                } else if (boxType == 5){
                    // 如果为凸，则不用修正笛卡尔旋转，直接矫正边界旋转即可
                    correctBoundary();
                } else if (boxType == 6){
                    // 对——这种有两种旋转状态的方块的旋转修正
                    correctSix();
                } else {
                    // 对1__和__1和田等有四种旋转状态的方块的旋转修正
                    correctSurplus();
                }
                // 修正、矫正完后才是方块的正确旋转位置
                break;
            case R.id.btn_Right:
                // 右
                move(0, 1);
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
        }

        // 每次点击刷新视图，重绘view
        view.invalidate();
    }

    /**
     * 暂停游戏
     */
    public void pauseGame(){
        if (isPause) {
            isPause = false;
        } else {
            isPause = true;
        }
    }

    /**
     * 开始游戏
     */
    public void startGame(){
        // 清除地图
        for (int x = 0; x < maps.length; x++) {
            // 地图方块的占有全部取消，全部变为false
            for (int y = 0; y < maps[0].length; y++) {
                maps[x][y] = false;
            }
        }
        // 暂停取消
        isPause = false;
        // 结束取消
        isOver = false;
        // 生成新的方块
        newBoxs();
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
                        handler.sendEmptyMessage(0);
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
        if (isPause || isOver || boxs == null) {
            return false;
        }
        // 下落一格成功，不做处理
        if (move(1,0)) {
            return true;
        } else {
            // 不能再移动了，堆积处理，对应地图map的位置设为True
            for (int i = 0; i < boxs.length; i++) {
                maps[boxs[i].x][boxs[i].y] = true;
            }
            // 堆积完，判断消行处理
            cleanLine();
            // 生成新的方块
            newBoxs();
            // 每生成一个新的就可以判断游戏是否结束了
            isOver = ifOverGame();
            return false;
        }
    }

    /**
     * 消行处理
     */
    public void cleanLine() {
        // 从下往上遍历
        for (int x = maps.length - 1; x >= 0; x--) {
            boolean canClean = true;
            // 判断每一行每一个是否都为True
            for (int y = 0; y < maps[0].length; y++) {
                // 只要有一个为空
                if (!maps[x][y]) {
                    canClean = false;
                    break;
                }
            }
            // 可以消行，执行消行
            if (canClean) {
                deleteLine(x);
                x++;
            }
        }
    }

    /**
     * 删掉指定一行
     */
    public void deleteLine(int deleteX) {
        for (int x = deleteX - 1; x >= 0; x--) {
            for (int y = 0; y < maps[0].length; y++) {
                maps[x+1][y] = maps[x][y];
            }
        }
        for (int y = 0; y < maps[0].length; y++) {
            maps[0][y] = false;
        }
    }


    /**
     * 判断游戏是否结束
     */
    public boolean ifOverGame() {
        if (boxs == null) {
            return false;
        }
        for (int i = 0; i < boxs.length; i++) {
            if (maps[boxs[i].x][boxs[i].y]) {
                return true;
            }
        }
        return false;
    }

    /**
     * 移动的方法
     * @param dx 横坐标偏移量
     * @param dy 纵坐标偏移量
     * @return
     */
    public boolean move(int dx, int dy) {
        if (isPause || isOver || boxs == null) {
            return false;
        }
        // 把方块预移动的点传入判断边界
        for (int i = 0; i < boxs.length; i++) {
            if (checkBoundary(boxs[i].x + dx, boxs[i].y + dy) > 0) {
                return false;
            }
        }
        // 遍历当前下落方块的所有单元块
        for (int i = 0; i < boxs.length; i++) {
            boxs[i].x += dx;
            boxs[i].y += dy;
        }
        return true;
    }

    public boolean directMove(int dx, int dy) {
        if (isPause || isOver || boxs == null) {
            return false;
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
        if (isPause || isOver || boxs == null) {
            return false;
        }

        // 遍历当前下落方块的所有单元块，每个都绕中心点顺时针旋转九十度
        for (int i = 0; i < boxs.length; i++) {
            // 先把这个方块的旋转前的状态保存下来
            historyBox[i].x = boxs[i].x;
            historyBox[i].y = boxs[i].y;
            // 旋转算法（笛卡尔公式）顺时针旋转boxs[0]这个点转九十度
            int checkY = -boxs[i].x + boxs[0].x + boxs[0].y;
            int checkX = boxs[i].y - boxs[0].y + boxs[0].x;
            boxs[i].y = checkY;
            boxs[i].x = checkX;
        }
        return true;
    }

    /**
     * 判断单元块是否出界，注意下落完毕的方块也是一种“边界”
     * @return 大于0的为出界，等于0的为未出界
     */
    public int checkBoundary(int x, int y) {
        if (x < 0) {
            // 说明顶部出地图
            return 1;
        } else if (y < 0) {
            // 左边出地图
            return 2;
        } else if (x >= maps.length) {
            // 底部出地图
            return 3;
        } else if (y >= maps[0].length) {
            // 右边出地图
            return 4;
        } else if (maps[x][y]) {
            // 该方块的位置已经被占了
            return 5;
        } else {
            // 不出界
            return 0;
        }
    }

    /**
     * 对有四种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctSurplus() {
        if (boxState == 1) {
            // 往上移一位
            directMove(-1, 0);
            boxState = 2;
        } else if (boxState == 2) {
            // 往右移一位
            directMove(0, 1);
            boxState = 3;
        } else if (boxState == 3) {
            // 往下移一位
            directMove(1, 0);
            boxState = 4;
        } else if (boxState == 4) {
            // 往左移一位
            directMove(0, -1);
            boxState = 1;
        }
        // 对边界处旋转的修正
        correctBoundary();
    }

    /**
     * 对-i_这种有两种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctThree() {
        if (boxState == 1) {
            // 往左移一位
            directMove(0, -1);
            boxState = 2;
        } else if (boxState == 2) {
            // 往右上移一位
            directMove(-1, 1);
            boxState = 3;
        } else if (boxState == 3) {
            // 往下移一位
            directMove(1, 0);
            boxState = 4;
        } else if (boxState == 4) {
            // 不移位
            boxState = 1;
        }
        // 对边界处旋转的修正
        correctBoundary();
    }

    /**
     * 对_i-这种有两种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctFour() {
        if (boxState == 1) {
            // 往上移一位
            directMove(-1, 0);
            boxState = 2;
        } else if (boxState == 2) {
            // 往右下移一位
            directMove(1, 1);
            boxState = 3;
        } else if (boxState == 3) {
            // 往左移一位
            directMove(0, -1);
            boxState = 4;
        } else if (boxState == 4) {
            // 不移位
            boxState = 1;
        }
        // 对边界处旋转的修正
        correctBoundary();
    }

    /**
     * 对——这种有两种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctSix() {
        if (boxState == 1) {
            // 往右移一位
            directMove(0, 1);
            boxState = 2;
        } else if (boxState == 2) {
            // 往左下移一位
            directMove(1, -1);
            boxState = 3;
        } else if (boxState == 3) {
            // 往上移一位
            directMove(-1, 0);
            boxState = 4;
        } else if (boxState == 4) {
            // 不移位
            boxState = 1;
        }
        // 对边界处旋转的修正
        correctBoundary();
    }

    /**
     * 判断旋转是否出界，出界则修正，不出界则终止函数
     */
    public void correctBoundary() {
        if (boxs == null) {
            return;
        }
        // 先找到出界原因
        int tmp = 0;
        for (int i = 0; i < boxs.length; i++) {
            if (checkBoundary(boxs[i].x, boxs[i].y) > 0) {
                tmp = checkBoundary(boxs[i].x, boxs[i].y);
                break;
            }
        }
        // 然后针对出界原因修正
        if (tmp == 5) {
            // 如果旋转后会碰到地图方块，取消刚才的旋转，恢复原样
            // 5必须放在最前面，防止触发边界的判断先生效
            for (int i = 0; i < historyBox.length; i++) {
                boxs[i].x = historyBox[i].x;
                boxs[i].y = historyBox[i].y;
            }
            return;
        } else if (tmp == 1) {
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
                // 不出界，不碰壁，无需修正，终止函数
                return;
        }
        // 然后再判断是否还需要修正
        correctBoundary();
    }
}