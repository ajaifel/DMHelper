package com.btinternet.jackbaxter007.dmhelper;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SendNearby extends AppCompatActivity {

    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private static final String TAG = "DMHelper";

    private ConnectionsClient connectionsClient;

    private TextView playerName, characterName, conStatus;
    private Character character;
    private Button button;
    private String endpointID;
    private String partnerEndpointId;

    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
//                    recievedText = new String(payload.asBytes(), UTF_8);
//                    Toast.makeText(getApplicationContext(),recievedText, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
//                    if (update.getStatus() == Status.SUCCESS && recievedText != null) {
//                    }
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
                    conStatus.setText("Device found, connecting...");
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    conStatus.setText("Device lost, Searching...");
                }
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

                        partnerEndpointId = endpointId;
                        conStatus.setText("Connection Successful, Sending Character");
                        try {
                            sendData();
                            conStatus.setText("Character Sent!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i(TAG, "onConnectionResult: connection failed");
                        Toast.makeText(getApplicationContext(),"onConnectionResult: connection failed", Toast.LENGTH_LONG).show();
                        conStatus.setText("Failed to Connect to Device");
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "onDisconnected: disconnected from partner");
                    Toast.makeText(getApplicationContext(),"onDisconnected: disconnected from partner", Toast.LENGTH_LONG).show();
                    conStatus.setText("You have been Disconnected from the Device");
                }
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_nearby);
        character = (Character) getIntent().getSerializableExtra("character");

        characterName = findViewById(R.id.charName);
        playerName = findViewById(R.id.playerName);
        button = findViewById(R.id.connect_button);
        conStatus = findViewById(R.id.prog_text);

        connectionsClient = Nearby.getConnectionsClient(this);

    }

    private void startDiscovery() {
        // Note: Discovery may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startDiscovery(
                getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build());
    }
    private void sendData() throws IOException {

        connectionsClient.sendPayload(
                partnerEndpointId, Payload.fromBytes(serialize(character)));
    }

    public void sendCharacter(View view) {
        startDiscovery();
        Toast.makeText(this, "Discover: pressed", Toast.LENGTH_LONG).show();
        conStatus.setText("Searching for Device...");
    }

    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
}
