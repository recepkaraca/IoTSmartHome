package com.iot.iotgoogleassistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE = 100;
    TextView textView;
    TextView textView2;
    TextView temperature;
    Switch relay1Switch;
    Switch relay2Switch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        textView2 = findViewById(R.id.textView2);
        temperature = findViewById(R.id.temperature);
        relay1Switch = findViewById(R.id.relay1);
        relay2Switch = findViewById(R.id.relay2);
        ImageView speak = findViewById(R.id.speak);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Need to speak");
                try {
                    startActivityForResult(intent, REQ_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        addListener();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    textView.setText(result.get(0).toString());
                    setValueFirebase(result.get(0).toString());
                }
                break;
            }
        }
    }

    protected void addListener() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        final DatabaseReference reference = firebaseDatabase.getReference();
        reference.child("relay1").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                if (value.compareToIgnoreCase("1") == 0)  relay1Switch.setChecked(true);
                else    relay1Switch.setChecked(false);
                handleChange(dataSnapshot.getKey(), value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.child("relay2").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                if (value.compareToIgnoreCase("1") == 0)  relay2Switch.setChecked(true);
                else    relay2Switch.setChecked(false);
                handleChange(dataSnapshot.getKey(), value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.child("temperature").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue().toString();
                temperature.setText("Temperature: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        relay1Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked)   setValueFirebase("turn on light one");
                else    setValueFirebase("turn off light one");
            }
        });

        relay2Switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position
                if(isChecked)   setValueFirebase("turn on light two");
                else    setValueFirebase("turn off light two");
            }
        });
    }

    protected void handleChange(String ref, String value) {
        textView2.setText(ref + ", " + value);
    }

    protected void setValueFirebase(String str) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(str.compareToIgnoreCase("turn on light one") == 0 || str.compareToIgnoreCase("turn on light 1") == 0) {
            DatabaseReference ref = database.getReference("relay1");
            ref.setValue("1");
        }else if(str.compareToIgnoreCase("turn off light one") == 0 || str.compareToIgnoreCase("turn off light 1") == 0) {
            DatabaseReference ref = database.getReference("relay1");
            ref.setValue("0");
        }
        else if(str.compareToIgnoreCase("turn on light two") == 0 || str.compareToIgnoreCase("turn on light 2") == 0) {
            DatabaseReference ref = database.getReference("relay2");
            ref.setValue("1");
        }else if(str.compareToIgnoreCase("turn off light two") == 0 || str.compareToIgnoreCase("turn off light 2") == 0) {
            DatabaseReference ref = database.getReference("relay2");
            ref.setValue("0");
        }
    }
    }
