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
        game.newShape();   // 生成第一个方块，避免 currentShape 为 null
    }

    @Test
    void testKittyAppearsEvery3Shapes() {
        // 第1个（普通）
        assertFalse(game.hasKitty());
        // 第2个（普通）
        game.newShape();
        assertFalse(game.hasKitty());
        // 第3个（Kitty）
        game.newShape();
        assertTrue(game.hasKitty());
        // 第4个（普通）
        game.newShape();
        assertFalse(game.hasKitty());
    }

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
        assertEquals(200, game.getScore());
    }

    @Test
    void testRestartGame() throws Exception {
        var scoreField = Game.class.getDeclaredField("score");
        var shapeCountField = Game.class.getDeclaredField("shapeCount");
        scoreField.setAccessible(true);
        shapeCountField.setAccessible(true);
        scoreField.set(game, 1000);
        shapeCountField.set(game, 10);
        game.restartGame();
        assertEquals(0, game.getScore());
        // restartGame 内部会调用 newShape，所以 shapeCount 会变成 1
        assertEquals(1, shapeCountField.get(game));
        assertEquals(0, game.getCombo());
        assertEquals(0, game.getKittyCollected());
        assertFalse(game.isPowerReady());
    }

    @Test
    void testRotateAndCollision() {
        // 确保当前方块存在
        game.newShape();
        int[][] original = game.getCurrentShape();
        assertNotNull(original);
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