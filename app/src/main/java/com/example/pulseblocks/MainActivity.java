// MainActivity.java
package com.example.pulseblocks;

import android.app.Activity;
import android.content.Context;
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
    private Button leftButton, rightButton;
    private TextView scoreText;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Crear layout principal
        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(Color.BLACK);

        // Crear vista del juego
        gameView = new GameView(this);
        RelativeLayout.LayoutParams gameParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        layout.addView(gameView, gameParams);

        // Crear botón izquierdo
        leftButton = new Button(this);
        leftButton.setText("←");
        leftButton.setTextSize(24);
        leftButton.setBackgroundColor(Color.DKGRAY);
        leftButton.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(100, 100);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        leftParams.setMargins(20, 0, 0, 20);
        layout.addView(leftButton, leftParams);

        // Crear botón derecho
        rightButton = new Button(this);
        rightButton.setText("→");
        rightButton.setTextSize(24);
        rightButton.setBackgroundColor(Color.DKGRAY);
        rightButton.setTextColor(Color.WHITE);
        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(100, 100);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rightParams.setMargins(0, 0, 20, 20);
        layout.addView(rightButton, rightParams);

        // Crear texto de puntuación
        scoreText = new TextView(this);
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
        layout.addView(scoreText, scoreParams);

        // Configurar listeners
        leftButton.setOnClickListener(v -> gameView.moveCannon(-1));
        rightButton.setOnClickListener(v -> gameView.moveCannon(1));

        setContentView(layout);
    }

    public void updateScore(int newScore) {
        score = newScore;
        runOnUiThread(() -> scoreText.setText("Score: " + score));
    }
}
