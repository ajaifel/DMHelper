package com.btinternet.jackbaxter007.dmhelper;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.btinternet.jackbaxter007.dmhelper.CharacterDatabase;
import com.btinternet.jackbaxter007.dmhelper.Character;

import java.lang.ref.WeakReference;

public class CharacterForm extends AppCompatActivity {

    private TextInputEditText c_name, p_name, c_class, c_level, c_stats, c_skills, c_equip, c_desc;
    private CharacterDatabase characterDatabase;
    private Character character;
    private Boolean update = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_character_form);
        c_name = findViewById(R.id.characterName);
        p_name = findViewById(R.id.playerName);
        c_level = findViewById(R.id.characterLevel);
        c_class = findViewById(R.id.characterClass);
        c_stats = findViewById(R.id.characterStats);
        c_skills = findViewById(R.id.characterSkills);
        c_equip = findViewById(R.id.characterEquip);
        c_desc = findViewById(R.id.characterDesc);

        characterDatabase = CharacterDatabase.getInstance(CharacterForm.this);

        Button save = findViewById(R.id.saveButton);

        if ((getIntent().getSerializableExtra("transfer"))!=null){
            character = (Character) getIntent().getSerializableExtra("character");
            c_name.setText(character.getChar_name());
            p_name.setText(character.getPlayer_name());
            c_class.setText(character.getCharacter_class());
            c_level.setText(character.getLevel());
            c_stats.setText(character.getStats());
            c_skills.setText(character.getSkills());
            c_equip.setText(character.getEquipment());
            c_desc.setText(character.getDescription());
            character = new Character(c_name.getText().toString(),
                    p_name.getText().toString(),
                    c_class.getText().toString(),
                    c_level.getText().toString(),
                    c_stats.getText().toString(),
                    c_skills.getText().toString(),
                    c_equip.getText().toString(),
                    c_desc.getText().toString());

            // create worker thread to insert data into database
            setResult(character,1);
            new InsertTask(CharacterForm.this, character).execute();
        }
        else if ( (character = (Character) getIntent().getSerializableExtra("character"))!=null ){
            //getSupportActionBar().setTitle("Update Character");
            update = true;
            save.setText("Update");
            c_name.setText(character.getChar_name());
            p_name.setText(character.getPlayer_name());
            c_class.setText(character.getCharacter_class());
            c_level.setText(character.getLevel());
            c_stats.setText(character.getStats());
            c_skills.setText(character.getSkills());
            c_equip.setText(character.getEquipment());
            c_desc.setText(character.getDescription());
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (update){
                    character.setChar_name(c_name.getText().toString());
                    character.setPlayer_name(p_name.getText().toString());
                    character.setCharacter_class(c_class.getText().toString());
                    character.setLevel(c_level.getText().toString());
                    character.setStats(c_stats.getText().toString());
                    character.setSkills(c_skills.getText().toString());
                    character.setEquipment(c_equip.getText().toString());
                    character.setDescription(c_desc.getText().toString());
                    characterDatabase.getCharacterDao().update(character);
                    setResult(character,2);
                } else {
                    // fetch data and create character object
                    character = new Character(c_name.getText().toString(),
                            p_name.getText().toString(),
                            c_class.getText().toString(),
                            c_level.getText().toString(),
                            c_stats.getText().toString(),
                            c_skills.getText().toString(),
                            c_equip.getText().toString(),
                            c_desc.getText().toString());

                    // create worker thread to insert data into database
                    setResult(character,1);
                    new InsertTask(CharacterForm.this, character).execute();
                }
            }
        });

    }
    private void setResult(Character character, int flag){
        setResult(flag,new Intent().putExtra("character", character));
        finish();
    }


    private static class InsertTask extends AsyncTask<Void,Void,Boolean> {

        private WeakReference<CharacterForm> activityReference;
        private Character character;

        // only retain a weak reference to the activity
        InsertTask(CharacterForm context, Character character) {
            activityReference = new WeakReference<>(context);
            this.character = character;
        }

        // doInBackground methods runs on a worker thread
        @Override
        protected Boolean doInBackground(Void... objs) {
            activityReference.get().characterDatabase.getCharacterDao().insert(character);
            return true;
        }

        // onPostExecute runs on main thread
        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool){
                activityReference.get().setResult(character,1);
            }
        }

    }

}
