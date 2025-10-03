// GameView.java
package com.example.pulseblocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
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
    private final Paint paint;
    private int screenWidth, screenHeight;
    private final int blockSize = Resources.getSystem().getDisplayMetrics().widthPixels / 17;
    private final MainActivity mainActivity;
    private TextView scoreText;

    // Grid system
    private int gridWidth, gridHeight;
    private int gridOffsetX, gridOffsetY;

    // Cannon

    private int cannonGridX; // Posición del cañón en la cuadrícula
    private int cannonY;
    private final int cannonWidth = blockSize;
    private final int cannonHeight = blockSize * 2;
    private float cannonShakeX = 0; // Para animación de disparo
    private long lastShootTime = 0;
    private long lastTouchTime = 0; // Guardamos el tiempo del último toque
    private static final long MIN_CLICK_INTERVAL = 250; // 500 ms = 0.5 segundos

    // Game objects
    private final List<Block> playerBlocks;
    private final List<BlockGroup> fallingGroups;
    private final List<ParticleEffect> particles;
    private final List<ScorePopup> scorePopups;

    // Game state
    private int score = 0;
    private int level = 0;
    private long lastGroupSpawn = 0;
    private long groupSpawnDelay = 12000; // 3 segundos
    private final Random random;
    private boolean gameRunning = true;

    // Animation variables
    private List<BackgroundStar> stars;
    private float backgroundHue = 0;
    private boolean cannonCanShot = true;
    private int cannonOverheat = 0;

    public GameView(Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);

        playerBlocks = new ArrayList<>();
        fallingGroups = new ArrayList<>();
        particles = new ArrayList<>();
        scorePopups = new ArrayList<>();
        stars = new ArrayList<>();
        random = new Random();

        // Crear estrellas de fondo
        createBackgroundStars();
    }

    public void setScoreText(TextView scoreText) {
        this.scoreText = scoreText;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        screenWidth = getWidth();
        screenHeight = getHeight() - 3 * blockSize;

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
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastTouchTime < MIN_CLICK_INTERVAL) {
                        return true;
                    }
                    lastTouchTime = currentTime;
                    if (cannonOverheat < 220) {
                        shootBlock();
                        cannonOverheat = 20 + cannonOverheat * 2;
                    }
                }
            }
        }
        return true;
    }

    public int getBlockSize () {
        return blockSize;
    }

    public void moveCannon(int direction) {
        int newGridX = cannonGridX + direction;
        if (newGridX >= 0 && newGridX < gridWidth) {
            cannonGridX = newGridX;
        }
    }

    public int[][] matrixBlockGenerator(int N, int M) {
        Random rand = new Random();
        int[][] matriz = new int[N][M];
        // 1. Primera fila: todos 1
        for (int j = 0; j < N; j++) {
            matriz[j][0] = 1;
        }
        // 2. Segunda fila: mezcla de 1 y 0 (no todos iguales)
        boolean valida;
        do {
            valida = true;
            int suma = 0;
            for (int j = 0; j < N; j++) {
                matriz[j][1] = rand.nextInt(2);
                // 0 o 1
                suma += matriz[j][1];
            }
            if (suma == 0 || suma == N) {
                valida = false;
                // todos 0 o todos 1 → repetir
            }
        } while (!valida);
        // 3. Tercera fila en adelante
        for (int i = 2; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (matriz[j][i - 1] == 1) {
                    matriz[j][i] = rand.nextInt(2);
                    // 0 o 1
                } else {
                    matriz[j][i] = 0;
                }
            }
        }
        return matriz;
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
        return (int) ((pixelX - gridOffsetX) / blockSize);
    }

    private int pixelToGridY(float pixelY) {
        return (int) ((pixelY - gridOffsetY) / blockSize);
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
        synchronized (playerBlocks) {
            playerBlocks.add(newBlock);
        }

        // Animación de disparo del cañón
        cannonShakeX = random.nextFloat() * 10 - 5; // Shake entre -5 y 5
        lastShootTime = System.currentTimeMillis();

        // Crear efecto de partículas al disparar
        createShootParticles(pixelX + blockSize / 2, cannonY);
    }

    private void createShootParticles(float x, float y) {
        for (int i = 0; i < 8; i++) {
            float angle = (float) (Math.PI * 2 * i / 8.0);
            float speed = 3 + random.nextFloat() * 2;

            synchronized (particles) {
                particles.add(new ParticleEffect(x, y,
                        (float) Math.cos(angle) * speed,
                        (float) Math.sin(angle) * speed - 2,
                        Color.CYAN, 1000));
            }
        }
    }

    private void createBackgroundStars() {
        int totalStars = 150; // Más estrellas para mayor densidad

        for (int i = 0; i < totalStars; i++) {
            float x = random.nextFloat() * 1080; // ancho de pantalla aproximado
            float y = random.nextFloat() * 1920; // alto de pantalla aproximado
            float size = random.nextFloat() * 2.5f + 0.5f; // tamaño variable
            float speed = random.nextFloat() * 0.3f; // velocidad de movimiento
            int color = Color.rgb(
                    (int)(50 + random.nextFloat() * 205), // rojo
                    (int)(150 + random.nextFloat() * 105), // verde
                    255 // azul neón
            );

            stars.add(new BackgroundStar(x, y, size, speed, color));
        }
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
        int[][] matrix = matrixBlockGenerator(2 + level, 2 + level);
        int gridX = random.nextInt(Math.max(1, gridWidth - matrix[0].length)); // Dejar espacio para las formas
        //int type = random.nextInt(4); // 4 tipos diferentes

        BlockGroup group = new BlockGroup();
        group.setFallSpeed(0.6f + (level * 0.4f));

        int pixelX = gridToPixelX(gridX);
        int pixelY = gridToPixelY(-2); // Empezar fuera de la pantalla

        for (int line = 0; line < matrix.length; line++) {
            for (int column = 0; column < matrix[0].length; column++) {
                if (matrix[line][column] == 1) {
                    group.addBlock(new Block(pixelX + blockSize * line, pixelY + blockSize * column, blockSize, Color.RED));
                }
            }
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

        updateOverheatBar();

        // Actualizar bloques del jugador
        synchronized (playerBlocks) {
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
                        createCollisionParticles(block.x + blockSize / 2, block.y + blockSize / 2);

                        // IMPORTANTE: Agregar el bloque al grupo
                        group.addBlock(block);

                        // Remover de la lista de bloques del jugador
                        playerIterator.remove();

                        // Verificar si forma un rectángulo completo
                        if (group.isCompleteRectangle()) {
                            List<BlockGroup> groupsToRemove = new ArrayList<>();
                            int points = group.getBlockCount() * 10;
                            score += points;
                            level = score / 500 + 1; // Subir nivel cada 500 puntos
                            cannonOverheat = 0; // Eliminar el sobrecalentamiento

                            // Crear popup de puntuación
                            createScorePopup(group.getCenterX(), group.getCenterY(), points);

                            // Crear explosión de partículas
                            createExplosionParticles(group.getCenterX(), group.getCenterY(), group.getColor());

                            updateScore();
                            group.startDisappearWithEffect();
                            // Marcar para eliminar
                            groupsToRemove.add(group);
                            for (BlockGroup blocks : groupsToRemove) {
                                fallingGroups.remove(blocks);
                                System.out.println("Grupo eliminado. Grupos restantes: " + fallingGroups.size());
                            }
                        }
                        // Salir del bucle de grupos (ya encontramos colisión)
                        break;
                    }
                }
            }
        }

        // Actualizar grupos que caen
        Iterator<BlockGroup> groupIterator = fallingGroups.iterator();
        while (groupIterator.hasNext()) {
            BlockGroup group = groupIterator.next();
            group.update();

            // Verificar si llegó al suelo
            if (group.getBottomY() >= screenHeight - 6 * blockSize) {
                // Game Over
                gameRunning = false;
                mainActivity.runOnUiThread(() -> {
                    mainActivity.showGameOver(score);
                });
                return;
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
    }

    private void updateParticles() {
        synchronized (particles) {
            Iterator<ParticleEffect> particleIterator = particles.iterator();
            while (particleIterator.hasNext()) {
                ParticleEffect particle = particleIterator.next();
                particle.update();
                if (particle.isDead()) {
                    particleIterator.remove();
                }
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
            synchronized (particles) {
                particles.add(new ParticleEffect(x, y,
                        (float) Math.cos(angle) * speed,
                        (float) Math.sin(angle) * speed,
                        Color.WHITE, 500));
            }
        }
    }

    private void createExplosionParticles(float x, float y, int color) {
        for (int i = 0; i < 15; i++) {
            float angle = (float) (Math.PI * 2 * i / 15.0);
            float speed = 5 + random.nextFloat() * 5;
            synchronized (particles) {
                particles.add(new ParticleEffect(x, y,
                        (float) Math.cos(angle) * speed,
                        (float) Math.sin(angle) * speed,
                        color, 800));
            }
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

    private void updateOverheatBar() {
        mainActivity.runOnUiThread(() -> {
            // Overheat bar
            mainActivity.animateOverheatBarTo(cannonOverheat);
        });

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

        // Dibujar el cañón (siempre al final para que esté encima)
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

        //paint.setColor(Color.WHITE);
        //paint.setStyle(Paint.Style.FILL);

        int baseHeight = blockSize / 2;
        int baseWidth = (int) (blockSize * 1.2);

        int tubeWidth = blockSize - 5;
        int tubeHeight = blockSize;

// Tubo con gradiente brillante
        Paint tubePaint = new Paint();
        tubePaint.setShader(new LinearGradient(
                cannonPixelX,
                cannonY,
                cannonPixelX + tubeWidth,
                cannonY + tubeHeight,
                Color.WHITE,
                Color.CYAN,
                Shader.TileMode.CLAMP
        ));
// Tubo redondeado
        RectF tubeRect = new RectF(
                cannonPixelX,
                cannonY,
                cannonPixelX + tubeWidth,
                cannonY + tubeHeight + blockSize
        );
        canvas.drawRoundRect(tubeRect, 15, 15, tubePaint);

// Pintura con gradiente para la base
        Paint basePaint = new Paint();
        basePaint.setShader(new LinearGradient(
                (float) cannonPixelX,
                (float) cannonY + cannonHeight - baseHeight,
                (float) (cannonPixelX + baseWidth),
                (float) cannonY + cannonHeight,
                Color.CYAN,
                Color.BLUE,
                Shader.TileMode.CLAMP
        ));

// Alas del cañón
        RectF baseRect = new RectF(
                cannonPixelX - (baseWidth) / 2f,
                cannonY + cannonHeight / 2f - baseHeight,
                cannonPixelX + (baseWidth) / 2f+ blockSize,
                cannonY + cannonHeight / 2f
        );
        canvas.drawRoundRect(baseRect, blockSize / 2f, blockSize / 2f, tubePaint);

        RectF baseRect2 = new RectF(
                cannonPixelX - (baseWidth) / 3f,
                cannonY + cannonHeight * 3 / 4f - baseHeight * 2,
                cannonPixelX + (baseWidth) / 3f + blockSize,
                cannonY + cannonHeight * 3 / 4f
        );
        canvas.drawRoundRect(baseRect2, blockSize / 2f, blockSize / 2f, tubePaint);

// Sobrecalentamiento del cañón
        Paint overheatPaint = new Paint();
        overheatPaint.setShader(new LinearGradient(
                cannonPixelX,                               // X0
                cannonY,                                    // Y0 (arriba del cañón)
                cannonPixelX,                               // X1
                cannonY + cannonHeight,                     // Y1 (abajo del cañón)
                Color.argb(cannonOverheat, 255, 0, 0),                                  // Color inicial (arriba)
                Color.TRANSPARENT,                          // Color final (abajo)
                Shader.TileMode.CLAMP                       // Extiende transparente hacia fuera
        ));
        if (cannonOverheat > 0) {
            cannonOverheat += -1;
        }
        canvas.drawCircle(
                cannonPixelX + baseWidth / 2f,
                cannonY + cannonHeight - tubeHeight,
                baseWidth,
                overheatPaint
        );

// Líneas de energía alrededor del tubo
        Paint glowPaint = new Paint();
        glowPaint.setColor(Color.CYAN);
        glowPaint.setAlpha(120);
        glowPaint.setStrokeWidth(4f);
        for (int i = -2; i <= 2; i++) {
            canvas.drawLine(
                    cannonPixelX - 10,
                    cannonY + tubeHeight / 2f + i * 10,
                    cannonPixelX,
                    cannonY + tubeHeight / 2f + i * 10,
                    glowPaint
            );
            canvas.drawLine(
                    cannonPixelX + tubeWidth,
                    cannonY + tubeHeight / 2f + i * 10,
                    cannonPixelX + tubeWidth + 10,
                    cannonY + tubeHeight / 2f + i * 10,
                    glowPaint
            );
        }


        // Efecto de brillo en el cañón
        //drawCannonGlow(canvas, cannonPixelX, cannonY);

        // Borde del cañón con pulso
        //long time = System.currentTimeMillis();
        //float glowIntensity = (float) (Math.sin(time * 0.01) * 0.5 + 0.5);
        //int glowAlpha = (int) (100 + glowIntensity * 155);
//
        //paint.setColor(Color.argb(glowAlpha, 0, 255, 255));
        //paint.setStyle(Paint.Style.STROKE);
        //paint.setStrokeWidth(3);
//
        //// Borde de la base
        //canvas.drawRect(
        //        cannonPixelX,
        //        cannonY + cannonHeight - baseHeight,
        //        cannonPixelX + baseWidth,
        //        cannonY + cannonHeight,
        //        paint
        //);
//
        //// Borde del tubo
        //canvas.drawRect(
        //        tubeX,
        //        cannonY,
        //        tubeX + tubeWidth,
        //        cannonY + tubeHeight,
        //        paint
        //);
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
        synchronized (playerBlocks) {
            for (Block block : playerBlocks) {
                // Dibujar trail del bloque
                drawBlockTrail(canvas, block);

                // Dibujar el bloque con efecto de brillo
                block.drawWithGlow(canvas, paint);
            }
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

        synchronized (particles) {
            for (ParticleEffect particle : particles) {
                particle.draw(canvas, paint);
            }
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

class BackgroundStar {
    public float x, y;
    public float size;
    public float brightness;
    public float speed;
    public int color;
    private float alpha; // para parpadeo
    private boolean fading;

    public BackgroundStar(float x, float y, float brightness, float speed, int color) {
        this.x = x;
        this.y = y;
        this.brightness = brightness;
        this.size = brightness * 2;
        this.speed = 0.5f + brightness;
        this.color = color;
        this.alpha = 0.5f + new Random().nextFloat() * 0.5f;
        this.fading = new Random().nextBoolean();
    }

    public void update() {
        y += speed;
        if (y > 1920) y = 0;

        // Efecto de parpadeo
        brightness += (float) ((Math.random() - 0.5) * 0.1);
        brightness = Math.max(0.1f, Math.min(1.0f, brightness));
        if (fading) {
            alpha -= 0.01f;
            if (alpha <= 0.3f) fading = false;
        } else {
            alpha += 0.01f;
            if (alpha >= 1f) fading = true;
        }
    }
}

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

