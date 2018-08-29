package ss.gomland.android.applogger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

public class AppLogger {
    public static void start(@NonNull Context context) throws PermissionManageOverlayException, PermissionWriteExternalStorageException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                throw new PermissionManageOverlayException();
            }
        }

        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            throw new PermissionWriteExternalStorageException();
        }

        Intent intent = new Intent(context, AppLoggerService.class);
        context.startService(intent);
    }

    public static void stop(@NonNull Context context) {
        context.stopService(new Intent(context, AppLoggerService.class));
    }
}
