package com.example.mobilecomputingproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LeaderBoardListItemAdapter extends ArrayAdapter {
    private final Integer[] rankingsImg;
    private final Integer[] rankingsTxt;
    private final String[] players;
    private final Integer[] points;
    private final Context context;

    public LeaderBoardListItemAdapter(Context context, Integer[] rankingsImg, Integer[] rankingsTxt, String[] players, Integer[] points) {
        super(context, R.layout.leaderboard_list_item, players);
        this.context = context;
        this.rankingsImg = rankingsImg;
        this.rankingsTxt = rankingsTxt;
        this.players = players;
        this.points = points;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if(convertView == null){
            row = layoutInflater.inflate(R.layout.leaderboard_list_item, null,true);
        }
        ImageView img = row.findViewById(R.id.ranking_img);
        TextView txt = row.findViewById(R.id.ranking_txt);
        TextView name = row.findViewById(R.id.player_name_ldb);
        TextView points_tv = row.findViewById(R.id.points);

        img.setImageResource(rankingsImg[position]);
        txt.setText(String.valueOf(rankingsTxt[position]));
        name.setText(players[position]);
        points_tv.setText(String.valueOf(points[position]));

        return row;
    }
}