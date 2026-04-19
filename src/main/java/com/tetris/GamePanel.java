package com.tetris;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import javax.sound.sampled.*;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Game game;
    private Timer timer;
    private boolean isPaused = false;
    private Image kitty;
    private Clip bgm;

    // 主题：0=深色 1=粉色 2=浅色
    private int theme = 0;
    private Color bgColor, blockColor;

    public GamePanel() {
        this(1);   // 默认普通难度
    }

    public GamePanel(int difficulty) {
        game = new Game(difficulty);
        game.newShape();
        kitty = new ImageIcon(getClass().getResource("/kitty.png")).getImage();
        setFocusable(true);
        addKeyListener(this);
        updateTheme();
        timer = new Timer(game.getSpeed(), this);
        timer.start();
        playBGM();
    }

    private void updateTheme() {
        if (theme == 0) {
            bgColor = new Color(0,0,0,210);
            blockColor = new Color(255,190,200);
        } else if (theme == 1) {
            bgColor = new Color(255,230,235,230);
            blockColor = new Color(255,80,140);
        } else {
            bgColor = new Color(255,255,255,230);
            blockColor = new Color(60,60,60);
        }
        setBackground(Color.BLACK);
    }

    private void playBGM() {
        try {
            // 从 classpath 获取音频文件的 URL（推荐方式）
            java.net.URL audioUrl = getClass().getResource("/bgm.wav");
            if (audioUrl == null) {
                System.err.println("bgm.wav not found in classpath");
                return;
            }
            // 通过 URL 获取 AudioInputStream
            AudioInputStream ais = AudioSystem.getAudioInputStream(audioUrl);
            bgm = AudioSystem.getClip();
            bgm.open(ais);
            bgm.loop(Clip.LOOP_CONTINUOUSLY);
            bgm.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(blockColor);

        // 绘制地图
        for (int i = 0; i < Game.HEIGHT; i++)
            for (int j = 0; j < Game.WIDTH; j++)
                if (game.getMap()[i][j] == 1)
                    g.fillRect(j * Game.BLOCK, i * Game.BLOCK, Game.BLOCK - 1, Game.BLOCK - 1);

        // 绘制当前方块
        int[][] s = game.getCurrentShape();
        int x = game.getCurX(), y = game.getCurY();
        for (int i = 0; i < s.length; i++)
            for (int j = 0; j < s[i].length; j++)
                if (s[i][j] == 1)
                    g.fillRect((x + j) * Game.BLOCK, (y + i) * Game.BLOCK, Game.BLOCK - 1, Game.BLOCK - 1);

        // 绘制固定的 Kitty
        if (kitty != null) {
            boolean[][] km = game.getKittyMap();
            for (int i = 0; i < Game.HEIGHT; i++)
                for (int j = 0; j < Game.WIDTH; j++)
                    if (km[i][j])
                        g.drawImage(kitty, j * Game.BLOCK + 2, i * Game.BLOCK + 2, Game.BLOCK - 5, Game.BLOCK - 5, this);
        }

        // 绘制当前方块上的 Kitty 标记
        if (game.hasKitty() && kitty != null) {
            int ki = game.getKittyI(), kj = game.getKittyJ();
            if (ki >= 0 && kj >= 0 && ki < s.length && kj < s[ki].length && s[ki][kj] == 1)
                g.drawImage(kitty, (x + kj) * Game.BLOCK + 2, (y + ki) * Game.BLOCK + 2, Game.BLOCK - 5, Game.BLOCK - 5, this);
        }

        // 炸弹提示
        if (game.getItemType() == 1) {
            g.setColor(Color.RED);
            g.setFont(new Font("黑体", Font.BOLD, 14));
            g.drawString("炸弹", 10, 45);
        }

        // 分数、连击、能量条、难度
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString("Score: " + game.getScore(), 10, 20);
        g.drawString("Combo: x" + game.getCombo(), 10, 45);

        // 能量条背景
        g.setColor(Color.GRAY);
        g.fillRect(10, 70, 60, 12);
        // 能量条填充
        g.setColor(Color.PINK);
        int energyWidth = game.getKittyCollected() * 20;
        g.fillRect(10, 70, energyWidth, 12);
        g.setColor(Color.WHITE);
        g.drawString("Kitty Power", 10, 68);

        // 难度显示
        String diffText;
        switch (game.getDifficulty()) {
            case 0: diffText = "简单"; break;
            case 1: diffText = "普通"; break;
            case 2: diffText = "困难"; break;
            default: diffText = "普通";
        }
        g.drawString("难度: " + diffText, 10, 95);

        // 主题切换提示
        g.drawString("按C切换主题", 10, 115);

        // 下一个方块预览
        g.drawString("Next:", 10, 150);
        int[][] next = game.getNextShape();
        int previewX = 10;
        int previewY = 160;
        int blockSize = Game.BLOCK;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(previewX - 2, previewY - 2, next[0].length * blockSize + 4, next.length * blockSize + 4);
        g.setColor(blockColor);
        for (int i = 0; i < next.length; i++) {
            for (int j = 0; j < next[i].length; j++) {
                if (next[i][j] == 1) {
                    g.fillRect(previewX + j * blockSize, previewY + i * blockSize, blockSize - 1, blockSize - 1);
                }
            }
        }

        // 暂停文字
        if (isPaused) {
            g.setColor(Color.PINK);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("PAUSED", 120, 220);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPaused) return;
        timer.setDelay(game.getSpeed());
        try {
            if (game.isValid(game.getCurX(), game.getCurY() + 1))
                game.setCurY(game.getCurY() + 1);
            else
                game.fixShape();
            repaint();
        } catch (Exception ex) {
            timer.stop();
            if (bgm != null) bgm.stop();
            JOptionPane.showMessageDialog(this, "游戏结束！");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();

        // 切换主题
        if (k == KeyEvent.VK_C) {
            theme = (theme + 1) % 3;
            updateTheme();
            repaint();
            return;
        }
        // 暂停/继续
        if (k == KeyEvent.VK_P) {
            isPaused = !isPaused;
            if (bgm != null) {
                if (isPaused) bgm.stop();
                else bgm.start();
            }
            repaint();
            return;
        }
        // 重启游戏
        if (k == KeyEvent.VK_R) {
            game.restartGame();
            isPaused = false;
            timer.setDelay(game.getSpeed());
            if (bgm != null) {
                bgm.setMicrosecondPosition(0);
                bgm.start();
            }
            repaint();
            return;
        }
        // 猫咪大招
        if (k == KeyEvent.VK_K) {
            if (game.isPowerReady()) {
                game.useKittyPower();
                repaint();
            }
            return;
        }

        if (isPaused) return;

        // 移动/旋转
        if (k == KeyEvent.VK_LEFT && game.isValid(game.getCurX() - 1, game.getCurY()))
            game.setCurX(game.getCurX() - 1);
        if (k == KeyEvent.VK_RIGHT && game.isValid(game.getCurX() + 1, game.getCurY()))
            game.setCurX(game.getCurX() + 1);
        if (k == KeyEvent.VK_DOWN && game.isValid(game.getCurX(), game.getCurY() + 1))
            game.setCurY(game.getCurY() + 1);
        if (k == KeyEvent.VK_UP) rotate();
        if (k == KeyEvent.VK_SPACE)
            while (game.isValid(game.getCurX(), game.getCurY() + 1))
                game.setCurY(game.getCurY() + 1);
        repaint();
    }

    private void rotate() {
        int h = game.getCurrentShape().length;
        int w = game.getCurrentShape()[0].length;
        int[][] r = new int[w][h];
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++)
                r[j][h - 1 - i] = game.getCurrentShape()[i][j];
        int[][] old = game.getCurrentShape();
        game.setCurrentShape(r);
        if (!game.isValid(game.getCurX(), game.getCurY()))
            game.setCurrentShape(old);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}