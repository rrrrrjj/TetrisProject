package com.tetris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CoreGameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.newShape();   // 生成第一个方块
    }

    @Test
    void testKittyPattern() {
        assertFalse(game.hasKitty());
        game.newShape();
        assertFalse(game.hasKitty());
        game.newShape();
        assertTrue(game.hasKitty());
        game.newShape();
        assertFalse(game.hasKitty());
    }

    @Test
    void testComboAfterLineClear() throws Exception {
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(game);
        for (int col = 0; col < Game.WIDTH; col++) {
            map[Game.HEIGHT - 1][col] = 1;
        }
        var clearMethod = Game.class.getDeclaredMethod("clearLines");
        clearMethod.setAccessible(true);
        clearMethod.invoke(game);
        assertEquals(1, game.getCombo());
        assertEquals(150, game.getScore());
    }

    @Test
    void testBombClearsBottomLine() throws Exception {
        var itemField = Game.class.getDeclaredField("itemType");
        itemField.setAccessible(true);
        itemField.set(game, 1);
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(game);
        for (int col = 0; col < Game.WIDTH; col++) {
            map[Game.HEIGHT - 1][col] = 1;
        }
        game.fixShape();
        for (int col = 0; col < Game.WIDTH; col++) {
            assertEquals(0, map[Game.HEIGHT - 1][col]);
        }
        assertEquals(200, game.getScore());
    }

    @Test
    void testDifficultySpeed() {
        Game easy = new Game(0);
        Game normal = new Game(1);
        Game hard = new Game(2);
        assertEquals(1600, easy.getSpeed());
        assertEquals(800, normal.getSpeed());
        assertEquals(400, hard.getSpeed());
    }
}