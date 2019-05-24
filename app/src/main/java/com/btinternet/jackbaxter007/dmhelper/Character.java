package com.btinternet.jackbaxter007.dmhelper;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Ignore;

import java.io.Serializable;

@Entity(tableName = "characters")
public class Character implements Serializable{

    @PrimaryKey(autoGenerate = true)
        private long char_id;

        @ColumnInfo(name = "character_name") // column name will be "character_name" instead of "char_name" in table
        private String char_name;
    private String player_name;
    private String character_class;
    private String level;
    private String stats;
    private String skills;
    private String equipment;
    private String description;

        public Character(String char_name,
                         String player_name,
                         String character_class,
                         String level,
                         String stats,
                         String skills,
                         String equipment,
                         String description) {
            this.char_id = char_id;
            this.char_name = char_name;
            this.player_name = player_name;
            this. character_class = character_class;
            this.level = level;
            this.stats = stats;
            this.skills = skills;
            this.equipment = equipment;
            this.description = description;
        }

    public void setChar_id(long char_id) {
        this.char_id = char_id;
    }

    public long getChar_id() {
        return char_id;
    }

    public String getChar_name() {
        return char_name;
    }

    public void setChar_name(String char_name) {
        this.char_name = char_name;
    }

    public String getPlayer_name() {
        return player_name;
    }

    public void setPlayer_name(String player_name) {
        this.player_name = player_name;
    }

    public String getCharacter_class() {
        return character_class;
    }

    public void setCharacter_class(String character_class) {
        this.character_class = character_class;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Character)) return false;

            Character character = (Character) o;

            if (char_id != character.char_id) return false;
            return char_name != null ? char_name.equals(character.char_name) : character.char_name == null;
        }

    @Override
    public int hashCode() {
        int result = (int)char_id;
        result = 31 * result + (char_name != null ? char_name.hashCode() : 0);
        return result;
    }
}
