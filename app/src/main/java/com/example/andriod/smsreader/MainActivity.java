package com.example.andriod.smsreader;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String PREF_USER_MOBILE_PHONE = "pref_user_mobile_phone";
    private static final int SMS_PERMISSION_CODE = 1;

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
            requestReadAndSendSmsPermission();
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
        try{
            System.out.println(":::::::::::::::::::::::");
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            File file = new File(path, "/" + "abc.csv");

           /* File file = new File(Environment.getExternalStoragePublicDirectory("SmsReader"), "");
            if (!file.exists()) {
                file.mkdir();
            }
            File sdCardFile = new File(Environment.getExternalStoragePublicDirectory("SmsReader") + "/filename.txt");

            //File file1=  new File(file,  "message.csv");
*/
            FileWriter csvWriter = new FileWriter(file);
            csvWriter.write("Sender");
            csvWriter.write("\t");
            csvWriter.write("Body");
            csvWriter.write("\t");
            csvWriter.append("dateSent");
            csvWriter.write("\t");
            csvWriter.append("dateReceived");
            csvWriter.append("\t");
            csvWriter.append("creator");
            csvWriter.append("\n");

        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        int body_index = 0;
        int address_index = 0;
        int creator_index=0;
        int date_index=0;
        int type_index=0;
        int subject_index=0;
        int person_index=0;
        int date_sent_index=0;


        for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
            if (cursor.getColumnName(idx).equals("address")) {
                address_index = idx;
            }
            if (cursor.getColumnName(idx).equals("body")) {
                body_index = idx;
            }
            if (cursor.getColumnName(idx).equals("creator")) {
                creator_index = idx;
            }
            if (cursor.getColumnName(idx).equals("date")) {
                date_index = idx;
            }
            if (cursor.getColumnName(idx).equals("date_sent")) {
                date_sent_index = idx;
            }
            if (cursor.getColumnName(idx).equals("person")) {
                person_index = idx;
            }
            if (cursor.getColumnName(idx).equals("type")) {
                type_index = idx;
            }if (cursor.getColumnName(idx).equals("subject")) {
                subject_index = idx;
            }


        }
            List<List<String>> rows = new ArrayList<>();
        double total_amount_debited = 0.0;
        if (cursor.moveToFirst()) { // must check the result to prevent exception
            do {
                List<String> row = new ArrayList<>();
               // Log.e(TAG, "senderNum: " + cursor.getString(address_index));
             /*   Log.e(TAG, cursor.getString(address_index) + " "+ cursor.getString(body_index) + " "+
                        cursor.getString(creator_index) + " "+cursor.getString(subject_index) + " "+cursor.getString(date_index) + " "+
                        cursor.getString(type_index) + " "+ cursor.getString(person_index) + " " + cursor.getString(date_sent_index));
*/
                Matcher m = regEx.matcher(cursor.getString(address_index));
                if (m.find()) {
                    try {

                        if (cursor.getString(body_index).contains("debited") && !cursor.getString(body_index).contains("requested") && !cursor.getString(body_index).contains("will be")) {
                        //    Log.e(TAG, "senderNum: " + cursor.getString(address_index) + "; message: " + cursor.getString(body_index));
                            row.add(cursor.getString(address_index));
                            row.add(cursor.getString(body_index));
                            Calendar cal = Calendar.getInstance();
                            System.out.println(cursor.getString(date_sent_index));
                            System.out.println(cursor.getString(date_index));
                            cal.setTimeInMillis(Long.parseLong(cursor.getString(date_sent_index)));
                            row.add(new SimpleDateFormat("dd MMMM yyyy").format(cal.getTime()).toString());
                            cal.setTimeInMillis(Long.parseLong(cursor.getString(date_index)));
                            row.add(new SimpleDateFormat("dd MMMM yyyy").format(cal.getTime()).toString());

                            row.add(cursor.getString(creator_index));



                         /*   List<List<String>> rows = Arrays.asList(
                                    Arrays.asList("Jean", "author", "Java"),
                                    Arrays.asList("David", "editor", "Python"),
                                    Arrays.asList("Scott", "editor", "Node.js")
                            );*/

rows.add(row);



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

            for (List<String> rowData : rows) {
                csvWriter.append(String.join("\t", rowData));
                csvWriter.append("\n");
            }

            csvWriter.flush();
            csvWriter.close();
          /*  String filename="abc.csv";
            File filelocation = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), filename);
            Uri file_path = Uri.fromFile(filelocation);
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
// set the type to 'email'
            emailIntent.setType("text/plain");
            //emailIntent.setDataAndType(Uri.fromFile(file), mimeType);
            String to[] = {"cruxaki@gmail.com", "abhi.gupta141@gmail.com"};
            emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
// the attachment
            emailIntent .putExtra(Intent.EXTRA_STREAM, file_path);
// the mail subject
            emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");
            startActivity(Intent.createChooser(emailIntent , "Send email..."));*/
       // System.out.println("total amount debited: " + total_amount_debited);
        mNumberEditText = (TextView) findViewById(R.id.textView);
        mNumberEditText.setText(String.valueOf(total_amount_debited));
        } catch (Exception e){
e.printStackTrace();
        }
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
                        Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(MainActivity.this,
                        READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestReadAndSendSmsPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                &&ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_SMS)) {
            Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
            return;
        }
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS,Manifest.permission.WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE  },
                SMS_PERMISSION_CODE);
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }
}