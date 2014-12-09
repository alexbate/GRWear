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


public class MainWearActivity extends Activity {

    private TextView mTextView;
    private String rawData;
    private GoogleApiClient mGoogleApiClient;

    public void sendMsgToNode(Node node) {
        PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(
                mGoogleApiClient, node.getId(), "/start/sendDataActivity", null);
        result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
            @Override
            public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                if (!sendMessageResult.getStatus().isSuccess()) {
                    Log.e("Click", "ERROR: failed to send Message: " + sendMessageResult.getStatus());
                }
                getData();

            }
        });
    }

    public void click(View view) {
        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                Node n = getConnectedNodesResult.getNodes().get(0);
                sendMsgToNode(n);
            }
        });

        int wait=0;
        while(rawData==null) {
            wait++;
            mTextView.setText("Waiting for menu for " + wait + " loops");
        }
        Intent intent = new Intent(this, GridActivity.class);
        intent.putExtra("uk.co.alexbate.GRWear.API_DATA", rawData);
        startActivity(intent);

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
                mTextView = (TextView) stub.findViewById(R.id.text);
                getData();
                if(rawData!=null) {
                    mTextView.setText(rawData);
                }
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
            }
        });
    }

}
