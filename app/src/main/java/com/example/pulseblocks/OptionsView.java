package com.example.pulseblocks;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.pulseblocks.MainActivity;

// Vista de opciones
class OptionsView extends LinearLayout {
    private MainActivity mainActivity;
    private SeekBar musicSeekBar, sfxSeekBar;
    private TextView musicLabel, sfxLabel;

    public OptionsView(Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.BLACK);
        setPadding(50, 50, 50, 50);

        createOptionsUI();
    }

    private void createOptionsUI() {
        // Título
        TextView title = new TextView(getContext());
        title.setText("OPCIONES");
        title.setTextSize(36);
        title.setTextColor(Color.CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 80);
        addView(title, titleParams);

        // Volumen de música
        musicLabel = new TextView(getContext());
        musicLabel.setText("Volumen Música: " + (int) (mainActivity.getMusicVolume() * 100) + "%");
        musicLabel.setTextSize(20);
        musicLabel.setTextColor(Color.WHITE);
        addView(musicLabel);

        musicSeekBar = new SeekBar(getContext());
        musicSeekBar.setMax(100);
        musicSeekBar.setProgress((int) (mainActivity.getMusicVolume() * 100));
        musicSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float volume = progress / 100.0f;
                    musicLabel.setText("Volumen Música: " + progress + "%");
                    mainActivity.updateVolumes(volume, mainActivity.getSfxVolume());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        LinearLayout.LayoutParams seekBarParams = new LinearLayout.LayoutParams(
                400, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        seekBarParams.setMargins(0, 10, 0, 40);
        addView(musicSeekBar, seekBarParams);

        // Volumen de efectos
        sfxLabel = new TextView(getContext());
        sfxLabel.setText("Volumen Efectos: " + (int) (mainActivity.getSfxVolume() * 100) + "%");
        sfxLabel.setTextSize(20);
        sfxLabel.setTextColor(Color.WHITE);
        addView(sfxLabel);

        sfxSeekBar = new SeekBar(getContext());
        sfxSeekBar.setMax(100);
        sfxSeekBar.setProgress((int) (mainActivity.getSfxVolume() * 100));
        sfxSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float volume = progress / 100.0f;
                    sfxLabel.setText("Volumen Efectos: " + progress + "%");
                    mainActivity.updateVolumes(mainActivity.getMusicVolume(), volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        LinearLayout.LayoutParams sfxSeekBarParams = new LinearLayout.LayoutParams(
                400, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sfxSeekBarParams.setMargins(0, 10, 0, 60);
        addView(sfxSeekBar, sfxSeekBarParams);

        // Botón volver
        Button backButton = new Button(getContext());
        backButton.setText("VOLVER AL MENÚ");
        backButton.setTextSize(18);
        backButton.setBackgroundColor(Color.DKGRAY);
        backButton.setTextColor(Color.WHITE);
        backButton.setOnClickListener(v -> mainActivity.showMenu());

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
                300, 70
        );
        addView(backButton, backParams);
    }
}
