package com.tetris;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 难度选择对话框
        String[] options = {"简单", "普通", "困难"};
        int choice = JOptionPane.showOptionDialog(null,
                "请选择游戏难度", "难度选择",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[1]);

        int difficulty;
        if (choice == 0) difficulty = 0;      // 简单
        else if (choice == 2) difficulty = 2; // 困难
        else difficulty = 1;                  // 普通（包括关闭对话框时默认）

        JFrame frame = new JFrame("俄罗斯方块 - Hello Kitty版");
        GamePanel panel = new GamePanel(difficulty);
        frame.add(panel);
        frame.setSize(320, 440);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}