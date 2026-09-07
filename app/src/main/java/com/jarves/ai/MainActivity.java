package com.jarves.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int VOICE_REQUEST = 1001;

    private TextView commandText;
    private TextView statusText;
    private TextToSpeech jarvesVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        commandText = findViewById(R.id.commandText);
        statusText = findViewById(R.id.statusText);

        Button voiceButton = findViewById(R.id.voiceButton);
        Button accessibilityButton =
                findViewById(R.id.accessibilityButton);

        jarvesVoice = new TextToSpeech(
                this,
                result -> {
                    if (result == TextToSpeech.SUCCESS) {
                        jarvesVoice.setLanguage(Locale.getDefault());
                    }
                }
        );

        voiceButton.setOnClickListener(v ->
                startListening()
        );

        accessibilityButton.setOnClickListener(v -> {
            Intent intent =
                    new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);

            startActivity(intent);
        });

        statusText.setText("Jarves ready");
        speak("Jarves is ready.");
    }

    private void startListening() {

        Intent intent =
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

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
                "Jarves ko command dijiye"
        );

        try {
            startActivityForResult(
                    intent,
                    VOICE_REQUEST
            );
        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Voice recognition available nahi hai",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

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

        String cmd =
                command.toLowerCase(Locale.ROOT);

        if (cmd.contains("youtube")) {

            openApp(
                    "com.google.android.youtube",
                    "YouTube"
            );

        } else if (cmd.contains("whatsapp")) {

            openApp(
                    "com.whatsapp",
                    "WhatsApp"
            );

        } else if (cmd.contains("settings")
                || cmd.contains("setting")) {

            openSettings();

        } else {

            statusText.setText(
                    "Command: " + command
            );

            speak(
                    "Command received. " + command
            );
        }
    }

    private void openApp(
            String packageName,
            String appName
    ) {

        Intent launchIntent =
                getPackageManager()
                        .getLaunchIntentForPackage(
                                packageName
                        );

        if (launchIntent != null) {

            startActivity(launchIntent);

            statusText.setText(
                    appName + " open kar raha hoon"
            );

            speak(
                    appName + " opening."
            );

        } else {

            statusText.setText(
                    appName + " installed nahi hai"
            );

            speak(
                    appName + " is not installed."
            );
        }
    }

    private void openSettings() {

        Intent intent =
                new Intent(Settings.ACTION_SETTINGS);

        startActivity(intent);

        statusText.setText(
                "Settings open kar raha hoon"
        );

        speak("Opening settings.");
    }

    private void speak(String text) {

        if (jarvesVoice != null) {

            jarvesVoice.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "JARVES_RESPONSE"
            );
        }
    }

    @Override
    protected void onDestroy() {

        if (jarvesVoice != null) {

            jarvesVoice.stop();
            jarvesVoice.shutdown();
        }

        super.onDestroy();
    }
            }
