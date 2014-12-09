package uk.co.alexbate.grwear;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import java.io.IOException;


public class PhoneDisplay extends ActionBarActivity implements MessageApi.MessageListener {
    private static final String TAG = "GRWear";
    private GoogleApiClient mGoogleApiClient;

    public void clickTestButton(View view) {
        new SyncTask().execute();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals("/start/sendDataActivity")) {
            new SyncTask().execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_display);
         mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d(TAG, "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }








    private class SyncTask extends AsyncTask<Void, Void, Integer> {

        public String getData() throws IOException {
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet("http://rsa33.user.srcf.net/api/grmenu?lim=1");
            HttpResponse response = httpclient.execute(httpget);
            if (response==null) {
                Log.e("getData", "response is null");
            } else {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String result = EntityUtils.toString(entity);
                    Log.d("getData", "" + result);
                    return result;
                } else {
                    Log.e("getData", "entity is null");
                }
            }
            return null;
        }

        public void sendData(String data) {
            PutDataMapRequest dataMap = PutDataMapRequest.create("/apiResult");
            dataMap.getDataMap().putString("API_DATA", data);
            PutDataRequest request = dataMap.asPutDataRequest();
            PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                    .putDataItem(mGoogleApiClient, request);

        }

        protected void onProgressUpdate(Integer... progress) {
            return;
        }

        protected void onPostExecute(Long result) {
            Log.d("SyncTask", "complete");
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                String rawData = getData();
                Log.d("doInBackground", "Sending " + rawData);
                sendData(rawData);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Integer.valueOf(0);
        }
    }
}
