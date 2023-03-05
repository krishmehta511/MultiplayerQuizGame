package com.example.mobilecomputingproject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    TextView userNameText;
    Button play_btn;
    Button leaderboard_btn;
    EditText user_name;
    Button create_user;
    String player_name = "";
    FirebaseDatabase database;
    DatabaseReference playerRef;
    int avatar = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide StatusBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        userNameText = findViewById(R.id.userNameText);
        play_btn = findViewById(R.id.create_room);
        leaderboard_btn = findViewById(R.id.leaderboard);

        String play_html = "<font color=#ffffff>play</font><font color=#006b38> ;</font>";
        String leaderboard_html = "<font color=#ffffff>leaderboard</font><font color=#006b38> ;</font>";

        play_btn.setText(Html.fromHtml(play_html, Html.FROM_HTML_MODE_COMPACT));
        leaderboard_btn.setText(Html.fromHtml(leaderboard_html, Html.FROM_HTML_MODE_COMPACT));

        database = FirebaseDatabase.getInstance();

        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        player_name = preferences.getString("player_name", "");
        if(player_name.equals("")){
            showDialog();
        } else {
            userNameText.setText("Hi, " + player_name);
        }

        play_btn.setOnClickListener(view ->{
            startActivity(new Intent(MainActivity.this, MainActivity2.class));
            finish();
        });

        leaderboard_btn.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, MainActivity5.class));
            finish();
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);
    }

    private void showDialog(){
        Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.signin_dialog);
        dialog.getWindow()
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT
                        , ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(false);

        TextView name = dialog.findViewById(R.id.name);
        user_name = dialog.findViewById(R.id.username);
        create_user = dialog.findViewById(R.id.button2);
        ImageView m = dialog.findViewById(R.id.male);
        ImageView f = dialog.findViewById(R.id.female);




        m.setOnClickListener(view -> {
            m.getLayoutParams().height = dpToPx(150);
            m.requestLayout();
            f.getLayoutParams().height = dpToPx(120);
            f.requestLayout();
            avatar = 1;
        });

        f.setOnClickListener(view -> {
            m.getLayoutParams().height = dpToPx(120);
            m.requestLayout();
            f.getLayoutParams().height = dpToPx(150);
            f.requestLayout();
            avatar = 2;
        });

        create_user.setOnClickListener(view -> {
            player_name = user_name.getText().toString();
            userNameText.setText("Hi, " + player_name);
            if(!player_name.equals("")){
                database.getReference().child("players/").child(player_name)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            name.setText("*Username already exists.");
                            user_name.setText("");
                        } else {
                            if (avatar == 0){
                                name.setText("Please select an avatar.");
                            } else {
                                addNewPlayer(avatar);
                                dialog.dismiss();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Error :(", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            } else {
                name.setText("*Enter valid username.");
            }
        });
        dialog.show();
    }

    public int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private void addNewPlayer(int avatar){
        Map<String, Object> newData = new HashMap<>();
        newData.put("points", 0);
        newData.put("avatar", avatar);
        playerRef = database.getReference("players/" + player_name);
        SharedPreferences preferences = getSharedPreferences("PREFS", 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("player_name", player_name);
        editor.apply();
        playerRef.setValue(newData);
    }
}