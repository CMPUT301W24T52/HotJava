package com.example.hotevents;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.units.qual.N;

import java.util.ArrayList;
import java.util.List;

public class UpcomingEventActivityAdapter extends RecyclerView.Adapter<UpcomingEventActivityAdapter.UpcomingEventActivityViewHolder> {
    private ArrayList<Event> events;
    private Context context;
    private View.OnClickListener onClickListener;
    public class UpcomingEventActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView upcomingEventTitle;
        public TextView upcomingEventDescription;
        public TextView startDate;
        public ImageView upcomingEventPoster;
        public TextView upcomingLocation;


        public UpcomingEventActivityViewHolder(@NonNull View itemView){
            super(itemView);
            context = itemView.getContext();
            upcomingEventTitle =  itemView.findViewById(R.id.upcoming_event_title_text);
            startDate = itemView.findViewById(R.id.event_start_time_text);
            upcomingEventPoster = itemView.findViewById(R.id.imageView);
            upcomingLocation = itemView.findViewById(R.id.event_location_text);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            Event event =  events.get(getAdapterPosition());
            intent.putExtra("event", (Parcelable) event);
            Log.d("UpcomingEventActivityAdapter", String.format("Event %s clicked", event.getTitle()));
            context.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public UpcomingEventActivityAdapter(ArrayList<Event> events, Context context){
        this.events = events;
        this.context = context;
    }

    @NonNull
    @Override
    public UpcomingEventActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(context).inflate(R.layout.content_upcoming_events, parent, false);
        return new UpcomingEventActivityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull UpcomingEventActivityViewHolder holder, int positions) {
        holder.setIsRecyclable(false);
        Event event = events.get(positions);
        event.setAdapterUpcomingEventsActivity(this);
        holder.upcomingEventTitle.setText(event.getTitle());
        holder.upcomingLocation.setText(event.getLocation());
        holder.startDate.setText(event.getStartDateTime().toString());

        if (event.getPosterStr() != null){
            try {
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(event.getPosterStr());
                Glide.with(context)
                        .load(storageRef)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.upcomingEventPoster);
            }
            catch (Exception e) {
                return;
            }
        }
    }
}
