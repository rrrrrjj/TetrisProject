package com.tetris;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleGameTest {

    // 测试1：Kitty 出现规律（2个普通 + 1个Kitty）
    @Test
    void testKittyPattern() {
        Game game = new Game();
        // 第1个
        game.newShape();
        assertFalse(game.hasKitty(), "第1个方块不应该是Kitty");
        // 第2个
        game.newShape();
        assertFalse(game.hasKitty(), "第2个方块不应该是Kitty");
        // 第3个
        game.newShape();
        assertTrue(game.hasKitty(), "第3个方块应该是Kitty");
        // 第4个
        game.newShape();
        assertFalse(game.hasKitty(), "第4个方块不应该是Kitty");
        // 第5个
        game.newShape();
        assertFalse(game.hasKitty(), "第5个方块不应该是Kitty");
        // 第6个
        game.newShape();
        assertTrue(game.hasKitty(), "第6个方块应该是Kitty");
    }

    // 测试2：得分倍率（简单难度）
    @Test
    void testEasyScoreMultiplier() throws Exception {
        Game easyGame = new Game(0); // 简单难度，倍率1.2
        // 反射调用 clearLines
        var clearMethod = Game.class.getDeclaredMethod("clearLines");
        clearMethod.setAccessible(true);
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(easyGame);
        // 填满最下面一行
        for (int j = 0; j < Game.WIDTH; j++) {
            map[Game.HEIGHT - 1][j] = 1;
        }
        clearMethod.invoke(easyGame);
        // 消除一行：基础100 + 连击50 =150，乘以1.2 = 180
        assertEquals(180, easyGame.getScore(), "简单难度得分应为180");
    }

    // 测试3：炸弹消除最下面一行
    @Test
    void testBombClearsBottomLine() throws Exception {
        Game game = new Game();
        // 强制设置道具为炸弹
        var itemField = Game.class.getDeclaredField("itemType");
        itemField.setAccessible(true);
        itemField.set(game, 1);
        // 填满最下面一行
        var mapField = Game.class.getDeclaredField("map");
        mapField.setAccessible(true);
        int[][] map = (int[][]) mapField.get(game);
        for (int j = 0; j < Game.WIDTH; j++) {
            map[Game.HEIGHT - 1][j] = 1;
        }
        // 触发炸弹
        game.fixShape();
        // 检查最下面一行是否清空
        for (int j = 0; j < Game.WIDTH; j++) {
            assertEquals(0, map[Game.HEIGHT - 1][j], "炸弹应清空最下面一行");
        }
        // 检查得分增加200
        assertEquals(200, game.getScore());
    }

    // 测试4：动态速度（根据分数变化）
    @Test
    void testDynamicSpeed() {
        Game game = new Game();
        assertEquals(800, game.getSpeed(), "初始速度应为800");
        // 反射修改分数
        try {
            var scoreField = Game.class.getDeclaredField("score");
            scoreField.setAccessible(true);
            scoreField.set(game, 500);
            assertEquals(600, game.getSpeed(), "分数500-999速度应为600");
            scoreField.set(game, 1000);
            assertEquals(400, game.getSpeed(), "分数1000-1999速度应为400");
            scoreField.set(game, 2000);
            assertEquals(300, game.getSpeed(), "分数2000-2999速度应为300");
            scoreField.set(game, 3500);
            assertEquals(200, game.getSpeed(), "分数>=3000速度应为200");
        } catch (Exception e) {
            fail("反射失败");
        }
    }
}