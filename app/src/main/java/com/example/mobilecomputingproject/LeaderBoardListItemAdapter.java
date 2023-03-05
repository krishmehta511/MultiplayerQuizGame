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

import java.util.ArrayList;

public class LeaderBoardListItemAdapter extends ArrayAdapter {
    private final ArrayList<Integer> rankingsImg;
    private final ArrayList<String> players;
    private final ArrayList<Integer> points;
    private final Context context;

    public LeaderBoardListItemAdapter(Context context, ArrayList<Integer> rankingsImg, ArrayList<String> players, ArrayList<Integer> points) {
        super(context, R.layout.leaderboard_list_item, players);
        this.context = context;
        this.rankingsImg = rankingsImg;
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
        TextView name = row.findViewById(R.id.player_name_ldb);
        TextView points_tv = row.findViewById(R.id.points);

        img.setImageResource(rankingsImg.get(position));
        name.setText(players.get(position));
        points_tv.setText(String.valueOf(points.get(position)));

        return row;
    }
}