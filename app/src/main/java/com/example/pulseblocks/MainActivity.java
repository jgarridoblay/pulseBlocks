// MainActivity.java
package com.example.pulseblocks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private GameView gameView;
    private MenuView menuView;
    private OptionsView optionsView;
    private RecordsView recordsView;
    private GameOverView gameOverView;
    private RelativeLayout mainLayout;

    // Estados de la aplicación
    public static final int STATE_MENU = 0;
    public static final int STATE_GAME = 1;
    public static final int STATE_OPTIONS = 2;
    public static final int STATE_RECORDS = 3;
    public static final int STATE_GAME_OVER = 4;

    private int currentState = STATE_MENU;

    // Configuración
    private SharedPreferences prefs;
    private float musicVolume = 0.7f;
    private float sfxVolume = 0.8f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar SharedPreferences
        prefs = getSharedPreferences("PulseBlocksPrefs", Context.MODE_PRIVATE);
        musicVolume = prefs.getFloat("musicVolume", 0.7f);
        sfxVolume = prefs.getFloat("sfxVolume", 0.8f);

        // Crear layout principal
        mainLayout = new RelativeLayout(this);
        mainLayout.setBackgroundColor(Color.BLACK);

        // Crear las diferentes vistas
        createViews();

        // Mostrar menú inicial
        showMenu();

        setContentView(mainLayout);
        // Ocultar barra de estado y navegación

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ usa WindowInsetsController
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                // Oculta barra de estado y navegación
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                // Permite que se muestren temporalmente al deslizar
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                );
            }
        } else {
            // Compatibilidad con versiones anteriores
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //gameView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //gameView.pause();
    }

    private void createViews() {
        // Vista del menú
        menuView = new MenuView(this);

        // Vista del juego
        gameView = new GameView(this);

        // Vista de opciones
        optionsView = new OptionsView(this);

        // Vista de récords
        recordsView = new RecordsView(this);

        // Vista de game over
        gameOverView = new GameOverView(this);
    }

    public void showMenu() {
        currentState = STATE_MENU;
        mainLayout.removeAllViews();
        mainLayout.addView(menuView);
    }

    public void startGame() {
        currentState = STATE_GAME;
        mainLayout.removeAllViews();

        // Recrear gameView para reiniciar el juego
        gameView = new GameView(this);
        mainLayout.addView(gameView);

        // Crear controles
        createGameControls();
    }

    public void showOptions() {
        currentState = STATE_OPTIONS;
        mainLayout.removeAllViews();
        mainLayout.addView(optionsView);
    }

    public void showRecords() {
        currentState = STATE_RECORDS;
        mainLayout.removeAllViews();
        mainLayout.addView(recordsView);
    }

    public void showGameOver(int finalScore) {
        currentState = STATE_GAME_OVER;

        // Verificar si es un nuevo récord
        boolean isNewRecord = saveScoreIfRecord(finalScore);

        mainLayout.removeAllViews();
        gameOverView.setScore(finalScore, isNewRecord);
        mainLayout.addView(gameOverView);
    }

    private boolean saveScoreIfRecord(int score) {
        List<Integer> records = getTopScores();

        // Si hay menos de 10 récords o el score es mayor que el menor récord
        if (records.size() < 10 || score > records.get(records.size() - 1)) {
            records.add(score);
            records.sort((a, b) -> b.compareTo(a)); // Ordenar descendente

            // Mantener solo los top 10
            if (records.size() > 10) {
                records = records.subList(0, 10);
            }

            // Guardar récords
            saveTopScores(records);
            return true;
        }

        return false;
    }

    private List<Integer> getTopScores() {
        List<Integer> scores = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            int score = prefs.getInt("record_" + i, -1);
            if (score != -1) {
                scores.add(score);
            }
        }
        return scores;
    }

    private void saveTopScores(List<Integer> scores) {
        SharedPreferences.Editor editor = prefs.edit();

        // Limpiar récords anteriores
        for (int i = 0; i < 10; i++) {
            editor.remove("record_" + i);
        }

        // Guardar nuevos récords
        for (int i = 0; i < scores.size(); i++) {
            editor.putInt("record_" + i, scores.get(i));
        }

        editor.apply();
    }

    private void createGameControls() {
        int blockSize = gameView.getBlockSize();
        // Botón izquierdo
        Button leftButton = new Button(this);
        leftButton.setText("");
        leftButton.setTextSize(blockSize / 2f);
        leftButton.setTextColor(Color.WHITE);

// Fondo futurista con gradiente y bordes
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#0F2027"), Color.parseColor("#2C5364")}
        );
        bg.setCornerRadius(40f);
        bg.setStroke(4, Color.CYAN);

        leftButton.setBackground(bg);

// Añadir el drawable de la flecha
        Drawable arrow = ContextCompat.getDrawable(this, R.drawable.arrow_left);
        leftButton.setCompoundDrawablesWithIntrinsicBounds(arrow, null, null, null);
        leftButton.setCompoundDrawablePadding(16);

// Layout params
        RelativeLayout.LayoutParams leftParams =
                new RelativeLayout.LayoutParams(blockSize * 3, blockSize * 3);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftParams.setMargins(20, 0, 0, 20);

        mainLayout.addView(leftButton, leftParams);

        // Botón derecho
        Button rightButton = new Button(this);
        rightButton.setText(""); // sin texto
        rightButton.setTextSize(blockSize / 2f);
        rightButton.setTextColor(Color.WHITE);

// Fondo futurista
        GradientDrawable bgRight = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#0F2027"), Color.parseColor("#2C5364")}
        );
        bgRight.setCornerRadius(40f);
        bgRight.setStroke(4, Color.CYAN);

        rightButton.setBackground(bgRight);

// Añadir la flecha a la derecha
        Drawable arrowRight = ContextCompat.getDrawable(this, R.drawable.arrow_right);
        rightButton.setCompoundDrawablesWithIntrinsicBounds(null, null, arrowRight, null);
        rightButton.setCompoundDrawablePadding(16);

// Layout params
        RelativeLayout.LayoutParams rightParams =
                new RelativeLayout.LayoutParams(blockSize * 3, blockSize * 3);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightParams.setMargins(0, 0, 20, 20);

        mainLayout.addView(rightButton, rightParams);


        // Texto de puntuación
        TextView scoreText = new TextView(this);
        scoreText.setText("Score: 0");
        scoreText.setTextSize(24);
        scoreText.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams scoreParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        scoreParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        scoreParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        scoreParams.setMargins(0, 20, 0, 0);
        mainLayout.addView(scoreText, scoreParams);

        // Configurar listeners
        leftButton.setOnClickListener(v -> gameView.moveCannon(-1));
        rightButton.setOnClickListener(v -> gameView.moveCannon(1));

        // Configurar actualización de puntuación
        gameView.setScoreText(scoreText);
    }

    public void updateVolumes(float music, float sfx) {
        this.musicVolume = music;
        this.sfxVolume = sfx;

        // Guardar en preferencias
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("musicVolume", music);
        editor.putFloat("sfxVolume", sfx);
        editor.apply();
    }

    public float getMusicVolume() { return musicVolume; }
    public float getSfxVolume() { return sfxVolume; }

    public List<Integer> getRecords() {
        return getTopScores();
    }

    @Override
    public void onBackPressed() {
        if (currentState == STATE_GAME) {
            showMenu();
        } else if (currentState != STATE_MENU) {
            showMenu();
        } else {
            super.onBackPressed();
        }
    }
}