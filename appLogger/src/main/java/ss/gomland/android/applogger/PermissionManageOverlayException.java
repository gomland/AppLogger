package ss.gomland.android.applogger;


public class PermissionManageOverlayException extends Exception {
    public PermissionManageOverlayException() {
        super("Need permission ACTION_MANAGE_OVERLAY_PERMISSION");
    }
}