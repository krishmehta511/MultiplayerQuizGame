package com.example.mobilecomputingproject;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity5 extends AppCompatActivity {
    ListView ldb;
    FirebaseDatabase database;
    ValueEventListener valueEventListener;
    DatabaseReference ref;
    ArrayList<Integer> rankings;
    ArrayList<String> playerNames;
    ArrayList<Integer> points;
    Map<String, ArrayList<Integer>> playerDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        ldb = findViewById(R.id.ldb_list);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                startActivity(new Intent(MainActivity5.this, MainActivity.class));
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(callback);

        //Initializing the database
        database = FirebaseDatabase.getInstance();

    }

    private void initAdapter(Map<String, ArrayList<Integer>> playerDetails){
        for(Map.Entry<String, ArrayList<Integer>> entry: playerDetails.entrySet()){
            playerNames.add(entry.getKey());
            points.add(entry.getValue().get(0));
        }
//        LeaderBoardListItemAdapter adapter =
//                new LeaderBoardListItemAdapter(MainActivity5.this, img, txt, names, points);
//        ldb.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(valueEventListener);
    }

    private void eventHandler(){
        playerDetails = new HashMap<>();
        ref = database.getReference("players");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ArrayList<Integer> arr = new ArrayList<>();
                    arr.add((int)(long)ds.child("points").getValue());
                    arr.add((int)(long)ds.child("avatar").getValue());
                    playerDetails.put(ds.getKey(), arr);
                    initAdapter(playerDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        ref.addValueEventListener(valueEventListener);
    }
}