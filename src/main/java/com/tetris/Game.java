package com.tetris;

import java.util.Random;
import java.util.Arrays;

public class Game {
    public static final int WIDTH = 15;
    public static final int HEIGHT = 20;
    public static final int BLOCK = 20;

    private int[][] map = new int[HEIGHT][WIDTH];
    private boolean[][] kittyMap = new boolean[HEIGHT][WIDTH];
    private int[][] currentShape;
    private int[][] nextShape;
    private int curX, curY;
    private int score = 0;

    private int shapeCount = 0;
    private boolean hasKitty;
    private int kittyI, kittyJ;
    private int itemType;          // 0=普通 1=炸弹

    // 连击系统
    private int combo = 0;
    // 猫咪能量条
    private int kittyCollected = 0;
    private boolean powerReady = false;

    // 难度相关
    private int difficulty;        // 0=简单 1=普通 2=困难
    private double scoreMultiplier;

    // 构造方法
    public Game() {
        this(1);   // 默认普通难度
    }

    public Game(int difficulty) {
        this.difficulty = difficulty;
        switch (difficulty) {
            case 0: scoreMultiplier = 1.2; break;  // 简单：得分更高
            case 1: scoreMultiplier = 1.0; break;  // 普通：标准
            case 2: scoreMultiplier = 0.8; break;  // 困难：得分更低
            default: scoreMultiplier = 1.0;
        }
        nextShape = createRandomShape();
    }

    public int[][] createRandomShape() {
        int[][][] shapes = {
                {{1,1},{1,1}},
                {{1,1,1,1}},
                {{0,1,0},{1,1,1}},
                {{1,0,0},{1,1,1}},
                {{0,0,1},{1,1,1}},
                {{0,1,1},{1,1,0}},
                {{1,1,0},{0,1,1}}
        };
        return shapes[new Random().nextInt(shapes.length)];
    }

    public void newShape() {
        currentShape = nextShape;
        nextShape = createRandomShape();
        curX = WIDTH / 2 - currentShape[0].length / 2;
        curY = 0;

        shapeCount++;
        hasKitty = (shapeCount % 3 == 0);   // 每2个普通后跟1个Kitty

        // 道具：非Kitty时随机炸弹
        if (!hasKitty && new Random().nextInt(5) == 0) {
            itemType = 1;
        } else {
            itemType = 0;
        }

        if (hasKitty) {
            Random rand = new Random();
            java.util.List<int[]> list = new java.util.ArrayList<>();
            for (int i = 0; i < currentShape.length; i++)
                for (int j = 0; j < currentShape[i].length; j++)
                    if (currentShape[i][j] == 1)
                        list.add(new int[]{i, j});
            if (!list.isEmpty()) {
                int[] pos = list.get(rand.nextInt(list.size()));
                kittyI = pos[0];
                kittyJ = pos[1];
            }
        }

        if (!isValid(curX, curY)) throw new RuntimeException("游戏结束");
    }

    public boolean isValid(int x, int y) {
        for (int i = 0; i < currentShape.length; i++) {
            for (int j = 0; j < currentShape[i].length; j++) {
                if (currentShape[i][j] == 1) {
                    int nx = x + j, ny = y + i;
                    if (nx < 0 || nx >= WIDTH || ny >= HEIGHT) return false;
                    if (ny >= 0 && map[ny][nx] == 1) return false;
                }
            }
        }
        return true;
    }

    // 炸弹道具：消除最下方一行
    public void useBomb() {
        int row = HEIGHT - 1;
        for (int j = 0; j < WIDTH; j++) {
            map[row][j] = 0;
            kittyMap[row][j] = false;
        }
        for (int k = row; k > 0; k--) {
            map[k] = Arrays.copyOf(map[k-1], WIDTH);
            kittyMap[k] = Arrays.copyOf(kittyMap[k-1], WIDTH);
        }
        map[0] = new int[WIDTH];
        kittyMap[0] = new boolean[WIDTH];
        score += (int)(200 * scoreMultiplier);
    }

