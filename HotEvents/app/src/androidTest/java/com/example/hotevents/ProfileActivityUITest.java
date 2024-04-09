package com.example.hotevents;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;

import org.junit.Before;
import org.junit.Test;

public class ProfileActivityUITest {

    private ActivityScenario<ProfileActivity> activityScenario;

    @Before
    public void setUp() {
        activityScenario = ActivityScenario.launch(ProfileActivity.class);
    }

    @Test
    public void profileActivityComponentsDisplayed() {
        // Check if the profile activity components are displayed
        Espresso.onView(withId(R.id.textViewName)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewEmail)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewPhoneNumber)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewLocation)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.imageViewProfilePhoto)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.linearEditProfile)).check(matches(isDisplayed()));
    }

    @Test
    public void editProfileButtonClicked() {
        // Click on the edit profile button and check if EditProfileActivity is launched
        Espresso.onView(withId(R.id.linearEditProfile)).perform(click());
        Espresso.onView(withText("Edit Profile")).check(matches(isDisplayed()));
    }
}
