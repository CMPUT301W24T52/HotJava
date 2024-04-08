package com.example.hotevents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProfileActivityTest {

    private ProfileActivity profileActivity;

    @Before
    public void setUp() {
        profileActivity = new ProfileActivity();
        profileActivity.onCreate(null);
    }

    @After
    public void tearDown() {
        profileActivity = null;
    }

    @Test
    public void testTextViewNameNotNull() {
        TextView textViewName = profileActivity.findViewById(R.id.textViewName);
        assertNotNull(textViewName);
    }

    @Test
    public void testTextViewEmailNotNull() {
        TextView textViewEmail = profileActivity.findViewById(R.id.textViewEmail);
        assertNotNull(textViewEmail);
    }

    @Test
    public void testTextViewContactNotNull() {
        TextView textViewContact = profileActivity.findViewById(R.id.textViewPhoneNumber);
        assertNotNull(textViewContact);
    }

    @Test
    public void testTextViewLocationNotNull() {
        TextView textViewLocation = profileActivity.findViewById(R.id.textViewLocation);
        assertNotNull(textViewLocation);
    }

    @Test
    public void testInitialFieldsNull() {
        assertNull(profileActivity.textViewName.getText());
        assertNull(profileActivity.textViewEmail.getText());
        assertNull(profileActivity.textViewContact.getText());
        assertNull(profileActivity.textViewLocation.getText());
    }

    @Test
    public void testSetFields() {
        // Mock data
        String name = "John Doe";
        String email = "john.doe@example.com";
        String contact = "1234567890";
        String location = "City";

        // Set fields with mock data
        profileActivity.textViewName.setText(name);
        profileActivity.textViewEmail.setText(email);
        profileActivity.textViewContact.setText(contact);
        profileActivity.textViewLocation.setText(location);

        // Test if fields are set correctly
        assertEquals(name, profileActivity.textViewName.getText().toString());
        assertEquals("Email ID: " + email, profileActivity.textViewEmail.getText().toString());
        assertEquals("Contact: " + contact, profileActivity.textViewContact.getText().toString());
        assertEquals("Location: " + location, profileActivity.textViewLocation.getText().toString());
    }
}
