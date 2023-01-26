package com.example.mobilecomputingproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity {

    TextView rooms_title;
    TextView create_room;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //Hide Status Bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        rooms_title = findViewById(R.id.rooms_title);
        create_room = findViewById(R.id.create_room);

        String room_string = "<font color=#ffffff>Rooms</font><font color=#006b38> (){</font>";
        String create_string = "<font color=#006b38>/* </font><font color=#ffffff>Create Room</font>" +
                "<font color=#006b38> */</font>";

        rooms_title.setText(Html.fromHtml(room_string, Html.FROM_HTML_MODE_COMPACT));
        create_room.setText(Html.fromHtml(create_string, Html.FROM_HTML_MODE_COMPACT));
    }
}