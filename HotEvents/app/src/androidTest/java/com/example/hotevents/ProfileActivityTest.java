package com.example.hotevents;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class ProfileActivityTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityScenarioRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    @Test
    public void testProfileActivityUI() {
        // Check if all UI elements are displayed
        Espresso.onView(withId(R.id.textViewName)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewEmail)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewPhoneNumber)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.textViewLocation)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.imageViewProfilePhoto)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        Espresso.onView(withId(R.id.linearEditProfile)).check(matches(isDisplayed()));

        // Perform click action on the edit profile button
        Espresso.onView(withId(R.id.linearEditProfile)).perform(ViewActions.click());

        // Perform click action on the back button
        Espresso.onView(withId(R.id.backButton)).perform(ViewActions.click());

        // Ensure that the profile activity is still displayed
        Espresso.onView(withId(R.id.textViewName)).check(matches(isDisplayed()));
    }
}