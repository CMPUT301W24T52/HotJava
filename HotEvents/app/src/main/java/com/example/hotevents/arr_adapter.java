// arr_adapter.java

package com.example.hotevents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.List;

/**
 * Custom ArrayAdapter to display user profiles in a ListView.
 */
public class arr_adapter extends ArrayAdapter<UserProfiles> {
    private final Context context;
    private final List<UserProfiles> users;

    /**
     * Constructor for the ArrayAdapter.
     *
     * @param context The context in which the adapter is being used.
     * @param users   The list of UserProfiles to be displayed.
     */
    public arr_adapter(Context context, List<UserProfiles> users) {
        super(context, R.layout.profile_list_user, users);
        this.context = context;
        this.users = users;
    }

    /**
     * ViewHolder pattern to efficiently recycle views.
     */
    private static class ViewHolder {
        CircleImageView profileImage;
        TextView username;
        TextView uid; // TextView for displaying UID
    }

    /**
     * Overrides the getView method to populate each item in the ListView.
     *
     * @param position    The position of the item in the data set.
     * @param convertView The recycled view to populate.
     * @param parent      The parent ViewGroup.
     * @return The populated view for the item at the given position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            // If convertView is null, inflate a new view and create a ViewHolder
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.profile_list_user, parent, false);

            // Initialize the ViewHolder
            holder = new ViewHolder();
            holder.profileImage = convertView.findViewById(R.id.profileImage);
            holder.username = convertView.findViewById(R.id.username);
            holder.uid = convertView.findViewById(R.id.uid); // Initialize UID TextView

            convertView.setTag(holder);
        } else {
            // If convertView is not null, reuse the existing ViewHolder
            holder = (ViewHolder) convertView.getTag();
        }

        // Retrieve the UserProfiles object at the given position
        UserProfiles user = users.get(position);

        // Set user data to the views
        holder.profileImage.setImageResource(user.getProfileImageRes());
        holder.username.setText(user.getUsername());
        holder.uid.setText(user.getUid()); // Set UID text

        return convertView;
    }
}
