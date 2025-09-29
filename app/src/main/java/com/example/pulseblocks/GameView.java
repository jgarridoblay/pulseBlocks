// GameView.java
package com.example.pulseblocks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameThread gameThread;
    private Paint paint;
    private int screenWidth, screenHeight;
    private int blockSize = 60;
    private MainActivity mainActivity;
    private TextView scoreText;

    // Grid system
    private int gridWidth, gridHeight;
    private int gridOffsetX, gridOffsetY;

    // Cannon
    private int cannonGridX; // Posición del cañón en la cuadrícula
    private int cannonY;
    private int cannonWidth = blockSize;
    private int cannonHeight = blockSize * 2;
    private float cannonShakeX = 0; // Para animación de disparo
    private long lastShootTime = 0;

    // Game objects
    private List<Block> playerBlocks;
    private List<BlockGroup> fallingGroups;
    private List<Block> staticBlocks;
    private List<ParticleEffect> particles;
    private List<ScorePopup> scorePopups;

    // Game state
    private int score = 0;
    private int level = 1;
    private long lastGroupSpawn = 0;
    private long groupSpawnDelay = 3000; // 3 segundos
    private Random random;
    private boolean gameRunning = true;

    // Animation variables
    private long gameStartTime;
    private float pulseRadius = 0;
    private float pulseAlpha = 255;
    private List<BackgroundStar> stars;
    private float backgroundHue = 0;

    public GameView(Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);

        playerBlocks = new ArrayList<>();
        fallingGroups = new ArrayList<>();
        staticBlocks = new ArrayList<>();
        particles = new ArrayList<>();
        scorePopups = new ArrayList<>();
        stars = new ArrayList<>();
        random = new Random();
        gameStartTime = System.currentTimeMillis();

        // Crear estrellas de fondo
        createBackgroundStars();
    }

    public void setScoreText(TextView scoreText) {
        this.scoreText = scoreText;
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
        newBlock.setVelocityY(-8); // Velocidad más lenta para mejor control
        newBlock.setShootTime(System.currentTimeMillis()); // Para animación de trail
        playerBlocks.add(newBlock);

        // Animación de disparo del cañón
        cannonShakeX = random.nextFloat() * 10 - 5; // Shake entre -5 y 5
        lastShootTime = System.currentTimeMillis();

        // Crear efecto de partículas al disparar
        createShootParticles(pixelX + blockSize/2, cannonY);
    }

    private void createShootParticles(float x, float y) {
        for (int i = 0; i < 8; i++) {
            float angle = (float) (Math.PI * 2 * i / 8.0);
            float speed = 3 + random.nextFloat() * 2;
            particles.add(new ParticleEffect(x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed - 2,
                    Color.CYAN, 1000));
        }
    }

    private void createBackgroundStars() {
        for (int i = 0; i < 50; i++) {
            stars.add(new BackgroundStar(
                    random.nextFloat() * 1000,
                    random.nextFloat() * 1000,
                    random.nextFloat() * 0.5f + 0.5f
            ));
        }
    }

    private void spawnBlockGroup() {
        if (System.currentTimeMillis() - lastGroupSpawn > groupSpawnDelay) {
            BlockGroup group = createRandomGroup();
            fallingGroups.add(group);
            lastGroupSpawn = System.currentTimeMillis();

            // Aumentar dificultad
            if (groupSpawnDelay > 1000) {
                groupSpawnDelay -= 30;
            }
        }
    }

    private BlockGroup createRandomGroup() {
        int gridX = random.nextInt(Math.max(1, gridWidth - 4)); // Dejar espacio para las formas
        int type = random.nextInt(4); // 4 tipos diferentes

        BlockGroup group = new BlockGroup();
        group.setFallSpeed(2 + (level * 0.5f));

        int pixelX = gridToPixelX(gridX);
        int pixelY = gridToPixelY(-2); // Empezar fuera de la pantalla

        type = 0;
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

        // Actualizar animaciones de fondo
        updateBackgroundAnimations();

        // Actualizar partículas
        updateParticles();

        // Actualizar popups de puntuación
        updateScorePopups();

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
            for (BlockGroup group : fallingGroups) {
                if (group.checkCollision(block)) {
                    // Detener el movimiento del bloque
                    block.setVelocityY(0);

                    // Buscar el bloque del grupo más cercano
                    Block closestBlock = group.getClosestBlock(block);

                    if (closestBlock != null) {
                        // Determinar dirección de aproximación
                        float dx = block.x - closestBlock.x;
                        float dy = block.y - closestBlock.y;

                        // Posicionar según la dirección predominante
                        if (Math.abs(dy) > Math.abs(dx)) {
                            // Aproximación vertical
                            block.x = closestBlock.x; // Misma columna
                            if (dy < 0) {
                                // Viene desde arriba
                                block.y = closestBlock.y - blockSize;
                            } else {
                                // Viene desde abajo
                                block.y = closestBlock.y + blockSize;
                            }
                        } else {
                            // Aproximación horizontal
                            block.y = closestBlock.y; // Misma fila
                            if (dx < 0) {
                                // Viene desde la izquierda
                                block.x = closestBlock.x - blockSize;
                            } else {
                                // Viene desde la derecha
                                block.x = closestBlock.x + blockSize;
                            }
                        }
                    } else {
                        // Si no hay bloque cercano, alinear a cuadrícula
                        int gridX = pixelToGridX(block.x);
                        int gridY = pixelToGridY(block.y);
                        block.x = gridToPixelX(gridX);
                        block.y = gridToPixelY(gridY);
                    }

                    // Crear efecto visual
                    createCollisionParticles(block.x + blockSize/2, block.y + blockSize/2);

                    // IMPORTANTE: Agregar el bloque al grupo
                    group.addBlock(block);

                    // Remover de la lista de bloques del jugador
                    playerIterator.remove();

                    // Salir del bucle de grupos (ya encontramos colisión)
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
                mainActivity.runOnUiThread(() -> {
                    mainActivity.showGameOver(score);
                });
                return;
            }

            // Verificar si forma un rectángulo completo
            if (group.isCompleteRectangle()) {
                int points = group.getBlockCount() * 10;
                score += points;
                level = score / 500 + 1; // Subir nivel cada 500 puntos

                // Crear popup de puntuación
                createScorePopup(group.getCenterX(), group.getCenterY(), points);

                // Crear explosión de partículas
                createExplosionParticles(group.getCenterX(), group.getCenterY(), group.getColor());

                updateScore();
                groupIterator.remove();
            }
        }

        // Actualizar shake del cañón
        updateCannonShake();
    }

    private void updateBackgroundAnimations() {
        // Rotar el color de fondo lentamente
        backgroundHue += 0.5f;
        if (backgroundHue > 360) backgroundHue = 0;

        // Actualizar estrellas
        for (BackgroundStar star : stars) {
            star.update();
            if (star.y > screenHeight + 10) {
                star.y = -10;
                star.x = random.nextFloat() * screenWidth;
            }
        }

        // Actualizar pulso
        long time = System.currentTimeMillis();
        pulseRadius = ((time % 2000) / 2000.0f) * 150;
        pulseAlpha = 255 - (pulseRadius / 150.0f * 200);
    }

    private void updateParticles() {
        Iterator<ParticleEffect> particleIterator = particles.iterator();
        while (particleIterator.hasNext()) {
            ParticleEffect particle = particleIterator.next();
            particle.update();
            if (particle.isDead()) {
                particleIterator.remove();
            }
        }
    }

    private void updateScorePopups() {
        Iterator<ScorePopup> popupIterator = scorePopups.iterator();
        while (popupIterator.hasNext()) {
            ScorePopup popup = popupIterator.next();
            popup.update();
            if (popup.isDead()) {
                popupIterator.remove();
            }
        }
    }

    private void updateCannonShake() {
        long timeSinceShoot = System.currentTimeMillis() - lastShootTime;
        if (timeSinceShoot < 200) { // 200ms de shake
            float progress = timeSinceShoot / 200.0f;
            cannonShakeX *= (1.0f - progress); // Disminuir el shake gradualmente
        } else {
            cannonShakeX = 0;
        }
    }

    private void createCollisionParticles(float x, float y) {
        for (int i = 0; i < 5; i++) {
            float angle = random.nextFloat() * (float) Math.PI * 2;
            float speed = 2 + random.nextFloat() * 3;
            particles.add(new ParticleEffect(x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    Color.WHITE, 500));
        }
    }

    private void createExplosionParticles(float x, float y, int color) {
        for (int i = 0; i < 15; i++) {
            float angle = (float) (Math.PI * 2 * i / 15.0);
            float speed = 5 + random.nextFloat() * 5;
            particles.add(new ParticleEffect(x, y,
                    (float) Math.cos(angle) * speed,
                    (float) Math.sin(angle) * speed,
                    color, 800));
        }
    }

    private void createScorePopup(float x, float y, int points) {
        scorePopups.add(new ScorePopup(x, y, "+" + points, 1500));
    }

    private void updateScore() {
        if (scoreText != null) {
            mainActivity.runOnUiThread(() -> {
                scoreText.setText("Score: " + score + " | Level: " + level);
            });
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas == null) return;

        // Fondo animado con gradiente dinámico
        drawAnimatedBackground(canvas);

        // Dibujar estrellas de fondo
        drawBackgroundStars(canvas);

        // Dibujar cañón con animaciones
        drawAnimatedCannon(canvas);

        // Dibujar líneas de cuadrícula con efectos
        // drawAnimatedGrid(canvas);

        // Dibujar bloques del jugador con trails
        drawPlayerBlocks(canvas);

        // Dibujar grupos que caen con efectos
        for (BlockGroup group : fallingGroups) {
            group.draw(canvas, paint);
        }

        // Dibujar efectos de partículas
        drawParticles(canvas);

        // Dibujar popups de puntuación
        drawScorePopups(canvas);

        // Dibujar efectos de pulso
        drawEnhancedPulseEffect(canvas);
    }

    private void drawAnimatedBackground(Canvas canvas) {
        // Calcular colores HSV para el fondo dinámico
        float[] hsv = new float[3];
        hsv[0] = backgroundHue; // Hue rotando
        hsv[1] = 0.3f; // Saturación baja para no ser muy brillante
        hsv[2] = 0.1f; // Brillo muy bajo

        int backgroundColor = Color.HSVToColor(hsv);
        canvas.drawColor(backgroundColor);

        // Agregar líneas de energía que se mueven
        drawEnergyLines(canvas);
    }

    private void drawEnergyLines(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        long time = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            float offset = (time % 3000) / 3000.0f * screenWidth + (i * screenWidth / 3);
            if (offset > screenWidth) offset -= screenWidth * 1.5f;

            paint.setColor(Color.argb(50 + i * 20, 0, 150 + i * 50, 255));

            canvas.drawLine(offset - 100, 0, offset + 100, screenHeight, paint);
        }
    }

    private void drawBackgroundStars(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);

        for (BackgroundStar star : stars) {
            int alpha = (int) (star.brightness * 255);
            paint.setColor(Color.argb(alpha, 255, 255, 255));
            canvas.drawCircle(star.x, star.y, star.size, paint);
        }
    }

    private void drawAnimatedCannon(Canvas canvas) {
        int cannonPixelX = gridToPixelX(cannonGridX) + (int) cannonShakeX;

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        // Base del cañón con efecto de brillo
        int baseWidth = blockSize;
        int baseHeight = blockSize / 2;
        canvas.drawRect(
                cannonPixelX,
                cannonY + cannonHeight - baseHeight,
                cannonPixelX + baseWidth,
                cannonY + cannonHeight,
                paint
        );

        // Tubo del cañón
        int tubeWidth = blockSize / 2;
        int tubeHeight = cannonHeight - baseHeight;
        int tubeX = cannonPixelX + (baseWidth - tubeWidth) / 2;
        canvas.drawRect(
                tubeX,
                cannonY,
                tubeX + tubeWidth,
                cannonY + tubeHeight,
                paint
        );

        // Efecto de brillo en el cañón
        drawCannonGlow(canvas, cannonPixelX, cannonY);

        // Borde del cañón con pulso
        long time = System.currentTimeMillis();
        float glowIntensity = (float) (Math.sin(time * 0.01) * 0.5 + 0.5);
        int glowAlpha = (int) (100 + glowIntensity * 155);

        paint.setColor(Color.argb(glowAlpha, 0, 255, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);

        // Borde de la base
        canvas.drawRect(
                cannonPixelX,
                cannonY + cannonHeight - baseHeight,
                cannonPixelX + baseWidth,
                cannonY + cannonHeight,
                paint
        );

        // Borde del tubo
        canvas.drawRect(
                tubeX,
                cannonY,
                tubeX + tubeWidth,
                cannonY + tubeHeight,
                paint
        );
    }

    private void drawCannonGlow(Canvas canvas, int cannonX, int cannonY) {
        // Efecto de resplandor alrededor del cañón
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 3; i++) {
            int alpha = 20 - (i * 5);
            int size = i * 10;
            paint.setColor(Color.argb(alpha, 0, 255, 255));

            canvas.drawRect(
                    cannonX - size,
                    cannonY - size,
                    cannonX + cannonWidth + size,
                    cannonY + cannonHeight + size,
                    paint
            );
        }
    }

    private void drawPlayerBlocks(Canvas canvas) {
        for (Block block : playerBlocks) {
            // Dibujar trail del bloque
            drawBlockTrail(canvas, block);

            // Dibujar el bloque con efecto de brillo
            block.drawWithGlow(canvas, paint);
        }
    }

    private void drawBlockTrail(Canvas canvas, Block block) {
        long currentTime = System.currentTimeMillis();
        long shootTime = block.getShootTime();

        if (currentTime - shootTime < 1000) { // Trail por 1 segundo
            paint.setStyle(Paint.Style.FILL);

            // Crear trail de partículas
            for (int i = 0; i < 5; i++) {
                float trailY = block.y + (i * 15);
                int alpha = 100 - (i * 15);

                if (alpha > 0) {
                    paint.setColor(Color.argb(alpha, 0, 255, 255));
                    canvas.drawRect(
                            block.x + 5, trailY,
                            block.x + block.size - 5, trailY + 10,
                            paint
                    );
                }
            }
        }
    }

    private void drawParticles(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);

        for (ParticleEffect particle : particles) {
            particle.draw(canvas, paint);
        }
    }

    private void drawScorePopups(Canvas canvas) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(null);

        for (ScorePopup popup : scorePopups) {
            popup.draw(canvas, paint);
        }
    }

    private void drawEnhancedPulseEffect(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        // Múltiples ondas de pulso
        long time = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            float phaseOffset = i * 666; // Desfase entre ondas
            float radius = ((time + phaseOffset) % 2000) / 2000.0f * 200;
            float alpha = 100 - (radius / 200.0f * 100);

            if (alpha > 0) {
                paint.setColor(Color.argb((int) alpha, 0, 255 - i * 50, 255));
                canvas.drawCircle(screenWidth / 2, screenHeight / 2, radius, paint);
            }
        }

        // Efecto de energía en el centro
        paint.setStyle(Paint.Style.FILL);
        float centerPulse = (float) (Math.sin(time * 0.02) * 0.5 + 0.5);
        int centerAlpha = (int) (50 + centerPulse * 100);
        paint.setColor(Color.argb(centerAlpha, 255, 255, 255));
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, 10 + centerPulse * 5, paint);
    }

    private void drawGrid(Canvas canvas) {
        // Dibujar cuadrícula de referencia (muy sutil)
        paint.setColor(Color.argb(20, 255, 255, 255));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);

        // Líneas verticales
        for (int x = 0; x <= gridWidth; x++) {
            int pixelX = gridToPixelX(x);
            canvas.drawLine(pixelX, gridOffsetY, pixelX, screenHeight - 150, paint);
        }

        // Líneas horizontales
        for (int y = 0; y <= gridHeight; y++) {
            int pixelY = gridToPixelY(y);
            if (pixelY < screenHeight - 150) {
                canvas.drawLine(gridOffsetX, pixelY, gridOffsetX + (gridWidth * blockSize), pixelY, paint);
            }
        }
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