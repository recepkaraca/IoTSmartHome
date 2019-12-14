package com.iot.iotgoogleassistant;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
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

public class AirConditionControlActivity extends AppCompatActivity {

    TextView minValueTextView;
    TextView maxValueTextView;
    TextView minValueLabel;
    TextView maxValueLabel;
    TextView measuredValueLabel;
    TextView humidityValueLabel;
    TextView isWorkingLabel;
    Double minValue, maxValue, measuredValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!Session.isLoggedIn) {
            Toast.makeText(getApplicationContext(), "Unauthorized access.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(AirConditionControlActivity.this, LoginActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_condition_control);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.air_condition_control_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        minValueTextView = findViewById(R.id.min_temperature_text);
        maxValueTextView = findViewById(R.id.max_temperature_text);
        minValueLabel = findViewById(R.id.air_condition_control_min_label);
        maxValueLabel = findViewById(R.id.air_condition_control_max_label);
        measuredValueLabel = findViewById(R.id.air_condition_control_temperature_label);
        humidityValueLabel = findViewById(R.id.air_condition_control_humidity_label);
        isWorkingLabel = findViewById(R.id.air_condition_control_is_working_label);
        minValue = maxValue = measuredValue = 0.0;

        addListener();

        CardView btn1 = findViewById(R.id.air_condition_ok_btn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMinMaxValue(minValueTextView.getText().toString(), maxValueTextView.getText().toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isWorking();
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
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference().child("temperature");
        reference.child("min_temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                minValueLabel.setText("Minimum temperature: " + value + "\u00B0" + "C");
                minValue = Double.parseDouble(value);
                isWorking();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.child("max_temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                maxValueLabel.setText("Maximum temperature: " + value + "\u00B0" + "C");
                maxValue = Double.parseDouble(value);
                isWorking();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.child("current_temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                measuredValueLabel.setText("Current temperature: " + value + "\u00B0" + "C");
                measuredValue = Double.parseDouble(value);
                isWorking();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.child("humidity").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                humidityValueLabel.setText("Current humidity: " + value + "\u00B0" + "C");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected void setMinMaxValue(String min, String max) {
        if(min.length() == 0 || max.length() == 0){
            Toast.makeText(getApplicationContext(), "Max and min value cannot be empty.", Toast.LENGTH_LONG).show();
        }else{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference refMin = database.getReference("temperature").child("min_temperature");
            DatabaseReference refMax = database.getReference("temperature").child("max_temperature");

            if(Double.parseDouble(min) <= Double.parseDouble(max)) {
                refMin.setValue(min);
                refMax.setValue(max);
                Toast.makeText(getApplicationContext(), "Saved.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Max value must be greater than min value or equal.", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void isWorking() {
        if(maxValue >= measuredValue && measuredValue >= minValue) {
            isWorkingLabel.setText("Is air condition working: " + "YES");
        } else {
            isWorkingLabel.setText("Is air condition working: " + "NO");
        }
    }
}
