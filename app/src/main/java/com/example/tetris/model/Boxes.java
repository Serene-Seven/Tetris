package com.example.tetris.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.example.tetris.Config;

import java.util.Random;

/**
 * 方块模型
 * Created by Administrator on 2018/3/10.
 */

public class Boxes {

    // 当前下落的方块
    public Point[] boxes;
    // 方块的种类
    final int TYPE_BOX = 7;
    // 方块的类型
    public int boxType;
    // 方块大小
    public int boxSize;
    // 方块的颜色
    public int boxColor;
    // 表示俄罗斯方块当前旋转状态
    int boxState;
    // 方块画笔
    Paint boxPaint;
    // 记录旋转前的状态
    Point[] historyBox;
    // 下一块方块
    public Point[] boxesNext;
    // 下一块方块预览版
    public Point[] miniboxesNext;
    // 下一块方块的类型
    public int boxNextType;
    // 下一块方块的方块大小
    public int boxNextSize;
    // 下一块方块的颜色
    public int boxNextColor;
    // 下一块方块画笔
    Paint nextBoxPaint;
    // 表示下一俄罗斯方块旋转状态
    int boxNextState;

    // 预览成问号
    public float[][] nextMark;
    // 方块成钻戒
    public float[][] boxDiamondRing;

    public Boxes(int boxSize) {
        this.boxSize = boxSize;
        boxPaint = new Paint();
        boxPaint.setAntiAlias(true);
        nextBoxPaint = new Paint();
        nextBoxPaint.setAntiAlias(true);
        // 初始化历史方块
        historyBox = new Point[]{new Point(), new Point(), new Point(), new Point()};
    }

    /**
     * 新的方块
     */
    public void newBoxes() {
        if (boxesNext == null) {
            // 第一次来
            newBoxesNext();
        }
        // 旧的下一方块赋给现在的方块
        boxes = boxesNext;
        boxType = boxNextType;
        boxState = boxNextState;
        boxColor = boxNextColor;
        boxPaint.setColor(boxColor);
        // 新生成下一方块
        newBoxesNext();
    }

    /**
     * 产生胜利的钻戒方块
     */
    public void newDiamondRing() {
        boxDiamondRing = new float[][]{
                {0, (float)3.5},// 第一行第一个红色三角形
                {1, (float)4.25},// 第一行第二个橘色倒三角形
                {0, 5},// 第一行第三个黄色三角形
                {1, (float)5.75},// 第一行第四个绿色倒三角形
                {0, (float)6.5},// 第一行第五个蓝色三角形
                {(float)1.1, 3},// 第二行第一个紫色三角形
                {(float)1.1, (float)4.5},// 第二行第二个粉色长方形
                {(float)1.1, 7},// 第二行第三个黄色三角形
                {(float)3.2, (float)3.4},{(float)3.2, (float)4.5},{(float)3.2, (float)5.6},// 第四行三个蓝色正方形
                {(float)4.3, (float)2.3},{(float)4.3, (float)6.7},// 第五行两个蓝色正方形
                {(float)5.4, (float)2.3},{(float)5.4, (float)6.7},// 第六行两个蓝色正方形
                {(float)6.5, (float)3.4},{(float)6.5, (float)4.5},{(float)6.5, (float)5.6},// 第七行三个蓝色正方形
        };
        // 制作问号
        nextMark = new float[][]{{(float)0.9, 3}, {(float)0.9, (float)4.2}, {2, (float)1.9}, {2, (float)5.1}, {(float)3.2, (float)5.1}, {(float)4.3, 4}, {(float)5.4, 4}, {(float)7.4, 4}};
        nextBoxPaint.setColor(Color.parseColor("#FFFFCCCC"));
    }

    /**
     * 画钻戒
     */
    public void drawDiamondRing(Canvas canvas) {
        Path path;
        float x;
        float y;
        // 第一行第一个红色三角形
        x = boxDiamondRing[0][1];
        y = boxDiamondRing[0][0];
        boxPaint.setColor(Color.rgb(240,67,20));
        path = new Path();
        path.moveTo(x*boxSize, y*boxSize);// 此点为多边形的起点
        path.lineTo((x-(float)0.5)*boxSize, (y+1)*boxSize);
        path.lineTo((x+(float)0.5)*boxSize, (y+1)*boxSize);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, boxPaint);

