// BlockGroup.java
package com.example.pulseblocks;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

class BlockGroup {
    private List<Block> blocks;
    private float fallSpeed = 2;
    private int minX, maxX, minY, maxY;
    private int blockSize;
    private long lastMoveTime = 0;
    private int moveInterval = 500; // Milisegundos entre movimientos de cuadrícula
    private float groupGlow = 0; // Para efectos de grupo
    private boolean isCompleting = false; // Para animación de completado
    private long completionStartTime = 0;

    public BlockGroup() {
        blocks = new ArrayList<>();
        blockSize = 60; // Debe coincidir con el blockSize del juego
    }

    public void addBlock(Block block) {
        blocks.add(block);
        updateBounds();
    }

    public void setFallSpeed(float speed) {
        this.fallSpeed = speed;
        // Ajustar intervalo de movimiento basado en la velocidad
        moveInterval = Math.max(200, (int)(800 / speed));
    }

    public void update() {
        long currentTime = System.currentTimeMillis();

        // Actualizar efecto de brillo del grupo
        groupGlow += 0.03f;
        if (groupGlow > Math.PI * 2) groupGlow = 0;

        // Mover en pasos discretos de cuadrícula
        if (currentTime - lastMoveTime > moveInterval) {
            for (Block block : blocks) {
                block.y += blockSize; // Mover exactamente una casilla de cuadrícula
            }
            lastMoveTime = currentTime;
            updateBounds();
        }

        // Actualizar cada bloque individual
        for (Block block : blocks) {
            block.update();
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        // Si el grupo se está completando, dibujar efecto especial
        if (isCompleting) {
            drawCompletionEffect(canvas, paint);
        }

        // Dibujar efecto de brillo del grupo
        drawGroupGlow(canvas, paint);

        // Dibujar cada bloque
        for (Block block : blocks) {
            block.draw(canvas, paint);
        }

        // Dibujar conexiones entre bloques
        drawBlockConnections(canvas, paint);
    }
    private void drawGroupGlow(Canvas canvas, Paint paint) {
        if (blocks.isEmpty()) return;

        updateBounds();
        float glow = (float) (Math.sin(groupGlow) * 0.3 + 0.3);

        // Resplandor alrededor del grupo completo
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        paint.setColor(Color.argb((int)(50 * glow), 255, 255, 0));

        canvas.drawRect(minX - 5, minY - 5, maxX + 5, maxY + 5, paint);
    }

    private void drawBlockConnections(Canvas canvas, Paint paint) {
        // Dibujar líneas de conexión entre bloques adyacentes
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        paint.setColor(Color.argb(100, 255, 255, 255));

        for (int i = 0; i < blocks.size(); i++) {
            Block block1 = blocks.get(i);
            for (int j = i + 1; j < blocks.size(); j++) {
                Block block2 = blocks.get(j);

                // Si están adyacentes, dibujar conexión
                if (areAdjacent(block1, block2)) {
                    canvas.drawLine(
                            block1.x + block1.size/2, block1.y + block1.size/2,
                            block2.x + block2.size/2, block2.y + block2.size/2,
                            paint
                    );
                }
            }
        }
    }

    private boolean areAdjacent(Block block1, Block block2) {
        float dx = Math.abs(block1.x - block2.x);
        float dy = Math.abs(block1.y - block2.y);

        return (dx == blockSize && dy == 0) || (dx == 0 && dy == blockSize);
    }

    private void drawCompletionEffect(Canvas canvas, Paint paint) {
        long elapsed = System.currentTimeMillis() - completionStartTime;
        float progress = elapsed / 500.0f; // 500ms de animación

        if (progress <= 1.0f) {
            updateBounds();

            // Efecto de explosión de luz
            paint.setStyle(Paint.Style.FILL);
            int alpha = (int) (255 * (1.0f - progress));
            paint.setColor(Color.argb(alpha, 255, 255, 0));

            float centerX = (minX + maxX) / 2.0f;
            float centerY = (minY + maxY) / 2.0f;
            float radius = progress * 100;

            canvas.drawCircle(centerX, centerY, radius, paint);
        }
    }

    public boolean checkCollision(Block playerBlock) {
        for (Block block : blocks) {
            // Verificar colisión en todas las direcciones
            float dx = Math.abs(block.x - playerBlock.x);
            float dy = Math.abs(block.y - playerBlock.y);

            // Colisión desde abajo (el bloque disparado impacta la parte inferior del grupo)
            if (dx < blockSize * 0.5f && playerBlock.y < block.y && dy < blockSize) {
                return true;
            }

            // Colisión lateral
            if (dy < blockSize * 0.5f && dx < blockSize) {
                return true;
            }

            // Colisión general cercana
            if (dx < blockSize * 0.9f && dy < blockSize * 0.9f) {
                return true;
            }
        }
        return false;
    }

    public Block getClosestBlock(Block playerBlock) {
        if (blocks.isEmpty()) return null;

        Block closest = blocks.get(0);
        float minDistance = getDistance(playerBlock, closest);

        for (Block block : blocks) {
            float distance = getDistance(playerBlock, block);
            if (distance < minDistance) {
                minDistance = distance;
                closest = block;
            }
        }

        return closest;
    }

    private float getDistance(Block b1, Block b2) {
        float dx = b1.x - b2.x;
        float dy = b1.y - b2.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public int getBottomY() {
        updateBounds();
        return maxY;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public float getCenterX() {
        updateBounds();
        return (minX + maxX) / 2.0f;
    }

    public float getCenterY() {
        updateBounds();
        return (minY + maxY) / 2.0f;
    }

    public int getColor() {
        return blocks.isEmpty() ? Color.WHITE : blocks.get(0).color;
    }

    public boolean isCompleteRectangle() {
        if (blocks.isEmpty()) return false;

        updateBounds();
        int expectedWidth = (maxX - minX) / blockSize + 1;
        int expectedHeight = (maxY - minY) / blockSize + 1;
        int expectedBlocks = expectedWidth * expectedHeight;

        // Verificar si tenemos suficientes bloques para un rectángulo
        return blocks.size() >= expectedBlocks && isRectangularShape();
    }

    private boolean isRectangularShape() {
        // Simplificado: verificar si los bloques forman una forma rectangular
        // En una implementación completa, esto sería más sofisticado
        updateBounds();
        int blockSize = blocks.get(0).size;
        int rows = (maxY - minY) / blockSize + 1;
        int cols = (maxX - minX) / blockSize + 1;

        // Crear matriz para verificar la forma
        boolean[][] matrix = new boolean[rows][cols];

        for (Block block : blocks) {
            int row = ((int)block.y - minY) / blockSize;
            int col = ((int)block.x - minX) / blockSize;
            if (row >= 0 && row < rows && col >= 0 && col < cols) {
                matrix[row][col] = true;
            }
        }

        // Verificar si forma un rectángulo completo
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (!matrix[i][j]) {
                    return false;
                }
            }
        }

        return true;
    }

    private void updateBounds() {
        if (blocks.isEmpty()) return;

        minX = (int)blocks.get(0).x;
        maxX = (int)(blocks.get(0).x + blocks.get(0).size);
        minY = (int)blocks.get(0).y;
        maxY = (int)(blocks.get(0).y + blocks.get(0).size);

        for (Block block : blocks) {
            minX = Math.min(minX, (int)block.x);
            maxX = Math.max(maxX, (int)(block.x + block.size));
            minY = Math.min(minY, (int)block.y);
            maxY = Math.max(maxY, (int)(block.y + block.size));
        }
    }
}

// Clases para efectos visuales
class ParticleEffect {
    public float x, y;
    public float velocityX, velocityY;
    public int color;
    public long lifeTime;
    public long creationTime;
    public float size;
    public float alpha;

