package com.example.hotevents;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.widget.TextView;
import org.junit.Test;

public class ProfileActivityTest {

    @Test
    public void testTextViewsNotNull() {
        // Mock data
        String name = "John Doe";
        String email = "john.doe@example.com";
        String contact = "1234567890";
        String location = "City";

        // Create ProfileActivity instance
        ProfileActivity profileActivity = new ProfileActivity();

        // Mock TextViews
        profileActivity.textViewName = mock(TextView.class);
        profileActivity.textViewEmail = mock(TextView.class);
        profileActivity.textViewContact = mock(TextView.class);
        profileActivity.textViewLocation = mock(TextView.class);

        // Set mock data to TextViews
        profileActivity.textViewName.setText(name);
        profileActivity.textViewEmail.setText(email);
        profileActivity.textViewContact.setText(contact);
        profileActivity.textViewLocation.setText(location);

        // Check if TextViews are not null
        assertNotNull(profileActivity.textViewName);
        assertNotNull(profileActivity.textViewEmail);
        assertNotNull(profileActivity.textViewContact);
        assertNotNull(profileActivity.textViewLocation);
    }
}
