package com.example.tetris;

import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.tetris.control.GameControl;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    // view里面没有变量，没有数据
    // 游戏区域控件
    View gamePanel;

    // 游戏控制器
    GameControl gameControl;

    // 与线程的逻辑通信
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String type = (String)msg.obj;
            if (type == null) {
                return;
            }
            if (type.equals("invalidate")) {
                // 接收到通知，刷新view
                gamePanel.invalidate();
            } else if (type.equals("pause")) {
                ((Button)findViewById(R.id.btn_Pause)).setText("暂停");
            } else if (type.equals("continue")) {
                ((Button)findViewById(R.id.btn_Pause)).setText("继续");
            } else if (type.equals("exit")) {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 实例化游戏控制器
        gameControl = new GameControl(handler, this);
        initView();
        initListener();
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
        findViewById(R.id.btn_Exit).setOnClickListener(this);
    }

    /**
     * 捕捉点击事件
     */
    public void onClick(View v) {
        gameControl.onClick(v.getId(), this);
        // 每次点击刷新视图，重绘view
        gamePanel.invalidate();
    }
}