    public ParticleEffect(float x, float y, float vx, float vy, int color, long lifeTime) {
        this.x = x;
        this.y = y;
        this.velocityX = vx;
        this.velocityY = vy;
        this.color = color;
        this.lifeTime = lifeTime;
        this.creationTime = System.currentTimeMillis();
        this.size = 3 + (float)Math.random() * 4;
        this.alpha = 255;
    }

    public void update() {
        x += velocityX;
        y += velocityY;
        velocityY += 0.1f; // Gravedad

        // Calcular alpha basado en el tiempo de vida
        long elapsed = System.currentTimeMillis() - creationTime;
        float progress = (float) elapsed / lifeTime;
        alpha = 255 * (1.0f - progress);
        size *= 0.98f; // Reducir tamaño gradualmente
    }

    public void draw(Canvas canvas, Paint paint) {
        if (alpha > 0) {
            int currentAlpha = Math.max(0, (int) alpha);
            paint.setColor(Color.argb(currentAlpha,
                    Color.red(color), Color.green(color), Color.blue(color)));
            canvas.drawCircle(x, y, size, paint);
        }
    }

    public boolean isDead() {
        return System.currentTimeMillis() - creationTime > lifeTime || alpha <= 0;
    }
}

class ScorePopup {
    public float x, y;
    public String text;
    public long lifeTime;
    public long creationTime;
    public float alpha;
    public float offsetY;

    public ScorePopup(float x, float y, String text, long lifeTime) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.lifeTime = lifeTime;
        this.creationTime = System.currentTimeMillis();
        this.alpha = 255;
        this.offsetY = 0;
    }

    public void update() {
        offsetY -= 2; // Mover hacia arriba

        // Calcular alpha basado en el tiempo de vida
        long elapsed = System.currentTimeMillis() - creationTime;
        float progress = (float) elapsed / lifeTime;
        alpha = 255 * (1.0f - progress);
    }

    public void draw(Canvas canvas, Paint paint) {
        if (alpha > 0) {
            int currentAlpha = Math.max(0, (int) alpha);
            paint.setColor(Color.argb(currentAlpha, 255, 255, 0));
            paint.setTextSize(24);
            canvas.drawText(text, x, y + offsetY, paint);
        }
    }

    public boolean isDead() {
        return System.currentTimeMillis() - creationTime > lifeTime || alpha <= 0;
    }
}

class BackgroundStar {
    public float x, y;
    public float size;
    public float brightness;
    public float speed;

    public BackgroundStar(float x, float y, float brightness) {
        this.x = x;
        this.y = y;
        this.brightness = brightness;
        this.size = brightness * 2;
        this.speed = 0.5f + brightness;
    }

    public void update() {
        y += speed;

        // Efecto de parpadeo
        brightness += (Math.random() - 0.5) * 0.1;
        brightness = Math.max(0.1f, Math.min(1.0f, brightness));
    }
}