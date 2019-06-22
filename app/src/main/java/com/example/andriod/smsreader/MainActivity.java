package com.example.andriod.smsreader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andriod.SmsReader.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREF_USER_MOBILE_PHONE = "pref_user_mobile_phone";
    private static final int SMS_PERMISSION_CODE = 0;

    private TextView mNumberEditText;
    private String mUserMobilePhone;
    private SharedPreferences mSharedPreferences;
    private Pattern regEx = Pattern.compile("[a-zA-Z0-9]{2}(-)?[a-zA-Z0-9]{6}");
    private Pattern regex = Pattern.compile("(?i)(Rs.|INR)(\\s)?(\\d{1,9})(,\\d{1,9})?(,\\d{1,9})?(,\\d{1,9})?(,\\d{1,9})?(,\\d{1,9})?(\\.\\d{1,})?\\s");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!hasReadSmsPermission()) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                    1);
        } else {
            initViews();
        }

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserMobilePhone = mSharedPreferences.getString(PREF_USER_MOBILE_PHONE, "");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initViews();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void initViews() {
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        int body_index = 0;
        int address_index = 0;
        for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
            if (cursor.getColumnName(idx).equals("address")) {
                address_index = idx;
            }
            if (cursor.getColumnName(idx).equals("body")) {
                body_index = idx;
            }
        }
        double total_amount_debited = 0.0;
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                //Log.e(TAG, "senderNum: " + cursor.getString(address_index));

                Matcher m = regEx.matcher(cursor.getString(address_index));
                if (m.find()) {
                    try {

                        if (cursor.getString(body_index).contains("debited") && !cursor.getString(body_index).contains("requested") && !cursor.getString(body_index).contains("will be")) {
                           // Log.e(TAG, "senderNum: " + cursor.getString(body_index) + "; message: " + cursor.getString(body_index));
                            Matcher m1 = regex.matcher(cursor.getString(body_index));
                            if (m1.find()) {
                                String actual_amount = "";
                                for (int i = 3; i <= m1.groupCount(); i++) {
                                    if (m1.group(i) != null) {
                                        if (m1.group(i).startsWith(",")) {
                                            String curr = m1.group(i);
                                            curr = curr.substring(1, curr.length());
                                            actual_amount += curr;
                                        } else {
                                            actual_amount += m1.group(i);
                                        }
                                    }
                                }
                                double d = Double.parseDouble(actual_amount);
                                total_amount_debited += d;

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            } while (cursor.moveToNext());
        } else {
            Log.e(TAG, "No message present: " );
        }
       // System.out.println("total amount debited: " + total_amount_debited);
        mNumberEditText = (TextView) findViewById(R.id.textView);
        mNumberEditText.setText(String.valueOf(total_amount_debited));
    }




    /**
     * Checks if stored SharedPreferences value needs updating and updates \o/
     */
    private void checkAndUpdateUserPrefNumber() {
        if (TextUtils.isEmpty(mUserMobilePhone) && !mUserMobilePhone.equals(mNumberEditText.getText().toString())) {
            mSharedPreferences
                    .edit()
                    .putString(PREF_USER_MOBILE_PHONE, mNumberEditText.getText().toString())
                    .apply();
        }
    }


    /**
     * Validates if the app has readSmsPermissions and the mobile phone is valid
     *
     * @return boolean validation value
     */
    private boolean hasValidPreConditions() {
        if (!hasReadSmsPermission()) {
            requestReadAndSendSmsPermission();
            return false;
        }

        if (!SmsHelper.isValidPhoneNumber(mNumberEditText.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.error_invalid_phone_number, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * Optional informative alert dialog to explain the user why the app needs the Read/Send SMS permission
     */
    private void showRequestPermissionsInfoAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.permission_alert_dialog_title);
        builder.setMessage(R.string.permission_dialog_message);
        builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                requestReadAndSendSmsPermission();
            }
        });
        builder.show();
    }

    /**
     * Runtime permission shenanigans
     */
    private boolean hasReadSmsPermission() {


        return ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadAndSendSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS},
                SMS_PERMISSION_CODE);
    }
}