package uk.co.alexbate.grwear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;


public class MainWearActivity extends Activity {

    private TextView mTextView;
    private String rawData;
    private GoogleApiClient mGoogleApiClient;

    public void sendMsgToNode() {
        PendingResult<NodeApi.GetConnectedNodesResult> res = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        NodeApi.GetConnectedNodesResult finalRes = res.await();
        List<Node> nodes = finalRes.getNodes();
        for (final Node node: nodes) {
            PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, node.getId(), "/start/sendDataActivity", null);
            result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        Log.e("Click", "ERROR: failed to send Message: " + sendMessageResult.getStatus());
                    }
                    Log.d("Sent message, status:", sendMessageResult.getStatus().getStatusMessage() + " to " + node.getDisplayName());
                    getData();

                }
            });
        }
    }

    public void click(View view) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                Log.d("SendMessage Thread", "thread running");
                sendMsgToNode();
            }
        }).start();
    }

    public void startGridActivity() {
        try {
            JSONObject json = new JSONObject(rawData);
            JSONArray jsonArray = json.getJSONArray("menus");
            json = jsonArray.getJSONObject(0);
            String dateS = json.getString("date");
            DateFormat format = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
            Date menuDate = format.parse(dateS);
            //Get midnight today
            Calendar c = new GregorianCalendar();
            c.set(GregorianCalendar.HOUR_OF_DAY, 0);
            c.set(GregorianCalendar.MINUTE, 0);
            c.set(GregorianCalendar.SECOND, 0);
            c.set(GregorianCalendar.MILLISECOND, 0);
            Date compareDate = c.getTime();
            if (!menuDate.before(compareDate)) {
                Intent intent = new Intent(this, GridActivity.class);
                intent.putExtra("uk.co.alexbate.GRWear.API_DATA", rawData);
                startActivity(intent);
            } else {
                mTextView.setText("Still fetching today's menu. Try again.");
            }
        } catch (JSONException e1) {
            Log.e("JSONException", e1.getMessage());
        } catch (ParseException e2) {
            Log.e("ParseException", e2.getMessage());
        }
    }

    private void initGoogleServices() {
       mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Log.d("initGoogleServices", "onConnected: " + connectionHint);
                        // Now you can use the Data Layer API
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                        Log.d("initGoogleServices", "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d("initGoogleServices", "onConnectionFailed: " + result);
                    }
                })
                        // Request access only to the Wearable API
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        initGoogleServices();
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) findViewById(R.id.text);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SendMessage Thread", "thread running");
                        sendMsgToNode();
                    }
                }).start();
            }
        });
    }

    public void getData() {
        PendingResult<DataItemBuffer> pendResult = Wearable.DataApi.getDataItems(mGoogleApiClient);
        pendResult.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                if (dataItems.getCount() != 0) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(0));

                    // This should read the correct value.
                    rawData = dataMapItem.getDataMap().getString("API_DATA");
                }

                dataItems.release();
                if (rawData != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startGridActivity();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.setText("Problem loading menu. Try again");
                        }
                    });
                }
            }
        });
    }

}
