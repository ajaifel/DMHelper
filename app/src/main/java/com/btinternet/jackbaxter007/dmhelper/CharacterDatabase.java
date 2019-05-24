package com.btinternet.jackbaxter007.dmhelper;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.btinternet.jackbaxter007.dmhelper.CharacterDao;

@Database(entities = { Character.class }, version = 1)
public abstract class CharacterDatabase extends RoomDatabase {
    public abstract CharacterDao getCharacterDao();

    private static CharacterDatabase characterDB;

    public static CharacterDatabase getInstance(Context context) {
        if (null == characterDB) {
            characterDB = buildDatabaseInstance(context);
        }
        return characterDB;
    }

    private static CharacterDatabase buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context,
                CharacterDatabase.class,
                "characterdb.db")
                .allowMainThreadQueries().build();
    }

    public void cleanUp(){
        characterDB = null;
    }
}