        // 第一行第二个橘色倒三角形
        x = boxDiamondRing[1][1];
        y = boxDiamondRing[1][0];
        boxPaint.setColor(Color.rgb(254,149,33));
        path = new Path();
        path.moveTo(x*boxSize, y*boxSize);// 此点为多边形的起点
        path.lineTo((x-(float)0.5)*boxSize, (y-1)*boxSize);
        path.lineTo((x+(float)0.5)*boxSize, (y-1)*boxSize);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, boxPaint);

        // 第一行第三个黄色三角形
        x = boxDiamondRing[2][1];
        y = boxDiamondRing[2][0];
        boxPaint.setColor(Color.rgb(255,255,133));
        path = new Path();
        path.moveTo(x*boxSize, y*boxSize);// 此点为多边形的起点
        path.lineTo((x-(float)0.5)*boxSize, (y+1)*boxSize);
        path.lineTo((x+(float)0.5)*boxSize, (y+1)*boxSize);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, boxPaint);

        // 第一行第四个绿色倒三角形
        x = boxDiamondRing[3][1];
        y = boxDiamondRing[3][0];
        boxPaint.setColor(Color.rgb(80,160,42));
        path = new Path();
        path.moveTo(x*boxSize, y*boxSize);// 此点为多边形的起点
        path.lineTo((x-(float)0.5)*boxSize, (y-1)*boxSize);
        path.lineTo((x+(float)0.5)*boxSize, (y-1)*boxSize);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, boxPaint);

        // 第一行第五个蓝色三角形
        x = boxDiamondRing[4][1];
        y = boxDiamondRing[4][0];
        boxPaint.setColor(Color.rgb(153,204,255));
        path = new Path();
        path.moveTo(x*boxSize, y*boxSize);// 此点为多边形的起点
        path.lineTo((x-(float)0.5)*boxSize, (y+1)*boxSize);
        path.lineTo((x+(float)0.5)*boxSize, (y+1)*boxSize);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, boxPaint);

        // 第二行第二个粉色长方形
        x = boxDiamondRing[6][1];
        y = boxDiamondRing[6][0];
        boxPaint.setColor(Color.parseColor("#ffcce6"));
        canvas.drawRect(x*boxSize, y*boxSize,
                (x+1)*boxSize, (y+2)*boxSize, boxPaint);

        // 第二行第一个紫色三角形
        x = boxDiamondRing[5][1];
        y = boxDiamondRing[5][0];
        float dx = boxDiamondRing[6][1];
        boxPaint.setColor(Color.rgb(175,75,250));
        path = new Path();
        path.moveTo(x*boxSize, y*boxSize);// 此点为多边形的起点
        path.lineTo((dx-(float)0.1)*boxSize, y*boxSize);
        path.lineTo((dx-(float)0.1)*boxSize, (y+2)*boxSize);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, boxPaint);

        // 第二行第三个黄色三角形
        x = boxDiamondRing[7][1];
        y = boxDiamondRing[7][0];
        dx = boxDiamondRing[6][1];
        boxPaint.setColor(Color.rgb(252,237,81));
        path = new Path();
        path.moveTo(x*boxSize, y*boxSize);// 此点为多边形的起点
        path.lineTo((dx+(float)1.1)*boxSize, y*boxSize);
        path.lineTo((dx+(float)1.1)*boxSize, (y+2)*boxSize);
        path.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path, boxPaint);

        boxPaint.setColor(Color.parseColor("#ff99ccff"));
        // 剩余的蓝色方块绘制
        for (int i = 8; i < boxDiamondRing.length; i++) {
            x = boxDiamondRing[i][1];
            y = boxDiamondRing[i][0];
            canvas.drawRect(x*boxSize, y*boxSize,
                    (x+1)*boxSize, (y+1)*boxSize, boxPaint);
        }
    }

    /**
     * 生成下一块方块
     * @param
     */
    public void newBoxesNext() {
        // 随机生成一个新的方块，[0,6]
        Random random = new Random();
        boxNextType = random.nextInt(TYPE_BOX);
        // 随机选取一个颜色
        boxNextColor = Config.color[random.nextInt(Config.colorType)];
        nextBoxPaint.setColor(boxNextColor);
        switch (boxNextType) {
            // 田
            case 0:
                boxesNext = new Point[] {new Point(0, 4), new Point(0, 5),
                        new Point(1, 4), new Point(1, 5)};
                miniboxesNext = new Point[] {new Point(1, 1), new Point(1, 2),
                        new Point(2, 1), new Point(2, 2)};
                boxNextState = 2;
                break;
            // 1__
            case 1:
                boxesNext = new Point[] {new Point(1, 3), new Point(0, 3),
                        new Point(1, 4), new Point(1, 5)};
                miniboxesNext = new Point[] {new Point(1, 1), new Point(2, 1),
                        new Point(2, 2), new Point(2, 3)};
                boxNextState = 1;
                break;
            // __1
            case 2:
                boxesNext = new Point[] {new Point(1, 5), new Point(0, 5),
                        new Point(1, 3), new Point(1, 4)};
                miniboxesNext = new Point[] {new Point(1, 3), new Point(2, 1),
                        new Point(2, 2), new Point(2, 3)};
                boxNextState = 4;
                break;
            // -i_
            case 3:
                boxesNext = new Point[] {new Point(0, 4), new Point(0, 3),
                        new Point(1, 4), new Point(1, 5)};
                miniboxesNext = new Point[] {new Point(1, 1), new Point(1, 2),
                        new Point(2, 2), new Point(2, 3)};
                boxNextState = 3;
                break;
            // _i-
            case 4:
                boxesNext = new Point[] {new Point(1, 4), new Point(0, 4),
                        new Point(1, 3), new Point(0, 5)};
                miniboxesNext = new Point[] {new Point(1, 2), new Point(1, 3),
                        new Point(2, 1), new Point(2, 2)};
                boxNextState = 4;
                break;
            // 凸
            case 5:
                boxesNext = new Point[] {new Point(1, 4), new Point(0, 4),
                        new Point(1, 3), new Point(1, 5)};
                miniboxesNext = new Point[] {new Point(1, 2), new Point(2, 1),
                        new Point(2, 2), new Point(2, 3)};
                boxNextState = 4;
                break;
            // ——
            case 6:
                boxesNext = new Point[] {new Point(0, 4), new Point(0, 3),
                        new Point(0, 5), new Point(0, 6)};
                miniboxesNext = new Point[] {new Point(2, 1), new Point(2, 2),
                        new Point(2, 3)};
                boxNextState = 4;
                break;
        }
    }

    public void drawBoxes(Canvas canvas) {
        // 方块绘制
        if (boxes != null) {
            for (int i = 0; i < boxes.length; i++) {
                canvas.drawRect(boxes[i].y*boxSize,
                        boxes[i].x*boxSize,
                        boxes[i].y*boxSize+boxSize,
                        boxes[i].x*boxSize+boxSize, boxPaint);
            }
        }
    }

    /**
     * 钻戒的移动函数
     */
    public boolean moveDiamondRing(int dx, int dy) {
        // 遍历钻戒的所有单元块
        for (int i = 0; i < boxDiamondRing.length; i++) {
            boxDiamondRing[i][0] += dx;
            boxDiamondRing[i][1] += dy;
        }
        return true;
    }

    /**
     * 移动的方法
     * @param dx 横坐标偏移量
     * @param dy 纵坐标偏移量
     * @return
     */
    public boolean move(int dx, int dy, Maps mapsModel, Boolean isPause) {
        if (isPause || boxes == null) {
            return false;
        }
        // 把方块预移动的点传入判断边界
        for (int i = 0; i < boxes.length; i++) {
            if (checkBoundary(boxes[i].x + dx, boxes[i].y + dy, mapsModel) > 0) {
                return false;
            }
        }
        // 遍历当前下落方块的所有单元块
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].x += dx;
            boxes[i].y += dy;
        }
        return true;
    }

    /**
     * 判断单元块是否出界，注意下落完毕的方块也是一种“边界”
     * @return 大于0的为出界，等于0的为未出界
     */
    public int checkBoundary(int x, int y, Maps mapsModel) {
        if (x < 0) {
            // 说明顶部出地图
            return 1;
        } else if (y < 0) {
            // 左边出地图
            return 2;
        } else if (x >= mapsModel.maps.length) {
            // 底部出地图
            return 3;
        } else if (y >= mapsModel.maps[0].length) {
            // 右边出地图
            return 4;
        } else if (mapsModel.maps[x][y]) {
            // 该方块的位置已经被占了
            return 5;
        } else {
            // 不出界
            return 0;
        }
    }

    public boolean directMove(int dx, int dy) {
        // 遍历当前下落方块的所有单元块
        for (int i = 0; i < boxes.length; i++) {
            boxes[i].x += dx;
            boxes[i].y += dy;
        }
        return true;
    }

    /**
     * 旋转
     * @return
     */
    public boolean rotate(Maps mapsModel, Boolean isPause) {
        if (isPause || boxes == null) {
            return false;
        }
        // 遍历当前下落方块的所有单元块，每个都绕中心点顺时针旋转九十度
        for (int i = 0; i < boxes.length; i++) {
            // 先把这个方块的旋转前的状态保存下来
            historyBox[i].x = boxes[i].x;
            historyBox[i].y = boxes[i].y;
            // 旋转算法（笛卡尔公式）顺时针旋转boxs[0]这个点转九十度
            int checkY = -boxes[i].x + boxes[0].x + boxes[0].y;
            int checkX = boxes[i].y - boxes[0].y + boxes[0].x;
            boxes[i].y = checkY;
            boxes[i].x = checkX;
        }
        // 旋转完位置并不完美，要修正笛卡尔旋转的位置,不同方块矫正方法不同，要进行判断
        if (boxType == 3) {
            // 对-i_这种有两种旋转状态的方块的旋转修正
            correctThree(mapsModel);
        } else if (boxType == 4) {
            // 对_i-这种有两种旋转状态的方块的旋转修正
            correctFour(mapsModel);
        } else if (boxType == 5){
            // 如果为凸，则不用修正笛卡尔旋转，直接矫正边界旋转即可
            correctBoundary(mapsModel);
        } else if (boxType == 6){
            // 对——这种有两种旋转状态的方块的旋转修正
            correctSix(mapsModel);
        } else {
            // 对1__和__1和田等有四种旋转状态的方块的旋转修正
            correctSurplus(mapsModel);
        }
        // 修正、矫正完后才是方块的正确旋转位置
        return true;
    }

    /**
     * 对1__和__1和田等有四种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctSurplus(Maps mapsModel) {
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
        correctBoundary(mapsModel);
    }

    /**
     * 对-i_这种有两种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctThree(Maps mapsModel) {
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
        correctBoundary(mapsModel);
    }

    /**
     * 对_i-这种有两种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctFour(Maps mapsModel) {
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
        correctBoundary(mapsModel);
    }

    /**
     * 对——这种有两种旋转状态的方块进行笛卡尔旋转的位置修正，包括边界处旋转的修正
     */
    public void correctSix(Maps mapsModel) {
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
        correctBoundary(mapsModel);
    }

    /**
     * 判断旋转是否出界，出界则修正，不出界则终止函数
     */
    public void correctBoundary(Maps mapsModel) {
        // 先找到出界原因
        int tmp = 0;
        for (int i = 0; i < boxes.length; i++) {
            if (checkBoundary(boxes[i].x, boxes[i].y, mapsModel) > 0) {
                tmp = checkBoundary(boxes[i].x, boxes[i].y, mapsModel);
                break;
            }
        }
        // 然后针对出界原因修正
        if (tmp == 5) {
            // 如果旋转后会碰到地图方块，取消刚才的旋转，恢复原样
            // 5必须放在最前面，防止触发边界的判断先生效
            for (int i = 0; i < historyBox.length; i++) {
                boxes[i].x = historyBox[i].x;
                boxes[i].y = historyBox[i].y;
            }
            return;
        } else if (tmp == 1) {
            for (int i = 0; i < boxes.length; i++) {
                boxes[i].x += 1;
            }
        } else if (tmp == 2) {
            for (int i = 0; i < boxes.length; i++) {
                boxes[i].y += 1;
            }
        } else if (tmp == 3) {
            for (int i = 0; i < boxes.length; i++) {
                boxes[i].x -= 1;
            }
        } else if (tmp == 4) {
            for (int i = 0; i < boxes.length; i++) {
                boxes[i].y -= 1;
            }
        } else {
            // 不出界，不碰壁，无需修正，终止函数
            return;
        }
        // 然后再判断是否还需要修正
        correctBoundary(mapsModel);
    }

    /**
     * 绘制下一块方块
     * @param canvas
     */
    public void drawNext(Canvas canvas, int width) {
        if (boxesNext != null) {
            // 预览区域为五行五列(没显示)
            boxNextSize = width / 5;
            for (int i = 0; i < miniboxesNext.length; i++) {
                if (boxNextType == 6) {
                    // 把——竹竿画长一点
                    canvas.drawRect((miniboxesNext[i].y - (float)0.5)*boxNextSize, (miniboxesNext[i].x)*boxNextSize,
                            (miniboxesNext[i].y + (float)0.5)*boxNextSize + boxNextSize, (miniboxesNext[i].x)*boxNextSize + boxNextSize,
                            nextBoxPaint);
                } else if (boxNextType == 0) {
                    // 把田画居中一点
                    canvas.drawRect((miniboxesNext[i].y + (float)0.5)*boxNextSize, (miniboxesNext[i].x + (float)0.5)*boxNextSize,
                            (miniboxesNext[i].y + (float)0.5)*boxNextSize + boxNextSize, (miniboxesNext[i].x + (float)0.5)*boxNextSize + boxNextSize,
                            nextBoxPaint);
                } else {
                    canvas.drawRect((miniboxesNext[i].y)*boxNextSize, (miniboxesNext[i].x)*boxNextSize,
                            (miniboxesNext[i].y)*boxNextSize + boxNextSize, (miniboxesNext[i].x)*boxNextSize + boxNextSize,
                            nextBoxPaint);
                }
            }
        }
    }

    /**
     * 下一块预览画成问号
     */
    public void drawMark(Canvas canvas, int width) {
        if (nextMark != null) {
            // 预览区域为五行五列(没显示)
            boxNextSize = width / 9;
            // 画问号
            for (int i = 0; i < nextMark.length; i++) {
                canvas.drawRect((nextMark[i][1])*boxNextSize, (nextMark[i][0])*boxNextSize,
                        (nextMark[i][1])*boxNextSize + boxNextSize, (nextMark[i][0])*boxNextSize + boxNextSize,
                        nextBoxPaint);
            }
        }
    }
}