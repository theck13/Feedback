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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.feedback.library.FeedbackSender;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ActivityFeedback extends Activity {
    private Account[] accountArray;
    private ActionBar actionBar;
    private ArrayAdapter<String> accountAdapter;
    private ArrayList<String> accountList;
    private Bitmap inputImage;
    private CheckBox checkScreen;
    private Dialog screenDialog;
    private EditText inputText;
    private FeedbackSender feedbackSender;
    private File fileImage;
    private File fileLog;
    private FileInputStream inputStream;
    private ImageView imageCrop;
    private ImageView imageScreen;
    private Intent intent;
    private LayoutInflater screenInflater;
    private LinearLayout layoutScreen;
    private Menu menuActionBar;
    private Pattern emailPattern;
    private Spinner spinnerFrom;
    private TextView textPreview;
    private View screenView;
    private static AlarmManager alarm;
    private static ConnectivityManager connectivityManager;
    private static FeedbackSender feedbackLater;
    private static File saveImage;
    private static File saveLog;
    private static HttpURLConnection connection;
    private static NetworkInfo activeNetworkInfo;
    private static PendingIntent pending;
    private static String saveComment;
    private static String saveFrom;
    private static URL url;
    private static boolean hasInternet;
    private static boolean saveAttachments;
    private final static String PASSWORD = "PASSWORD";
    private final static String SENDER = "SENDER@gmail.com";
    private final static String RECEIVER = "RECEIVER@gmail.com";
    // Edit URL for desired internet connection test.
    private final static String URL = "http://www.google.com";
    // Increase TIMEOUT value for slower internet connections.
    private final static int TIMEOUT = 5000;

@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        // Set action bar title to Feedback.
        actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.title_feedback));

        // Set views.
        spinnerFrom = (Spinner) findViewById(R.id.spinner_from);
        inputText = (EditText) findViewById(R.id.input_test);
        checkScreen = (CheckBox) findViewById(R.id.check_screen);
        layoutScreen = (LinearLayout) findViewById(R.id.layout_screen);
        imageCrop = (ImageView) findViewById(R.id.image_crop);
        textPreview = (TextView) findViewById(R.id.text_preview);

        // Get screenshot from previous activity.
        getScreen();

        // Get device account(s) for spinner (select primary by default).
        getAccounts();
        spinnerFrom.setAdapter(accountAdapter);
        spinnerFrom.setSelection(1);

        // Set feedback to include screenshot/logs by default.
        checkScreen.setChecked(true);

        // Set text listener for feedback text.
        inputText.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){
                if (s.length() > 0) {
                    menuActionBar.findItem(R.id.action_send_disabled).setVisible(false);
                    menuActionBar.findItem(R.id.action_send_enabled).setVisible(true);
                }
                else {
                    menuActionBar.findItem(R.id.action_send_disabled).setVisible(true);
                    menuActionBar.findItem(R.id.action_send_enabled).setVisible(false);
                }
            }
        });

        // Set click listener for checkbox.
        checkScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkScreen.isChecked()) {
                    layoutScreen.setVisibility(View.VISIBLE);
                }
                else {
                    layoutScreen.setVisibility(View.INVISIBLE);
                }
            }
        });

        // Set click listener for screenshot image.
        imageCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayScreen();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Pop activity off stack when back button pressed.
        NavUtils.navigateUpFromSameTask(this);
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);

        // Set default menu visibility.
        menuActionBar = menu;
        menuActionBar.findItem(R.id.action_send_disabled).setVisible(true);
        menuActionBar.findItem(R.id.action_send_enabled).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Pop activity off stack when home button pressed.
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_send_enabled:
                if (hasConnection(this) && hasInternet()) {
                    sendFeedback();
                } else {
                    saveFeedback();
                }

                showMessage(getResources().getString(R.string.feedback_sending_message));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Show screenshot pop-up dialog when image is pressed.
     */
    private void displayScreen() {
        screenDialog = new Dialog(ActivityFeedback.this, R.style.DialogImage);
        screenInflater = LayoutInflater.from(ActivityFeedback.this);
        screenView = screenInflater.inflate(R.layout.layout_screen, null);
        imageScreen = (ImageView) screenView.findViewById(R.id.image_screen);
        imageScreen.setImageBitmap(inputImage);
        screenDialog.setContentView(screenView);
        screenDialog.show();
    }

    /**
     * Get Google accounts from Android to populate spinner dropdown list.
     * Replace with custom method if application uses non-Google accounts.
     */
    public void getAccounts() {
        emailPattern = Patterns.EMAIL_ADDRESS;
        accountArray = AccountManager.get(getApplicationContext()).getAccounts();
        accountList = new ArrayList<String>();
        accountList.add(getResources().getString(R.string.feedback_anonymous));

        for (Account accountItem : accountArray) {
            if (emailPattern.matcher(accountItem.name).matches() && accountItem.type.matches("com.google")) {
                accountList.add(accountItem.name);
            }
        }

        accountAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, accountList);
    }

    /**
     * Get screenshot file from application cache to display as preview.
     */
    public void getScreen() {
        try {
            inputStream = new FileInputStream(getIntent().getStringExtra("screenImage"));
            inputImage = BitmapFactory.decodeStream(inputStream);
            imageCrop.setImageBitmap(inputImage);
        }
        catch (Exception e) {
        }
    }

    /**
     * Test whether device has data or wifi enabled.
     *
     * @param   context  application context of calling activity
     * @return  TRUE if data or wifi is enable, FALSE otherwise
     */
    public static boolean hasConnection(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting());
    }

    /**
     * Test whether device has internet access.
     *
     * @return  TRUE if internet is accessible, FALSE otherwise
     */
    public static boolean hasInternet() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... arg) {
                try {
                    url = new URL(URL);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(TIMEOUT);
                    connection.connect();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        hasInternet = true;
                    } else {
                        hasInternet = false;
                    }
                } catch (Exception e) {
                }

                return null;
            }
        }.execute();

        return hasInternet;
    }

    /**
     * Save feedback information and set alarm to send feedback later,
     * pop Feedback activity off stack, and return to Main activity.
     */
    public void saveFeedback() {
        saveAttachments = checkScreen.isChecked();
        saveFrom = spinnerFrom.getSelectedItem().toString();
        saveComment = inputText.getText().toString();
        saveImage = sendImage();
        saveLog = sendLog();
        NavUtils.navigateUpFromSameTask(this);
        setAlarm(this);
    }

    /**
     * Send feedback immediately (since internet connection exists).
     */
    public void sendFeedback() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... arg) {
                try {
                    feedbackSender = new FeedbackSender(SENDER, PASSWORD);

                    if (checkScreen.isChecked()) {
                        feedbackSender.sendMail("Feedback [" + spinnerFrom.getSelectedItem().toString() + "]", inputText.getText().toString(), SENDER, RECEIVER, sendImage(), sendLog());
                    } else {
                        feedbackSender.sendMail("Feedback [" + spinnerFrom.getSelectedItem().toString() + "]", inputText.getText().toString(), SENDER, RECEIVER);
                    }
                } catch (Exception e) {
                }

                return null;
            }
        }.execute();

        // Pop activity off stack after feedback is sent.
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * Send feedback later (when internet connection exists).
     *
     * @param   checked  TRUE if screenshot/log checkbox is checked, FALSE otherwise
     * @param   from     account sending feedback
     * @param   comment  text entered by user for feedback
     * @param   image    screenshot attached to feedback for context
     * @param   log      device information attached to feedback for context
     */
    public static void sendFeedback(final boolean checked, final String from, final String comment, final File image, final File log) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... arg) {
                try {
                    feedbackLater = new FeedbackSender(SENDER, PASSWORD);

                    if (checked) {
                        feedbackLater.sendMail("Feedback [" + from + "]", comment, SENDER, RECEIVER, image, log);
                    } else {
                        feedbackLater.sendMail("Feedback [" + from + "]", comment, SENDER, RECEIVER);
                    }
                } catch (Exception e) {
                }

                return null;
            }
        }.execute();
    }

    /**
     * Get screenshot from application cache to send as attachment in feedback email.
     *
     * @return  File from application cache to send as attachment in feedback email.
     */
    public File sendImage() {
        try {
            fileImage = new File(getIntent().getStringExtra("screenImage"));
            return fileImage;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Get device information from application cache to send as attachment in feedback email.
     *
     * @return  File from application cache to send as attachment in feedback email.
     */
    public File sendLog() {
        try {
            fileLog = new File(getIntent().getStringExtra("deviceInfo"));
            return fileLog;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Set broadcast receiver of type FeedbackBroadcastReceiver and create an alarm to test internet
     * connection repeatedly until feedback is sent.
     *
     * @param   context  application context of calling activity
     */
    public void setAlarm(Context context) {
        intent = new Intent (this, FeedbackBroadcastReceiver.class);
        pending = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pending);
        // Set alarm to start immediately and repeat every fifteen minutes, approximately.
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, pending);
    }

    /**
     * Show toast message when Feedback activity is popped from the stack.
     */
    public void showMessage(String option) {
        Toast.makeText(getApplicationContext(), option, Toast.LENGTH_SHORT).show();
    }

    /**
     * Send saved feedback once internet connection is established and cancel alarm.
     */
    public static class FeedbackBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (hasConnection(context) && hasInternet()) {
                sendFeedback(saveAttachments, saveFrom, saveComment, saveImage, saveLog);
                alarm.cancel(pending);
            }
        }
    }
}