    // 固定当前方块，处理道具和能量
    public void fixShape() {
        // 炸弹道具
        if (itemType == 1) {
            useBomb();
            newShape();
            return;
        }

        // 正常固定（非炸弹）
        for (int i = 0; i < currentShape.length; i++) {
            for (int j = 0; j < currentShape[i].length; j++) {
                if (currentShape[i][j] == 1) {
                    int mx = curX + j, my = curY + i;
                    if (my >= 0) {
                        map[my][mx] = 1;
                        if (hasKitty && i == kittyI && j == kittyJ) {
                            kittyMap[my][mx] = true;
                            // 增加猫咪能量
                            kittyCollected++;
                            if (kittyCollected >= 3) {
                                kittyCollected = 3;
                                powerReady = true;
                            }
                        }
                    }
                }
            }
        }
        clearLines();
        newShape();
    }

    // 消除行并计算连击得分 实现消行
    public void clearLines() {
        int lines = 0;
        for (int i = HEIGHT-1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < WIDTH; j++) {
                if (map[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                // 消除当前行
                for (int k = i; k > 0; k--) {
                    map[k] = Arrays.copyOf(map[k-1], WIDTH);
                    kittyMap[k] = Arrays.copyOf(kittyMap[k-1], WIDTH);
                }
                map[0] = new int[WIDTH];
                kittyMap[0] = new boolean[WIDTH];
                lines++;
                i++;  // 继续检查同一行
            }
        }

        if (lines > 0) {
            combo++;
            int bonus = combo * 50;
            int addScore = (int)((lines * 100 + bonus) * scoreMultiplier);
            score += addScore;
        } else {
            combo = 0;
        }
    }

    // 猫咪大招：清除所有 Kitty 并加分
    public void useKittyPower() {
        if (!powerReady) return;

        int cleared = 0;
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (kittyMap[i][j]) {
                    map[i][j] = 0;
                    kittyMap[i][j] = false;
                    score += (int)(50 * scoreMultiplier);
                    cleared++;
                }
            }
        }
        score += (int)(500 * scoreMultiplier);   // 额外奖励
        kittyCollected = 0;
        powerReady = false;
    }

    // 重启游戏
    public void restartGame() {
        map = new int[HEIGHT][WIDTH];
        kittyMap = new boolean[HEIGHT][WIDTH];
        score = 0;
        shapeCount = 0;
        hasKitty = false;
        itemType = 0;
        combo = 0;
        kittyCollected = 0;
        powerReady = false;
        nextShape = createRandomShape();
        newShape();
    }

    // 动态速度 + 难度影响
    public int getSpeed() {
        int baseSpeed;
        if (score < 500) baseSpeed = 800;
        else if (score < 1000) baseSpeed = 600;
        else if (score < 2000) baseSpeed = 400;
        else if (score < 3000) baseSpeed = 300;
        else baseSpeed = 200;

        switch (difficulty) {
            case 0: return baseSpeed * 2;     // 简单：更慢
            case 1: return baseSpeed;         // 普通：不变
            case 2: return Math.max(50, baseSpeed / 2); // 困难：更快（不低于50ms）
            default: return baseSpeed;
        }
    }

    // ----- getter 方法 -----
    public int[][] getMap() { return map; }
    public boolean[][] getKittyMap() { return kittyMap; }
    public int[][] getCurrentShape() { return currentShape; }
    public int[][] getNextShape() { return nextShape; }
    public int getCurX() { return curX; }
    public int getCurY() { return curY; }
    public int getScore() { return score; }
    public boolean hasKitty() { return hasKitty; }
    public int getKittyI() { return kittyI; }
    public int getKittyJ() { return kittyJ; }
    public int getItemType() { return itemType; }
    public int getCombo() { return combo; }
    public int getKittyCollected() { return kittyCollected; }
    public boolean isPowerReady() { return powerReady; }
    public int getDifficulty() { return difficulty; }

    // ----- setter 方法（用于移动和旋转）-----
    public void setCurX(int curX) { this.curX = curX; }
    public void setCurY(int curY) { this.curY = curY; }
    public void setCurrentShape(int[][] s) { this.currentShape = s; }
}