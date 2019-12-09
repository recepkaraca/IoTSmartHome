package com.iot.iotgoogleassistant;

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

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // DELETE THIS
        Session.isLoggedIn = true;
        Session.isAdmin = true;
        Session.username = "recepkaraca";
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.login_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();


        //Database db = new Database(LoginActivity.this);
        //String username = "recepkaraca";
        //String password = "123456";
        //db.insertData(username, password);
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
                }
                else {
                    Toast.makeText(getApplicationContext(), "Successfully logged in.", Toast.LENGTH_LONG).show();
                    Session.username = username;
                    Session.isLoggedIn = true;
                    Cursor resAdmin = sqLiteDatabase.rawQuery("SELECT * FROM admins WHERE username = '" + username + "'", null);
                    if(resAdmin.getCount() != 0) {
                        Session.isAdmin = true;
                        Toast.makeText(getApplicationContext(), "Parent account.", Toast.LENGTH_LONG).show();
                    }else {
                        Session.isAdmin = false;
                        Toast.makeText(getApplicationContext(), "Ordinary account.", Toast.LENGTH_LONG).show();
                    }
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                }
            }
        });
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
}
