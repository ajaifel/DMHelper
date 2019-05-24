package com.btinternet.jackbaxter007.dmhelper;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;
@Dao
public interface CharacterDao {
    @Query("SELECT * FROM "+ "characters")
    List<Character> getAll();


    /*
     * Insert the object in database
     * @param character, object to be inserted
     */
    @Insert
    void insert(Character character);

    /*
     * update the object in database
     * @param character, object to be updated
     */
    @Update
    void update(Character repos);

    /*
     * delete the object from database
     * @param character, object to be deleted
     */
    @Delete
    void delete(Character character);

    /*
     * delete list of objects from database
     * @param character, array of objects to be deleted
     */
    @Delete
    void delete(Character... characters);      // Character... is varargs, here character is an array

}
