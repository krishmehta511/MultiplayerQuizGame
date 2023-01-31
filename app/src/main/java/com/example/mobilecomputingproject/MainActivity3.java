package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity3 extends AppCompatActivity {
    FirebaseDatabase database;

    String roomId = "";
    String playerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        database = FirebaseDatabase.getInstance("https://mobilecomputingproject-d70e0-default-rtdb.asia-southeast1.firebasedatabase.app");

        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("player_name", "");

        Bundle extras = getIntent().getExtras();
        roomId = extras.getString("room_id");

        //On pressing back if host
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int len = roomId.length();
                String hostName = roomId.substring(0, len - 7);
                if(playerName.equals(hostName)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity3.this);
                    builder.setCancelable(true)
                            .setTitle("Exit?")
                            .setMessage("You are the room host. On exiting the room will get deleted")
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                DatabaseReference ref = database.getReference("rooms/" + roomId);
                                ref.removeValue();
                                goToPrevPage();
                                finish();
                            })
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);


    }

    private void goToPrevPage(){
        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
        startActivity(intent);
    }
}