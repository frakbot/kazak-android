package io.kazak.push;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.kazak.BuildConfig;

public class GcmListenerService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"schedule"};

    private static final String KEY_TOKEN_SENT_TO_SERVER = "gcm_token_sent";

    public GcmListenerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = retrieveToken(instanceID);

            sendToServer(token);
            saveTokenSentFlag(this);
            subscribeTopics(token);
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            removeTokenSentFlag(this);
        }

        // TODO do something with the token
        Log.i(TAG, "GCM token received");
    }

    private static void saveTokenSentFlag(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(KEY_TOKEN_SENT_TO_SERVER, true).apply();
    }

    private static void removeTokenSentFlag(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putBoolean(KEY_TOKEN_SENT_TO_SERVER, false).apply();
    }

    private static String retrieveToken(InstanceID instanceID) throws IOException {
        return instanceID.getToken(BuildConfig.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
    }

    private void sendToServer(String token) {
        // TODO send the token to the server
    }

    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        for (String topic : TOPICS) {
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }

}
