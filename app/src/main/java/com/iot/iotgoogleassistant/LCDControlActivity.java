package com.iot.iotgoogleassistant;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LCDControlActivity extends AppCompatActivity {

    Switch lcdBacklightSwitch;
    TextView lcdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!Session.isLoggedIn) {
            Toast.makeText(getApplicationContext(), "Unauthorized access.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(LCDControlActivity.this, LoginActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lcd_control);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.lcd_control_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        lcdBacklightSwitch = findViewById(R.id.lcd_backlight_control_switch);
        lcdTextView = findViewById(R.id.lcd_value_text);

        addListener();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI(getWindow());
    }

    protected void addListener() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        reference.child("lcd").child("lcd_backlight").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                if (value.compareToIgnoreCase("1") == 0) lcdBacklightSwitch.setChecked(true);
                else lcdBacklightSwitch.setChecked(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        lcdBacklightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)   setValueFirebase("lcd_backlight", 1);
                else    setValueFirebase("lcd_backlight", 0);
            }
        });

        reference.child("lcd").child("lcd_value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                lcdTextView.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        CardView btn1 = findViewById(R.id.lcd_control_ok_btn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setStringValueFirebase("lcd_value", lcdTextView.getText().toString());
            }
        });

    }

    protected void setValueFirebase(String lcdControl, int value) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("lcd").child(lcdControl);
        ref.setValue(Integer.toString(value));
    }

    protected void setStringValueFirebase(String lcdControl, String value) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("lcd").child(lcdControl);
        ref.setValue(value);
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
