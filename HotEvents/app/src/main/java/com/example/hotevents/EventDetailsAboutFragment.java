package com.example.hotevents;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventDetailsAboutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventDetailsAboutFragment extends Fragment {

    private static final String ARG_PARAM1 = "description";

    private String eventDescription;

    public EventDetailsAboutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param description Event description
     * @return A new instance of fragment EventDetailsAboutFragment.
     */
    public static EventDetailsAboutFragment newInstance(String description) {
        EventDetailsAboutFragment fragment = new EventDetailsAboutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, description);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventDescription = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details_about, container, false);
        // Inflate the layout for this fragment
        TextView textView = view.findViewById(R.id.event_about);
        if (eventDescription != null) {
            textView.setText(eventDescription);
        }
        return view;
    }
}