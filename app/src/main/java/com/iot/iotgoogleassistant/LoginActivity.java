package com.iot.iotgoogleassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    public static int wrongPassCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.login_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();


        Database db = new Database(LoginActivity.this);
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
        Cursor resUser = sqLiteDatabase.rawQuery("SELECT * FROM users", null);
        if(resUser.getCount() == 0){
            String username = "admin";
            String password = "admin";
            db.insertData(username, password);
            db.insertDataAdmin("admin");
        }
        addListener();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI(getWindow());
    }

    public static void hideSystemUI(Window window) {
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    protected void addListener() {
        CardView btn = findViewById(R.id.login_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = ((TextView)findViewById(R.id.username)).getText().toString();
                String password = ((TextView)findViewById(R.id.password)).getText().toString();
                Database db = new Database(LoginActivity.this);
                SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                Cursor resUser = sqLiteDatabase.rawQuery("SELECT * FROM users WHERE username = '" + username + "' AND password = '" + password + "'", null);
                if(resUser.getCount() == 0) {
                    Toast.makeText(getApplicationContext(), "Invalid username or password.", Toast.LENGTH_LONG).show();
                    wrongPassCounter++;
                }
                else {
                    Toast.makeText(getApplicationContext(), "Successfully logged in.", Toast.LENGTH_LONG).show();
                    logIn(username);
                    if(isAdmin(username)) {
                        setAlarm("0");
                        wrongPassCounter = 0;
                    }
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
                if(wrongPassCounter >= 3) {
                    setAlarm("1");
                }
            }
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference("nfc");
        reference.child("nfc_id").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                String nfcUsername;
                Database db = new Database(LoginActivity.this);
                SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                Cursor resNFC = sqLiteDatabase.rawQuery("SELECT * FROM nfc WHERE nfc_id = '" + value + "'", null);
                if (resNFC.moveToFirst()){
                    do {
                        nfcUsername = resNFC.getString(1);
                    } while(resNFC.moveToNext());
                    logIn(nfcUsername);
                    setValueFirebase(nfcUsername);
                    if(isAdmin(nfcUsername)) {
                        setAlarm("0");
                    }
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }else {
                    Toast.makeText(getApplicationContext(), "Access Denied.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected boolean isAdmin(String username) {
        Database db = new Database(LoginActivity.this);
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
        Cursor resAdmin = sqLiteDatabase.rawQuery("SELECT * FROM admins WHERE username = '" + username + "'", null);
        if(resAdmin.getCount() != 0) {
            Toast.makeText(getApplicationContext(), "Admin account.", Toast.LENGTH_LONG).show();
            return true;
        }else {
            Toast.makeText(getApplicationContext(), "User account.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    protected void setValueFirebase(String username) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("nfc").child("nfc_username");
        ref.setValue(username);
    }

    protected void setBeepFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("beep_sound");
        ref.setValue("1");
    }

    protected void setAlarm(String value) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("alarm");
        ref.setValue(value);
    }

    protected void logIn(String username) {
        Session.username = username;
        Session.isLoggedIn = true;
        Session.isAdmin = isAdmin(username);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("nfc");
        ref.child("nfc_username").setValue(username);
        setBeepFirebase();
    }
}
