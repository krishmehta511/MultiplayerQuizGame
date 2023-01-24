package com.example.mobilecomputingproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hide StatusBar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView title = findViewById(R.id.AppName);
        Button play_btn = findViewById(R.id.create_room);
        Button leaderboard_btn = findViewById(R.id.leaderboard);
        Button settings_btn = findViewById(R.id.settings);

        String title_html = "<font color=#ffffff>CompQuiz()</font><font color=#006b38> {</font>";
        String play_html = "<font color=#ffffff>play</font><font color=#006b38> ;</font>";
        String leaderboard_html = "<font color=#ffffff>leaderboard</font><font color=#006b38> ;</font>";
        String settings_html = "<font color=#ffffff>settings</font><font color=#006b38> ;</font>";

        title.setText(Html.fromHtml(title_html, Html.FROM_HTML_MODE_COMPACT));
        play_btn.setText(Html.fromHtml(play_html, Html.FROM_HTML_MODE_COMPACT));
        leaderboard_btn.setText(Html.fromHtml(leaderboard_html, Html.FROM_HTML_MODE_COMPACT));
        settings_btn.setText(Html.fromHtml(settings_html, Html.FROM_HTML_MODE_COMPACT));


    }
}