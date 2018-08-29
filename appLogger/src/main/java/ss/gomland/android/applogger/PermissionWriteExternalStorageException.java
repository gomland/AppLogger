package ss.gomland.android.applogger;

public class PermissionWriteExternalStorageException extends Exception {
    public PermissionWriteExternalStorageException() {
        super("Need permission WRITE_EXTERNAL_STORAGE");
    }
}