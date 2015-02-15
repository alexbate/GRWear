package uk.co.alexbate.grwear;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class SyncTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "GRWearMobile";
    private final GoogleApiClient mGoogleApiClient;

    public String getData() throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet("http://rsa33.user.srcf.net/api/grmenu?lim=2");
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
        return 0;
    }

    SyncTask(GoogleApiClient client) {
        mGoogleApiClient = client;
        Log.d("SyncTask created", mGoogleApiClient.toString());
    }
}