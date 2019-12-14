package com.iot.iotgoogleassistant;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.regex.Pattern;

public class AddNFCActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!Session.isLoggedIn) {
            Toast.makeText(getApplicationContext(), "Unauthorized access.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(AddNFCActivity.this, LoginActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_nfc);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.add_nfc_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        CardView btn = findViewById(R.id.add_nfc_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cardID = ((TextView)findViewById(R.id.add_nfc_id)).getText().toString();
                String pattern = "[0-9A-F]{1}[0-9A-F]{1}:[0-9A-F]{1}[0-9A-F]{1}:[0-9A-F]{1}[0-9A-F]{1}:[0-9A-F]{1}[0-9A-F]{1}";
                boolean isMatch = Pattern.matches(pattern, cardID);
                if(isMatch) {
                    Database db = new Database(AddNFCActivity.this);
                    db.insertDataNFC(cardID, Session.username);
                    db.close();
                    Intent i = new Intent(AddNFCActivity.this, AccountControlActivity.class);
                    startActivity(i);
                    Toast.makeText(getApplicationContext(), "Operation successfull.", Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(), "Card id format is invalid.", Toast.LENGTH_LONG).show();
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
