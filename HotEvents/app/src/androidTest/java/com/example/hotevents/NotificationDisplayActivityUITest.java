package com.example.hotevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;

public class NotificationDisplayActivityUITest {

    private ActivityScenario<NotificationDisplayActivity> activityScenario;

    @Before
    public void setUp() {
        activityScenario = ActivityScenario.launch(NotificationDisplayActivity.class);
    }

    @Test
    public void notificationActivityComponentsDisplayed() {
        // Check if the components of the notification activity are displayed
        onView(withId(R.id.notification_toolbar)).check(matches(isDisplayed()));
        onView(withId(R.id.backButton)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_textview)).check(matches(isDisplayed()));
        onView(withId(R.id.notification_listview)).check(matches(isDisplayed()));
    }
}
