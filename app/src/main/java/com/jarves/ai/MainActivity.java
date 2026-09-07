```java
package com.jarves.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionListener;
import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import android.net.Uri;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

        // ---------------- TTS ----------------

        jarvesVoice = new TextToSpeech(
                this,
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {

                        if (status == TextToSpeech.SUCCESS) {

                            jarvesVoice.setLanguage(
                                    Locale.ENGLISH
                            );

                            jarvesVoice.setSpeechRate(0.95f);
                        }
                    }
                }
        );

        // ---------------- VOICE BUTTON ----------------

        voiceButton.setOnClickListener(v -> {
            startListening();
        });

        // ---------------- ACCESSIBILITY BUTTON ----------------

        accessibilityButton.setOnClickListener(v -> {
            openAccessibility();
        });

        statusText.setText(
                "Jarves ready. Voice command bolo."
        );
    }

    // =========================================================
    // VOICE LISTENING
    // =========================================================

    private void startListening() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {

            Toast.makeText(
                    this,
                    "Speech recognition available nahi hai",
                    Toast.LENGTH_LONG
            ).show();

            speak(
                    "Speech recognition is not available."
            );

            return;
        }

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
                "Jarves ko command bolo"
        );

        try {

            startActivityForResult(
                    intent,
                    VOICE_REQUEST
            );

        } catch (Exception e) {

            Toast.makeText(
                    this,
                    "Voice input start nahi ho saka",
                    Toast.LENGTH_SHORT
            ).show();

            speak(
                    "Voice input could not be started."
            );
        }
    }

    // =========================================================
    // VOICE RESULT
    // =========================================================

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

        if (requestCode != VOICE_REQUEST) {
            return;
        }

        if (resultCode != RESULT_OK || data == null) {

            statusText.setText(
                    "Voice command nahi mili."
            );

            return;
        }

        ArrayList<String> results =
                data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS
                );

        if (results == null || results.isEmpty()) {

            statusText.setText(
                    "Command samajh nahi aayi."
            );

            speak(
                    "I could not understand the command."
            );

            return;
        }

        String command =
                results.get(0);

        if (command == null) {
            return;
        }

        command = command.trim();

        commandText.setText(
                command
        );

        executeCommand(command);
    }

    // =========================================================
    // COMMAND EXECUTION
    // =========================================================

    private void executeCommand(String command) {

        if (command == null) {
            return;
        }

        String originalCommand =
                command.trim();

        String cmd =
                originalCommand
                        .toLowerCase(Locale.ROOT)
                        .trim();

        if (cmd.length() == 0) {
            return;
        }

        // -----------------------------------------------------
        // HOME
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "home",
                "go home",
                "open home",
                "ghar",
                "home screen",
                "home page"
        )) {

            performHome();

            return;
        }

        // -----------------------------------------------------
        // BACK
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "back",
                "go back",
                "piche",
                "peeche",
                "wapas",
                "go previous"
        )) {

            performBack();

            return;
        }

        // -----------------------------------------------------
        // RECENTS
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "recent",
                "recents",
                "recent apps",
                "open recent",
                "recent application"
        )) {

            performRecents();

            return;
        }

        // -----------------------------------------------------
        // SETTINGS
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "settings",
                "open settings",
                "setting kholo",
                "settings kholo"
        )) {

            openSettings();

            return;
        }

        // -----------------------------------------------------
        // ACCESSIBILITY
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "accessibility",
                "open accessibility",
                "accessibility kholo"
        )) {

            openAccessibility();

            return;
        }

        // -----------------------------------------------------
        // SCROLL DOWN
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "scroll down",
                "scroll downward",
                "neeche scroll",
                "niche scroll",
                "scroll neeche",
                "scroll niche",
                "down scroll"
        )) {

            performScrollDown();

            return;
        }

        // -----------------------------------------------------
        // SCROLL UP
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "scroll up",
                "scroll upward",
                "upar scroll",
                "ऊपर scroll",
                "scroll upar",
                "up scroll"
        )) {

            performScrollUp();

            return;
        }

        // -----------------------------------------------------
        // CLICK COMMAND
        // -----------------------------------------------------

        if (cmd.startsWith("click ")
                || cmd.startsWith("click on ")
                || cmd.startsWith("press ")
                || cmd.startsWith("tap ")
                || cmd.startsWith("open button ")) {

            String target =
                    extractClickTarget(
                            originalCommand
                    );

            if (target.length() > 0) {

                performClick(target);

            } else {

                speak(
                        "Please tell me what to click."
                );
            }

            return;
        }

        // -----------------------------------------------------
        // TYPE COMMAND
        // -----------------------------------------------------

        if (cmd.startsWith("type ")
                || cmd.startsWith("type text ")
                || cmd.startsWith("enter ")
                || cmd.startsWith("write ")
                || cmd.startsWith("likho ")) {

            String text =
                    extractTypeText(
                            originalCommand
                    );

            if (text.length() > 0) {

                performType(text);

            } else {

                speak(
                        "Please tell me what I should type."
                );
            }

            return;
        }

        // -----------------------------------------------------
        // CLEAR / DELETE TEXT
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "clear text",
                "delete text",
                "remove text",
                "text clear karo",
                "text delete karo"
        )) {

            clearEditableText();

            return;
        }

        // -----------------------------------------------------
        // READ SCREEN
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "read screen",
                "screen read karo",
                "screen padho",
                "what is on screen",
                "screen par kya hai",
                "screen pe kya hai"
        )) {

            readScreen();

            return;
        }

        // -----------------------------------------------------
        // CHECK ACCESSIBILITY
        // -----------------------------------------------------

        if (containsAny(
                cmd,
                "accessibility status",
                "is accessibility running",
                "accessibility check"
        )) {

            if (JarvesAccessibilityService.isRunning()) {

                statusText.setText(
                        "Accessibility service ON hai."
                );

                speak(
                        "Accessibility service is running."
                );

            } else {

                statusText.setText(
                        "Accessibility service OFF hai."
                );

                speak(
                        "Please enable Jarves accessibility service."
                );
            }

            return;
        }

        // -----------------------------------------------------
        // OPEN APP
        // -----------------------------------------------------

        String appName =
                extractAppName(
                        originalCommand
                );

        if (appName.length() > 0) {

            openInstalledApp(appName);

            return;
        }

        // -----------------------------------------------------
        // UNKNOWN COMMAND
        // -----------------------------------------------------

        statusText.setText(
                "Command: " + originalCommand
        );

        speak(
                "Command samajh nahi aayi."
        );
    }

    // =========================================================
    // HOME
    // =========================================================

    private void performHome() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service != null) {

            boolean result =
                    service.goHome();

            if (result) {

                statusText.setText(
                        "Home open kar raha hoon"
                );

                speak(
                        "Opening home."
                );

                return;
            }
        }

        try {

            Intent homeIntent =
                    new Intent(
                            Intent.ACTION_MAIN
                    );

            homeIntent.addCategory(
                    Intent.CATEGORY_HOME
            );

            homeIntent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
            );

            startActivity(homeIntent);

            statusText.setText(
                    "Home open kar raha hoon"
            );

            speak(
                    "Opening home."
            );

        } catch (Exception e) {

            speak(
                    "I could not open home."
            );
        }
    }

    // =========================================================
    // BACK
    // =========================================================

    private void performBack() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        boolean result =
                service.globalBack();

        if (result) {

            statusText.setText(
                    "Back"
            );

            speak(
                    "Going back."
            );

        } else {

            speak(
                    "Back action failed."
            );
        }
    }

    // =========================================================
    // RECENTS
    // =========================================================

    private void performRecents() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        boolean result =
                service.openRecents();

        if (result) {

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
    }

    // =========================================================
    // SCROLL DOWN
    // =========================================================

    private void performScrollDown() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        boolean result =
                service.scrollForward();

        if (result) {

            statusText.setText(
                    "Scrolling down"
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

    // =========================================================
    // SCROLL UP
    // =========================================================

    private void performScrollUp() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        boolean result =
                service.scrollBackward();

        if (result) {

            statusText.setText(
                    "Scrolling up"
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

    // =========================================================
    // CLICK
    // =========================================================

    private String extractClickTarget(
            String command) {

        if (command == null) {
            return "";
        }

        String result =
                command.trim();

        String lower =
                result.toLowerCase(
                        Locale.ROOT
                );

        String[] prefixes = {

                "click on ",
                "click ",
                "press ",
                "tap ",
                "open button "
        };

        for (String prefix : prefixes) {

            if (lower.startsWith(prefix)) {

                return result
                        .substring(prefix.length())
                        .trim();
            }
        }

        return result;
    }

    private void performClick(
            String target) {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            statusText.setText(
                    "Accessibility OFF"
            );

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        boolean result =
                service.clickText(target);

        if (result) {

            statusText.setText(
                    "Clicked: " + target
            );

            speak(
                    "Clicking " + target
            );

        } else {

            statusText.setText(
                    "Button nahi mila: " + target
            );

            speak(
                    "I could not find " +
                    target
            );
        }
    }

    // =========================================================
    // TYPE TEXT
    // =========================================================

    private String extractTypeText(
            String command) {

        if (command == null) {
            return "";
        }

        String result =
                command.trim();

        String lower =
                result.toLowerCase(
                        Locale.ROOT
                );

        String[] prefixes = {

                "type text ",
                "type ",
                "enter ",
                "write ",
                "likho "
        };

        for (String prefix : prefixes) {

            if (lower.startsWith(prefix)) {

                return result
                        .substring(prefix.length())
                        .trim();
            }
        }

        return result;
    }

    private void performType(
            String text) {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        boolean focused =
                service.focusEditableField();

        boolean typed =
                service.typeText(text);

        if (typed) {

            statusText.setText(
                    "Typed: " + text
            );

            speak(
                    "Text entered."
            );

        } else if (!focused) {

            statusText.setText(
                    "Input field nahi mila"
            );

            speak(
                    "I could not find an input field."
            );

        } else {

            statusText.setText(
                    "Text type nahi ho saka"
            );

            speak(
                    "I could not enter the text."
            );
        }
    }

    // =========================================================
    // CLEAR EDITABLE TEXT
    // =========================================================

    private void clearEditableText() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        AccessibilityNodeInfo root =
                service.getRootInActiveWindow();

        if (root == null) {

            speak(
                    "I cannot access the current screen."
            );

            return;
        }

        AccessibilityNodeInfo input =
                findEditableField(root);

        if (input == null) {

            speak(
                    "I could not find a text field."
            );

            return;
        }

        android.os.Bundle args =
                new android.os.Bundle();

        args.putCharSequence(
                AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                ""
        );

        boolean result =
                input.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        args
                );

        input.recycle();

        if (result) {

            statusText.setText(
                    "Text cleared"
            );

            speak(
                    "Text cleared."
            );

        } else {

            speak(
                    "I could not clear the text."
            );
        }
    }

    private AccessibilityNodeInfo findEditableField(
            AccessibilityNodeInfo node) {

        if (node == null) {
            return null;
        }

        if (node.isEditable()) {

            return AccessibilityNodeInfo.obtain(
                    node
            );
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                AccessibilityNodeInfo result =
                        findEditableField(child);

                child.recycle();

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    // =========================================================
    // READ SCREEN
    // =========================================================

    private void readScreen() {

        JarvesAccessibilityService service =
                JarvesAccessibilityService.getInstance();

        if (service == null) {

            speak(
                    "Please enable Jarves accessibility service."
            );

            return;
        }

        List<String> texts =
                service.getVisibleTexts();

        if (texts == null ||
                texts.isEmpty()) {

            statusText.setText(
                    "Screen par readable text nahi mila."
            );

            speak(
                    "I could not find readable text on the screen."
            );

            return;
        }

        StringBuilder builder =
                new StringBuilder();

        int count = 0;

        for (String text : texts) {

            if (text == null ||
                    text.trim().length() == 0) {
                continue;
            }

            if (count > 0) {
                builder.append(". ");
            }

            builder.append(
                    text.trim()
            );

            count++;

            if (count >= 15) {
                break;
            }
        }

        String screenText =
                builder.toString();

        statusText.setText(
                screenText
        );

        speak(
                screenText
        );
    }

    // =========================================================
    // EXTRACT APP NAME
    // =========================================================

    private String extractAppName(
            String command) {

        if (command == null) {
            return "";
        }

        String result =
                command.trim();

        String lower =
                result.toLowerCase(
                        Locale.ROOT
                );

        String[] prefixes = {

                "open ",
                "launch ",
                "start ",
                "run ",
                "khol ",
                "kholo ",
                "chalao ",
                "chala ",
                "app open ",
                "application open "
        };

        for (String prefix : prefixes) {

            if (lower.startsWith(prefix)) {

                return result
                        .substring(prefix.length())
                        .trim();
            }
        }

        // If user simply says an app name
        if (!containsAny(
                lower,
                "what",
                "who",
                "how",
                "why",
                "please",
                "tell me",
                "read",
                "click",
                "press",
                "tap",
                "type",
                "scroll"
        )) {

            return result;
        }

        return "";
    }

    // =========================================================
    // OPEN INSTALLED APP
    // =========================================================

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
                requestedName
                        .toLowerCase(
                                Locale.ROOT
                        )
                        .trim();

        // -----------------------------------------------------
        // Search installed launcher apps
        // -----------------------------------------------------

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

        // -----------------------------------------------------
        // Common apps
        // -----------------------------------------------------

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

        // -----------------------------------------------------
        // YouTube fallback
        // -----------------------------------------------------

        if (searchName.contains("youtube")) {

            try {

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
                        "Opening YouTube."
                );

                return;

            } catch (Exception ignored) {
            }
        }

        // -----------------------------------------------------
        // Browser search fallback
        // -----------------------------------------------------

        if (searchName.contains("google")
                || searchName.contains("chrome")) {

            try {

                Intent browser =
                        new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(
                                        "https://www.google.com"
                                )
                        );

                startActivity(
                        browser
                );

                statusText.setText(
                        "Google open kar raha hoon"
                );

                speak(
                        "Opening Google."
                );

                return;

            } catch (Exception ignored) {
            }
        }

        // -----------------------------------------------------
        // Not found
        // -----------------------------------------------------

        statusText.setText(
                requestedName +
                " installed app list me nahi mila"
        );

        speak(
                "I could not find " +
                requestedName
        );
    }

    // =========================================================
    // COMMON APP PACKAGES
    // =========================================================

    private String findCommonAppPackage(
            String name) {

        PackageManager pm =
                getPackageManager();

        String[] packages = {

                "com.whatsapp",
                "com.google.android.youtube",
                "com.instagram.android",
                "com.facebook.katana",
                "org.telegram.messenger",
                "com.google.android.apps.maps",
                "com.google.android.gm",
                "com.google.android.googlequicksearchbox",
                "com.google.android.apps.messaging",
                "com.android.settings",
                "com.android.chrome"
        };

        for (String packageName : packages) {

            try {

                ApplicationInfo info =
                        pm.getApplicationInfo(
                                packageName,
                                0
                        );

                String label =
                        pm.getApplicationLabel(
                                info
                        ).toString();

                String labelLower =
                        label.toLowerCase(
                                Locale.ROOT
                        );

                if (labelLower.equals(name)
                        || labelLower.contains(name)
                        || name.contains(labelLower)) {

                    return packageName;
                }

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    // =========================================================
    // SETTINGS
    // =========================================================

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

            speak(
                    "Opening settings."
            );

        } catch (Exception e) {

            speak(
                    "I could not open settings."
            );
        }
    }

    // =========================================================
    // ACCESSIBILITY SETTINGS
    // =========================================================

    private void openAccessibility() {

        try {

            Intent intent =
                    new Intent(
                            Settings.ACTION_ACCESSIBILITY_SETTINGS
                    );

            startActivity(intent);

            statusText.setText(
                    "Accessibility settings open hain"
            );

            speak(
                    "Opening accessibility settings."
            );

        } catch (Exception e) {

            speak(
                    "I could not open accessibility settings."
            );
        }
    }

    // =========================================================
    // CONTAINS ANY
    // =========================================================

    private boolean containsAny(
            String text,
            String... values) {

        if (text == null) {
            return false;
        }

        String lower =
                text.toLowerCase(
                        Locale.ROOT
                );

        for (String value : values) {

            if (value == null) {
                continue;
            }

            if (lower.equals(
                    value.toLowerCase(Locale.ROOT)
            )) {

                return true;
            }

            if (lower.contains(
                    value.toLowerCase(Locale.ROOT)
            )) {

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    private void speak(
            String text) {

        if (text == null ||
                text.trim().length() == 0) {

            return;
        }

        if (jarvesVoice == null) {
            return;
        }

        try {

            jarvesVoice.speak(
                    text,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "JARVES_RESPONSE"
            );

        } catch (Exception ignored) {
        }
    }

    // =========================================================
    // DESTROY
    // =========================================================

    @Override
    protected void onDestroy() {

        if (jarvesVoice != null) {

            try {

                jarvesVoice.stop();
                jarvesVoice.shutdown();

            } catch (Exception ignored) {
            }

            jarvesVoice = null;
        }

        super.onDestroy();
    }
}
```
