package com.jarves.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import android.net.Uri;
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

        // Jarves voice
        jarvesVoice = new TextToSpeech(
                this,
                result -> {
                    if (result == TextToSpeech.SUCCESS) {
                        int languageResult =
                                jarvesVoice.setLanguage(Locale.getDefault());

                        if (languageResult ==
                                TextToSpeech.LANG_MISSING_DATA ||
                                languageResult ==
                                TextToSpeech.LANG_NOT_SUPPORTED) {

                            jarvesVoice.setLanguage(Locale.US);
                        }
                    }
                }
        );

        voiceButton.setOnClickListener(v -> {
            startListening();
        });

        accessibilityButton.setOnClickListener(v -> {
            try {
                Intent intent =
                        new Intent(
                                Settings.ACTION_ACCESSIBILITY_SETTINGS
                        );

                startActivity(intent);

            } catch (Exception e) {
                Toast.makeText(
                        this,
                        "Accessibility settings open nahi ho paayi.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        statusText.setText("Jarves ready");
    }

    private void startListening() {

        Intent intent =
                new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                );

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

            statusText.setText(
                    "Voice recognition available nahi hai."
            );

            speak(
                    "Voice recognition is not available."
            );
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

                String command =
                        results.get(0).trim();

                commandText.setText(
                        command
                );

                executeCommand(command);
            }
        }
    }

    private void executeCommand(String command) {

        String cmd =
                command.toLowerCase(
                        Locale.ROOT
                );

        // YouTube
        if (containsAny(
                cmd,
                "youtube",
                "you tube"
        )) {

            openApp(
                    "com.google.android.youtube",
                    "YouTube"
            );

        // WhatsApp
        } else if (containsAny(
                cmd,
                "whatsapp",
                "what's app",
                "whats app"
        )) {

            openApp(
                    "com.whatsapp",
                    "WhatsApp"
            );

        // Settings
        } else if (containsAny(
                cmd,
                "settings",
                "setting",
                "सेटिंग"
        )) {

            openSettings();

        // Accessibility
        } else if (containsAny(
                cmd,
                "accessibility",
                "phone control"
        )) {

            openAccessibility();

        } else {

            statusText.setText(
                    "Command received"
            );

            speak(
                    "I heard you say " + command
            );
        }
    }

    private boolean containsAny(
            String text,
            String... words
    ) {

        for (String word : words) {

            if (text.contains(word)) {
                return true;
            }
        }

        return false;
    }

    private void openApp(
            String packageName,
            String appName
    ) {

        try {

            Intent launchIntent =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (launchIntent != null) {

                launchIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                startActivity(
                        launchIntent
                );

                statusText.setText(
                        appName +
                        " open kar raha hoon"
                );

                speak(
                        "Opening " +
                        appName
                );

                return;
            }

            /*
             * Agar package name se app launch nahi hota,
             * to Android ke launcher intent ke through
             * installed application ko search karne ki
             * koshish karenge.
             */
            Intent launchApps =
                    getPackageManager()
                            .getLaunchIntentForPackage(
                                    packageName
                            );

            if (launchApps != null) {

                startActivity(
                        launchApps
                );

                return;
            }

            // YouTube ke liye browser fallback
            if (appName.equals("YouTube")) {

                Intent browser =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.youtube.com"
                                )
                        );

                startActivity(
                        browser
                );

                statusText.setText(
                        "YouTube open kar raha hoon"
                );

                speak(
                        "Opening YouTube"
                );

                return;
            }

            statusText.setText(
                    appName +
                    " installed nahi mila"
            );

            speak(
                    appName +
                    " was not found."
            );

        } catch (Exception e) {

            statusText.setText(
                    appName +
                    " open nahi ho saka"
            );

            speak(
                    "I could not open " +
                    appName
            );
        }
    }

    private void openSettings() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            startActivity(
                    intent
            );

            statusText.setText(
                    "Settings open kar raha hoon"
            );

            speak(
                    "Opening settings"
            );

        } catch (Exception e) {

            speak(
                    "I could not open settings"
            );
        }
    }

    private void openAccessibility() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                    );

            startActivity(
                    intent
            );

            speak(
                    "Opening accessibility settings"
            );

        } catch (Exception e) {

            speak(
                    "I could not open accessibility settings"
            );
        }
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
