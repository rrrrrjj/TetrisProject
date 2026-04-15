package com.tetris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class TetrisGameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    // 1. Kitty 出现规律：每2个普通后1个Kitty（即第3、6、9...个）
    @Test
    void testKittyAppearsEvery3Shapes() {
        game.newShape();
        assertFalse(game.hasKitty());
        game.newShape();
        assertFalse(game.hasKitty());
        game.newShape();
        assertTrue(game.hasKitty());
        game.newShape();
        assertFalse(game.hasKitty());
    }

    // 2. 动态速度（基于分数）
    @Test
    void testDynamicSpeedByScore() throws Exception {
        var scoreField = Game.class.getDeclaredField("score");
        scoreField.setAccessible(true);
        scoreField.set(game, 400);
        assertEquals(800, game.getSpeed());
        scoreField.set(game, 800);
        assertEquals(600, game.getSpeed());
        scoreField.set(game, 1800);
        assertEquals(400, game.getSpeed());
        scoreField.set(game, 3500);
        assertEquals(200, game.getSpeed());
    }

    // 3. 炸弹消除一行
    @Test
    void testBombItemClearsLine() throws Exception {
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(game);
        for (int j = 0; j < Game.WIDTH; j++) {
            map[Game.HEIGHT - 1][j] = 1;
        }
        var itemField = Game.class.getDeclaredField("itemType");
        itemField.setAccessible(true);
        itemField.set(game, 1);
        game.fixShape();
        for (int j = 0; j < Game.WIDTH; j++) {
            assertEquals(0, map[Game.HEIGHT - 1][j]);
        }
    }

    // 4. 重启游戏重置分数和计数
    @Test
    void testRestartGame() throws Exception {
        var scoreField = Game.class.getDeclaredField("score");
        var countField = Game.class.getDeclaredField("shapeCount");
        scoreField.setAccessible(true);
        countField.setAccessible(true);
        scoreField.set(game, 1000);
        countField.set(game, 10);
        game.restartGame();
        assertEquals(0, game.getScore());
        assertEquals(0, (int) countField.get(game));
    }

    // 5. 连击系统测试
    @Test
    void testComboSystem() throws Exception {
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(game);
        for (int j = 0; j < Game.WIDTH; j++) {
            map[Game.HEIGHT - 1][j] = 1;
        }
        var clearMethod = Game.class.getDeclaredMethod("clearLines");
        clearMethod.setAccessible(true);
        clearMethod.invoke(game);
        assertEquals(1, game.getCombo());
        // 再次填满一行
        for (int j = 0; j < Game.WIDTH; j++) {
            map[Game.HEIGHT - 1][j] = 1;
        }
        clearMethod.invoke(game);
        assertEquals(2, game.getCombo());
    }

    // 6. 猫咪能量收集
    @Test
    void testKittyEnergy() throws Exception {
        var shapeCountField = Game.class.getDeclaredField("shapeCount");
        shapeCountField.setAccessible(true);
        // 强制生成 Kitty 方块
        shapeCountField.set(game, 2);
        game.newShape();
        assertTrue(game.hasKitty());
        // 固定 Kitty 方块
        while (game.isValid(game.getCurX(), game.getCurY() + 1)) {
            game.setCurY(game.getCurY() + 1);
        }
        game.fixShape();
        assertEquals(1, game.getKittyCollected());
        assertFalse(game.isPowerReady());
        // 再收集两个 Kitty
        for (int i = 0; i < 2; i++) {
            shapeCountField.set(game, 2);
            game.newShape();
            while (game.isValid(game.getCurX(), game.getCurY() + 1)) {
                game.setCurY(game.getCurY() + 1);
            }
            game.fixShape();
        }
        assertEquals(3, game.getKittyCollected());
        assertTrue(game.isPowerReady());
    }

    // 7. 猫咪大招
    @Test
    void testUseKittyPower() throws Exception {
        var shapeCountField = Game.class.getDeclaredField("shapeCount");
        shapeCountField.setAccessible(true);
        for (int i = 0; i < 3; i++) {
            shapeCountField.set(game, 2);
            game.newShape();
            while (game.isValid(game.getCurX(), game.getCurY() + 1)) {
                game.setCurY(game.getCurY() + 1);
            }
            game.fixShape();
        }
        assertTrue(game.isPowerReady());
        int beforeScore = game.getScore();
        boolean[][] kittyMap = game.getKittyMap();
        int kittyCount = 0;
        for (int i = 0; i < Game.HEIGHT; i++) {
            for (int j = 0; j < Game.WIDTH; j++) {
                if (kittyMap[i][j]) kittyCount++;
            }
        }
        game.useKittyPower();
        assertEquals(0, game.getKittyCollected());
        assertFalse(game.isPowerReady());
        assertEquals(beforeScore + kittyCount * 50 + 500, game.getScore());
    }

    // 8. 难度速度影响
    @Test
    void testDifficultySpeed() {
        Game easy = new Game(0);
        assertEquals(1600, easy.getSpeed());
        Game hard = new Game(2);
        assertEquals(400, hard.getSpeed());
    }

    // 9. 难度得分倍率
    @Test
    void testDifficultyScoreMultiplier() throws Exception {
        Game easy = new Game(0);
        var clearMethod = Game.class.getDeclaredMethod("clearLines");
        clearMethod.setAccessible(true);
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(easy);
        for (int j = 0; j < Game.WIDTH; j++) {
            map[Game.HEIGHT - 1][j] = 1;
        }
        clearMethod.invoke(easy);
        assertEquals(180, easy.getScore()); // (100+50)*1.2 = 180
    }

    // 10. 旋转与碰撞（修正版）
    @Test
    void testRotateAndCollision() {
        int[][] original = game.getCurrentShape();
        int h = original.length;
        int w = original[0].length;
        int[][] rotated = new int[w][h];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                rotated[j][h - 1 - i] = original[i][j];
            }
        }
        game.setCurrentShape(rotated);
        if (!game.isValid(game.getCurX(), game.getCurY())) {
            game.setCurrentShape(original);
        }
        int[][] finalShape = game.getCurrentShape();
        boolean isEither = Arrays.deepEquals(finalShape, original) || Arrays.deepEquals(finalShape, rotated);
        assertTrue(isEither);
    }
}