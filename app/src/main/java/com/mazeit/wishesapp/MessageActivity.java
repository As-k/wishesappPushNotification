package com.mazeit.wishesapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MessageActivity";
    private EditText editTextMessage, editTextPhone;
    HttpConnectionThread Hf=null;
    private Handler handler = new Handler();
    private String sDataReceived = "";
    private String sTextInDisplay = "";
    private String sessionId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        getSupportActionBar().hide();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("PushNotificationMessages"));

        Intent intent = getIntent();
        TextView textViewDisplayMessage = (TextView) findViewById(R.id.textViewDisplayMessage);

        if (intent!= null) {
            if (intent.hasExtra("LoggedIn")) {
                Log.d(TAG, "Logged In intent...");
                //userLoggedIn = true;

                SharedPreferences sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("LoggedIn", true);
                editor.commit();

                intent.removeExtra("LoggedIn");
            }
            if (intent.hasExtra("SessionId")) {
                sessionId = intent.getStringExtra("SessionId");
                Log.d(TAG, "SessionId: " + sessionId);
                intent.removeExtra("SessionId");
            }
            // Process the message, if any arrives
            if (intent.hasExtra("message") && intent.hasExtra("phone")) {
                Log.d(TAG, intent.getStringExtra("message"));
                Log.d(TAG, intent.getStringExtra("phone"));

                String message = intent.getStringExtra("message");
                String phone = intent.getStringExtra("phone");
                Toast.makeText(getApplicationContext(), "OnCreate(): " + message + " by " + phone, Toast.LENGTH_SHORT).show();

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                // Get from the SharedPreferences
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TAG, MODE_PRIVATE);
                String messageHistory = sharedPreferences.getString("MessageHistory", null);
                String sCurrentText = phone + " at " + dateFormat.format(date)+ ":\n" + message + "\n\n" + messageHistory;

                // Save into SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("MessageHistory", sCurrentText);
                editor.commit();

                intent.removeExtra("message");
                intent.removeExtra("phone");
            }
        }

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TAG, MODE_PRIVATE);
        Boolean loggedIn = sharedPreferences.getBoolean("LoggedIn", false);
        if (loggedIn != true) {
                Log.d(TAG, "userLoggedIn flag is not true...");
                startActivity(new Intent(this, DashboardActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
                return;
        }

        // Get from the SharedPreferences
        String messageHistory = sharedPreferences.getString("MessageHistory", null);

        textViewDisplayMessage.setText(messageHistory);
        sTextInDisplay = messageHistory;

        editTextMessage = (EditText)findViewById(R.id.editTextMessage);
        final int maxLength = 2000;
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= maxLength){
                    editTextMessage.setError("Message can not exceed the maximum size of 2000 characters.");
                }
            }
        });
    }

