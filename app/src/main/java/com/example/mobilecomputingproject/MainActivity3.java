package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        //Hide Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        playersGV = findViewById(R.id.player_grid);
        playerAvatar = playersGV.findViewById(R.id.player_avatar);
        playerNameTxt = playersGV.findViewById(R.id.player_name);
        startGame = findViewById(R.id.start_game_btn);
        remove = findViewById(R.id.remove_btn);
        totalPlayers = findViewById(R.id.total_players);


        database = FirebaseDatabase.getInstance();

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
            if(players.size() > 1){
                database.getReference("rooms/"+roomId+"/Game Status").setValue("Started");
            } else {
                Toast.makeText(this, "Need at least 2 players to start game.", Toast.LENGTH_SHORT).show();
            }
        });


        playersGV.setOnItemClickListener((adapterView, view, i, l) -> {
            if(playerName.equals(hostName)){
                remove.setEnabled(true);
                playerToRemove = players.get(i);
                remove.setText("Remove " + playerToRemove);
                remove.setVisibility(View.VISIBLE);
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
    }

    private void updateAdapter(ArrayList<String> players){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.grid_item, R.id.player_name, players);
        playersGV.setAdapter(adapter);
        totalPlayers.setText("Total Players: " + players.size());
    }

    private void allEventsHandler(){
        ref = database.getReference("rooms").child(roomId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                players.clear();
                if(snapshot.exists()){
                    showPlayersNew(snapshot);
                    goToGameScreenNew(snapshot);
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

    private void showPlayersNew(DataSnapshot snapshot){
        for(DataSnapshot player: snapshot.getChildren()){
            String entry = player.getKey();
            assert entry != null;
            if(entry.equals("Host")){
                for(DataSnapshot p: snapshot.child("Host").getChildren()){
                    players.add(p.getKey());
                }
            } else if(entry.equals("Players")){
                for (DataSnapshot p: snapshot.child("Players").getChildren()){
                    players.add(p.getKey());
                }
            }
        }
        updateAdapter(players);
    }

    private void goToGameScreenNew(DataSnapshot snapshot){
        if(snapshot.child("Game Status").exists()){
            String status = snapshot.child("Game Status").getValue().toString();
            if (status.equals("Started")){
                startActivity(new Intent(MainActivity3.this, MainActivity4.class)
                        .putExtra("room_id", roomId)
                        .putExtra("host_name", hostName));
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
                            .setPositiveButton("Yes", (dialogInterface, i1) -> {
                                database.getReference("rooms/" + roomId)
                                        .removeValue();
                            })
                            .setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    builder.setCancelable(true)
                            .setTitle("Exit?")
                            .setMessage("Do you wish to exit the room?")
                            .setPositiveButton("Yes", (dialogInterface, i1) -> {
                                database.getReference("rooms/" + roomId + "/Players/" + playerName)
                                        .removeValue();
                            })
                            .setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };
    }
}
