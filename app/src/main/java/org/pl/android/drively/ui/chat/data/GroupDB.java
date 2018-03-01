package org.pl.android.drively.ui.chat.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.pl.android.drively.data.model.chat.Room;
import org.pl.android.drively.data.model.chat.RoomMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupDB {
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_GROUP_ID + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_GROUP_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_GROUP_ADMIN + TEXT_TYPE + COMMA_SEP +
                    GroupDB.FeedEntry.COLUMN_GROUP_MEMBER + TEXT_TYPE + COMMA_SEP +
                    "PRIMARY KEY (" + GroupDB.FeedEntry.COLUMN_GROUP_ID + COMMA_SEP +
                    GroupDB.FeedEntry.COLUMN_GROUP_MEMBER + "))";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + GroupDB.FeedEntry.TABLE_NAME;
    private static GroupDB.GroupDBHelper mDbHelper = null;
    private static GroupDB instance = null;


    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private GroupDB() {
    }

    public static GroupDB getInstance(Context context) {
        if (instance == null) {
            instance = new GroupDB();
            mDbHelper = new GroupDB.GroupDBHelper(context);
        }
        return instance;
    }

    public void addGroup(Room group) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_GROUP_ID, group.getId());
        values.put(FeedEntry.COLUMN_GROUP_NAME, group.getName());
        values.put(FeedEntry.COLUMN_GROUP_ADMIN, group.getAdmin());

        for (RoomMember member : group.getMembers()) {
            values.put(FeedEntry.COLUMN_GROUP_MEMBER, member.getMemberId());
            // Insert the new row, returning the primary key value of the new row
            db.insert(FeedEntry.TABLE_NAME, null, values);
        }
    }

    public void deleteGroup(String idGroup) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(FeedEntry.TABLE_NAME, FeedEntry.COLUMN_GROUP_ID + " = " + idGroup, null);
    }

    public void addListGroup(ArrayList<Room> listGroup) {
        for (Room group : listGroup) {
            addGroup(group);
        }
    }

    public Room getGroup(String id) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + GroupDB.FeedEntry.TABLE_NAME + " where " + FeedEntry.COLUMN_GROUP_ID + " = " + id, null);
        Room newGroup = new Room();
        while (cursor.moveToNext()) {
            String idGroup = cursor.getString(0);
            String nameGroup = cursor.getString(1);
            String admin = cursor.getString(2);
            String member = cursor.getString(3);
            newGroup.setId(idGroup);
            newGroup.setName(nameGroup);
            newGroup.setAdmin(admin);
            newGroup.getMembers().add(new RoomMember(member));
        }
        return newGroup;
    }

    public ArrayList<Room> getListGroups() {
        Map<String, Room> mapGroup = new HashMap<>();
        ArrayList<String> listKey = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + GroupDB.FeedEntry.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                String idGroup = cursor.getString(0);
                String nameGroup = cursor.getString(1);
                String admin = cursor.getString(2);
                String member = cursor.getString(3);
                if (!listKey.contains(idGroup)) {
                    Room newGroup = new Room();
                    newGroup.setId(idGroup);
                    newGroup.setName(nameGroup);
                    newGroup.setAdmin(admin);
                    newGroup.getMembers().add(new RoomMember(member));
                    listKey.add(idGroup);
                    mapGroup.put(idGroup, newGroup);
                } else {
                    mapGroup.get(idGroup).getMembers().add(new RoomMember(member));
                }
            }
            cursor.close();
        } catch (Exception e) {
            return new ArrayList<>();
        }

        ArrayList<Room> listGroup = new ArrayList<>();
        for (String key : listKey) {
            listGroup.add(mapGroup.get(key));
        }

        return listGroup;
    }

    public void dropDB() {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "groups";
        static final String COLUMN_GROUP_ID = "groupID";
        static final String COLUMN_GROUP_NAME = "name";
        static final String COLUMN_GROUP_ADMIN = "admin";
        static final String COLUMN_GROUP_MEMBER = "memberID";
    }

    private static class GroupDBHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "GroupChat.db";

        GroupDBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
