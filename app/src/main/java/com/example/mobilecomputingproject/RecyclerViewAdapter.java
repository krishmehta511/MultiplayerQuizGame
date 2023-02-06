package com.example.mobilecomputingproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> players = new ArrayList<>();
    private ArrayList<Integer> player_scores = new ArrayList<>();
    private ArrayList<String> player_gender = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context mContext, ArrayList<String> players, ArrayList<Integer> player_scores, ArrayList<String> player_gender) {
        this.mContext = mContext;
        this.players = players;
        this.player_scores = player_scores;
        this.player_gender = player_gender;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.playerAlias.setText(players.get(position));
        if (player_gender.get(position).equals("M")){
            holder.playerAvatar.setImageResource(R.drawable.m_avatar);
        } else {
            holder.playerAvatar.setImageResource(R.drawable.f_avatar);
        }
        holder.playerScore.setText(String.valueOf(player_scores.get(position)));
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        TextView playerAlias;
        TextView playerScore;
        ImageView playerAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playerAlias = itemView.findViewById(R.id.player_alias);
            playerScore = itemView.findViewById(R.id.score_text);
            playerAvatar = itemView.findViewById(R.id.player_photo);
        }
    }
}
