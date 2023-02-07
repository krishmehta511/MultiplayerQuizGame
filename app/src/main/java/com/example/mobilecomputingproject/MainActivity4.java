package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MainActivity4 extends AppCompatActivity {
    String player_name;
    String host_name;
    Boolean isHost;
    String room_id;
    FirebaseDatabase database;
    DatabaseReference ref;
    ValueEventListener valueEventListener;
    RecyclerView recyclerView;
    ArrayList<String> players = new ArrayList<>();
    ArrayList<Integer> player_scores = new ArrayList<>();
    ArrayList<String> player_avatar = new ArrayList<>();
    SQLiteDatabase db;
    final String db_name = "Leaderboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        //Remove Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Get player name, host name and room id
        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        player_name = prefs.getString("player_name", "");
        Bundle bundle = getIntent().getExtras();
        host_name = bundle.getString("host_name");
        room_id = bundle.getString("room_id");

        isHost = player_name.equals(host_name);

        //Initialize Firebase database
        database = FirebaseDatabase.getInstance();

        //Handle back press based on host or user
        OnBackPressedCallback callback = customBackFunc();
        getOnBackPressedDispatcher().addCallback(callback);

        allEventsHandler();

        players.add("P1");
        players.add("P2");
        players.add("P3");
        players.add("P4");
        players.add("P5");

        player_scores.add(4);
        player_scores.add(2);
        player_scores.add(10);
        player_scores.add(6);
        player_scores.add(9);

        player_avatar.add("F");
        player_avatar.add("M");
        player_avatar.add("F");
        player_avatar.add("M");
        player_avatar.add("F");



        initRecycler();

        gameEndDialog();

    }

    public void updateDatabase(){
        db = openOrCreateDatabase(db_name, Context.MODE_PRIVATE, null);

        //create player_name and player_score table
        HashMap<String, Integer> hm = new HashMap<>();
        for(int i = 0; i < players.size(); i++){
            hm.put(players.get(i), player_scores.get(i));
        }

        //Sort map according to player scores
        HashMap<String, Integer> sortedHm = sortMap(hm);

        //For winner update win and for rest update loss
        for (Map.Entry<String, Integer> entry: sortedHm.entrySet()){
            Cursor cursor = db.rawQuery("Select * from Leaderboard where name='"+entry.getKey()+"'", null);
            Log.d("xxxxx", cursor.getString(0) + " " + cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3));
            if(entry.getValue() == 10){
                Log.d("xxxxx", "winner");
                if (cursor.moveToFirst()){
                    db.execSQL("Update Leaderboard set wins='"+ (cursor.getInt(1) + 1)+"' where name='"+entry.getKey()+"'");
                }
                cursor.close();
            } else {
                Log.d("xxxxx", "winner");
                if (cursor.moveToFirst()){
                    db.execSQL("Update Leaderboard set losses='"+ (cursor.getInt(2) + 1)+"' where name='"+entry.getKey()+"'");
                }
                cursor.close();
            }
            if (cursor.moveToFirst()){
                db.execSQL("Update Leaderboard set points='"+ (cursor.getInt(2) + entry.getValue())+"' where name='"+entry.getKey()+"'");
            }
            cursor.close();
        }

    }

    private HashMap<String, Integer> sortMap(HashMap<String, Integer> hm){
        List<Map.Entry<String, Integer> > list =
                new LinkedList<>(hm.entrySet());

        // Sort the list
        list.sort(Map.Entry.comparingByValue());

        // put data from sorted list to hashmap
        HashMap<String, Integer> temp = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private void initRecycler(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity4.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.live_score);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, players, player_scores, player_avatar);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(valueEventListener);
    }

    private OnBackPressedCallback customBackFunc(){
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity4.this);
                DatabaseReference dRef = database.getReference("rooms").child(room_id);
                if(player_name.equals(host_name)){
                    builder.setCancelable(true)
                            .setTitle("End Game")
                            .setMessage("If the host leaves the game will end and host will " +
                                    "be penalised. Do you want to leave ?")
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton("Yes", ((dialogInterface, i) -> {
                                dRef.child("Game Status").setValue("Not Started");
                            }));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    builder.setCancelable(true)
                            .setTitle("Leave Game")
                            .setMessage("If you leave the game you will be penalised." +
                                    " Do you want to leave ?")
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton("Yes", ((dialogInterface, i) -> {
                                dRef.child("Players").child(player_name).removeValue();
                            }));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };
    }

    private void goToPrevPage(){
        Intent intent = new Intent(MainActivity4.this, MainActivity3.class);
        intent.putExtra("room_id", room_id);
        startActivity(intent);
        finish();
    }

    private void allEventsHandler(){
        ref = database.getReference("rooms").child(room_id);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String status = snapshot.child("Game Status").getValue().toString();
                    if(status.equals("Not Started")){
                        goToPrevPage();
                    }
                    if(!isHost){
                        if(!snapshot.child("Players").child(player_name).exists()){
                            goToPrevPage();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

    private void gameEndDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.game_end_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Button end_game = dialog.findViewById(R.id.game_end_btn);
        ListView standings = dialog.findViewById(R.id.player_rankings);

        ArrayList<String> rankings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i == 0){
                rankings.add((i+1) + ")          P"+(i+1));
            } else {
                rankings.add((i+1) + ")         P"+(i+1));
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item, rankings);
        standings.setAdapter(adapter);

        end_game.setOnClickListener(view -> {
//            startActivity(new Intent(this, MainActivity3.class));
//            finish();
            updateDatabase();
        });

        dialog.show();

    }

}