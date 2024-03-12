package com.example.hotevents;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UpcomingEventAdapter extends RecyclerView.Adapter<UpcomingEventAdapter.UpcomingEventViewHolder> {
    private ArrayList<Event> upcomingEvents;
    private Context context;

    public UpcomingEventAdapter(ArrayList<Event> upcomingEvents, Context context){
        this.upcomingEvents = upcomingEvents;
        this.context = context;
    }

    @NonNull
    @Override
    public UpcomingEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.content_upcoming_events, parent, false);
        return new UpcomingEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingEventViewHolder holder, int position) {
        Event event = upcomingEvents.get(position);
        holder.upcomingEventTitle.setText(event.getTitle());
        holder.upcomingEventDescription.setText(event.getDescription());
    }

    @Override
    public int getItemCount() {
        return upcomingEvents.size();
    }

    public class UpcomingEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView upcomingEventTitle;
        public TextView upcomingEventDescription;

        // ...Other Event Information

        public UpcomingEventViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            upcomingEventTitle = (TextView) itemView.findViewById(R.id.upcoming_event_title_text);
            upcomingEventDescription = (TextView) itemView.findViewById(R.id.upcoming_event_description_text);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            Event event = upcomingEvents.get(getAdapterPosition());
            intent.putExtra("event", event);
            Log.d("UpcomingEventAdapter", String.format("Event %s clicked", event.getTitle()));
            context.startActivity(intent);
        }
    }
}
