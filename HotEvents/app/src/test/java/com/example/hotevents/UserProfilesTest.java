package com.example.hotevents;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class UserProfilesTest {

    @Before
    public void setUp() {
        // Initialization code, if any
    }

    @Test
    public void testUserProfileCreation() {
        // Mock data
        String profileImageUrl = "https://example.com/profile.jpg";
        String username = "testUser";
        String uid = "123456789";

        // Create a UserProfile object
        UserProfiles userProfile = new UserProfiles(profileImageUrl, username, uid);

        // Verify that the object is created correctly
        assertEquals(profileImageUrl, userProfile.getProfileImageUrl());
        assertEquals(username, userProfile.getUsername());
        assertEquals(uid, userProfile.getUid());
    }

    @Test
    public void testUserProfileSetterMethods() {
        // Mock data
        String profileImageUrl = "https://example.com/profile.jpg";
        String newProfileImageUrl = "https://example.com/new_profile.jpg";
        String username = "testUser";
        String newUsername = "updatedUser";
        String uid = "123456789";
        String newUid = "987654321";

        // Create a UserProfile object
        UserProfiles userProfile = new UserProfiles(profileImageUrl, username, uid);

        // Set new values using setter methods
        userProfile.setProfileImageUrl(newProfileImageUrl);
        userProfile.setUsername(newUsername);
        userProfile.setUid(newUid);

        // Verify that the values are updated correctly
        assertEquals(newProfileImageUrl, userProfile.getProfileImageUrl());
        assertEquals(newUsername, userProfile.getUsername());
        assertEquals(newUid, userProfile.getUid());
    }
}
