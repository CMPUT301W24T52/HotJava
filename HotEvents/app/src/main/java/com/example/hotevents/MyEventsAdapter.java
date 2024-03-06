package com.example.hotevents;

import android.content.Context;
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
public class MyEventsAdapter extends ArrayAdapter<Event> {
    private ArrayList<Event> myEvents;
    private Context context;

    public MyEventsAdapter(Context context, ArrayList<Event> myEvents){
        super(context, 0, myEvents);
        this.myEvents = myEvents;
        this.context = context;
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view;
        if (convertView ==  null){
            view = LayoutInflater.from(getContext()).inflate(R.layout.contents_myevents, parent, false);
        } else {
            view = convertView;
        }
        Event event = myEvents.get(position);

        TextView eventTitle = view.findViewById(R.id.event_title_text);
        ImageView poster = view.findViewById(R.id.imageView);       // to be implemented

        eventTitle.setText(event.getTitle());
//        poster.set

        return view;
    }
}
