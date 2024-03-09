package com.example.hotevents;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * My Events View Adapter
 */
public class MyEventsAdapter extends RecyclerView.Adapter<MyEventsAdapter.MyEventViewHolder> {
    private ArrayList<Event> myEvents;
    private Context context;

    public class MyEventViewHolder extends RecyclerView.ViewHolder {
        public TextView myEventTitle;
        public TextView myEventLocation;
        public TextView myEventDate;
        public ImageView myEventImg;
        public MyEventViewHolder(@NonNull View itemView) {
            super(itemView);
            myEventTitle = (TextView) itemView.findViewById(R.id.event_title_text);
            myEventLocation = (TextView) itemView.findViewById(R.id.event_location);
            myEventDate = (TextView) itemView.findViewById(R.id.event_start_date);
        }
    }

    public MyEventsAdapter(ArrayList<Event> myEvents, Context context){
        this.myEvents = myEvents;
        this.context = context;
    }

    @NonNull
    @Override
    public MyEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView
                = LayoutInflater.from(context).inflate(R.layout.content_myevents, parent, false);
        itemView.setOnClickListener(v -> {

        });
        return new MyEventViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyEventViewHolder holder, int position) {
        Event event = myEvents.get(position);
        Log.d("MyEventsAdapter",event.getTitle());
        holder.myEventTitle.setText(event.getTitle());
        holder.myEventLocation.setText("Location");
        holder.myEventDate.setText(event.getStartDateTime().toString());
        Log.d("Note", "made it here");
    }

    @Override
    public int getItemCount() {
        return myEvents.size();
    }





//    @NonNull
//    @Override
//    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
//        View view;
//        if (convertView ==  null){
//            view = LayoutInflater.from(getContext()).inflate(R.layout.contents_myevents, parent, false);
//        } else {
//            view = convertView;
//        }
//        Event event = myEvents.get(position);
//
//        TextView eventTitle = view.findViewById(R.id.event_title_text);
//        ImageView poster = view.findViewById(R.id.imageView);       // to be implemented
//
//        eventTitle.setText(event.getTitle());
////        poster.set
//
//        return view;

}
