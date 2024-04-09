package com.example.hotevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * A custom notification adapter for displaying notification data in a ListView.
 */

public class NotificationsAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Notification> mNotifications;

    public NotificationsAdapter(Context context, ArrayList<Notification> notifications) {
        mContext = context;
        mNotifications = notifications;
    }

    @Override
    public int getCount() {
        return mNotifications.size();
    }

    @Override
    public Object getItem(int position) {
        return mNotifications.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String formatTimestamp(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.content_notification, parent, false);
            holder = new ViewHolder();
            holder.messageTextView = convertView.findViewById(R.id.notification_message);
            holder.timestampTextView = convertView.findViewById(R.id.notification_timestamp);
            holder.poster = convertView.findViewById(R.id.imageView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Notification notification = mNotifications.get(position);

        holder.messageTextView.setText(notification.getNotificationMessage());
        holder.timestampTextView.setText(formatTimestamp(notification.getTimestamp()));

        // Check if the poster URL is not null
        if (notification.getPoster() != null) {
            // Fetch and display the poster
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(notification.getPoster());
            Glide.with(mContext)
                    .load(storageRef)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                    .skipMemoryCache(true)
                    .into(holder.poster);
        }
        return convertView;
    }


    private static class ViewHolder {
        TextView messageTextView;
        TextView timestampTextView;
        ImageView poster;
    }
}
