// Block.java
package com.example.pulseblocks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

class Block {
    public float x, y;
    public int size;
    public int color;
    private float velocityX = 0;
    private float velocityY = 0;
    private boolean alignToGrid = true;
    private long shootTime = 0; // Para efectos de trail
    private float glowIntensity = 0; // Para efectos de brillo

    public Block(float x, float y, int size, int color) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.color = color;
        this.glowIntensity = (float) Math.random();
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public void setAlignToGrid(boolean align) {
        this.alignToGrid = align;
    }

    public void setShootTime(long time) {
        this.shootTime = time;
    }

    public long getShootTime() {
        return shootTime;
    }

    public void update() {
        x += velocityX;
        y += velocityY;

        // Actualizar efecto de brillo
        glowIntensity += 0.05f;
        if (glowIntensity > Math.PI * 2) glowIntensity = 0;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(x, y, x + size, y + size, paint);

        // Borde
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(x, y, x + size, y + size, paint);
    }

    public void drawWithGlow(Canvas canvas, Paint paint) {
        // Dibujar efecto de brillo
        float glow = (float) (Math.sin(glowIntensity) * 0.5 + 0.5);

        // Capas de resplandor
        for (int i = 0; i < 3; i++) {
            int alpha = (int) (30 * glow) - (i * 8);
            if (alpha > 0) {
                paint.setColor(Color.argb(alpha, 0, 255, 255));
                paint.setStyle(Paint.Style.FILL);
                float expansion = i * 3;
                canvas.drawRect(
                        x - expansion, y - expansion,
                        x + size + expansion, y + size + expansion,
                        paint
                );
            }
        }

        // Dibujar el bloque normal
        draw(canvas, paint);

        // Efecto de energía en los bordes
        paint.setColor(Color.argb((int) (100 + glow * 155), 0, 255, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3 + glow * 2);
        canvas.drawRect(x, y, x + size, y + size, paint);
    }

    public Rect getBounds() {
        return new Rect((int)x, (int)y, (int)(x + size), (int)(y + size));
    }

    public boolean intersects(Block other) {
        return getBounds().intersect(other.getBounds());
    }

    // Verificar si está alineado a la cuadrícula
    public boolean isGridAligned() {
        return (x % size == 0) && (y % size == 0);
    }

    // Obtener posición en la cuadrícula
    public int getGridX(int gridOffsetX) {
        return (int)((x - gridOffsetX) / size);
    }

    public int getGridY(int gridOffsetY) {
        return (int)((y - gridOffsetY) / size);
    }
}