/*   @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            editTextMessage.setError(null);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            editTextMessage.setText("Something");
        }
    }*/

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Process the message, if any arrives
        if (intent.hasExtra("message") && intent.hasExtra("phone")) {
            Log.d(TAG, intent.getStringExtra("message"));

            String message = intent.getStringExtra("message");
            String phone = intent.getStringExtra("phone");
            Toast.makeText(getApplicationContext(), "New Intent: " + message + " by " + phone, Toast.LENGTH_SHORT).show();

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            // Get from the SharedPreferences
            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences(TAG, MODE_PRIVATE);
            String messageHistory = sharedPreferences.getString("MessageHistory", null);
            String sCurrentText = phone + " at " + dateFormat.format(date)+ ":\n" + message + "\n\n" + messageHistory;

            // Save into SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("MessageHistory", sCurrentText);
            editor.commit();

            TextView textViewDisplayMessage = (TextView) findViewById(R.id.textViewDisplayMessage);
            textViewDisplayMessage.setText(sCurrentText);
            sTextInDisplay = sCurrentText;

            intent.removeExtra("message");
            intent.removeExtra("phone");
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    public void sendMessage(View view){
        Hf = new HttpConnectionThread( null );
        editTextPhone = (EditText)findViewById(R.id.editTextPhone);
        String phoneNo = editTextPhone.getText().toString();
        String msg = editTextMessage.getText().toString();

        Hf.setHttpConnectionThreadListener(new HttpConnectionThread.HttpConnectionThreadListener() {
            @Override
            public void getURLData(String s1, String sessionId) {
                sDataReceived=s1;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Data Recieved: " + sDataReceived);
                        if(sDataReceived.equals("{\"result\":\"ok\",\"result_code\":\"200\"," +
                                "\"result_string\":\"Successfully sent Push Message to Consumer\"}")) {
                            Toast.makeText(getApplicationContext(), "Sent the data.", Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Error.  Message was not sent.", Toast.LENGTH_SHORT).show();

                        editTextMessage.setText("");
                    }
                });
            }
        });

        /* JSON format: {
                            "phoneNo":"4321432143",
                            "message":"Hello: how are you?"
                        }
        */
        String data = "{\"phoneNo\":\""+phoneNo+"\", \"message\":\""+msg+"\"}";
        JSONObject jsData = null;
        try {
            jsData = new JSONObject(data);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " +e);
        }
        String urlSMServerUpdateToken = "http://192.168.1.2:8080/smserver/consumer/notification/";
        Hf.setURL(urlSMServerUpdateToken);
        Hf.setURLMethod("POST");
        Hf.setInputData(jsData.toString());
        Hf.setSession(sessionId);
        Thread thread = new Thread(Hf);
        thread.start();

        //Clear the input box and show "Sending..."
        editTextMessage.setText("Sending...");
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("Message");
            String phone = intent.getStringExtra("Phone");

            Toast.makeText(getApplicationContext(), "Broadcast Receiver: " + message + " by " + phone, Toast.LENGTH_SHORT).show();

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            TextView textViewDisplayMessage = (TextView) findViewById(R.id.textViewDisplayMessage);
            String sCurrentText = phone + " at " + dateFormat.format(date)+ ":\n" + message + "\n\n" + sTextInDisplay;
            textViewDisplayMessage.setText(sCurrentText);
            sTextInDisplay = sCurrentText;

            SharedPreferences sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("MessageHistory", sTextInDisplay);
            editor.commit();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message, menu);//Menu Resource, Menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.saved:
                Toast.makeText(getApplicationContext(),"Messages Saved",Toast.LENGTH_LONG).show();
                return true;
            case R.id.cleanMessages: {
                TextView textViewDisplayMessage = (TextView) findViewById(R.id.textViewDisplayMessage);
                sTextInDisplay = "";
                SharedPreferences sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("MessageHistory", sTextInDisplay);
                editor.commit();
                Toast.makeText(getApplicationContext(), "Cleaned all messages", Toast.LENGTH_LONG).show();
                textViewDisplayMessage.setText(sTextInDisplay);
                return true;
            }
            case R.id.buttonLogOut: {
                Hf = new HttpConnectionThread( null );
                Hf.setHttpConnectionThreadListener(new HttpConnectionThread.HttpConnectionThreadListener() {
                    @Override
                    public void getURLData(String s1, String sessionId) {
                        sDataReceived=s1;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "Data Recieved: " + sDataReceived);
                                Toast.makeText(getApplicationContext(), "You have been logged out.", Toast.LENGTH_SHORT).show();
                                //userLoggedIn = false;
                                SharedPreferences sharedPreferences = getSharedPreferences(TAG, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("LoggedIn", false);
                                editor.commit();
                                finish();
                                return;
                            }
                        });
                    }
                });

                String urlSMServerUpdateToken = "http://192.168.1.2:8080/smserver/logout";
                Hf.setURL(urlSMServerUpdateToken);
                Hf.setURLMethod("POST");
                Hf.setSession(sessionId);
                Thread thread = new Thread(Hf);
                thread.start();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
