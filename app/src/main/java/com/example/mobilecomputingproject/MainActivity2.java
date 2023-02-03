package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.SpannedString;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

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
    TextView rooms_title;
    Button create_room;
    Button refresh;
    String player_name = "";
    String roomId = "";

    FirebaseDatabase database;
    DatabaseReference roomRef;
    DatabaseReference roomsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Hide Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //database instance
        database = FirebaseDatabase.getInstance("https://mobilecomputingproject-d70e0-default-rtdb.asia-southeast1.firebasedatabase.app");

        listView = findViewById(R.id.rooms_list);
        rooms_title = findViewById(R.id.rooms_title);
        create_room = findViewById(R.id.create_room);
        refresh = findViewById(R.id.refresh_button);

        room_list = new ArrayList<>();

        //Styling
        String room_string = "<font color=#ffffff>Rooms</font><font color=#006b38> (){</font>";
        String create_string = "<font color=#006b38>// </font><font color=#ffffff>Create Room</font>";
        rooms_title.setText(Html.fromHtml(room_string, Html.FROM_HTML_MODE_COMPACT));
        create_room.setText(Html.fromHtml(create_string, Html.FROM_HTML_MODE_COMPACT));

        //Getting player name and setting room name
        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        player_name = prefs.getString("player_name", "");

        //Create a new room
        create_room.setOnClickListener(view -> createRoom());

        //Join a room
        listView.setOnItemClickListener((adapterView, view, i, l) -> joinRoom(i));

        //Refresh
        refresh.setOnClickListener(view -> getRooms());

        //On pressing back
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);
        //fetch all existing rooms
        getRooms();
    }

    private void joinRoom(int i){
        roomId = room_list.get(i);
        roomRef = database.getReference("rooms/" + roomId + "/Players").child(player_name);
        roomRef.setValue("");
        goToRoomPage();
    }

    private void createRoom(){
        Map<String, Object> roomData = new HashMap<>();
        roomData.put(player_name, "");
        roomRef = database.getReference("rooms/" + player_name + "'s Room/").child("Host");
        roomRef.setValue(roomData);
        roomId = player_name + "'s Room";
        database.getReference("rooms/" + player_name + "'s Room/").child("Game Status")
                        .setValue("Not Started");
        goToRoomPage();
    }

    private void getRooms(){
        roomsRef = database.getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                room_list.clear();
                Iterable<DataSnapshot> rooms = snapshot.getChildren();
                for(DataSnapshot dataSnapshot: rooms) {
                    String roomName = dataSnapshot.getKey();
                    room_list.add(roomName);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity2.this, R.layout.list_item, room_list);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void goToRoomPage(){
        Intent intent = new Intent(getApplicationContext(), MainActivity3.class);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
        finish();
    }


}