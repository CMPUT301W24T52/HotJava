package com.example.hotevents;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import com.bumptech.glide.Glide;


/**
 * My Events View Adapter
 */
public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder> {
    private ArrayList<Event> myEvents;
    private Context context;
    private View.OnClickListener onClickListener;
//    private OnItemClickListener onItemClickListener;

    /**
     * View holder for RecyclerView
     * implements onClickListen to create listener on each event
     */
    public class MyEventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView myEventTitle;
        public TextView myEventLocation;
        public TextView myEventDate;
        public ImageView myEventImg;
        public ImageView eventImage;

        /**
         * Constructor for View holder class
         * @param itemView object holding my event item view
         */
        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            eventImage = (ImageView) itemView.findViewById(R.id.imageView);
            myEventTitle = (TextView) itemView.findViewById(R.id.event_title_text);
            myEventLocation = (TextView) itemView.findViewById(R.id.event_location);
            myEventDate = (TextView) itemView.findViewById(R.id.event_start_date);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(context, EventDetailsActivity.class);
            Event event = myEvents.get(getAdapterPosition());
            myIntent.putExtra("event", event);
            Log.d("MyEventAdapter", String.format("Event %s clicked", event.getTitle()));
            context.startActivity(myIntent);
        }
    }

    /**
     * constructor for adapter
     * @param myEvents array of events objects
     * @param context context
     */
    public MyEventsAdapter(ArrayList<Event> myEvents, Context context){
        this.myEvents = myEvents;
        this.context = context;
    }

    @NonNull
    @Override
    public MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView
                = LayoutInflater.from(context).inflate(R.layout.content_myevents, parent, false);
        return new MyEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int position) {
        Event event = myEvents.get(position);
        Log.d("MyEventsAdapter",event.getTitle());
        holder.myEventTitle.setText(event.getTitle());
        holder.myEventLocation.setText("Location");
        holder.myEventDate.setText(event.getStartDateTime().toString());

        // Format the date to show only day, month, day of the month, hour (12-hour), and AM/PM indicator
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd • hh:mm a", Locale.getDefault());
        String formattedDate = sdf.format(event.getStartDateTime());
        holder.myEventDate.setText(formattedDate);
//        loadImageFromFirestoreStorage(event.getPosterUrl(), holder.eventImage);

        //listener
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        Log.d("Note", "made it here");
    }
    public static void loadImageFromFirestoreStorage(String imageUrl, ImageView imageView) {
        // Use Glide library to load the image from the provided URL into the ImageView
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .placeholder(R.drawable.default_poster) // Placeholder image resource
                .error(R.drawable.thumbnail) // Error image resource
                .into(imageView);
    }
    @Override
    public int getItemCount() {
        return myEvents.size();
    }


}
