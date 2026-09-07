package com.jarves.ai;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class JarvesAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Jarves future commands yahan process karega.
    }

    @Override
    public void onInterrupt() {
        // Accessibility service interrupted.
    }

    public boolean clickText(String text) {

        AccessibilityNodeInfo root =
                getRootInActiveWindow();

        if (root == null) {
            return false;
        }

        return clickNode(root, text);
    }

    private boolean clickNode(
            AccessibilityNodeInfo node,
            String text) {

        if (node == null) {
            return false;
        }

        CharSequence nodeText = node.getText();

        if (nodeText != null &&
                nodeText.toString().equalsIgnoreCase(text)) {

            if (node.isClickable()) {
                return node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                );
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                if (clickNode(child, text)) {
                    return true;
                }

                child.recycle();
            }
        }

        return false;
    }
}
