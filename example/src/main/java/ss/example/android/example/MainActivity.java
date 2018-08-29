package ss.example.android.example;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ss.gomland.android.applogger.AppLogger;
import ss.gomland.android.applogger.PermissionManageOverlayException;
import ss.gomland.android.applogger.PermissionWriteExternalStorageException;

public class MainActivity extends AppCompatActivity {
    private final int REQUEST_CODE_MANAGE_OVERLAY_PERMISSION = 111;
    private final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MANAGE_OVERLAY_PERMISSION) {
            //TODO
        } else if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            //TODO
        }
    }


    public void onClickStart(View v) {
        try {
            AppLogger.start(this);
        } catch (PermissionManageOverlayException e) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_MANAGE_OVERLAY_PERMISSION);
        } catch (PermissionWriteExternalStorageException e) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
        }
    }

    public void onClickStop(View v) {
        AppLogger.stop(this);
    }

    public void onClickCreateLog(View v){
        Log.v(MainActivity.class.getSimpleName(), "verse 로그 생성. 한글 테스트");
        Log.i(MainActivity.class.getSimpleName(), "info 로그 생성.");
        Log.d(MainActivity.class.getSimpleName(), "Create debug log.");
        Log.w(MainActivity.class.getSimpleName(), "Create warning log.");
        Log.e(MainActivity.class.getSimpleName(), "Create error log.");
    }
}
