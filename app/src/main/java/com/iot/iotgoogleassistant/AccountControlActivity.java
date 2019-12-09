package com.iot.iotgoogleassistant;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AccountControlActivity extends AppCompatActivity {

    HashSet<String> hashUsername;
    List<String> listUsername;
    Spinner spinnerUser;
    TextView isAdmin;
    TextView giveRevokeAdminText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!Session.isLoggedIn) {
            Toast.makeText(getApplicationContext(), "Unauthorized access.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(AccountControlActivity.this, LoginActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_control);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.account_control_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        spinnerUser =  findViewById(R.id.users_spinner);
        isAdmin = findViewById(R.id.is_admin_textview);
        giveRevokeAdminText = findViewById(R.id.account_control_admin_user_btn_textView);
        hashUsername = new HashSet<>();
        getUsers();
        addListener();
    }

    protected void getUsers() {
        hashUsername.clear();
        Database db = new Database(AccountControlActivity.this);
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
        Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM users WHERE username != '" + Session.username + "'", null);
        if (c.moveToFirst()){
            do {
                String username = c.getString(0);
                hashUsername.add(username);
            } while(c.moveToNext());
        }
        c.close();
        db.close();

        listUsername = new ArrayList<>(hashUsername);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, listUsername);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUser.setAdapter(adapter);
    }

    protected void addListener() {
        spinnerUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Database db = new Database(AccountControlActivity.this);
                SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                Cursor c = sqLiteDatabase.rawQuery("SELECT * FROM admins WHERE username = '" + spinnerUser.getSelectedItem().toString() + "'", null);
                if(c.getCount() == 0){
                    isAdmin.setText("Is Admin: NO");
                    giveRevokeAdminText.setText("Give Admin Rights");

                }else {
                    isAdmin.setText("Is Admin: YES");
                    giveRevokeAdminText.setText("Revoke Admin Rights");
                }
                c.close();
                db.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        CardView btn1 = findViewById(R.id.account_control_add_user_btn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Session.isAdmin){
                    Intent i = new Intent(AccountControlActivity.this, RegisterActivity.class);
                    startActivity(i);
                }else {
                    Toast.makeText(getApplicationContext(), "You're not admin.", Toast.LENGTH_LONG).show();
                }
            }
        });

        CardView btn2 = findViewById(R.id.account_control_delete_user_btn);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Session.isAdmin){
                    Database db = new Database(AccountControlActivity.this);
                    SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                    if(isAdmin.getText().toString().endsWith("YES")){
                        sqLiteDatabase.execSQL("DELETE FROM admins WHERE username = '" + spinnerUser.getSelectedItem().toString() + "'");
                        sqLiteDatabase.execSQL("DELETE FROM users WHERE username = '" + spinnerUser.getSelectedItem().toString() + "'");
                    }else {
                        sqLiteDatabase.execSQL("DELETE FROM users WHERE username = '" + spinnerUser.getSelectedItem().toString() + "'");
                    }
                    sqLiteDatabase.close();
                }else {
                    Toast.makeText(getApplicationContext(), "You're not admin.", Toast.LENGTH_LONG).show();
                }
                getUsers();
            }
        });

        CardView btn3 = findViewById(R.id.account_control_admin_user_btn);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Session.isAdmin){
                    Database db = new Database(AccountControlActivity.this);
                    SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();
                    if(isAdmin.getText().toString().endsWith("YES")){
                        sqLiteDatabase.execSQL("DELETE FROM admins WHERE username = '" + spinnerUser.getSelectedItem().toString() + "'");
                    }else {
                        db.insertDataAdmin(spinnerUser.getSelectedItem().toString());
                    }
                    sqLiteDatabase.close();
                }else {
                    Toast.makeText(getApplicationContext(), "You're not admin.", Toast.LENGTH_LONG).show();
                }
                getUsers();
            }
        });

        CardView btn4 = findViewById(R.id.account_control_logout_user_btn);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session.isAdmin = false;
                Session.isLoggedIn = false;
                Session.username = "";
                Intent i = new Intent(AccountControlActivity.this, LoginActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI(getWindow());
    }

    private AlphaAnimation buttonClick = new AlphaAnimation(1F, 0.2F);

    public void onClickBtn(View v) {
        v.startAnimation(buttonClick);
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
}
