package com.example.tetris.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.tetris.Config;

/**
 * Created by Administrator on 2018/3/10.
 */

public class Maps {
    // 地图，布尔值判断该单元块是否占据方块
    public boolean[][] maps;
    // 地图宽高
    int yWidth, xHeight;
    // 方块大小
    public int boxSize;
    // 地图画笔
    Paint mapPaint;
    // 辅助线画笔
    Paint linePaint;
    // 状态画笔
    Paint statePaint;

    public Maps(int boxSize, int yWidth, int xHeight) {
        maps = new boolean[Config.mGameRow][Config.mGameCol];
        this.boxSize = boxSize;
        this.yWidth = yWidth;
        this.xHeight = xHeight;
        // 初始化画笔
        linePaint = new Paint();
        linePaint.setColor(Color.GRAY);
        statePaint = new Paint();
        statePaint.setColor(Color.parseColor("#FFFF3366"));
        statePaint.setTextSize(100);
        mapPaint = new Paint();
        mapPaint.setColor(Color.parseColor("#FFFFCCCC"));
        //一般都会把抗锯齿打开
        linePaint.setAntiAlias(true);
        statePaint.setAntiAlias(true);
        mapPaint.setAntiAlias(true);
    }

    public void drawMaps(Canvas canvas) {
        // 绘制地图，即把已经下落完的那些方块绘制出来
        for (int x = 0; x < maps.length; x++) {
            for (int y = 0; y < maps[0].length; y++) {
                if (maps[x][y]) {
                    canvas.drawRect(y*boxSize, x*boxSize,
                            y*boxSize+boxSize, x*boxSize+boxSize, mapPaint);
                }
            }
        }
    }

    public void drawLines(Canvas canvas) {
        // 地图辅助线
        for (int x = 0; x < maps.length + 1; x++) {
            // 横线每一条都从X为0开始
            canvas.drawLine(0, x*boxSize, yWidth, x*boxSize, linePaint);
        }
        for (int y = 0; y < maps[0].length + 1; y++) {
            // 竖线每一条都从Y为0开始
            canvas.drawLine(y*boxSize, 0, y*boxSize, xHeight, linePaint);
        }
    }

    public void drawState(Canvas canvas, boolean isOver, boolean isPause) {
        // 游戏结束画面
        if (isOver) {
            canvas.drawText("游戏结束", yWidth/2-statePaint.measureText("游戏结束")/2, xHeight/2, statePaint);
        }
        // 游戏暂停状态
        if (isPause && !isOver) {
            canvas.drawText("暂停中", yWidth/2-statePaint.measureText("暂停中")/2, xHeight/2, statePaint);
        }
    }

    public void cleanMaps() {
        // 清除地图
        for (int x = 0; x < maps.length; x++) {
            // 地图方块的占有全部取消，全部变为false
            for (int y = 0; y < maps[0].length; y++) {
                maps[x][y] = false;
            }
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
}