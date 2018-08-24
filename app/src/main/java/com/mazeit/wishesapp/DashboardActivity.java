package com.mazeit.wishesapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {
    private static final String TAG = "DashboardActivity";
    HttpConnectionThread Hf=null;
    private Handler handler = new Handler();
    public String phone_no,pass_word;
    private String recievedData ="";
    private String sAppSessionId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        getSupportActionBar().hide();

    }

    public void logIn(View view){
        final EditText phoneNo = (EditText)findViewById(R.id.editTextPhoneno);
        phone_no = phoneNo.getText().toString();
        if (phone_no.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter Phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        final EditText password = (EditText)findViewById(R.id.editTextPassword);
        pass_word = password.getText().toString();
        if (pass_word.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        //  Send any registration to your app's servers.
        sendRegistrationToServer(refreshedToken, phone_no);


        final Button logIn =(Button)findViewById(R.id.buttonLogIn);

        Hf = new HttpConnectionThread( null );
        Hf.setHttpConnectionThreadListener(new HttpConnectionThread.HttpConnectionThreadListener() {
            @Override
            public void getURLData(String s1, String sessionId) {
                recievedData = s1;
                sAppSessionId = sessionId;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Data Recieved: " + recievedData);
                        logIn.setText("Login");
                        if(recievedData.equals("{\"result\": \"ok\"}")) {
                            Toast.makeText(getApplicationContext(), "You have been logged in successfully.", Toast.LENGTH_SHORT).show();
                            Intent in = new Intent(getApplicationContext(), MessageActivity.class);
                            in.putExtra("LoggedIn", true);
                            in.putExtra("SessionId", sAppSessionId);
                            startActivity(in);
                            phoneNo.setText("");
                            password.setText("");
                            finish();
                            return;
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Invalid phone number or password.  Login failed.", Toast.LENGTH_SHORT).show();
                            password.setText("");
                        }

                    }
                });
            }
        });

        String data = "{\"phone_number\":\""+phone_no+"\", \"code\":\""+pass_word+"\"}";

        String urlSMServerUpdateToken = "http://192.168.1.2:8080/smserver/login";
        Hf.setURL(urlSMServerUpdateToken);
        Hf.setURLMethod("POST");
        Hf.setInputData(data);
        Thread thread = new Thread(Hf);
        thread.start();

        logIn.setText("Logging In ...");
    }

    private void sendRegistrationToServer(String token, String phoneNo) {
        Hf = new HttpConnectionThread( null );
        Hf.setHttpConnectionThreadListener(new HttpConnectionThread.HttpConnectionThreadListener() {
            @Override
            public void getURLData(String s1, String sessionId) {
                recievedData=s1;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Recieved Data: " + recievedData);
                    }
                });
            }
        });

        String urlSMServerUpdateToken = "http://192.168.1.2:8080/smsashish/consumer/"+phoneNo+"/device/" + token;
        Hf.setURL(urlSMServerUpdateToken);
        Hf.setURLMethod("PUT");
        Thread thread = new Thread(Hf);
        thread.start();
    }
    public void createAccount(View v) {
        Toast.makeText(this, "sorry", Toast.LENGTH_SHORT).show();
    }
}
