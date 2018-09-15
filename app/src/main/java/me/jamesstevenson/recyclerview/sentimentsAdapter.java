// Almost all of the code i've used for the recycler view was found at: https://www.androidhive.info/2016/01/android-working-with-recycler-view/


package me.jamesstevenson.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class sentimentsAdapter extends RecyclerView.Adapter<sentimentsAdapter.MyViewHolder> {

    private List<sentiment> sentimentsList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, date, score;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            score = (TextView) view.findViewById(R.id.score);
            date = (TextView) view.findViewById(R.id.date);
        }
    }


    public sentimentsAdapter(List<sentiment> sentimentsList) {
        this.sentimentsList = sentimentsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sentiment_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        sentiment sentiment = sentimentsList.get(position);
        holder.name.setText(sentiment.getName());
        holder.score.setText(sentiment.getScore());
        holder.date.setText(sentiment.getDate());
    }

    @Override
    public int getItemCount() {
        return sentimentsList.size();
    }
}