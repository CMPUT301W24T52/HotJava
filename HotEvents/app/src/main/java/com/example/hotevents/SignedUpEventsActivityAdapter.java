package com.example.hotevents;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.TextureView;
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

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

public class SignedUpEventsActivityAdapter extends RecyclerView.Adapter<SignedUpEventsActivityAdapter.SignedUpEventsActivityViewHolder> {
    private ArrayList<Event> events;
    private Context context;

    public SignedUpEventsActivityAdapter(ArrayList<Event> events, Context context) {
        this.events = events;
        this.context = context;
    }

    public class SignedUpEventsActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView signedUpEventTitle;
        public TextView startDate;
        public ImageView poster;
        public TextView signedUpEventLocation;

        public SignedUpEventsActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            signedUpEventTitle = itemView.findViewById(R.id.upcoming_event_title_text);
            startDate = itemView.findViewById(R.id.event_start_time_text);
            signedUpEventLocation = itemView.findViewById(R.id.event_location_text);
            poster = itemView.findViewById(R.id.imageView);
            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            Event event = events.get(getAdapterPosition());
            intent.putExtra("event", (Parcelable) event);
            context.startActivity(intent);
        }
    }

    @NonNull
    @Override
    public SignedUpEventsActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.content_upcoming_events, parent, false);
        return new SignedUpEventsActivityViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SignedUpEventsActivityViewHolder holder, int position) {
    holder.setIsRecyclable(false);
    Event event = events.get(position);
    event.setAdapterSignedUpEventsActivity(this);
    holder.signedUpEventTitle.setText(event.getTitle());
    holder.signedUpEventLocation.setText(event.getLocation());
    holder.startDate.setText(event.getStartDateTime().toString());

        if (event.getPosterStr() != null){
            try {
                StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(event.getPosterStr());
                Glide.with(context)
                        .load(storageRef)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(holder.poster);
            }
            catch (Exception e) {
                return;
            }
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }
}
