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
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

public class CharacterListActivity extends AppCompatActivity implements CharactersAdapter.OnCharacterItemClick{

    private TextView textViewMsg;
    private RecyclerView recyclerView;
    private CharacterDatabase characterDatabase;
    private List<Character> characters;
    private CharactersAdapter charactersAdapter;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_list);

        initializeVies();
        displayList();
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
                            case 3:
                                characters.get(pos);
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

}
