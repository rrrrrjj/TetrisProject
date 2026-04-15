package com.tetris;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CoreGameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        // 假设你的 Game 构造函数已经修改为不自动生成第一个方块
        // 如果构造函数中仍然调用了 newShape()，请取消下面一行的注释，并注释掉 game.newShape()
        // 这里按最安全的方式：直接 new，然后手动生成第一个方块
        game = new Game();
        game.newShape();   // 第一个方块，shapeCount = 1
    }

    // 1. Kitty 出现规律：2个普通 + 1个Kitty
    @Test
    void testKittyPattern() {
        // 第1个方块（普通）
        assertFalse(game.hasKitty(), "第1个方块不应该是Kitty");
        // 第2个方块（普通）
        game.newShape();
        assertFalse(game.hasKitty(), "第2个方块不应该是Kitty");
        // 第3个方块（Kitty）
        game.newShape();
        assertTrue(game.hasKitty(), "第3个方块应该是Kitty");
        // 第4个方块（普通）
        game.newShape();
        assertFalse(game.hasKitty(), "第4个方块应该是普通");
    }

    // 2. 连击系统：消除一行后 combo 应该变成 1，得分增加 (100 + 50)
    @Test
    void testComboAfterLineClear() throws Exception {
        // 通过反射获取 map 并填满最下面一行
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(game);
        for (int col = 0; col < Game.WIDTH; col++) {
            map[Game.HEIGHT - 1][col] = 1;
        }
        // 反射调用 clearLines
        var clearMethod = Game.class.getDeclaredMethod("clearLines");
        clearMethod.setAccessible(true);
        clearMethod.invoke(game);
        // 验证连击数 = 1，得分 = 100 + 50 = 150（普通难度倍率1.0）
        assertEquals(1, game.getCombo());
        assertEquals(150, game.getScore());
    }

    // 3. 炸弹道具：应该清空最下面一行并加200分
    @Test
    void testBombClearsBottomLine() throws Exception {
        // 强制设置道具类型为炸弹
        var itemField = Game.class.getDeclaredField("itemType");
        itemField.setAccessible(true);
        itemField.set(game, 1);
        // 填满最下面一行
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(game);
        for (int col = 0; col < Game.WIDTH; col++) {
            map[Game.HEIGHT - 1][col] = 1;
        }
        // 触发炸弹（fixShape 会调用 useBomb）
        game.fixShape();
        // 检查最下面一行是否全空
        for (int col = 0; col < Game.WIDTH; col++) {
            assertEquals(0, map[Game.HEIGHT - 1][col]);
        }
        // 检查得分增加200（普通难度）
        assertEquals(200, game.getScore());
    }

    // 4. 难度速度影响：简单速度应为普通的两倍，困难速度应为普通的一半
    @Test
    void testDifficultySpeed() {
        Game easy = new Game(0);
        Game normal = new Game(1);
        Game hard = new Game(2);
        // 在分数为0时，基础速度是800
        assertEquals(1600, easy.getSpeed());
        assertEquals(800, normal.getSpeed());
        assertEquals(400, hard.getSpeed());
    }
}