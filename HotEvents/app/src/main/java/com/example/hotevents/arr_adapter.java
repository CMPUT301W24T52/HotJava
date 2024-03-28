// arr_adapter.java

package com.example.hotevents;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Custom ArrayAdapter to display user profiles in a ListView.
 */
public class arr_adapter extends ArrayAdapter<UserProfiles> {
    Context context;
    List<UserProfiles> users;

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
        holder.username.setText(user.getUsername());
        holder.uid.setText(user.getUid()); // Set UID text
        StorageReference storageRef;
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.getProfileImageUrl());
        Glide.with(context)
                .load(storageRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                .skipMemoryCache(true)
                .into(holder.profileImage);

        // Set OnClickListener on profileImage
        holder.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(user);
            }
        });

        return convertView;
    }
    // Method to show dialog
    private void showDialog(UserProfiles user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_user_profile, null);
        ImageView profileImageDialog = dialogView.findViewById(R.id.dialogProfileImage);

        // Set profile image and username in the dialog
        StorageReference storageRef;
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(user.getProfileImageUrl());
        Glide.with(context)
                .load(storageRef)
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                .skipMemoryCache(true)
                .into(profileImageDialog);


        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
