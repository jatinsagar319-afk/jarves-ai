package com.jarves.ai;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class JarvesAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Future Jarves commands yahan process honge.
    }

    @Override
    public void onInterrupt() {
        // Service interrupted.
    }

    public void clickText(String text) {

        AccessibilityNodeInfo root = getRootInActiveWindow();

        if (root == null) {
            return;
        }

        clickNode(root, text);
    }

    private boolean clickNode(
            AccessibilityNodeInfo node,
            String text
    ) {

        if (node == null) {
            return false;
        }

        CharSequence nodeText = node.getText();

        if (nodeText != null &&
                nodeText.toString().equalsIgnoreCase(text)) {

            if (node.isClickable()) {
                node.performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                );
                return true;
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {

            AccessibilityNodeInfo child =
                    node.getChild(i);

            if (child != null) {

                boolean clicked =
                        clickNode(child, text);

                child.recycle();

                if (clicked) {
                    return true;
                }
            }
        }

        return false;
    }
}
