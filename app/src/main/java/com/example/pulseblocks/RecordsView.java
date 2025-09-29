package com.example.pulseblocks;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

// Vista de récords
class RecordsView extends LinearLayout {
    private MainActivity mainActivity;

    public RecordsView(Context context) {
        super(context);
        this.mainActivity = (MainActivity) context;
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        setBackgroundColor(Color.BLACK);
        setPadding(50, 50, 50, 50);

        createRecordsUI();
    }

    private void createRecordsUI() {
        // Título
        TextView title = new TextView(getContext());
        title.setText("RÉCORDS");
        title.setTextSize(36);
        title.setTextColor(Color.CYAN);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleParams.setMargins(0, 0, 0, 50);
        addView(title, titleParams);

        // Lista de récords
        List<Integer> records = mainActivity.getRecords();

        if (records.isEmpty()) {
            TextView noRecords = new TextView(getContext());
            noRecords.setText("No hay récords aún");
            noRecords.setTextSize(20);
            noRecords.setTextColor(Color.WHITE);
            noRecords.setGravity(Gravity.CENTER);
            addView(noRecords);
        } else {
            for (int i = 0; i < records.size(); i++) {
                TextView recordText = new TextView(getContext());
                String position = (i + 1) + ".";
                String score = records.get(i).toString();
                recordText.setText(String.format("%-3s %s puntos", position, score));
                recordText.setTextSize(18);
                recordText.setTextColor(i < 3 ? Color.YELLOW : Color.WHITE); // Top 3 en dorado
                recordText.setTypeface(null, i == 0 ? Typeface.BOLD : Typeface.NORMAL);

                LinearLayout.LayoutParams recordParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                recordParams.setMargins(0, 5, 0, 5);
                addView(recordText, recordParams);
            }
        }

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
        backParams.setMargins(0, 50, 0, 0);
        addView(backButton, backParams);
    }
}
