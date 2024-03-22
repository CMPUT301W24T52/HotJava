package com.example.hotevents;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

/**
 * My Events View Adapter
 */
public class AdminEventsAdapter extends RecyclerView.Adapter<AdminEventsAdapter.AdminEventsViewHolder> {
    private ArrayList<Event> adminEvents;
    private Context context;
    private View.OnClickListener onClickListener;
//    private OnItemClickListener onItemClickListener;

    /**
     * View holder for RecyclerView
     * implements onClickListen to create listener on each event
     */
    public class AdminEventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView myEventTitle;
        public TextView myEventLocation;
        public TextView myEventDate;
        public ImageView myEventImg;

        /**
         * Constructor for View holder class
         * @param itemView object holding my event item view
         */
        public AdminEventsViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            myEventTitle = (TextView) itemView.findViewById(R.id.event_title_text);
            myEventLocation = (TextView) itemView.findViewById(R.id.event_location_text);
            myEventDate = (TextView) itemView.findViewById(R.id.event_start_time_text);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(context, EventDetailsActivity.class);
            Event event = adminEvents.get(getAdapterPosition());
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
    public AdminEventsAdapter(ArrayList<Event> myEvents, Context context){
        this.adminEvents = myEvents;
        this.context = context;
    }

    @NonNull
    @Override
    public AdminEventsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView
                = LayoutInflater.from(context).inflate(R.layout.content_admin_events, parent, false);
        return new AdminEventsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminEventsViewHolder holder, int position) {
        Event event = adminEvents.get(position);
        Log.d("AdminEventsAdapter",event.getTitle());
        holder.myEventTitle.setText(event.getTitle());
        holder.myEventLocation.setText("Location");
        holder.myEventDate.setText(event.getStartDateTime().toString());

        //listener
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        Log.d("Note", "made it here");
    }

    @Override
    public int getItemCount() {
        return adminEvents.size();
    }


}
