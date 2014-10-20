/*
 * Copyright (C) 2014 The Android Open Source Project, Tyler Heck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.feedback;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;

import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityMain extends FragmentActivity {
    private ActionBar actionBar;
    private ActivityManager activityManager;
    private Date dateTime;
    private FileOutputStream fileOutput;
    private Intent intentView;
    private List<ActivityManager.RunningAppProcessInfo> runningProcesses;
    private Menu menuActionBar;
    private PackageInfo packageInfo;
    private SimpleDateFormat timeFormat;
    private String appCode;
    private String appName;
    private String appVersion;
    private String runningApplications;
    private String stringInfo;
    private TelephonyManager telephonyManager;
    private View screenView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set action bar title to Main.
        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.title_main));

        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menuActionBar = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback:
                launchFeedback();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Get device information and save to application cache to send as attachment in feedback email.
     */
    public void getDeviceInfo() {
        try {
            dateTime = new Date();
            timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);

            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appName = getApplicationContext().getString(packageInfo.applicationInfo.labelRes);
            appVersion = packageInfo.versionName;
            appCode = Integer.toString(packageInfo.versionCode);

            // Get application information.
            stringInfo = "date/time: " + timeFormat.format(dateTime) + "\n\n";
            stringInfo += "packageName: " + packageInfo.packageName + "\n";
            stringInfo += "packageCode: " + appCode + "\n";
            stringInfo += "packageVersion: " + appVersion + "\n";

            // Get network information.
            telephonyManager = ((TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE));

            if (!telephonyManager.getNetworkOperatorName().equals("")) {
                stringInfo += "operatorNetName: " + telephonyManager.getNetworkOperatorName() + "\n";
            } else if (!telephonyManager.getSimOperatorName().equals("")) {
                stringInfo += "operatorSimName: " + telephonyManager.getSimOperatorName() + "\n";
            }

            // Get device information.
            stringInfo += "Build.MODEL: " + Build.MODEL + "\n";
            stringInfo += "Build.BRAND: " + Build.BRAND + "\n";
            stringInfo += "Build.DEVICE: " + Build.DEVICE + "\n";
            stringInfo += "Build.PRODUCT: " + Build.PRODUCT + "\n";
            stringInfo += "Build.ID: " + Build.ID + "\n";
            stringInfo += "Build.TYPE: " + Build.TYPE + "\n";
            stringInfo += "Build.VERSION.SDK_INT: " + Build.VERSION.SDK_INT + "\n";
            stringInfo += "Build.VERSION.RELEASE: " + Build.VERSION.RELEASE + "\n";
            stringInfo += "Build.VERSION.INCREMENTAL: " + Build.VERSION.INCREMENTAL + "\n";
            stringInfo += "Build.VERSION.CODENAME: " + Build.VERSION.CODENAME + "\n";
            stringInfo += "Build.BOARD: " + Build.BOARD + "\n\n";

            // Get other application information.
            activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            runningProcesses = activityManager.getRunningAppProcesses();
            runningApplications = "";

            for (ActivityManager.RunningAppProcessInfo runningProcess : runningProcesses) {
                runningApplications += "\n  " + runningProcess.processName;
            }

            stringInfo += "Applications running:" + runningApplications;

            // Save information to cached file.
            fileOutput = new FileOutputStream(getCacheDir().getAbsolutePath() + "/deviceInfo.log");
            fileOutput.write(stringInfo.getBytes());
            fileOutput.close();
        } catch (Exception e) {
        }
    }

    /**
     * Get screenshot and save to application cache to send as attachment in feedback email.
     */
    public void getScreenImage() {
        try {
            screenView = findViewById(android.R.id.content).getRootView();
            screenView.setDrawingCacheEnabled(true);
            fileOutput = new FileOutputStream(getCacheDir().getAbsolutePath() + "/screenImage.png");
            screenView.getDrawingCache().compress(Bitmap.CompressFormat.PNG, 100, fileOutput);
            fileOutput.close();
        } catch (Exception e) {
        }
    }

    /**
     * Get device information, get screenshot, and launch Feedback activity.
     */
    public void launchFeedback() {
        getDeviceInfo();
        getScreenImage();

        intentView = new Intent(ActivityMain.this, ActivityFeedback.class);
        intentView.putExtra("deviceInfo", getCacheDir().getAbsolutePath() + "/deviceInfo.log");
        intentView.putExtra("screenImage", getCacheDir().getAbsolutePath() + "/screenImage.png");
        intentView.putExtra("screenConfig", getResources().getConfiguration().orientation);

        if (intentView != null) {
            startActivity(intentView);
        }
    }
}
