package com.example.hotevents;

import android.content.Intent;
import android.view.KeyEvent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import org.junit.Rule;
import org.junit.Test;


import java.util.ArrayList;

@LargeTest
public class ViewEventActivitiesTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testSearchEvent(){
        // test search view window
        onView(withId(R.id.searchView)).perform(scrollTo());
        onView(withId(R.id.searchView)).perform(click());
        //Type value into search and submit
        onView(withId(R.id.searchView)).perform(pressKey(KeyEvent.KEYCODE_A)).perform(pressKey(KeyEvent.KEYCODE_ENTER));
        onView(withId(R.id.searchView)).check(doesNotExist());
    }

    @Test
    public void testOpenSignedUpEventsActivity(){
        onView(withId(R.id.signedUpEvents_button)).perform(scrollTo(), click());
        onView(withId(R.id.signedUpEvents_button)).check(doesNotExist());
    }
    @Test
    public void testOpenUpcomingEventsActivity(){
        onView(withId(R.id.upcomingEventsList_button)).perform(scrollTo(), click());
        onView(withId(R.id.upcomingEventsList_button)).check(doesNotExist());
    }
}
