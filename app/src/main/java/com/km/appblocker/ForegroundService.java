package com.km.appblocker;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.km.mylibrary.AppChecker;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ForegroundService extends Service {

    AppChecker appChecker = new AppChecker();
    String currentApp;
    private Notification.Builder mBuilder;
    Window window;
    ArrayList<AppInfo> appList = new ArrayList<>();

    public ForegroundService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();
        String NOTIFICATION_CHANNEL_ID = "AppBlocker";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "For Channel", NotificationManager.IMPORTANCE_LOW);
        chan.setSound(null, null);

        NotificationManager manager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);

        manager.createNotificationChannel(chan);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        mBuilder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = mBuilder
                .setContentTitle("Tracking your usage.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setTicker("Oi oi!")
                .setColorized(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(2, notification);

        window = new Window(ForegroundService.this);

        new Handler().post(new tracking());
        updateOverlay();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return android.app.Service.START_REDELIVER_INTENT;

    }

    public class tracking implements Runnable {

        final Handler handler = new Handler();

        @Override
        public void run() {
            updateOverlay();

            handler.postDelayed(this, 2500);
        }
    }

    private String updateOverlay(){
        currentApp = appChecker.getForegroundApp(ForegroundService.this);
        System.out.println("===========> " + currentApp);

        Gson gson = new Gson();
        String json = getSharedPreferences("AppList",MODE_PRIVATE).getString("AppList", null);
        Type type = new TypeToken<ArrayList<AppInfo>>() {}.getType();
        appList =  gson.fromJson(json, type);

        if (appList != null) {
            if (!appList.isEmpty()) {
                boolean isMatch = false;
                for (AppInfo appInfo : appList) {
                    if (appInfo.isBlock) {
                        if (currentApp != null && currentApp.equals(appInfo.packageName)) {
                            isMatch = true;

                            window.open();
                            break;
                        }
                    }
                }

                if (!isMatch) {
                    window.close();
                }
            }
        }


        return currentApp;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        updateOverlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Intent restartIntent = new Intent(this, RestartReceiver.class);
        this.sendBroadcast(restartIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Intent restartIntent = new Intent(this, RestartReceiver.class);
        this.sendBroadcast(restartIntent);
    }

}