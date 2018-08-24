package com.mazeit.mypushnotification;

import android.os.Handler;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.mazeit.wishesapp.HttpConnectionThread;

/**
 * Created by User on 5/16/2017.
 */

public class MyFirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIDService";
    HttpConnectionThread Hf=null;
    private Handler handler = new Handler();
    private String sRecievedData ="";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // TODO: Implement this method to send any registration to your app's servers.
//        sendRegistrationToServer(refreshedToken);
    }

/*    private void sendRegistrationToServer(String token) {
        Hf = new HttpConnectionThread( null );
        Hf.setHttpConnectionThreadListener(new HttpConnectionThread.HttpConnectionThreadListener() {
            @Override
            public void getURLData(String s1) {
                sRecievedData=s1;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Recieved Data: " + sRecievedData);
                    }
                });
            }
        });

        String urlSMServerUpdateToken = "http://192.168.1.2:8080/smsashish/consumer/5869065877/device/" + token;
        Hf.setURL(urlSMServerUpdateToken);
        Hf.setURLMethod("PUT");
        Thread thread = new Thread(Hf);
        thread.start();
    }*/
}
