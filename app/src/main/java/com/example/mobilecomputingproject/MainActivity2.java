package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    ListView listView;
    List<String> room_list;
    Button create_room;
    String player_name = "";
    String roomId = "";
    FirebaseDatabase database;
    ValueEventListener valueEventListener;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Hide Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //database instance
        database = FirebaseDatabase.getInstance();

        listView = findViewById(R.id.rooms_list);
        create_room = findViewById(R.id.create_room);

        room_list = new ArrayList<>();

        //Getting player name and setting room name
        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        player_name = prefs.getString("player_name", "");

        //Create a new room
        create_room.setOnClickListener(view -> createRoom());

        //Join a room
        listView.setOnItemClickListener((adapterView, view, i, l) -> joinRoom(i));

        //On pressing back
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ref.removeEventListener(valueEventListener);
                Log.d("xxxxx", "Main Activity 2 listener removed.");
                Intent intent = new Intent(MainActivity2.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);
        //fetch all existing rooms
        getRooms();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(valueEventListener);
        Log.d("xxxxx", "Main Activity 2 listener removed.");
    }

    private void joinRoom(int i){
        roomId = room_list.get(i);
        database.getReference("rooms/" + roomId + "/Players").child(player_name)
                .setValue(0);
        goToRoomPage();
    }

    private void createRoom(){
        roomId = player_name + "'s Room";
        database.getReference("rooms/" + roomId).child("Game Status")
                        .setValue("Not Started");
        database.getReference("rooms/" + roomId).child("Host")
                .setValue(player_name);
        database.getReference("rooms/" + roomId + "/Players").child(player_name)
                .setValue(0);
        goToRoomPage();
    }

    private void getRooms(){
        Log.d("xxxxx", "Main Activity 2 listener started.");
        ref = database.getReference("rooms");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("xxxxx", "Main Activity 2 listener called.");
                room_list.clear();
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for(DataSnapshot dataSnapshot: rooms) {
                    String roomName = dataSnapshot.getKey();
                    room_list.add(roomName);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity2.this, R.layout.list_item, room_list);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

    private void goToRoomPage(){
        ref.removeEventListener(valueEventListener);
        Log.d("xxxxx", "Main Activity 2 listener removed.");
        Intent intent = new Intent(MainActivity2.this, MainActivity3.class);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
        finish();
    }


}