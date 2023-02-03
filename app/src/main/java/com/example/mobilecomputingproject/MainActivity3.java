package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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


        database = FirebaseDatabase.getInstance("https://mobilecomputingproject-d70e0-default-rtdb.asia-southeast1.firebasedatabase.app");

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
            players.remove(playerToRemove);
            database.getReference("rooms/"+roomId+"/Players/"+playerToRemove).removeValue();
            updateAdapter(players);
            playerToRemove = "";
            remove.setEnabled(false);
            remove.setVisibility(View.INVISIBLE);
            updateTotalPlayers(players.size());
        });

        showPlayers(database, roomId);

        goToGameScreen();

        if(!playerName.equals(hostName)){
            goToPrevScreenIfRemoved();
        }

    }

    private void showPlayers(FirebaseDatabase database,String roomId){
        DatabaseReference prefs = database.getReference("rooms/" + roomId);
        prefs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                players.clear();
                if(snapshot.exists()){
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
                    updateTotalPlayers(players.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity3.this, "Error!!", Toast.LENGTH_SHORT).show();
                goToPrevPage();
            }
        });
    }

    private void updateAdapter(ArrayList<String> players){
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.grid_item, R.id.player_name, players);
        playersGV.setAdapter(adapter);
    }

    private void updateTotalPlayers(int i){
        totalPlayers.setText("Total Players: " + i);
    }

    private void goToPrevPage(){
        Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
        startActivity(intent);
    }

    private void goToGameScreen(){
        DatabaseReference ref = database.getReference("rooms/"+roomId+"/Game Status");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String status = snapshot.getValue().toString();
                if(snapshot.exists()){
                    if(status.equals("Started")){
                        startActivity(new Intent(getApplicationContext(), MainActivity4.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity3.this, "Error!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void goToPrevScreenIfRemoved(){
        DatabaseReference refs = database.getReference("rooms/"+roomId+"/Players/"+playerName);
        refs.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    startActivity(new Intent(getApplicationContext(), MainActivity2.class));
                    finish();
                    Toast.makeText(getApplicationContext(), "You were removed by Host.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private OnBackPressedCallback customBackFunc(){
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int len = roomId.length();
                String hostName = roomId.substring(0, len - 7);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity3.this);
                if(playerName.equals(hostName)){
                    builder.setCancelable(true)
                            .setTitle("Exit?")
                            .setMessage("You are the room host. On exiting the room will get deleted")
                            .setPositiveButton("Yes", (dialogInterface, i1) -> {
                                DatabaseReference ref = database.getReference("rooms/" + roomId);
                                ref.removeValue();
                                goToPrevPage();
                                finish();
                            })
                            .setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    builder.setCancelable(true)
                            .setTitle("Exit?")
                            .setMessage("Do you wish to exit the room?")
                            .setPositiveButton("Yes", (dialogInterface, i1) -> {
                                DatabaseReference ref = database.getReference("rooms/" + roomId + "/" + playerName);
                                ref.removeValue();
                                goToPrevPage();
                                finish();
                            })
                            .setNegativeButton("No", (dialogInterface, i1) -> dialogInterface.cancel());
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };
    }
}
