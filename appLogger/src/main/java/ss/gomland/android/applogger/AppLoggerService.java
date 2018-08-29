package ss.gomland.android.applogger;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

class AppLoggerService extends Service implements View.OnClickListener, View.OnTouchListener {
    private WindowManager.LayoutParams mFloatingLayoutParams;
    private Point mStartPoint = new Point();
    private Point mOriginPoint = new Point();

    private WindowManager mWindowManager;
    private LinearLayout mFloatingView;
    private Button mStartBtn, mStopBtn;
    private Thread mRecordThread;
    private Process mProcessClear, mProcessLog;

    private Handler mHandler = new Handler();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        mFloatingLayoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_PHONE);
        mFloatingLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatingLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mFloatingLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        mFloatingLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        mFloatingView = getLayout();

        mWindowManager.addView(mFloatingView, mFloatingLayoutParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWindowManager.removeView(mFloatingView);
        stopRecord();
    }

    @SuppressLint("ClickableViewAccessibility")
    private LinearLayout getLayout() {
        LinearLayout floatingView = new LinearLayout(this);
        floatingView.setBackgroundColor(Color.BLUE);
        floatingView.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setGravity(Gravity.CENTER);
        titleView.setText(R.string.logger_title);
        titleView.setTextColor(Color.RED);
        titleView.setOnTouchListener(this);
        floatingView.addView(titleView);

        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) titleView.getLayoutParams();
        p.setMargins(0, 15, 0, 15);
        titleView.setLayoutParams(p);

        LinearLayout btnLayout = new LinearLayout(this);
        btnLayout.setOrientation(LinearLayout.HORIZONTAL);
        floatingView.addView(btnLayout);

        mStartBtn = new Button(this);
        mStartBtn.setText(R.string.logger_start_logging);
        mStartBtn.setOnClickListener(this);
        btnLayout.addView(mStartBtn);

        mStopBtn = new Button(this);
        mStopBtn.setText(R.string.logger_stop_logging);
        mStopBtn.setVisibility(View.GONE);
        mStopBtn.setOnClickListener(this);
        btnLayout.addView(mStopBtn);

        return floatingView;
    }

    @Override
    public void onClick(View view) {
        if (view instanceof Button) {
            if (view == mStartBtn) {
                startRecord();
                mStartBtn.setVisibility(View.GONE);
                mStopBtn.setVisibility(View.VISIBLE);
            } else {
                stopRecord();
                mStartBtn.setVisibility(View.VISIBLE);
                mStopBtn.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartPoint.x = (int) ev.getRawX();
                mStartPoint.y = (int) ev.getRawY();
                mOriginPoint.x = mFloatingLayoutParams.x;
                mOriginPoint.y = mFloatingLayoutParams.y;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = mStartPoint.x - ev.getRawX();
                float deltaY = mStartPoint.y - ev.getRawY();
                mFloatingLayoutParams.x = (int) (mOriginPoint.x - deltaX);
                mFloatingLayoutParams.y = (int) (mOriginPoint.y - deltaY);
                mWindowManager.updateViewLayout(mFloatingView, mFloatingLayoutParams);
                break;
        }
        return true;
    }

    private void startRecord() {
        mRecordThread = new Thread() {
            @Override
            public void run() {
                String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        + "/" + System.currentTimeMillis();
                try {
                    mProcessClear = new ProcessBuilder()
                            .command("logcat", "-c")
                            .redirectErrorStream(true)
                            .start();
                    mProcessLog = new ProcessBuilder()
                            .command("logcat", "-f", fileName)
                            .redirectErrorStream(true)
                            .start();
                    mProcessLog.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (mProcessLog != null) {
                        mProcessLog.destroy();
                        mProcessLog = null;
                    }

                    if (mProcessClear != null) {
                        mProcessClear.destroy();
                        mProcessClear = null;
                    }

                    encodingKorean(fileName);
                }
            }
        };
        mRecordThread.start();
    }

    private void stopRecord() {
        if (mRecordThread != null) {
            mRecordThread.interrupt();
            mRecordThread = null;
        }
    }

    private String getCurrentTime(String format) {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat(format);
        return sdfNow.format(date);
    }

    private void encodingKorean(String filrName) {
        File src = new File(filrName);

        if (src.exists()) {
            final String saveFileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    + "/" + getPackageName() + getCurrentTime("yyyyMMdd_HHmmss") + ".txt";
            FileWriter fileWriter = new FileWriter(saveFileName);
            fileWriter.create("euc-kr");

            InputStream is = null;
            BufferedReader reader = null;
            String readLine;
            try {
                is = new FileInputStream(src.getAbsolutePath());
                reader = new BufferedReader(new InputStreamReader(is));
                while ((readLine = reader.readLine()) != null) {
                    fileWriter.write(readLine + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            fileWriter.close();
            src.delete();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppLoggerService.this, "Save File : " + saveFileName, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
