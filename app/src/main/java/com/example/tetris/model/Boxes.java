package com.example.tetris.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

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
    // 表示俄罗斯方块当前旋转状态，有四种
    int boxState = 1;
    // 方块画笔
    Paint boxPaint;
    // 记录旋转前的状态
    Point[] historyBox;

    public Boxes(int boxSize) {
        this.boxSize = boxSize;
        boxPaint = new Paint();
        boxPaint.setColor(Color.BLACK);
        boxPaint.setAntiAlias(true);
        // 初始化历史方块
        historyBox = new Point[]{new Point(), new Point(), new Point(), new Point()};
    }

    /**
     * 新的方块
     */
    public void newBoxes() {
        // 随机生成一个新的方块，[0,6]
        Random random = new Random();
        boxType = random.nextInt(TYPE_BOX);
        switch (boxType) {
            // 田
            case 0:
                boxes = new Point[] {new Point(0, 4), new Point(0, 5),
                        new Point(1, 4), new Point(1, 5)};
                boxState = 2;
                break;
            // 1__
            case 1:
                boxes = new Point[] {new Point(1, 3), new Point(0, 3),
                        new Point(1, 4), new Point(1, 5)};
                boxState = 1;
                break;
            // __1
            case 2:
                boxes = new Point[] {new Point(1, 5), new Point(0, 5),
                        new Point(1, 3), new Point(1, 4)};
                boxState = 4;
                break;
            // -i_
            case 3:
                boxes = new Point[] {new Point(0, 4), new Point(0, 3),
                        new Point(1, 4), new Point(1, 5)};
                boxState = 3;
                break;
            // _i-
            case 4:
                boxes = new Point[] {new Point(1, 4), new Point(0, 4),
                        new Point(1, 3), new Point(0, 5)};
                boxState = 4;
                break;
            // 凸
            case 5:
                boxes = new Point[] {new Point(1, 4), new Point(0, 4),
                        new Point(1, 3), new Point(1, 5)};
                boxState = 4;
                break;
            // ——
            case 6:
                boxes = new Point[] {new Point(0, 4), new Point(0, 3),
                        new Point(0, 5), new Point(0, 6)};
                boxState = 4;
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
     * 移动的方法
     * @param dx 横坐标偏移量
     * @param dy 纵坐标偏移量
     * @return
     */
    public boolean move(int dx, int dy, Maps mapsModel, Boolean isOver, Boolean isPause) {
        if (isPause || isOver || boxes == null) {
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
    public boolean rotate(Maps mapsModel, Boolean isOver, Boolean isPause) {
        if (isPause || isOver || boxes == null) {
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
}