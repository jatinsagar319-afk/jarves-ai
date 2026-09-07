package com.jarves.ai;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JarvesAccessibilityService extends AccessibilityService {

    private static JarvesAccessibilityService instance;

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Jarves automation events
    }

    @Override
    public void onInterrupt() {
        // Accessibility service interrupted
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public static boolean isRunning() {
        return instance != null;
    }

    // --------------------------------------------------
    // CLICK TEXT
    // --------------------------------------------------

    public boolean clickText(String text) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null || text == null) {
            return false;
        }

        return findAndClick(
                root,
                text.toLowerCase(Locale.ROOT).trim()
        );
    }

    private boolean findAndClick(
            AccessibilityNodeInfo node,
            String target) {

        if (node == null) {
            return false;
        }

        CharSequence nodeText =
                node.getText();

        CharSequence description =
                node.getContentDescription();

        if (matches(nodeText, target)
                || matches(description, target)) {

            if (node.isClickable()) {

                return node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                );
            }

            AccessibilityNodeInfo parent =
                    node.getParent();

            if (parent != null &&
                    parent.isClickable()) {

                return parent.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                );
            }
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                boolean result =
                        findAndClick(
                                child,
                                target
                        );

                child.recycle();

                if (result) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matches(
            CharSequence value,
            String target) {

        if (value == null ||
                target == null) {

            return false;
        }

        String current =
                value.toString()
                        .toLowerCase(Locale.ROOT)
                        .trim();

        return current.equals(target)
                || current.contains(target)
                || target.contains(current);
    }

    // --------------------------------------------------
    // TYPE TEXT
    // --------------------------------------------------

    public boolean typeText(String text) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null || text == null) {
            return false;
        }

        AccessibilityNodeInfo input =
                findEditableField(root);

        if (input == null) {
            return false;
        }

        Bundle arguments =
                new Bundle();

        arguments.putCharSequence(
                AccessibilityNodeInfo
                        .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
        );

        boolean result =
                input.performAction(
                        AccessibilityNodeInfo.ACTION_SET_TEXT,
                        arguments
                );

        input.recycle();

        return result;
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

    // --------------------------------------------------
    // FOCUS EDITABLE FIELD
    // --------------------------------------------------

    public boolean focusEditableField() {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        AccessibilityNodeInfo input =
                findEditableField(root);

        if (input == null) {
            return false;
        }

        boolean result =
                input.performAction(
                        AccessibilityNodeInfo.ACTION_FOCUS
                );

        input.recycle();

        return result;
    }

    // --------------------------------------------------
    // GLOBAL BACK
    // --------------------------------------------------

    public boolean globalBack() {

        return performGlobalAction(
                GLOBAL_ACTION_BACK
        );
    }

    // --------------------------------------------------
    // HOME
    // --------------------------------------------------

    public boolean goHome() {

        return performGlobalAction(
                GLOBAL_ACTION_HOME
        );
    }

    // --------------------------------------------------
    // RECENTS
    // --------------------------------------------------

    public boolean openRecents() {

        return performGlobalAction(
                GLOBAL_ACTION_RECENTS
        );
    }

    // --------------------------------------------------
    // GET VISIBLE TEXT
    // --------------------------------------------------

    public List<String> getVisibleTexts() {

        List<String> result =
                new ArrayList<>();

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return result;
        }

        collectTexts(
                root,
                result
        );

        return result;
    }

    private void collectTexts(
            AccessibilityNodeInfo node,
            List<String> result) {

        if (node == null) {
            return;
        }

        CharSequence text =
                node.getText();

        if (text != null &&
                text.length() > 0) {

            result.add(
                    text.toString()
            );
        }

        CharSequence description =
                node.getContentDescription();

        if (description != null &&
                description.length() > 0) {

            result.add(
                    description.toString()
            );
        }

        for (int i = 0;
             i < node.getChildCount();
             i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                collectTexts(
                        child,
                        result
                );

                child.recycle();
            }
        }
    }
    }
