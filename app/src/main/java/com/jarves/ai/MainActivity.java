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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;
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

        jarvesVoice = new TextToSpeech(this, result -> {
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
        });

        voiceButton.setOnClickListener(v -> startListening());

        accessibilityButton.setOnClickListener(
                v -> openAccessibility()
        );

        statusText.setText("Jarves ready");
    }

    private void startListening() {

        Intent intent = new Intent(
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
            speak("Voice recognition is not available.");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

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

                commandText.setText(command);

                executeCommand(command);
            }
        }
    }

    private void executeCommand(String command) {

        String cmd =
                command.toLowerCase(Locale.ROOT);

        if (containsAny(
                cmd,
                "home",
                "go home",
                "ghar",
                "ghar jao",
                "home jao"
        )) {
            performHome();
            return;
        }

        if (containsAny(
                cmd,
                "back",
                "go back",
                "peeche",
                "piche",
                "wapas",
                "wapas jao"
        )) {
            performBack();
            return;
        }

        if (containsAny(
                cmd,
                "recent",
                "recent apps",
                "recent app",
                "recent kholo"
        )) {
            performRecents();
            return;
        }

        if (containsAny(
                cmd,
                "settings",
                "setting",
                "सेटिंग"
        )) {
            openSettings();
            return;
        }

        if (containsAny(
                cmd,
                "accessibility",
                "phone control"
        )) {
            openAccessibility();
            return;
        }

        String appName =
                extractAppName(cmd);

        if (!appName.isEmpty()) {
            openInstalledApp(appName);
            return;
        }

        statusText.setText(
                "Command received: " + command
        );

        speak("Command received.");
    }

    private void performHome() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service != null) {

            if (service.goHome()) {
                statusText.setText(
                        "Home par ja raha hoon"
                );
                speak("Going home.");
            } else {
                speak("I could not go home.");
            }

        } else {
            speak(
                    "Phone control service is not active."
            );
        }
    }

    private void performBack() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service != null) {

            if (service.globalBack()) {
                statusText.setText(
                        "Back ja raha hoon"
                );
                speak("Going back.");
            } else {
                speak("I could not go back.");
            }

        } else {
            speak(
                    "Phone control service is not active."
            );
        }
    }

    private void performRecents() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service != null) {

            if (service.openRecents()) {
                statusText.setText(
                        "Recent apps open kar raha hoon"
                );
                speak("Opening recent apps.");
            } else {
                speak(
                        "I could not open recent apps."
                );
            }

        } else {
            speak(
                    "Phone control service is not active."
            );
        }
    }

    private String extractAppName(String command) {

        String name = command;

        String[] removeWords = {
                "open",
                "launch",
                "start",
                "run",
                "khol",
                "kholo",
                "chalao",
                "chala",
                "app",
                "application",
                "jarves",
                "jarvis"
        };

        for (String word : removeWords) {
            name = name.replace(word, " ");
        }

        return name.trim();
    }

    private void openInstalledApp(
            String requestedName) {

        PackageManager pm =
                getPackageManager();

        Intent launcherIntent =
                new Intent(
                        Intent.ACTION_MAIN,
                        null
                );

        launcherIntent.addCategory(
                Intent.CATEGORY_LAUNCHER
        );

        List<ResolveInfo> apps =
                pm.queryIntentActivities(
                        launcherIntent,
                        PackageManager.MATCH_ALL
                );

        String searchName =
                requestedName.toLowerCase(
                        Locale.ROOT
                );

        for (ResolveInfo info : apps) {

            String label =
                    info.loadLabel(pm).toString();

            String labelLower =
                    label.toLowerCase(
                            Locale.ROOT
                    );

            if (labelLower.equals(searchName)
                    || labelLower.contains(searchName)
                    || searchName.contains(labelLower)) {

                try {

                    Intent launchIntent =
                            pm.getLaunchIntentForPackage(
                                    info.activityInfo.packageName
                            );

                    if (launchIntent != null) {

                        startActivity(launchIntent);

                        statusText.setText(
                                label +
                                " open kar raha hoon"
                        );

                        speak(
                                "Opening " + label
                        );

                        return;
                    }

                } catch (Exception ignored) {
                }
            }
        }

        String packageName =
                findCommonAppPackage(searchName);

        if (packageName != null) {

            try {

                Intent intent =
                        pm.getLaunchIntentForPackage(
                                packageName
                        );

                if (intent != null) {

                    startActivity(intent);

                    statusText.setText(
                            "App open kar raha hoon"
                    );

                    speak(
                            "Opening application."
                    );

                    return;
                }

            } catch (Exception ignored) {
            }
        }

        if (searchName.contains("youtube")) {

            try {

                Intent browser =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.youtube.com"
                                )
                        );

                startActivity(browser);

                speak("Opening YouTube.");

                return;

            } catch (Exception ignored) {
            }
        }

        statusText.setText(
                requestedName +
                " installed app list me nahi mila"
        );

        speak(
                "I could not find " +
                requestedName
        );
    }

    private String findCommonAppPackage(
            String name) {

        PackageManager pm =
                getPackageManager();

        String[] packages;

        if (name.contains("whatsapp")) {

            packages = new String[]{
                    "com.whatsapp",
                    "com.whatsapp.w4b"
            };

        } else if (name.contains("youtube")) {

            packages = new String[]{
                    "com.google.android.youtube"
            };

        } else if (name.contains("instagram")) {

            packages = new String[]{
                    "com.instagram.android"
            };

        } else if (name.contains("facebook")) {

            packages = new String[]{
                    "com.facebook.katana"
            };

        } else if (name.contains("telegram")) {

            packages = new String[]{
                    "org.telegram.messenger"
            };

        } else {

            return null;
        }

        for (String packageName : packages) {

            try {

                ApplicationInfo info =
                        pm.getApplicationInfo(
                                packageName,
                                0
                        );

                if (info != null) {
                    return packageName;
                }

            } catch (
                    PackageManager.NameNotFoundException ignored) {
            }
        }

        return null;
    }

    private void openSettings() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_SETTINGS
                    );

            startActivity(intent);

            statusText.setText(
                    "Settings open kar raha hoon"
            );

            speak("Opening settings.");

        } catch (Exception e) {

            speak(
                    "I could not open settings."
            );
        }
    }

    private void openAccessibility() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                    );

            startActivity(intent);

            speak(
                    "Opening accessibility settings."
            );

        } catch (Exception e) {

            speak(
                    "I could not open accessibility settings."
            );
        }
    }

    private boolean containsAny(
            String text,
            String... words) {

        for (String word : words) {

            if (text.contains(word)) {
                return true;
            }
        }

        return false;
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
