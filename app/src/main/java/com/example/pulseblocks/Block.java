// Block.java
package com.example.pulseblocks;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

class Block {
    public float x, y;
    public int size;
    public int color;
    private int blockSize = 60;
    private float velocityX = 0;
    private float velocityY = 0;
    private boolean alignToGrid = true;
    private long shootTime = 0; // Para efectos de trail
    float glowIntensity = 0; // Para efectos de brillo

    private float rotation = 0;
    private float scale = 1.0f;
    private float alpha = 1.0f;

    public float getX() { return x; }
    public float getY() { return y; }
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
    public void setRotation(float rotation) { this.rotation = rotation; }
    public void setScale(float scale) { this.scale = scale; }
    public void setAlpha(float alpha) { this.alpha = alpha; }

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

    public void render(Canvas canvas) {
        // Guardar el estado del canvas
        canvas.save();

        // Aplicar transformaciones
        canvas.translate(x, y);
        canvas.rotate(rotation, blockSize / 2f, blockSize / 2f);
        canvas.scale(scale, scale, blockSize / 2f, blockSize / 2f);

        // Crear Paint para el bloque con gradiente radial
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Calcular colores para el gradiente (más claro en el centro)
        int brightColor = Color.rgb(
                Math.min(255, Color.red(color) + 60),
                Math.min(255, Color.green(color) + 60),
                Math.min(255, Color.blue(color) + 60)
        );
        int darkColor = Color.rgb(
                Math.max(0, Color.red(color) - 30),
                Math.max(0, Color.green(color) - 30),
                Math.max(0, Color.blue(color) - 30)
        );

        // Crear gradiente radial desde el centro
        RadialGradient gradient = new RadialGradient(
                blockSize / 2f, blockSize / 2f, blockSize * 0.6f,
                new int[]{brightColor, color, darkColor},
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP
        );

        paint.setShader(gradient);
        paint.setAlpha((int)(alpha * 255));

        // Dibujar el bloque con esquinas redondeadas
        RectF rect = new RectF(2, 2, blockSize - 2, blockSize - 2);
        canvas.drawRoundRect(rect, 10, 10, paint);

        // Borde brillante
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setAlpha((int)(100 * alpha));

        RectF borderRect = new RectF(4, 4, blockSize - 4, blockSize - 4);
        canvas.drawRoundRect(borderRect, 8, 8, borderPaint);

        // Efecto de brillo superior (cristal)
        Paint glossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient glossGradient = new LinearGradient(
                0, 5, 0, blockSize / 2f,
                Color.argb((int)(120 * alpha), 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
        );
        glossPaint.setShader(glossGradient);

        RectF glossRect = new RectF(8, 5, blockSize - 8, blockSize / 3f);
        canvas.drawRoundRect(glossRect, 8, 8, glossPaint);

        // Sombra interior para profundidad
        Paint shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.BLACK);
        shadowPaint.setAlpha((int)(40 * alpha));
        shadowPaint.setMaskFilter(new BlurMaskFilter(4, BlurMaskFilter.Blur.INNER));
        canvas.drawRoundRect(rect, 10, 10, shadowPaint);

        // Restaurar el estado del canvas
        canvas.restore();
    }
}