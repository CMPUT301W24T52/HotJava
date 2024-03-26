package com.example.hotevents;

import android.content.Context;
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

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class AttendeeListAdapter extends RecyclerView.Adapter<AttendeeListAdapter.MyViewHolder> {

    Context context;
    ArrayList<Attendee> attendeesArray;

    public AttendeeListAdapter(Context context, ArrayList<Attendee> attendeesArray) {
        this.context = context;
        this.attendeesArray = attendeesArray;
    }

    @NonNull
    @Override
    public AttendeeListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.content_attendee, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendeeListAdapter.MyViewHolder holder, int position) {
        // Whenever an item pops into view, set the items data
        Attendee attendee = attendeesArray.get(position);
        holder.attendeeName.setText(attendee.getName());
        holder.attendeeCheckin.setText("Check in: " + attendee.getCheckinCount());
        if (attendee.getCheckinCount() > 0) {
            holder.checkinCheckmark.setImageResource(R.drawable.greencheckmark);
        } else {
            holder.checkinCheckmark.setImageResource(R.drawable.greycheckmark);
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(attendee.getProfileImageUrl());
        Glide.with(context)
                .load(storageRef)
                .into(holder.attendeePhoto);
    }

    @Override
    public int getItemCount() {
        return attendeesArray.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView attendeePhoto;
        TextView attendeeName, attendeeCheckin;
        ImageView checkinCheckmark;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            attendeePhoto = itemView.findViewById(R.id.attendee_photo);
            attendeeName = itemView.findViewById(R.id.attendee_name);
            attendeeCheckin = itemView.findViewById(R.id.attendee_checkin);
            checkinCheckmark = itemView.findViewById(R.id.checkin_checkmark);
        }
    }
}
