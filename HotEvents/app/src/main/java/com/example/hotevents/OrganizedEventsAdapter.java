package com.example.hotevents;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Parcelable;
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

import java.util.ArrayList;
import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * My Events View Adapter
 */
public class OrganizedEventsAdapter extends RecyclerView.Adapter<OrganizedEventsAdapter.OrganizedEventsViewHolder> {
    private ArrayList<Event> organizedEvents;
    private Context context;
    private Boolean photoDownloaded = false;
    private View.OnClickListener onClickListener;
//    private OnItemClickListener onItemClickListener;

    /**
     * View holder for RecyclerView
     * implements onClickListen to create listener on each event
     */
    public class OrganizedEventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView myEventTitle;
        public TextView myEventLocation;
        public TextView myEventDate;
        public ImageView myEventImg;

        /**
         * Constructor for View holder class
         * @param itemView object holding my event item view
         */
        public OrganizedEventsViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            myEventTitle = (TextView) itemView.findViewById(R.id.upcoming_event_title_text);
            myEventLocation = (TextView) itemView.findViewById(R.id.event_location_text);
            myEventDate = (TextView) itemView.findViewById(R.id.event_start_time_text);
            myEventImg = (ImageView) itemView.findViewById(R.id.imageView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(context, EventDetailsActivity.class);
            Event event = organizedEvents.get(getAdapterPosition());
            myIntent.putExtra("event", (Parcelable) event);
            Log.d("MyEventAdapter", String.format("Event %s clicked", event.getTitle()));
            context.startActivity(myIntent);
        }
    }

    /**
     * constructor for adapter
     * @param myEvents array of events objects
     * @param context context
     */
    public OrganizedEventsAdapter(ArrayList<Event> myEvents, Context context){
        this.organizedEvents = myEvents;
        this.context = context;
    }

    @NonNull
    @Override
    public OrganizedEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView
                = LayoutInflater.from(context).inflate(R.layout.content_upcoming_events, parent, false);
        return new OrganizedEventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizedEventsViewHolder holder, int position) {
        Event event = organizedEvents.get(position);
        event.setAdapterOrganizedEvents(this);
        Log.d("OrganizedEventsAdapter",event.getTitle());
        holder.myEventTitle.setText(event.getTitle());
        holder.myEventLocation.setText(event.getLocation());
        holder.myEventDate.setText(event.getStartDateTime().toString());

        if (!photoDownloaded){
            event.assignPoster(holder.myEventImg);
            photoDownloaded = true;
        }
        else{
            if (event.getPoster() != null){
                holder.myEventImg.setImageBitmap(event.getPoster());
            }
        }

    }

    @Override
    public int getItemCount() {
        return organizedEvents.size();
    }


}
