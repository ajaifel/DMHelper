package com.btinternet.jackbaxter007.dmhelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CharacterListActivity extends AppCompatActivity implements CharactersAdapter.OnCharacterItemClick{

    private static final Strategy STRATEGY = Strategy.P2P_STAR;
    private static final String TAG = "DMHelper";

    private TextView textViewMsg;
    private RecyclerView recyclerView;
    private CharacterDatabase characterDatabase;
    private List<Character> characters;
    private CharactersAdapter charactersAdapter;
    private int pos;
    FloatingActionButton fab2;
    private ConnectionsClient connectionsClient;
    private String endpointID;
    private String partnerEndpointId;
    private Character inCharacter;

    // Callbacks for receiving payloads
    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    try {
                        inCharacter = (Character) deserialize(payload.asBytes());
                        characters.add(inCharacter);
                        inCharacter = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
                    if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                        displayList();
                    }
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_list);

        fab2 = findViewById(R.id.fab2);
        initializeVies();
        displayList();

        connectionsClient = Nearby.getConnectionsClient(this);
    }

    private void displayList(){
// initialize database instance
        characterDatabase = CharacterDatabase.getInstance(CharacterListActivity.this);
// fetch list of characters in background thread
        new RetrieveTask(this).execute();
    }

    private static class RetrieveTask extends AsyncTask<Void,Void,List<Character>>{

        private WeakReference<CharacterListActivity> activityReference;

        // only retain a weak reference to the activity
        RetrieveTask(CharacterListActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<Character> doInBackground(Void... voids) {
            if (activityReference.get()!=null)
                return activityReference.get().characterDatabase.getCharacterDao().getAll();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Character> characters) {
            if (characters!=null && characters.size()>0 ){
                activityReference.get().characters = characters;

                // hides empty text view
                activityReference.get().textViewMsg.setVisibility(View.GONE);

                // create and set the adapter on RecyclerView instance to display list
                activityReference.get().charactersAdapter = new CharactersAdapter(characters,activityReference.get());
                activityReference.get().recyclerView.setAdapter(activityReference.get().charactersAdapter);
            }
        }

    }

    private void initializeVies(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewMsg = (TextView) findViewById(R.id.back_empty);

        // Action button to add a character
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(listener);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(CharacterListActivity.this));

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(CharacterListActivity.this,CharacterForm.class),100);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode > 0 ){
            if( resultCode == 1){
                characters.add((Character) data.getSerializableExtra("character"));
            }else if( resultCode == 2){
                characters.set(pos,(Character) data.getSerializableExtra("character"));
            }
            listVisibility();
        }
    }

    @Override
    public void onCharacterClick(final int pos) {
        new AlertDialog.Builder(CharacterListActivity.this)
                .setTitle("Select Options")
                .setItems(new String[]{"Delete", "Update", "Send"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i){
                            case 0:
                                characterDatabase.getCharacterDao().delete(characters.get(pos));
                                characters.remove(pos);
                                listVisibility();
                                break;
                            case 1:
                                CharacterListActivity.this.pos = pos;
                                startActivityForResult(
                                        new Intent(CharacterListActivity.this,
                                                CharacterForm.class).putExtra("character",characters.get(pos)),
                                        100);
                                break;
                        }
                    }
                }).show();

    }

    private void listVisibility(){
        int emptyMsgVisibility = View.GONE;
        if (characters.size() == 0){ // no item to display
            if (textViewMsg.getVisibility() == View.GONE)
                emptyMsgVisibility = View.VISIBLE;
        }
        textViewMsg.setVisibility(emptyMsgVisibility);
        charactersAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        characterDatabase.cleanUp();
        super.onDestroy();
    }


    private void startAdvertising() {
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
                "WHY", getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build());
    }

    public void findCharacters(View view) {
        startAdvertising();
        Toast.makeText(this, "Looking for Players...", Toast.LENGTH_LONG).show();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

}
