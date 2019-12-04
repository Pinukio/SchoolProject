package com.example.schoolproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private ArrayList<Item> list;
    private Activity activity;

    RecyclerAdapter(ArrayList<Item> list, Activity activity) {
        this.list = list;
        this.activity =activity;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_main, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        Item item = list.get(position);
        holder.title.setText(item.title);
        holder.itemView.setId(position);
        String date = item.year + "-" + item.month + "-" + item.day;
        holder.day.setText(date);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            MyViewHolder h = holder;
            @Override
            public void onClick(View v) {
                ((MainActivity)activity).expand((long)position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView day;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.Card_title);
            day = itemView.findViewById(R.id.Card_day);
        }

    }

    @Override
    public long getItemId(int position) {
        return (long)position;
    }

}
