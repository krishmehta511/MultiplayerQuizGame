package com.example.mobilecomputingproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MainActivity3 extends AppCompatActivity {
    FirebaseDatabase database;
    String roomId = "";
    String hostName = "";
    String playerName = "";
    GridView playersGV;
    ImageView playerAvatar;
    TextView playerNameTxt;
    Button startGame;
    Button remove;
    String playerToRemove = "";
    TextView totalPlayers;
    ArrayList<String> players;
    DatabaseReference ref;
    ValueEventListener valueEventListener;
    Map<String, Object> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        //Hide Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Initialize Database
        database = FirebaseDatabase.getInstance();

        playersGV = findViewById(R.id.player_grid);
        playerAvatar = playersGV.findViewById(R.id.player_avatar);
        playerNameTxt = playersGV.findViewById(R.id.player_name);
        startGame = findViewById(R.id.start_game_btn);
        remove = findViewById(R.id.remove_btn);
        totalPlayers = findViewById(R.id.total_players);

        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("player_name", "");

        //To get room id from prev activity
        Bundle extras = getIntent().getExtras();
        roomId = extras.getString("room_id");
        hostName = roomId.substring(0, roomId.length() - 7);

        //Custom back function
        OnBackPressedCallback callback = customBackFunc();
        getOnBackPressedDispatcher().addCallback(callback);

        players = new ArrayList<>();
        updateAdapter(players);

        //Host Privileges
        if(playerName.equals(hostName)){
            startGame.setEnabled(true);
            startGame.setVisibility(View.VISIBLE);
        }

        startGame.setOnClickListener(view -> {
            if(players.size() >= 0){
                database.getReference("rooms/"+roomId+"/Game Status").setValue("Started");
                createQuestions();
            } else {
                Toast.makeText(this, "Need at least 2 players to start game.", Toast.LENGTH_SHORT).show();
            }
        });


        playersGV.setOnItemClickListener((adapterView, view, i, l) -> {
            if(playerName.equals(hostName)){
                remove.setEnabled(true);
                playerToRemove = players.get(i);
                if(!playerToRemove.equals(hostName)){
                    remove.setText(String.format("Remove %s", playerToRemove));
                    remove.setVisibility(View.VISIBLE);
                }
            }
        });

        remove.setOnClickListener(view -> {
            database.getReference("rooms/"+roomId+"/Players/"+playerToRemove).removeValue();
            playerToRemove = "";
            remove.setEnabled(false);
            remove.setVisibility(View.INVISIBLE);
        });

        allEventsHandler();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(valueEventListener);
        Log.d("xxxxx", "Main Activity 3 listener removed.");
    }

    private void createQuestions(){
        questions = new HashMap<>();
        for (int i = 0; i < 20; i++) {
            questions.clear();
            int number = new Random().nextInt(9) + 1;
            ArrayList<Integer> multipliers = new ArrayList<>();
            while (multipliers.size() < 4){
                int mul = new Random().nextInt(10) + 1;
                if (!multipliers.contains(mul)){
                    multipliers.add(mul);
                }
            }
            int selected_mul = multipliers.get(new Random().nextInt(4));
            ArrayMap<String, String> options = new ArrayMap<>();
            for (int j = 0; j < 4; j++) {
                options.put(String.valueOf(j+1), String.valueOf(number * multipliers.get(j)));
            }
            questions.put(number + "  x  " + selected_mul, options);
            database.getReference("rooms").child(roomId).child("Questions").push()
                    .setValue(questions);
        }
    }

    private void updateAdapter(ArrayList<String> players){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.grid_item, R.id.player_name, players);
        playersGV.setAdapter(adapter);
        totalPlayers.setText(String.format("Total Players: %s", players.size()));
    }

    private void allEventsHandler(){
        Log.d("xxxxx", "Main Activity 3 listener started.");
        ref = database.getReference("rooms").child(roomId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("xxxxx", "Main Activity 3 listener called.");
                players.clear();
                if(snapshot.exists()){
                    showPlayers(snapshot);
                    goToGameScreen(snapshot);
                    doesPlayerExistInRoom(snapshot);
                } else if (!snapshot.exists()) {
                    goToPrevPage();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

    private void showPlayers(DataSnapshot snapshot){
        for(DataSnapshot player: snapshot.child("Players").getChildren()){
            players.add(player.getKey());
        }
        updateAdapter(players);
    }

    private void goToGameScreen(DataSnapshot snapshot){
        if(snapshot.child("Game Status").exists()){
            String status = (String) snapshot.child("Game Status").getValue();
            if (status != null && status.equals("Started")){
                ref.removeEventListener(valueEventListener);
                Log.d("xxxxx", "Main Activity 3 listener removed.");
                startActivity(new Intent(MainActivity3.this, MainActivity4.class)
                        .putExtra("room_id", roomId));
                finish();
            }
        }
    }

    private void doesPlayerExistInRoom(DataSnapshot snapshot){
        if(!playerName.equals(hostName)){
            if(!snapshot.child("Players").child(playerName).exists()){
                players.remove(playerName);
                updateAdapter(players);
                goToPrevPage();
            }
        }
    }

    private void goToPrevPage(){
        ref.removeEventListener(valueEventListener);
        Log.d("xxxxx", "Main Activity 3 listener removed.");
        Intent intent = new Intent(MainActivity3.this, MainActivity2.class);
        startActivity(intent);
        finish();
    }

    private OnBackPressedCallback customBackFunc(){
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity3.this);
                if(playerName.equals(hostName)){
                    builder.setCancelable(true)
                            .setTitle("Exit?")
                            .setMessage("You are the room host. On exiting the room will get deleted")
                            .setPositiveButton("Yes", (dialogInterface, i1) ->
                                    database.getReference("rooms/" + roomId)
                                    .removeValue())
                            .setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    builder.setCancelable(true)
                            .setTitle("Exit?")
                            .setMessage("Do you wish to exit the room?")
                            .setPositiveButton("Yes", (dialogInterface, i1) ->
                                    database.getReference("rooms/" + roomId + "/Players/" + playerName)
                                    .removeValue())
                            .setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };
    }
}
