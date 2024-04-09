package com.example.hotevents;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import android.util.Log;
import android.widget.ListView;

import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class BrowseProfilesActivityTest {

    @Rule
    public ActivityScenarioRule<BrowseProfilesActivity> activityScenarioRule =
            new ActivityScenarioRule<>(BrowseProfilesActivity.class);

    @Test
    public void testBrowseProfilesActivityUI() {
        // Wait for the ListView to be displayed
        try {
            Thread.sleep(2000); // Adjust delay time as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Check if ListView is displayed
        Espresso.onView(withId(R.id.usersList)).check(matches(isDisplayed()));

        // Retrieve the ListView's height
        ActivityScenario<BrowseProfilesActivity> scenario = activityScenarioRule.getScenario();
        scenario.onActivity(activity -> {
            ListView listView = activity.findViewById(R.id.usersList);
            Log.d("ListViewHeight", "Height: " + listView.getHeight());
        });

        // Perform click on the first item in the ListView
        Espresso.onView(withId(R.id.usersList)).perform(ViewActions.click());

        // Check if the back button is displayed
        Espresso.onView(withId(R.id.backButton)).check(matches(isDisplayed()));

        // Perform click on the back button
        Espresso.onView(withId(R.id.backButton)).perform(ViewActions.click());

        // Check if the ListView is displayed again after clicking back
        Espresso.onView(withId(R.id.usersList)).check(matches(isDisplayed()));
    }
}
