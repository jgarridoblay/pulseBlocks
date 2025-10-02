
// MenuViews.java
package com.example.pulseblocks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

// Vista del menú principal
class MenuView extends LinearLayout {
    private MainActivity mainActivity;

    public MenuView(Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.BLACK);

        createMenuUI();
        startMenuAnimations();
    }

    private void createMenuUI() {
        // Título del juego
        TextView title = new TextView(getContext());
        title.setText("PULSE BLOCKS");
        title.setTextSize(48);
        title.setTextColor(Color.CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setTag("title"); // Tag para animaciones

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 100);
        addView(title, titleParams);

        // Botones del menú
        createAnimatedMenuButton("INICIAR PARTIDA", () -> mainActivity.startGame(), 0);
        createAnimatedMenuButton("OPCIONES", () -> mainActivity.showOptions(), 1);
        createAnimatedMenuButton("RÉCORDS", () -> mainActivity.showRecords(), 2);
        createAnimatedMenuButton("SALIR", () -> System.exit(0), 3);

        // Subtítulo
        TextView subtitle = new TextView(getContext());
        subtitle.setText("Completa rectángulos para ganar puntos");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.WHITE);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTag("subtitle"); // Tag para animaciones

        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        subtitleParams.setMargins(0, 50, 0, 0);
        addView(subtitle, subtitleParams);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createAnimatedMenuButton(String text, Runnable action, int index) {
        Button button = new Button(getContext());
        button.setText(text);
        button.setTextSize(18);
        button.setTextColor(Color.WHITE);
        button.setBackgroundResource(R.drawable.futuristic_button);
        button.setAllCaps(false); // Evita que Android ponga todo en mayúsculas
        button.setPadding(32, 16, 32, 16);
        button.setEllipsize(null);
        button.setSingleLine(false);
        button.setHorizontallyScrolling(false);
        button.setGravity(Gravity.CENTER);

        button.setTag("button_" + index); // Tag para animaciones

        // Efecto de hover/press
        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    animateButtonPress(button, true);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    animateButtonPress(button, false);
                    break;
            }
            return false;
        });

        button.setOnClickListener(v -> {
            animateButtonClick(button, action);
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                400, 80
        );
        params.setMargins(0, 10, 0, 10);
        addView(button, params);
    }

    private void startMenuAnimations() {
        // Animar título con pulso
        View title = findViewWithTag("title");
        if (title != null) {
            ScaleAnimation pulseAnimation = new ScaleAnimation(
                    1.0f, 1.1f, 1.0f, 1.1f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );
            pulseAnimation.setDuration(2000);
            pulseAnimation.setRepeatMode(Animation.REVERSE);
            pulseAnimation.setRepeatCount(Animation.INFINITE);
            title.startAnimation(pulseAnimation);
        }

        // Animar botones con entrada escalonada
        for (int i = 0; i < 4; i++) {
            View button = findViewWithTag("button_" + i);
            if (button != null) {
                TranslateAnimation slideIn = new TranslateAnimation(
                        -300, 0, 0, 0
                );
                slideIn.setDuration(500);
                slideIn.setStartOffset(i * 150); // Escalonar las animaciones

                AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setDuration(500);
                fadeIn.setStartOffset(i * 150);

                AnimationSet animSet = new AnimationSet(false);
                animSet.addAnimation(slideIn);
                animSet.addAnimation(fadeIn);

                button.startAnimation(animSet);
            }
        }

        // Animar subtítulo
        View subtitle = findViewWithTag("subtitle");
        if (subtitle != null) {
            AlphaAnimation fadeInSubtitle = new AlphaAnimation(0, 1);
            fadeInSubtitle.setDuration(1000);
            fadeInSubtitle.setStartOffset(1000);
            subtitle.startAnimation(fadeInSubtitle);
        }
    }

    private void animateButtonPress(Button button, boolean pressed) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                pressed ? 1.0f : 0.95f, pressed ? 0.95f : 1.0f,
                pressed ? 1.0f : 0.95f, pressed ? 0.95f : 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(100);
        scaleAnim.setFillAfter(true);
        button.startAnimation(scaleAnim);

        // Cambiar color
        button.setBackgroundColor(pressed ? Color.GRAY : Color.DKGRAY);
    }

    private void animateButtonClick(Button button, Runnable action) {
        // Animación de click con efecto de ondas
        ScaleAnimation clickAnim = new ScaleAnimation(
                1.0f, 1.2f, 1.0f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        clickAnim.setDuration(150);
        clickAnim.setRepeatMode(Animation.REVERSE);
        clickAnim.setRepeatCount(1);

        clickAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                action.run();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        button.startAnimation(clickAnim);
    }
}