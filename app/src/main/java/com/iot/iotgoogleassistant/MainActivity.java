package com.iot.iotgoogleassistant;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!Session.isLoggedIn) {
            Toast.makeText(getApplicationContext(), "Unauthorized access.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.main_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        CardView btn1 = findViewById(R.id.relay_control_btn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, RelayControlActivity.class);
                startActivity(i);
            }
        });

        CardView btn2 = findViewById(R.id.air_condition_control_btn);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AirConditionControlActivity.class);
                startActivity(i);
            }
        });

        CardView btn3 = findViewById(R.id.pir_control_btn);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, PirControlActivity.class);
                startActivity(i);
            }
        });

        CardView btn4 = findViewById(R.id.assistant_control_btn);
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AssistantActivity.class);
                startActivity(i);
            }
        });

        CardView btn5 = findViewById(R.id.lcd_control_btn);
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LCDControlActivity.class);
                startActivity(i);
            }
        });

        CardView btn6 = findViewById(R.id.account_control_btn);
        btn6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, AccountControlActivity.class);
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
