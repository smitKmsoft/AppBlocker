package com.km.appblocker;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    RecyclerView appList;
    ArrayList<AppInfo> installedAppList = new ArrayList<>();
    MainAdapter adapter;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    AppOpsManager appOps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appList = findViewById(R.id.appList);

        appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);

        installedAppList = getInstalledApps(MainActivity.this);

        preferences = getSharedPreferences("AppList",MODE_PRIVATE);
        editor = preferences.edit();
        editor.clear();
        Gson gson = new Gson();
        String json = gson.toJson(installedAppList);
        editor.putString("AppList",json);
        editor.apply();

        if (installedAppList == null){
            installedAppList = new ArrayList<>();
        }
        if (!installedAppList.isEmpty()) {
            LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false);
            appList.setLayoutManager(manager);
            adapter = new MainAdapter(MainActivity.this,installedAppList);
            appList.setAdapter(adapter);
        }

        checkOverlayPermission();
        startService();
    }

    public ArrayList<AppInfo> getInstalledApps(@NonNull Context context) {
        ArrayList<AppInfo> appList = new ArrayList<>();
        Gson gson = new Gson();
        String json = getSharedPreferences("AppList",MODE_PRIVATE).getString("AppList", null);
        Type type = new TypeToken<ArrayList<AppInfo>>() {}.getType();
        appList =  gson.fromJson(json, type);


        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !packageInfo.packageName.equals(getPackageName())) {
                String appName = packageInfo.applicationInfo.loadLabel(packageManager).toString();
                Drawable appIcon = packageInfo.applicationInfo.loadIcon(packageManager);
                if (appList != null) {
                    if (!appList.isEmpty()) {
                        boolean isMatch = false;
                        for (int i = 0; i < appList.size(); i++) {

                            AppInfo appInfo = appList.get(i);
                            if (appInfo.packageName.equals(packageInfo.applicationInfo.packageName)) {
                                isMatch = true;
                            }
                        }

                        if (!isMatch) {
                            appList.add(new AppInfo(appName,false,packageInfo.applicationInfo.packageName));
                        }

                    } else {
                        appList.add(new AppInfo(appName,false,packageInfo.applicationInfo.packageName));
                    }

                } else {
                    appList = new ArrayList<>();
                    appList.add(new AppInfo(appName,false,packageInfo.applicationInfo.packageName));


                }

            }
        }

        return appList;
    }

    public void updateList(String packageName, boolean isBlock) {

        for (AppInfo appInfo : installedAppList) {
            if (appInfo.packageName.equals(packageName)) {
                appInfo.isBlock = isBlock;
                break;
            }
        }

        editor.clear();

        Gson gson = new Gson();
        String json = gson.toJson(installedAppList);
        editor.putString("AppList", json);
        editor.apply();
    }

    // method for starting the service
    public void startService(){
        // check if the user has already granted
        // the Draw over other apps permission
        if(Settings.canDrawOverlays(this)) {
            // start the service based on the android version
            startForegroundService(new Intent(this, ForegroundService.class));
        }
    }

    // method to ask user to grant the Overlay permission
    public void checkOverlayPermission(){

        if (!Settings.canDrawOverlays(this)) {
            // send user to the device settings
            Intent myIntent =new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(myIntent);
        }


        if(appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName()) != AppOpsManager.MODE_ALLOWED) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 1);
        }
    }

    // check for permission again when user grants it from
    // the device settings, and start the service
    @Override
    protected void onResume() {
        super.onResume();
        startService();
    }
}