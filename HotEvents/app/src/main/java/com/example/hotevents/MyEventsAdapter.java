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

        /**
         * Constructor for View holder class
         * @param itemView object holding my event item view
         */
        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            myEventTitle = (TextView) itemView.findViewById(R.id.event_title_text);
            myEventLocation = (TextView) itemView.findViewById(R.id.event_location);
            myEventDate = (TextView) itemView.findViewById(R.id.event_start_date);
            myEventImg = (ImageView) itemView.findViewById(R.id.imageView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Intent myIntent = new Intent(context, EventDetailsActivity.class);
            Event event = myEvents.get(getAdapterPosition());
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
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        Event event = myEvents.get(position);
        Log.d("MyEventsAdapter",event.getTitle());
        holder.myEventTitle.setText(event.getTitle());
        holder.myEventLocation.setText("Location");
        holder.myEventDate.setText(event.getStartDateTime().toString());

        //Setting the poster bitmap
        Bitmap img = event.getPoster();
        if (img != null){
            holder.myEventImg.setImageBitmap(img);
        }

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
        return myEvents.size();
    }


}
