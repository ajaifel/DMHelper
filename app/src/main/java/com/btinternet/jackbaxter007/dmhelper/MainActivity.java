package com.btinternet.jackbaxter007.dmhelper;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate.Status;
import com.google.android.gms.nearby.connection.Strategy;

public class MainActivity extends AppCompatActivity {

    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private static final String TAG = "DMHelper";

    private ConnectionsClient connectionsClient;

    private static final String[] REQUIRED_PERMISSIONS =
            new String[] {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
            };

    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 1;

    private Button advertise;
    private Button discover;
    private Button connect;
    private Button sendPayload;

    private String endpointID;
    private String recievedText;
    private String partnerEndpointId;

    EditText textInput;

    private TextView mTextMessage;

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    recievedText = new String(payload.asBytes(), UTF_8);
                    Toast.makeText(getApplicationContext(),recievedText, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == Status.SUCCESS && recievedText != null) {
                    }
                }
            };

    // Callbacks for finding other devices
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    Log.i(TAG, "onEndpointFound: endpoint found, connecting");
                    Toast.makeText(getApplicationContext(),"onEndpointFound: endpoint found, connecting", Toast.LENGTH_LONG).show();
                    connectionsClient.requestConnection("WHY", endpointId, connectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {}
            };

    // Callbacks for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, "onConnectionInitiated: accepting connection");
                    Toast.makeText(getApplicationContext(),"onConnectionInitiated: accepting connection", Toast.LENGTH_LONG).show();
                    connectionsClient.acceptConnection(endpointId, payloadCallback);
                    //opponentName = connectionInfo.getEndpointName();
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    if (result.getStatus().isSuccess()) {
                        Log.i(TAG, "onConnectionResult: connection successful");
                        Toast.makeText(getApplicationContext(),"onConnectionResult: connection successful", Toast.LENGTH_LONG).show();

                        connectionsClient.stopDiscovery();
                        connectionsClient.stopAdvertising();

                        partnerEndpointId = endpointId;
                    } else {
                        Log.i(TAG, "onConnectionResult: connection failed");
                        Toast.makeText(getApplicationContext(),"onConnectionResult: connection failed", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "onDisconnected: disconnected from partner");
                    Toast.makeText(getApplicationContext(),"onDisconnected: disconnected from partner", Toast.LENGTH_LONG).show();
                }
            };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

//        advertise = findViewById(R.id.advertise);
//        discover = findViewById(R.id.discover);
//        connect = findViewById(R.id.connect);
//        sendPayload = findViewById(R.id.send);
//        textInput = (EditText)findViewById(R.id.text_to_send);

//        mTextMessage = (TextView) findViewById(R.id.message);
//        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

       connectionsClient = Nearby.getConnectionsClient(this);

        Intent intent = new Intent(this, CharacterListActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    protected void onStop() {
        connectionsClient.stopAllEndpoints();

        super.onStop();
    }

    /** Returns true if the app was granted all the permissions. Otherwise, returns false. */
    private static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /** Handles user acceptance (or denial) of our permission request. */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return;
        }

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Error: Missing Permissions", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        recreate();
    }

    private void startDiscovery() {
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());
    }

    /** Broadcasts our presence using Nearby Connections so other players can find us. */
    private void startAdvertising() {
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
                "WHY", getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());
    }

    public void sendData (View view){
        String value = textInput.getText().toString();
        connectionsClient.sendPayload(
                partnerEndpointId, Payload.fromBytes(value.getBytes(UTF_8)));
    }

    public void mAdvertise(View view) {
        startAdvertising();
        Toast.makeText(this, "Advertise: pressed", Toast.LENGTH_LONG).show();
    }

    public void mDiscover(View view) {
        startDiscovery();
        Toast.makeText(this, "Discover: pressed", Toast.LENGTH_LONG).show();
    }

    public void mConnect(View view) {
        Intent intent = new Intent(this, CharacterListActivity.class);
        startActivity(intent);
    }
}
