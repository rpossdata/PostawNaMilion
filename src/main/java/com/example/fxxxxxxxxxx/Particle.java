package com.example.fxxxxxxxxxx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Particle {
    private double x, y, vx, vy;
    private double size;
    private Color color;
    boolean isConfetti;
    private double maxWidth, maxHeight;

    public Particle(double width, double height) {
        this(width, height, false);
    }

    public Particle(double width, double height, boolean isConfetti) {
        this.isConfetti = isConfetti;
        this.maxWidth = width;
        this.maxHeight = height;
        x = Math.random() * width;
        y = Math.random() * height;
        vx = (Math.random() - 0.5) * 2;
        vy = (Math.random() - 0.5) * 2;
        size = isConfetti ? 5 + Math.random() * 5 : 2 + Math.random() * 3;
        color = Color.hsb(Math.random() * 360, 0.7, 0.9);
    }

    public void update(double width, double height) {
        this.maxWidth = width;
        this.maxHeight = height;
        x += vx;
        y += vy;
        if (x < 0 || x > width) vx = -vx;
        if (y < 0 || y > height) vy = -vy;
        if (isConfetti) {
            vy += 0.1;
        }
    }

    public void adjustForNewWidth(double newWidth) {
        if (x > newWidth) x = newWidth;
        this.maxWidth = newWidth;
    }

    public void adjustForNewHeight(double newHeight) {
        if (y > newHeight) y = newHeight;
        this.maxHeight = newHeight;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x, y, size, size);
    }
}
