package com.iot.iotgoogleassistant;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class AssistantActivity extends AppCompatActivity {

    private final int REQ_CODE = 100;
    TextView assistantLabel;
    ImageButton infoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(!Session.isLoggedIn) {
            Toast.makeText(getApplicationContext(), "Unauthorized access.", Toast.LENGTH_LONG).show();
            Intent i = new Intent(AssistantActivity.this, LoginActivity.class);
            startActivity(i);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);

        AnimationDrawable animationDrawable = ((AnimationDrawable) findViewById(R.id.assistant_root_layout).getBackground());
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(2000);
        animationDrawable.start();

        assistantLabel = findViewById(R.id.assistant_label);
        infoBtn = findViewById(R.id.asssitant_info_btn);

        CardView speak = findViewById(R.id.speak_btn);
        speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String language = "tr-TR";
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
                intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, language);
                intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, language);
                intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, language);
                intent.putExtra(RecognizerIntent.EXTRA_RESULTS, language);
                try {
                    startActivityForResult(intent, REQ_CODE);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(getApplicationContext(),
                            "Sorry your device not supported",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AssistantActivity.this)
                        .setTitle("Voice Commands")
                        .setMessage("Röle 1'i aç.\nRöle 1'i kapat.\nRöle 2'yi aç.\nRöle 2'yi kapat.\n" +
                                "LCD ışığını aç.\nLCD ışığını kapat.\nHareket sensörünü aç.\n" +
                                "Hareket sensörünü kapat.\nKlimayi ... derece ile ... derece arasına ayarla.\n" +
                                "Çıkış yap.")

                        .setPositiveButton(android.R.string.yes, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    assistantLabel.setText(result.get(0).toString());
                    setValueFirebase(result.get(0).toString());
                }
                break;
            }
        }
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

    protected void setValueFirebase(String str) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        if(str.compareToIgnoreCase("röle 1'i aç") == 0 || str.compareToIgnoreCase("röle biri aç") == 0) {
            DatabaseReference ref = database.getReference("relay");
            ref.child("relay1").setValue("1");
        }else if(str.compareToIgnoreCase("röle 1'i kapat") == 0 || str.compareToIgnoreCase("röle biri kapat") == 0) {
            DatabaseReference ref = database.getReference("relay");
            ref.child("relay1").setValue("0");
        }else if(str.compareToIgnoreCase("röle 2'yi aç") == 0 || str.compareToIgnoreCase("röle ikiyi aç") == 0) {
            DatabaseReference ref = database.getReference("relay");
            ref.child("relay2").setValue("1");
        }else if(str.compareToIgnoreCase("röle 2'yi kapat") == 0 || str.compareToIgnoreCase("röle ikiyi kapat") == 0) {
            DatabaseReference ref = database.getReference("relay");
            ref.child("relay2").setValue("0");
        }else if(str.compareToIgnoreCase("lcd ışığını aç") == 0){
            DatabaseReference ref = database.getReference("lcd");
            ref.child("lcd_backlight").setValue("1");
        }else if(str.compareToIgnoreCase("lcd ışığını kapat") == 0) {
            DatabaseReference ref = database.getReference("lcd");
            ref.child("lcd_backlight").setValue("0");
        }else if(str.compareToIgnoreCase("hareket sensörünü aç") == 0) {
            DatabaseReference ref = database.getReference("pir");
            ref.child("pir_enabled").setValue("1");
            ref.child("pir_relay1").setValue("1");
        }else if(str.compareToIgnoreCase("hareket sensörünü kapat") == 0) {
            DatabaseReference ref = database.getReference("pir");
            ref.child("pir_enabled").setValue("0");
            ref.child("pir_relay1").setValue("1");
        }else if(str.toLowerCase().startsWith("klima") || str.toLowerCase().startsWith("klimayı")) {
            str = str.replaceAll("[^0-9]+", " ");
            String[] strArr = str.trim().split(" ");
            if(strArr.length == 2){
                System.out.println("str[0] -> " + strArr[0]);
                System.out.println("str[0] -> " + strArr[1]);
                if(Integer.parseInt(strArr[0]) <= Integer.parseInt(strArr[1])){
                    DatabaseReference ref = database.getReference("temperature");
                    ref.child("min_temperature").setValue(strArr[0]);
                    ref.child("max_temperature").setValue(strArr[1]);
                }
            }
        }
        else if(str.compareToIgnoreCase("çıkış yap") == 0) {
            Session.isAdmin = false;
            Session.isLoggedIn = false;
            Session.username = "";
            clearNFCFirebase();
            setBeepFirebase();
            Intent i = new Intent(AssistantActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }

    protected void clearNFCFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference refID = database.getReference("nfc").child("nfc_id");
        DatabaseReference refUsername = database.getReference("nfc").child("nfc_username");
        refID.setValue("");
        refUsername.setValue("");
    }

    protected void setBeepFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("beep_sound");
        ref.setValue("1");
    }
}
