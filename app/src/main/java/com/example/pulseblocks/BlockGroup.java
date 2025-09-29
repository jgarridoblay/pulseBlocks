// BlockGroup.java
package com.example.pulseblocks;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

class BlockGroup {
    private List<Block> blocks;
    private float fallSpeed = 2;
    private int minX, maxX, minY, maxY;

    public BlockGroup() {
        blocks = new ArrayList<>();
    }

    public void addBlock(Block block) {
        blocks.add(block);
        updateBounds();
    }

    public void setFallSpeed(float speed) {
        this.fallSpeed = speed;
    }

    public void update() {
        for (Block block : blocks) {
            block.y += fallSpeed;
        }
        updateBounds();
    }

    public void draw(Canvas canvas, Paint paint) {
        for (Block block : blocks) {
            block.draw(canvas, paint);
        }
    }

    public boolean checkCollision(Block playerBlock) {
        for (Block block : blocks) {
            if (block.intersects(playerBlock)) {
                return true;
            }
        }
        return false;
    }

    public int getBottomY() {
        updateBounds();
        return maxY;
    }

    public int getBlockCount() {
        return blocks.size();
    }

    public boolean isCompleteRectangle() {
        if (blocks.isEmpty()) return false;

        updateBounds();
        int expectedWidth = (maxX - minX) / blocks.get(0).size + 1;
        int expectedHeight = (maxY - minY) / blocks.get(0).size + 1;
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