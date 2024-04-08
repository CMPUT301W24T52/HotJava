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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Adapter class responsible for populating views from Event Array (model)
 */
public class UpcomingEventAdapter extends RecyclerView.Adapter<UpcomingEventAdapter.UpcomingEventViewHolder> {
    private ArrayList<Event> upcomingEvents;
    private Context context;
    private Boolean photoDownloaded = false;

    /**
     * Constructor for Upcoming Events Adapter
     * @param upcomingEvents Array containing Event objects
     * @param context activity context
     */
    public UpcomingEventAdapter(ArrayList<Event> upcomingEvents, Context context){
        this.upcomingEvents = upcomingEvents;
        this.context = context;
    }

    /**
     * Method called when a view holder is created
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return UpcomingEventViewHolder
     */
    @NonNull
    @Override
    public UpcomingEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.content_upcoming_events, parent, false);
        return new UpcomingEventViewHolder(itemView);
    }

    /**
     * Method called when view holder is bound to Event
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.upcomingEventPoster);
            }
            catch (Exception e){
                return;
            }

        }

    }

    /**
     * method to get item count
     * @return number of items in upcomingEvents Array
     */
    @Override
    public int getItemCount() {
        return upcomingEvents.size();
    }

    /**
     * Class that represents the view holder
     */
    public class UpcomingEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView upcomingEventTitle;
        public TextView upcomingEventDescription;
        public TextView startDate;
        public ImageView upcomingEventPoster;
        public TextView upcomingLocation;

        // ...Other Event Information

        /**
         * Constructor for viewHolder
         * @param itemView The view of the item displayed on screen
         */
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

        /**
         * Method to handle click events for individual item views
         * @param v The view that was clicked.
         */
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
