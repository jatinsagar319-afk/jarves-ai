package com.jarves.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int VOICE_REQUEST = 1001;

    private TextView commandText;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        commandText = findViewById(R.id.commandText);
        statusText = findViewById(R.id.statusText);

        Button voiceButton = findViewById(R.id.voiceButton);
        Button accessibilityButton = findViewById(R.id.accessibilityButton);

        voiceButton.setOnClickListener(v -> startVoiceRecognition());

        accessibilityButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
        );

        intent.putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Jarves ko command dijiye..."
        );

        try {
            startActivityForResult(intent, VOICE_REQUEST);
        } catch (Exception e) {
            commandText.setText("Voice recognition available nahi hai.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS
                    );

            if (results != null && !results.isEmpty()) {
                String command = results.get(0);

                commandText.setText(command);

                executeCommand(command);
            }
        }
    }

    private void executeCommand(String command) {

        String lower = command.toLowerCase(Locale.ROOT);

        if (lower.contains("youtube")) {

            Intent intent = getPackageManager()
                    .getLaunchIntentForPackage(
                            "com.google.android.youtube"
                    );

            if (intent != null) {
                startActivity(intent);
                statusText.setText("YouTube open kar raha hoon...");
            } else {
                statusText.setText("YouTube installed nahi mila.");
            }

        } else if (lower.contains("whatsapp")) {

            Intent intent = getPackageManager()
                    .getLaunchIntentForPackage(
                            "com.whatsapp"
                    );

            if (intent != null) {
                startActivity(intent);
                statusText.setText("WhatsApp open kar raha hoon...");
            } else {
                statusText.setText("WhatsApp installed nahi mila.");
            }

        } else if (lower.contains("settings")) {

            startActivity(
                    new Intent(Settings.ACTION_SETTINGS)
            );

            statusText.setText("Settings open kar raha hoon...");

        } else {

            statusText.setText(
                    "Command samajh gaya: " + command
            );
        }
    }
                      }
