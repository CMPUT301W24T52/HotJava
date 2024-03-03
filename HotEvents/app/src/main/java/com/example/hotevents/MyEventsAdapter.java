package com.example.hotevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

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

        return view;

    }
}
