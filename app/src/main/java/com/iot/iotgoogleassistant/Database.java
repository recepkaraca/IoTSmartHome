package com.iot.iotgoogleassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "iot_smart_home";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String ROW_USERNAME = "username";
    private static final String ROW_PASSWORD = "password";

    private static final String TABLE_ADMINS = "admins";
    private static final String ROW_ADMIN_USERNAME = "username";

    private static final String TABLE_NFC = "nfc";
    private static final String ROW_NFC_ID = "nfc_id";
    private static final String ROW_NFC_USERNAME = "username";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + "("
                + ROW_USERNAME + " TEXT PRIMARY KEY NOT NULL, "
                + ROW_PASSWORD + " TEXT NOT NULL )");

        db.execSQL("CREATE TABLE " + TABLE_ADMINS + "("
                + ROW_ADMIN_USERNAME + " TEXT PRIMARY KEY NOT NULL, "
                + "FOREIGN KEY (" + ROW_ADMIN_USERNAME + ") REFERENCES "+TABLE_USERS+"("+ROW_USERNAME+"))");

        db.execSQL("CREATE TABLE " + TABLE_NFC + "("
                + ROW_NFC_ID + " TEXT PRIMARY KEY NOT NULL, "
                + ROW_NFC_USERNAME + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + ROW_NFC_USERNAME + ") REFERENCES "+TABLE_USERS+"("+ROW_USERNAME+"))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADMINS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NFC);
        onCreate(db);
    }

    public void insertData(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(ROW_USERNAME, username);
            cv.put(ROW_PASSWORD, password);
            db.insert(TABLE_USERS, null, cv);
        } catch (Exception e) {
        }
        db.close();
    }

    public void insertDataAdmin(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(ROW_USERNAME, username);
            db.insert(TABLE_ADMINS, null, cv);
        } catch (Exception e) {
        }
        db.close();
    }

    public void insertDataNFC(String nfcID, String username){
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues cv = new ContentValues();
            cv.put(ROW_NFC_ID, nfcID);
            cv.put(ROW_NFC_USERNAME, username);
            db.insert(TABLE_NFC, null, cv);
        } catch (Exception e) {
        }
        db.close();
    }
}
