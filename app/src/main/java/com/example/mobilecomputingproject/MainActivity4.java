package com.example.mobilecomputingproject;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity4 extends AppCompatActivity {
    String playerName;
    String hostName;
    Boolean isHost;
    String roomId;
    FirebaseDatabase database;
    DatabaseReference ref;
    ValueEventListener valueEventListener;
    RecyclerView recyclerView;
    ArrayList<String> questions = new ArrayList<>();
    ArrayList<String> options = new ArrayList<>();
    ArrayList<String> players = new ArrayList<>();
    ArrayList<Integer> player_scores = new ArrayList<>();
    ArrayList<String> player_avatar = new ArrayList<>();
    TextView question;
    Button op1;
    Button op2;
    Button op3;
    Button op4;
    int question_count = -1;
    int current_score = 0;
    int correct_answers = 0;
    int wrong_answers = 0;
    int player_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        //Remove Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Initialize Firebase database
        database = FirebaseDatabase.getInstance();

        //Initialize Widgets
        initRecycler();
        question = findViewById(R.id.question);
        op1 = findViewById(R.id.op1);
        op2 = findViewById(R.id.op2);
        op3 = findViewById(R.id.op3);
        op4 = findViewById(R.id.op4);

        //Get player name, host name and room id
        SharedPreferences prefs = getSharedPreferences("PREFS", 0);
        playerName = prefs.getString("player_name", "");
        Bundle bundle = getIntent().getExtras();
        roomId = bundle.getString("room_id");
        hostName = roomId.substring(0, roomId.length() - 7);
        isHost = playerName.equals(hostName);

        //Handle back press based on host or user
        OnBackPressedCallback callback = customBackFunc();
        getOnBackPressedDispatcher().addCallback(callback);

        //Get all questions and options from db
        addQuestions();

        allEventsHandler();

        op1.setOnClickListener(view -> onClick(op1));
        op2.setOnClickListener(view -> onClick(op2));
        op3.setOnClickListener(view -> onClick(op3));
        op4.setOnClickListener(view -> onClick(op4));

    }

    private void onClick(Button op){
        checkAnswer(op);
        updateQuestions();
    }

    private void initRecycler(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity4.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView = findViewById(R.id.live_score);
        recyclerView.setLayoutManager(layoutManager);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, players, player_scores, player_avatar);
        recyclerView.setAdapter(adapter);
    }

    private void addQuestions(){
        database.getReference("rooms").child(roomId).child("Questions")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            String key = ds.getKey();
                            assert key != null;
                            for(DataSnapshot dss: snapshot.child(key).getChildren()){
                                questions.add(dss.getKey());
                                for(DataSnapshot dsss: dss.getChildren()){
                                    options.add((String) dsss.getValue());
                                }
                            }
                        }
                        updateQuestions();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkAnswer(Button option){
        String[] question_txt = question.getText().toString().split("x");
        int correct_ans = Integer.parseInt(question_txt[0].trim()) * Integer.parseInt(question_txt[1].trim());
        int chosen_opt = Integer.parseInt(option.getText().toString());
        DatabaseReference sRef = database.getReference("rooms").child(roomId)
                .child("Players").child(playerName);
        if(correct_ans == chosen_opt){
            correct_answers ++;
            sRef.setValue(current_score+1);
        } else {
            wrong_answers ++;
            sRef.setValue(current_score-1);
        }
    }

    private void updateQuestions(){
        question_count++;
        question.setText(questions.get(question_count));
        op1.setText(options.get(4 * question_count));
        op2.setText(options.get((4 * question_count) + 1));
        op3.setText(options.get((4 * question_count) + 2));
        op4.setText(options.get((4 * question_count) + 3));
    }

    private void updateRecycler(DataSnapshot snapshot){
        players.clear();
        player_scores.clear();
        player_avatar.clear();
        for(DataSnapshot ds: snapshot.child("Players").getChildren()){
            players.add(ds.getKey());
            player_scores.add(Integer.valueOf(String.valueOf(ds.getValue())));
            player_avatar.add("M");
        }
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, players, player_scores, player_avatar);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ref.removeEventListener(valueEventListener);
        Log.d("xxxxx", "Main Activity 4 listener removed.");
        question_count = -1;
        current_score = 0;
    }

    private OnBackPressedCallback customBackFunc(){
        return new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity4.this);
                DatabaseReference dRef = database.getReference("rooms").child(roomId);
                if(playerName.equals(hostName)){
                    builder.setCancelable(true)
                            .setTitle("End Game")
                            .setMessage("If the host leaves the game will end and host will " +
                                    "be penalised. Do you want to leave ?")
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton("Yes", ((dialogInterface, i) ->
                                    dRef.child("Game Status").setValue("Not Started")));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    builder.setCancelable(true)
                            .setTitle("Leave Game")
                            .setMessage("If you leave the game you will be penalised." +
                                    " Do you want to leave ?")
                            .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton("Yes", ((dialogInterface, i) ->
                                    dRef.child("Players").child(playerName).removeValue()));
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        };
    }

    private void goToPrevPage(){
        question_count = -1;
        current_score = 0;
        ref.removeEventListener(valueEventListener);
        Log.d("xxxxx", "Main Activity 4 listener removed.");
        Intent intent = new Intent(MainActivity4.this, MainActivity3.class);
        intent.putExtra("room_id", roomId);
        startActivity(intent);
        finish();
    }

    private void allEventsHandler(){
        Log.d("xxxxx", "Main Activity 4 listener started.");
        ref = database.getReference("rooms").child(roomId);
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("xxxxx", "Main Activity 4 listener called.");
                if (snapshot.exists()){
                    String status = (String) snapshot.child("Game Status").getValue();
                    if(status == null || status.equals("Not Started")){
                        ref.child("Questions").removeValue();
                        for(DataSnapshot ds: snapshot.child("Players").getChildren()){
                            ref.child("Players").child(ds.getKey()).setValue(0);
                        }
                        goToPrevPage();
                    }
                    if(status != null && status.equals("Game End")){
                        ref.child("Players").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                player_count = (int) snapshot.getChildrenCount();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        ref.child("Winner").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String winner = (String) snapshot.getValue();
                                assert winner != null;
                                if(player_count > 1){
                                    updateWinnerPoints(winner);
                                } else {
                                    gameEndDialog(winner);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    if(!snapshot.child("Players").child(playerName).exists()){
                        question_count = -1;
                        current_score = 0;
                        ref.removeEventListener(valueEventListener);
                        startActivity(new Intent(MainActivity4.this, MainActivity2.class));
                        finish();
                    }
                    if(snapshot.child("Players").child(playerName).exists()){
                        current_score = Integer.parseInt(String.valueOf(snapshot.child("Players")
                                .child(playerName).getValue()));
                        if(current_score >= 10){
                            ref.child("Winner").setValue(playerName);
                            ref.child("Game Status").setValue("Game End");
                        }
                        updateRecycler(snapshot);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addValueEventListener(valueEventListener);
    }

    private void updateWinnerPoints(String winner){
        DatabaseReference dbRef = database.getReference("players").child(winner).child("points");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dbRef.setValue((int)(long)snapshot.getValue() - 10);
                gameEndDialog(winner);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void gameEndDialog(String winner){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.game_end_dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Button end_game = dialog.findViewById(R.id.game_end_btn);
        TextView winner_txt = dialog.findViewById(R.id.ge_winner);
        ImageView avtr = dialog.findViewById(R.id.ge_avatar);

        database.getReference("players").child(winner).child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null){
                    if (snapshot.getValue().toString().equals("1")){
                        avtr.setImageResource(R.drawable.m_avatar);
                    } else {
                        avtr.setImageResource(R.drawable.f_avatar);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        winner_txt.setText(winner);

        DatabaseReference dRef = database.getReference("rooms").child(roomId);

        end_game.setOnClickListener(view -> {
            dialog.dismiss();
            current_score = 0;
            question_count = -1;
            dRef.child("Game Status").setValue("Not Started");
            dRef.child("Questions").removeValue();
            dRef.child("Players").child(playerName).setValue(0);
            dRef.child("Winner").removeValue();
            ref.removeEventListener(valueEventListener);
            startActivity(new Intent(this, MainActivity3.class)
                    .putExtra("room_id", roomId));
            finish();
        });

        dialog.show();

    }

}