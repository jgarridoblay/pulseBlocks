// MainActivity.java
package com.example.pulseblocks;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
        // Botón izquierdo
        Button leftButton = new Button(this);
        leftButton.setText("←");
        leftButton.setTextSize(24);
        leftButton.setBackgroundColor(Color.DKGRAY);
        leftButton.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(100, 100);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftParams.setMargins(20, 0, 0, 20);
        mainLayout.addView(leftButton, leftParams);

        // Botón derecho
        Button rightButton = new Button(this);
        rightButton.setText("→");
        rightButton.setTextSize(24);
        rightButton.setBackgroundColor(Color.DKGRAY);
        rightButton.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(100, 100);
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