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
    // 地图的颜色，拿来保存已下落的方块的颜色
    public int[][] colors;
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
        colors = new int[Config.mGameRow][Config.mGameCol];
        this.boxSize = boxSize;
        this.yWidth = yWidth;
        this.xHeight = xHeight;
        // 初始化画笔
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        statePaint = new Paint();
        statePaint.setColor(Color.parseColor("#FFFF3366"));
        statePaint.setTextSize(100);
        mapPaint = new Paint();
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
                    if (colors[x][y] != 0) {
                        mapPaint.setColor(colors[x][y]);
                    }
                    canvas.drawRect(y*boxSize, x*boxSize,
                            y*boxSize+boxSize, x*boxSize+boxSize, mapPaint);
                }
            }
        }
    }

    public void drawLines(Canvas canvas) {
        if (!Config.isGuideLine) return;
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

    public void drawState(Canvas canvas, boolean isPause, boolean isVictory) {
        // 游戏暂停状态
        if (isPause  && !isVictory) {
            canvas.drawText("暂停中", yWidth/2-statePaint.measureText("暂停中")/2, xHeight/2, statePaint);
        }
        // 游戏胜利界面
        if (isVictory) {
            canvas.drawText("LOVE YOU", yWidth/2-statePaint.measureText("LOVE YOU")/2, xHeight/3, statePaint);
        }
    }

    public void cleanMaps() {
        // 清除地图的逻辑数据，颜色数据
        for (int x = 0; x < maps.length; x++) {
            // 地图方块的占有全部取消，全部变为false
            for (int y = 0; y < maps[0].length; y++) {
                maps[x][y] = false;
                colors[x][y] = 0;
            }
        }
    }

    /**
     * 消行处理
     */
    public int cleanLine() {
        int lines = 0;
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
                lines++;
            }
        }
        return lines;
    }

    /**
     * 删掉指定一行
     */
    public void deleteLine(int deleteX) {
        for (int x = deleteX - 1; x >= 0; x--) {
            for (int y = 0; y < maps[0].length; y++) {
                maps[x+1][y] = maps[x][y];
                colors[x+1][y] = colors[x][y];
            }
        }
        for (int y = 0; y < maps[0].length; y++) {
            maps[0][y] = false;
            colors[0][y] = 0;
        }
    }
}