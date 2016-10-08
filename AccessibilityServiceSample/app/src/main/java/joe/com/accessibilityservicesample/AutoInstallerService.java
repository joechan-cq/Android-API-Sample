package joe.com.accessibilityservicesample;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class AutoInstallerService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("AutoInstallerService", "onServiceConnected: ");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        List<AccessibilityNodeInfo> nodeInfos = getRootInActiveWindow().findAccessibilityNodeInfosByText("下一步");
        if (nodeInfos == null || nodeInfos.size() == 0) {
            nodeInfos = getRootInActiveWindow().findAccessibilityNodeInfosByText("安装");
            if (performClick(nodeInfos)) {
                return;
            }
        } else {
            if (performClick(nodeInfos)) {
                return;
            }
        }
        nodeInfos = getRootInActiveWindow().findAccessibilityNodeInfosByText("打开");
        performClick(nodeInfos);
    }

    private boolean performClick(List<AccessibilityNodeInfo> nodeInfos) {
        if (nodeInfos != null && nodeInfos.size() > 0) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
                if (nodeInfo.isClickable() && nodeInfo.isEnabled()) {
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onInterrupt() {

    }
}
