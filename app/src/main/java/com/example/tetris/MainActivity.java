package com.example.tetris;

import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.tetris.control.GameControl;
import com.example.tetris.model.Music;
import com.example.tetris.model.Score;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // view里面没有变量，没有数据
    // 游戏区域控件
    View gamePanel;
    // 下一块预览组件
    public View nextPanel;

    // 游戏控制器
    GameControl gameControl;

    // 与线程的逻辑通信
    public Handler handler = new Handler(new Handler.Callback(){
        @Override
        public boolean handleMessage(Message msg) {
            String type = (String)msg.obj;
            if (type == null) {
                return false;
            }
            if (type.equals("invalidate")) {
                // 接收到自动下落线程通知，刷新游戏view，还有下一块预览view，因为可能自动下落到底
                refresh();
            } else if (type.equals("pause")) {
                if (msg.arg1 == 0) {
                    // 现在是暂停
                    ((Button)findViewById(R.id.btn_Pause)).setText("继续");
                } else {
                    // 现在是继续
                    ((Button)findViewById(R.id.btn_Pause)).setText("暂停");
                }
            } else if (type.equals("guideLine")) {
                if (msg.arg1 == 0) {
                    ((Button)findViewById(R.id.btn_Line)).setText("关画线");
                } else {
                    ((Button)findViewById(R.id.btn_Line)).setText("开画线");
                }
            } else if (type.equals("setMusic")) {
                if (msg.arg1 == 0) {
                    ((Button)findViewById(R.id.btn_Music)).setText("关音效");
                } else {
                    ((Button)findViewById(R.id.btn_Music)).setText("开音效");
                }
            } else if (type.equals("setOver")) {
                gameControl.setOver();
            } else if (type.equals("exit")) {
                finish();
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 实例化游戏控制器
        gameControl = new GameControl(handler, this);
        initView();
        init();
        // 逻辑层数据准备就绪，通知界面层刷新
        refresh();
    }

    /**
     * 初始化视图
     */
    public void initView() {
        // 得到父容器
        FrameLayout layoutGame = findViewById(R.id.Fr_Game);
        // 实例化游戏区域
        gamePanel = new View(this) {
            // 重写游戏区域绘制
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                // 绘制
                gameControl.draw(canvas);
            }
        };
        // 设置游戏区域大小
        gamePanel.setLayoutParams(new FrameLayout.LayoutParams(Config.yWith, Config.xHeight));
        // 设置游戏背景颜色
        //gamePanel.setBackgroundColor(Color.GRAY);
        // 添加到父容器
        layoutGame.addView(gamePanel);

        // 实例化下一块预览区域
        nextPanel = new View(this){
            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                gameControl.drawNext(canvas, nextPanel.getWidth());
            }
        };
        // 设置游戏区域大小
        nextPanel.setLayoutParams(new FrameLayout.LayoutParams(Config.yWith * 2/3, -1));
        //nextPanel.setBackgroundColor(Color.BLUE);
        // 添加到父容器
        FrameLayout layoutNext = findViewById(R.id.Fr_Next);
        layoutNext.addView(nextPanel);
    }

    /**
     * 初始化监听
     */
    public void init() {
        findViewById(R.id.btn_Left).setOnClickListener(this);
        findViewById(R.id.btn_Top).setOnClickListener(this);
        findViewById(R.id.btn_Right).setOnClickListener(this);
        findViewById(R.id.btn_Down).setOnClickListener(this);
        findViewById(R.id.btn_Start).setOnClickListener(this);
        findViewById(R.id.btn_Pause).setOnClickListener(this);
        findViewById(R.id.btn_QuickDown).setOnClickListener(this);
        findViewById(R.id.btn_Exit).setOnClickListener(this);
        findViewById(R.id.btn_Line).setOnClickListener(this);
        findViewById(R.id.btn_Music).setOnClickListener(this);
        findViewById(R.id.btn_Chang).setOnClickListener(this);
    }

    /**
     * 捕捉点击事件
     */
    public void onClick(View v) {
        gameControl.onClick(v.getId());
        // 每次点击按钮都要刷新视图，重绘view
        refresh();
    }

    /**
     * 刷新组件界面，各种视图等
     */
    public void refresh() {
        // 刷新游戏区域，下一块预览，分数
        gamePanel.invalidate();
        nextPanel.invalidate();
        ((TextView)findViewById(R.id.tv_score)).setText("" + Score.score);
        ((TextView)findViewById(R.id.tv_record)).setText("" + Score.record);
        ((TextView)findViewById(R.id.tv_level)).setText("" + Score.level);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!GameControl.isPause) {
            gameControl.pauseGame();
        }
        if (Music.mPlayer != null && Music.mPlayer.isPlaying()){
            // 暂停播放BGM
            Music.mPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Music.mPlayer != null && Config.isMusic && gameControl.boxesModel.boxes != null) {
            // 如果没在播放并且设置音效打开，说明暂停了，那么继续播放
            // 注意，第一次启动游戏的时候是会调用onResume的
            Music.mPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (GameControl.isPause) {
            gameControl.pauseGame();
        }
    }
}