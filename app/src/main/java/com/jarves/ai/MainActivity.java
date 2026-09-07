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

    // ==================================================
    // VOICE LISTENING
    // ==================================================

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

            speak(
                    "Voice recognition is not available."
            );
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

            if (results != null &&
                    !results.isEmpty()) {

                String command =
                        results.get(0).trim();

                commandText.setText(command);

                executeCommand(command);
            }
        }
    }

    // ==================================================
    // COMMAND ENGINE
    // ==================================================

    private void executeCommand(String command) {

        if (command == null ||
                command.trim().isEmpty()) {

            return;
        }

        String cmd =
                command.toLowerCase(Locale.ROOT).trim();

        // Remove Jarves/Jarvis from command
        cmd = cmd.replace("jarves", " ");
        cmd = cmd.replace("jarvis", " ");
        cmd = cmd.trim();

        // ---------------- HOME ----------------

        if (containsAny(
                cmd,
                "home",
                "go home",
                "ghar",
                "ghar jao",
                "home jao",
                "home par jao"
        )) {

            performHome();
            return;
        }

        // ---------------- BACK ----------------

        if (containsAny(
                cmd,
                "back",
                "go back",
                "peeche",
                "piche",
                "wapas",
                "wapas jao",
                "back jao"
        )) {

            performBack();
            return;
        }

        // ---------------- RECENTS ----------------

        if (containsAny(
                cmd,
                "recent",
                "recent apps",
                "recent app",
                "recent kholo",
                "recent apps kholo"
        )) {

            performRecents();
            return;
        }

        // ---------------- SETTINGS ----------------

        if (containsAny(
                cmd,
                "settings",
                "setting",
                "सेटिंग",
                "सेटिंग्स"
        )) {

            openSettings();
            return;
        }

        // ---------------- ACCESSIBILITY ----------------

        if (containsAny(
                cmd,
                "accessibility",
                "accessibility settings",
                "phone control"
        )) {

            openAccessibility();
            return;
        }

        // ---------------- SCROLL DOWN ----------------

        if (containsAny(
                cmd,
                "scroll down",
                "scroll neeche",
                "neeche scroll",
                "neeche scroll karo",
                "niche scroll",
                "niche scroll karo",
                "down scroll"
        )) {

            performScrollDown();
            return;
        }

        // ---------------- SCROLL UP ----------------

        if (containsAny(
                cmd,
                "scroll up",
                "scroll upar",
                "upar scroll",
                "upar scroll karo",
                "up scroll"
        )) {

            performScrollUp();
            return;
        }

        // ---------------- CLICK COMMAND ----------------

        if (containsAny(
                cmd,
                "click",
                "click karo",
                "dabao",
                "daba do",
                "open button",
                "button dabao",
                "par click karo"
        )) {

            String target =
                    extractClickTarget(cmd);

            if (!target.isEmpty()) {

                performClick(target);

                return;
            }
        }

        // ---------------- TYPE COMMAND ----------------

        if (containsAny(
                cmd,
                "type",
                "type karo",
                "likho",
                "likh do",
                "enter karo",
                "text likho"
        )) {

            String text =
                    extractTypeText(cmd);

            if (!text.isEmpty()) {

                performType(text);

                return;
            }
        }

        // ---------------- OPEN APP ----------------

        String appName =
                extractAppName(cmd);

        if (!appName.isEmpty()) {

            openInstalledApp(appName);
            return;
        }

        statusText.setText(
                "Command received: " + command
        );

        speak(
                "Command received."
        );
    }

    // ==================================================
    // CLICK TARGET EXTRACTION
    // ==================================================

    private String extractClickTarget(String command) {

        String target = command;

        String[] removeWords = {

                "jarves",
                "jarvis",

                "click",
                "click karo",
                "click kar",
                "dabao",
                "daba do",
                "button",
                "par",
                "pe",
                "ko"
        };

        for (String word : removeWords) {

            target = target.replace(
                    word,
                    " "
            );
        }

        return target.trim();
    }

    // ==================================================
    // TYPE TEXT EXTRACTION
    // ==================================================

    private String extractTypeText(String command) {

        String text = command;

        String[] removeWords = {

                "jarves",
                "jarvis",

                "type",
                "type karo",
                "type kar",
                "likho",
                "likh do",
                "enter karo",
                "text",
                "mein",
                "me",
                "karo"
        };

        for (String word : removeWords) {

            text = text.replace(
                    word,
                    " "
            );
        }

        return text.trim();
    }

    // ==================================================
    // CLICK SCREEN TEXT
    // ==================================================

    private void performClick(String target) {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            statusText.setText(
                    "Accessibility service active nahi hai."
            );

            speak(
                    "Phone control service is not active."
            );

            return;
        }

        boolean result =
                service.clickText(target);

        if (result) {

            statusText.setText(
                    target + " par click kar diya"
            );

            speak(
                    "Clicked " + target
            );

        } else {

            statusText.setText(
                    target + " screen par nahi mila"
            );

            speak(
                    "I could not find " + target
            );
        }
    }

    // ==================================================
    // TYPE TEXT
    // ==================================================

    private void performType(String text) {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            statusText.setText(
                    "Accessibility service active nahi hai."
            );

            speak(
                    "Phone control service is not active."
            );

            return;
        }

        boolean focused =
                service.focusEditableField();

        boolean typed =
                service.typeText(text);

        if (focused && typed) {

            statusText.setText(
                    "Text type kar diya: " + text
            );

            speak(
                    "Text typed."
            );

        } else if (typed) {

            statusText.setText(
                    "Text type kar diya: " + text
            );

            speak(
                    "Text typed."
            );

        } else {

            statusText.setText(
                    "Text field nahi mila."
            );

            speak(
                    "I could not find a text field."
            );
        }
    }

    // ==================================================
    // SCROLL DOWN
    // ==================================================

    private void performScrollDown() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Phone control service is not active."
            );

            return;
        }

        if (service.scrollForward()) {

            statusText.setText(
                    "Neeche scroll kar raha hoon"
            );

            speak(
                    "Scrolling down."
            );

        } else {

            statusText.setText(
                    "Scroll nahi ho saka"
            );

            speak(
                    "I could not scroll down."
            );
        }
    }

    // ==================================================
    // SCROLL UP
    // ==================================================

    private void performScrollUp() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Phone control service is not active."
            );

            return;
        }

        if (service.scrollBackward()) {

            statusText.setText(
                    "Upar scroll kar raha hoon"
            );

            speak(
                    "Scrolling up."
            );

        } else {

            statusText.setText(
                    "Scroll nahi ho saka"
            );

            speak(
                    "I could not scroll up."
            );
        }
    }

    // ==================================================
    // HOME
    // ==================================================

    private void performHome() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service != null) {

            if (service.goHome()) {

                statusText.setText(
                        "Home par ja raha hoon"
                );

                speak(
                        "Going home."
                );

            } else {

                speak(
                        "I could not go home."
                );
            }

        } else {

            speak(
                    "Phone control service is not active."
            );
        }
    }

    // ==================================================
    // BACK
    // ==================================================

    private void performBack() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service != null) {

            if (service.globalBack()) {

                statusText.setText(
                        "Back ja raha hoon"
                );

                speak(
                        "Going back."
                );

            } else {

                speak(
                        "I could not go back."
                );
            }

        } else {

            speak(
                    "Phone control service is not active."
            );
        }
    }

    // ==================================================
    // RECENTS
    // ==================================================

    private void performRecents() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service != null) {

            if (service.openRecents()) {

                statusText.setText(
                        "Recent apps open kar raha hoon"
                );

                speak(
                        "Opening recent apps."
                );

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

    // ==================================================
    // APP NAME EXTRACTION
    // ==================================================

    private String extractAppName(String command) {

        String name = command;

        String[] removeWords = {

                "open",
                "launch",
                "start",
                "run",

                "khol",
                "kholo",
                "khol do",

                "chalao",
                "chala",

                "app",
                "application",

                "jarves",
                "jarvis"
        };

        for (String word : removeWords) {

            name = name.replace(
                    word,
                    " "
            );
        }

        return name.trim();
    }

    // ==================================================
    // OPEN INSTALLED APP
    // ==================================================

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
                ).trim();

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

                        startActivity(
                                launchIntent
                        );

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

        // Common apps

        String packageName =
                findCommonAppPackage(
                        searchName
                );

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

        // YouTube fallback

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

 
