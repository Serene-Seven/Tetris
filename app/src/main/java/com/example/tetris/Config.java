package com.example.tetris;

import android.graphics.Color;

/**
 * Created by Administrator on 2018/3/10.
 */

public class Config {
    // 地图行列数，x对应行数，y对应列数
    public static final int mGameCol = 10;
    public static final int mGameRow = mGameCol*2;

    // 地图行方向宽度，列方向宽度
    public static int yWith;
    public static int xHeight;

    // 控制方块下落速度
    public static int sleepTime = 1020;

    // 地图辅助线开关
    public static boolean isGuideLine = true;

    // 游戏音效开关
    public static boolean isMusic = true;

    // 方块颜色
    public static final int color[] = {
            Color.rgb(240,0,0),
            Color.rgb(0,240,240),
            Color.rgb(0,0,240),
            Color.rgb(240,0,0),
            Color.rgb(240,160,0),
            Color.rgb(0,240,0),
            Color.rgb(160,0,240),
            Color.parseColor("#ffff9999") };
    // 方块颜色种类
    public static final int colorType = 8;
}