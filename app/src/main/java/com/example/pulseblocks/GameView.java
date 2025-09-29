// GameView.java
package com.example.pulseblocks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private Paint paint;
    private int screenWidth, screenHeight;
    private int blockSize = 60;

    // Grid system
    private int gridWidth, gridHeight;
    private int gridOffsetX, gridOffsetY;

    // Cannon
    private int cannonGridX; // Posición del cañón en la cuadrícula
    private int cannonX;
    private int cannonY;
    private int cannonWidth = blockSize;
    private int cannonHeight = blockSize * 2;

    // Game objects
    private List<Block> playerBlocks;
    private List<BlockGroup> fallingGroups;
    private List<Block> staticBlocks;

    // Game state
    private int score = 0;
    private int level = 1;
    private long lastGroupSpawn = 0;
    private long groupSpawnDelay = 3000; // 3 segundos
    private Random random;
    private boolean gameRunning = true;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);

        playerBlocks = new ArrayList<>();
        fallingGroups = new ArrayList<>();
        staticBlocks = new ArrayList<>();
        random = new Random();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight();

        // Configurar sistema de cuadrícula
        gridWidth = screenWidth / blockSize;
        gridHeight = screenHeight / blockSize;
        gridOffsetX = (screenWidth - (gridWidth * blockSize)) / 2;
        gridOffsetY = 50; // Margen superior

        // Posicionar el cañón en el centro de la cuadrícula
        cannonGridX = gridWidth / 2;
        cannonX = screenWidth / 2 - cannonWidth / 2;
        cannonY = screenHeight - cannonHeight - 50;

        gameThread = new GameThread();
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        gameRunning = false;
        if (gameThread != null) {
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getY() < screenHeight - 200) { // Solo disparar si no toca los botones
                shootBlock();
            }
        }
        return true;
    }

    public void moveCannon(int direction) {
        int newGridX = cannonGridX + direction;
        if (newGridX >= 0 && newGridX < gridWidth) {
            cannonGridX = newGridX;
        }
        int newX = cannonX + (direction * blockSize);
        if (newX >= 0 && newX <= screenWidth - cannonWidth) {
            cannonX = newX;
        }
    }

    // Convertir coordenadas de cuadrícula a píxeles
    private int gridToPixelX(int gridX) {
        return gridOffsetX + (gridX * blockSize);
    }

    private int gridToPixelY(int gridY) {
        return gridOffsetY + (gridY * blockSize);
    }

    // Convertir píxeles a coordenadas de cuadrícula
    private int pixelToGridX(float pixelX) {
        return (int)((pixelX - gridOffsetX) / blockSize);
    }

    private int pixelToGridY(float pixelY) {
        return (int)((pixelY - gridOffsetY) / blockSize);
    }

    private void shootBlock() {
        int pixelX = gridToPixelX(cannonGridX);
        Block newBlock = new Block(
                pixelX,
                cannonY - blockSize,
                blockSize,
                Color.CYAN
        );
        newBlock.setVelocityY(-blockSize / 4); // Velocidad alineada a la cuadrícula
        playerBlocks.add(newBlock);
    }

    private void spawnBlockGroup() {
        if (System.currentTimeMillis() - lastGroupSpawn > groupSpawnDelay) {
            BlockGroup group = createRandomGroup();
            fallingGroups.add(group);
            lastGroupSpawn = System.currentTimeMillis();

            // Aumentar dificultad
            if (groupSpawnDelay > 1000) {
                groupSpawnDelay -= 50;
            }
        }
    }

    private BlockGroup createRandomGroup() {
        int gridX = random.nextInt(Math.max(1, gridWidth - 4)); // Dejar espacio para las formas
        int type = random.nextInt(4); // 4 tipos diferentes

        BlockGroup group = new BlockGroup();
        group.setFallSpeed(blockSize / 30.0f); // Velocidad alineada a cuadrícula

        int pixelX = gridToPixelX(gridX);
        int pixelY = gridToPixelY(0); // Empezar en la primera fila

        //type = 0;
        switch (type) {
            case 0: // Línea en L
                group.addBlock(new Block(pixelX, pixelY, blockSize, Color.RED));
                group.addBlock(new Block(pixelX, pixelY + blockSize, blockSize, Color.RED));
                group.addBlock(new Block(pixelX + blockSize, pixelY, blockSize, Color.RED));
                break;
            case 1: // T invertida
                group.addBlock(new Block(pixelX + blockSize, pixelY, blockSize, Color.GREEN));
                group.addBlock(new Block(pixelX, pixelY + blockSize, blockSize, Color.GREEN));
                group.addBlock(new Block(pixelX + blockSize, pixelY + blockSize, blockSize, Color.GREEN));
                group.addBlock(new Block(pixelX + blockSize * 2, pixelY + blockSize, blockSize, Color.GREEN));
                break;
            case 2: // U invertida
                group.addBlock(new Block(pixelX, pixelY, blockSize, Color.BLUE));
                group.addBlock(new Block(pixelX + blockSize * 2, pixelY, blockSize, Color.BLUE));
                group.addBlock(new Block(pixelX, pixelY + blockSize, blockSize, Color.BLUE));
                group.addBlock(new Block(pixelX + blockSize, pixelY + blockSize, blockSize, Color.BLUE));
                group.addBlock(new Block(pixelX + blockSize * 2, pixelY + blockSize, blockSize, Color.BLUE));
                break;
            default: // Línea simple
                group.addBlock(new Block(pixelX, pixelY, blockSize, Color.YELLOW));
                group.addBlock(new Block(pixelX + blockSize, pixelY, blockSize, Color.YELLOW));
                group.addBlock(new Block(pixelX + blockSize * 2, pixelY, blockSize, Color.YELLOW));
                break;
        }

        return group;
    }

    private void update() {
        if (!gameRunning) return;

        // Spawner grupos
        spawnBlockGroup();

        // Actualizar bloques del jugador
        Iterator<Block> playerIterator = playerBlocks.iterator();
        while (playerIterator.hasNext()) {
            Block block = playerIterator.next();
            block.update();

            // Remover si sale de la pantalla
            if (block.y < -blockSize) {
                playerIterator.remove();
                continue;
            }

            // Verificar colisión con grupos que caen
            boolean collision = false;
            for (BlockGroup group : fallingGroups) {
                if (group.checkCollision(block)) {
                    group.addBlock(block);
                    playerIterator.remove();
                    collision = true;
                    break;
                }
            }
        }

        // Actualizar grupos que caen
        Iterator<BlockGroup> groupIterator = fallingGroups.iterator();
        while (groupIterator.hasNext()) {
            BlockGroup group = groupIterator.next();
            group.update();

            // Verificar si llegó al suelo
            if (group.getBottomY() >= screenHeight - 150) {
                // Game Over
                gameRunning = false;
                return;
            }

            // Verificar si forma un rectángulo completo
            if (group.isCompleteRectangle()) {
                score += group.getBlockCount() * 10;
                ((MainActivity) getContext()).updateScore(score);
                groupIterator.remove();
            }
        }
    }

    public void draw(Canvas canvas) {
        if (canvas == null) return;

        // Limpiar pantalla con gradiente oscuro
        canvas.drawColor(Color.rgb(10, 10, 20));

        // Dibujar cañón
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(cannonX, cannonY, cannonX + cannonWidth, cannonY + cannonHeight, paint);

        // Dibujar bloques del jugador
        for (Block block : playerBlocks) {
            block.draw(canvas, paint);
        }

        // Dibujar grupos que caen
        for (BlockGroup group : fallingGroups) {
            group.draw(canvas, paint);
        }

        // Dibujar efecto de pulso (opcional)
        drawPulseEffect(canvas);
    }

    private void drawPulseEffect(Canvas canvas) {
        // Efecto visual simple de pulso
        paint.setColor(Color.argb(30, 0, 255, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        long time = System.currentTimeMillis();
        float pulseRadius = (time % 1000) / 1000.0f * 100;
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, pulseRadius, paint);
    }

    private class GameThread extends Thread {
        @Override
        public void run() {
            while (gameRunning) {
                Canvas canvas = null;
                try {
                    canvas = getHolder().lockCanvas();
                    synchronized (getHolder()) {
                        update();
                        draw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        getHolder().unlockCanvasAndPost(canvas);
                    }
                }

                try {
                    Thread.sleep(16); // ~60 FPS
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}


