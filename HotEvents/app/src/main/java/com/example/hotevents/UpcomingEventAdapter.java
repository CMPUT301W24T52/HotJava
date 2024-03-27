package com.example.hotevents;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class UpcomingEventAdapter extends RecyclerView.Adapter<UpcomingEventAdapter.UpcomingEventViewHolder> {
    private ArrayList<Event> upcomingEvents;
    private Context context;
    private Boolean photoDownloaded = false;

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
        //https://stackoverflow.com/questions/38182223/recyclerview-wrong-position-set-onbindviewholder
        holder.setIsRecyclable(false);
        Event event = upcomingEvents.get(holder.getAdapterPosition());
        event.setAdapterUpComingEvents(this);
        holder.upcomingEventTitle.setText(event.getTitle());
        holder.upcomingLocation.setText(event.getLocation());
        // Format the startDateTime to a string representation
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd hh:mm aa", Locale.getDefault());
        String formattedStartDate = dateFormat.format(event.getStartDateTime());

        holder.startDate.setText(formattedStartDate);
        holder.upcomingEventTitle.setText(event.getTitle());
        
        if (event.getPosterStr() != null){
            try {
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(event.getPosterStr());
                Glide.with(context)
                        .load(storageRef)
                        .into(holder.upcomingEventPoster);
            }
            catch (Exception e){
                return;
            }

        }

    }

    @Override
    public int getItemCount() {
        return upcomingEvents.size();
    }

    public class UpcomingEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView upcomingEventTitle;
        public TextView upcomingEventDescription;
        public TextView startDate;
        public ImageView upcomingEventPoster;
        public TextView upcomingLocation;

        // ...Other Event Information

        public UpcomingEventViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            upcomingEventTitle = (TextView) itemView.findViewById(R.id.upcoming_event_title_text);
//            upcomingEventDescription = (TextView) itemView.findViewById(R.id.upcoming_event_description_text);
            startDate = (TextView) itemView.findViewById(R.id.event_start_time_text);
            upcomingEventPoster = (ImageView) itemView.findViewById(R.id.imageView);
            upcomingLocation = (TextView) itemView.findViewById(R.id.event_location_text);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            Event event = upcomingEvents.get(getAdapterPosition());
            intent.putExtra("event", (Parcelable) event);
            Log.d("UpcomingEventAdapter", String.format("Event %s clicked", event.getTitle()));
            context.startActivity(intent);
        }
    }
}
