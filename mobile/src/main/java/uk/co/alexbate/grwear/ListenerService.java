package uk.co.alexbate.grwear;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

public class ListenerService extends WearableListenerService {
    private static final String TAG = "GRWearMobile";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/start/sendDataActivity")) {
            Log.d("Message", "received");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            new SyncTask(mGoogleApiClient).execute();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                           Log.w("ListenerService", "ConnectionSuspended");
                        }
                    })
                    .build();
            mGoogleApiClient.connect();

        }
    }
}
