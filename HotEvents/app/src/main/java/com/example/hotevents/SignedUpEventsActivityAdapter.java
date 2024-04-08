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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

/**
 * Class to represent SignedUpEventsActivity Adapter
 */
public class SignedUpEventsActivityAdapter extends RecyclerView.Adapter<SignedUpEventsActivityAdapter.SignedUpEventsActivityViewHolder> {
    private ArrayList<Event> events;
    private Context context;

    /**
     * Constructor for Adapter
     * @param events array of events to handle
     * @param context context of current activity
     */
    public SignedUpEventsActivityAdapter(ArrayList<Event> events, Context context) {
        this.events = events;
        this.context = context;
    }

    /**
     * Class to represent View Holder
     */
    public class SignedUpEventsActivityViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView signedUpEventTitle;
        public TextView startDate;
        public ImageView poster;
        public TextView signedUpEventLocation;

        /**
         * View holder Constructor
         * @param itemView View of the given event displayed
         */
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


        /**
         * Method to handle when a view is clicked
         * @param v The view that was clicked.
         */
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            Event event = events.get(getAdapterPosition());
            intent.putExtra("event", (Parcelable) event);
            context.startActivity(intent);
        }
    }

    /**
     * Method to handle functionality when View Holder is created
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return instance of View holder with given itemView
     */
    @NonNull
    @Override
    public SignedUpEventsActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.content_upcoming_events, parent, false);
        return new SignedUpEventsActivityViewHolder(itemView);
    }

    /**
     * Method to handle functionality when a View Holder is bound
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
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
                        .into(holder.poster);
            }
            catch (Exception e) {
                return;
            }
        }
    }

    /**
     * Method that gets size of adapter array
     * @return size of array
     */
    @Override
    public int getItemCount() {
        return events.size();
    }
}
