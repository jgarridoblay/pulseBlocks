package com.example.pulseblocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

// Vista de Game Over con animaciones
class GameOverView extends LinearLayout {
    private MainActivity mainActivity;
    private int finalScore;
    private boolean isNewRecord;

    public GameOverView(Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.BLACK);
    }

    public void setScore(int score, boolean newRecord) {
        this.finalScore = score;
        this.isNewRecord = newRecord;
        removeAllViews();
        createGameOverUI();
        startGameOverAnimations();
    }

    private void createGameOverUI() {
        // Título Game Over
        TextView gameOverTitle = new TextView(getContext());
        gameOverTitle.setText("GAME OVER");
        gameOverTitle.setTextSize(42);
        gameOverTitle.setTextColor(Color.RED);
        gameOverTitle.setTypeface(null, Typeface.BOLD);
        gameOverTitle.setGravity(Gravity.CENTER);
        gameOverTitle.setTag("gameOverTitle");

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 50);
        addView(gameOverTitle, titleParams);

        // Puntuación final
        TextView scoreText = new TextView(getContext());
        scoreText.setText("Puntuación Final: " + finalScore);
        scoreText.setTextSize(28);
        scoreText.setTextColor(Color.WHITE);
        scoreText.setGravity(Gravity.CENTER);
        scoreText.setTag("scoreText");
        addView(scoreText);

        // Mensaje de nuevo récord
        if (isNewRecord) {
            TextView newRecordText = new TextView(getContext());
            newRecordText.setText("¡NUEVO RÉCORD!");
            newRecordText.setTextSize(24);
            newRecordText.setTextColor(Color.YELLOW);
            newRecordText.setTypeface(null, Typeface.BOLD);
            newRecordText.setGravity(Gravity.CENTER);
            newRecordText.setTag("newRecord");

            LinearLayout.LayoutParams recordParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            recordParams.setMargins(0, 20, 0, 40);
            addView(newRecordText, recordParams);
        } else {
            // Espacio en blanco para mantener el layout
            View spacer = new View(getContext());
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 60
            );
            addView(spacer, spacerParams);
        }

        // Botones con animaciones
        createAnimatedGameOverButton("JUGAR DE NUEVO", () -> mainActivity.startGame(), 0);
        createAnimatedGameOverButton("VER RÉCORDS", () -> mainActivity.showRecords(), 1);
        createAnimatedGameOverButton("MENÚ PRINCIPAL", () -> mainActivity.showMenu(), 2);
    }

    /*
        // Botones
        Button playAgainButton = new Button(getContext());
        playAgainButton.setText("JUGAR DE NUEVO");
        playAgainButton.setTextSize(18);
        playAgainButton.setBackgroundColor(Color.DKGRAY);
        playAgainButton.setTextColor(Color.WHITE);
        playAgainButton.setOnClickListener(v -> mainActivity.startGame());

        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(
                350, 80
        );
        playParams.setMargins(0, 20, 0, 10);
        addView(playAgainButton, playParams);

        Button recordsButton = new Button(getContext());
        recordsButton.setText("VER RÉCORDS");
        recordsButton.setTextSize(18);
        recordsButton.setBackgroundColor(Color.DKGRAY);
        recordsButton.setTextColor(Color.WHITE);
        recordsButton.setOnClickListener(v -> mainActivity.showRecords());

        LinearLayout.LayoutParams recordsParams = new LinearLayout.LayoutParams(
                350, 80
        );
        recordsParams.setMargins(0, 0, 0, 10);
        addView(recordsButton, recordsParams);

        Button menuButton = new Button(getContext());
        menuButton.setText("MENÚ PRINCIPAL");
        menuButton.setTextSize(18);
        menuButton.setBackgroundColor(Color.DKGRAY);
        menuButton.setTextColor(Color.WHITE);
        menuButton.setOnClickListener(v -> mainActivity.showMenu());

        LinearLayout.LayoutParams menuParams = new LinearLayout.LayoutParams(
                350, 80
        );
        addView(menuButton, menuParams);
     */
    @SuppressLint("ClickableViewAccessibility")
    private void createAnimatedGameOverButton(String text, Runnable action, int index) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setTextSize(18);
        button.setBackgroundColor(Color.DKGRAY);
        button.setTextColor(Color.WHITE);
        button.setTag("gameOverButton_" + index);

        // Efecto de hover
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    animateButtonPress((Button) v, true);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    animateButtonPress((Button) v, false);
                    break;
            }
            return false;
        });

        button.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                350, 80
        );
        params.setMargins(0, index == 0 ? 20 : 0, 0, 10);
        addView(button, params);
    }

    private void startGameOverAnimations() {
        // Animación dramática del título "GAME OVER"
        View title = findViewWithTag("gameOverTitle");
        if (title != null) {
            // Shake effect
            TranslateAnimation shake = new TranslateAnimation(-10, 10, 0, 0);
            shake.setDuration(50);
            shake.setRepeatCount(6);
            shake.setRepeatMode(Animation.REVERSE);

            // Scale in
            ScaleAnimation scaleIn = new ScaleAnimation(0, 1, 0, 1,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            scaleIn.setDuration(500);

            AnimationSet titleAnimSet = new AnimationSet(false);
            titleAnimSet.addAnimation(scaleIn);

            title.startAnimation(titleAnimSet);

            // Shake después de la escala
            title.postDelayed(shakeView(title),1000);

        }
    }
    // Método que aplica una animación de "shake"
    private Runnable shakeView(View view) {
        Animation shake = new TranslateAnimation(0, 15, 0, 0);
        shake.setDuration(100);
        shake.setRepeatMode(Animation.REVERSE);
        shake.setRepeatCount(5);
        view.startAnimation(shake);
        return null;
    }

    private void animateButtonPress(Button button, boolean pressed) {
        // Animación de escala (se encoge al presionar)
        ScaleAnimation scaleAnim = new ScaleAnimation(
                pressed ? 1.0f : 0.95f, pressed ? 0.95f : 1.0f,
                pressed ? 1.0f : 0.95f, pressed ? 0.95f : 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(100);
        scaleAnim.setFillAfter(true);
        button.startAnimation(scaleAnim);

        // Cambio de color al presionar
        button.setBackgroundColor(pressed ? Color.GRAY : Color.DKGRAY);
    }
}