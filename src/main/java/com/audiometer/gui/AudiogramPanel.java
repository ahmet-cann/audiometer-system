package com.audiometer.gui;

import java.awt.BasicStroke;
import com.audiometer.model.Ear;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

public class AudiogramPanel extends JPanel {
    private final Map<Integer, Integer> freqXMap = new HashMap<>();
    private final Map<String, Integer> thresholds = new HashMap<>();
    private final int[] frequencies = {250, 500, 1000, 2000, 4000, 8000};

    public AudiogramPanel() {
        setBackground(Color.WHITE);
        freqXMap.put(250,   80);
        freqXMap.put(500,  160);
        freqXMap.put(1000, 240);
        freqXMap.put(2000, 320);
        freqXMap.put(4000, 400);
        freqXMap.put(8000, 480);
    }

    public void addThreshold(Ear ear, int frequency, int db) {
        thresholds.put(ear.name() + "_" + frequency, db);
        repaint();
    }

    public void clearGraph() {
        thresholds.clear();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        g2.setColor(new Color(50, 50, 50));
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Saf Ton Audiogramı", 220, 20);


        g2.setColor(Color.LIGHT_GRAY);
        for (int freq : frequencies) {
            int x = freqXMap.get(freq);
            g2.drawLine(x, 40, x, 400);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(freq >= 1000 ? (freq/1000) + "k" : freq + "Hz", x - 15, 30);
            g2.setColor(Color.LIGHT_GRAY);
        }

        for (int db = 0; db <= 120; db += 10) {
            int y = 40 + (db * 3);
            g2.drawLine(60, y, 500, y);
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g2.drawString(db + " dB", 15, y + 5);
            g2.setColor(Color.LIGHT_GRAY);
        }


        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Frekans (Hz)", 240, 425);

        Graphics2D rotated = (Graphics2D) g2.create();
        rotated.rotate(-Math.PI / 2);
        rotated.setFont(new Font("SansSerif", Font.BOLD, 12));
        rotated.drawString("Şiddet (dB HL)", -280, 10);
        rotated.dispose();


        drawConnectingLines(g2, "RIGHT", Color.RED);
        drawConnectingLines(g2, "LEFT",  Color.BLUE);


        thresholds.forEach((key, db) -> {
            String[] parts  = key.split("_");
            String earStr   = parts[0];
            int freq        = Integer.parseInt(parts[1]);
            int x           = freqXMap.get(freq);
            int y           = 40 + (db * 3);

            if ("RIGHT".equals(earStr)) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(x - 6, y - 6, 12, 12);
            } else {
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(x - 5, y - 5, x + 5, y + 5);
                g2.drawLine(x + 5, y - 5, x - 5, y + 5);
            }
        });


        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(74, 44, 12, 12);
        g2.drawString("Sağ Kulak (O)", 92, 55);
        g2.setColor(Color.BLUE);
        g2.drawLine(74, 64, 86, 76);
        g2.drawLine(86, 64, 74, 76);
        g2.drawString("Sol Kulak (X)", 92, 74);
    }

    private void drawConnectingLines(Graphics2D g2, String earStr, Color color) {
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f));
        int prevX = -1, prevY = -1;
        for (int freq : frequencies) {
            String key = earStr + "_" + freq;
            if (thresholds.containsKey(key)) {
                int db       = thresholds.get(key);
                int currentX = freqXMap.get(freq);
                int currentY = 40 + (db * 3);
                if (prevX != -1) g2.drawLine(prevX, prevY, currentX, currentY);
                prevX = currentX;
                prevY = currentY;
            }
        }
        g2.setStroke(new BasicStroke(1.0f));
    }
}
