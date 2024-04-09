package com.example.hotevents;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class BrowseUserProfileActivityTest {

    @Rule
    public ActivityScenarioRule<BrowseUserProfileActivity> activityScenarioRule =
            new ActivityScenarioRule<>(BrowseUserProfileActivity.class);

    @Test
    public void testBrowseUserProfileActivityUI() {
        // Wait for the activity to be launched
        ActivityScenario<BrowseUserProfileActivity> activityScenario = activityScenarioRule.getScenario();

        // Check if all UI elements are displayed
        Espresso.onView(withId(R.id.textViewName)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewEmail)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewPhoneNumber)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewLocation)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.linearDeleteProfile)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.imageViewProfilePhoto)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.removeProfilePhotoButton)).check(matches(isDisplayed()));

//        // Perform click action on the removeProfilePhotoButton
//        Espresso.onView(withId(R.id.removeProfilePhotoButton)).perform(ViewActions.click());

        // Ensure that the activity is still displayed
        Espresso.onView(withId(R.id.textViewName)).check(matches(isDisplayed()));
    }
}

