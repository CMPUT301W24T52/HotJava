package com.example.hotevents;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserProfileManagerTest {

    @Mock
    Context mockContext;

    @Mock
    ImageView mockImageView;

    @Mock
    DocumentSnapshot mockDocumentSnapshot;

    UserProfileManager userProfileManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userProfileManager = new UserProfileManager(mockContext);
    }

    @Test
    public void testFetchUserData() {
        // Mock document snapshot
        when(mockDocumentSnapshot.exists()).thenReturn(true);

        // Define behavior for the callback
        UserProfileManager.UserDataCallback callback = documentSnapshot -> {
            assertEquals(mockDocumentSnapshot, documentSnapshot);
        };

        // Call fetchUserData with the mocked callback
        userProfileManager.fetchUserData(callback);
    }

    @Test
    public void testLoadProfilePicture() {
        // Mock document snapshot
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.contains("ProfilePictureCustom")).thenReturn(true);
        when(mockDocumentSnapshot.getString("ProfilePictureCustom")).thenReturn("https://example.com/profile.jpg");

        // Call loadProfilePicture with the mocked document snapshot and image view
        userProfileManager.loadProfilePicture(mockImageView, mockDocumentSnapshot);

        // Verify that downloadAndSetProfilePicture was called with the correct parameters
        verify(userProfileManager).downloadAndSetProfilePicture(mockImageView, "https://example.com/profile.jpg");
    }

    @Test
    public void testGenerateDefaultProfilePhoto() {
        // Call generateDefaultProfilePhoto with a mock initial character
        Bitmap bitmap = userProfileManager.generateDefaultProfilePhoto('A');

        // Verify that the returned bitmap is not null
        assertEquals(100, bitmap.getWidth()); // Assuming the width is 100 pixels, adjust accordingly
        assertEquals(100, bitmap.getHeight()); // Assuming the height is 100 pixels, adjust accordingly
    }

    @Test
    public void testStopListening() {
        // Call stopListening
        userProfileManager.stopListening();
    }
}
