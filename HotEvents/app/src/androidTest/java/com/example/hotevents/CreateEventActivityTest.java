package com.example.hotevents;

import static androidx.test.core.app.ActivityScenario.launch;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

//@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventActivityTest {
    @Rule
    public ActivityScenarioRule<CreateEventActivity> scenario =
            new ActivityScenarioRule<CreateEventActivity>(CreateEventActivity.class);

    // Test max attendees
    @Test
    public void testMaxAttendees() {
        onView(withId(R.id.max_attendee_switch)).perform(scrollTo(), click());
        onView(withId(R.id.max_attendee_input_text)).perform(scrollTo());
        onView(withId(R.id.max_attendee_input_text)).check(matches(isDisplayed()));
        onView(withId(R.id.max_attendee_switch)).perform(click());
        onView(withId(R.id.max_attendee_input_text)).check(matches(not(isDisplayed())));
    }

    // Test create event success
    @Test
    public void testCreateEvent() {
        // Have to create event to pass in
        QRCodes qrCode = new QRCodes("hotevents:checkin:3Ir78m6Dcf6ZKC9RnFfT");

        Event event = new Event(new Date(), new Date(), null, "4a2d37f1b5970890",
                null, "gs://hotevents-hotjava.appspot.com/Event Images/poster_3Ir78m6Dcf6ZKC9RnFfT.jpg",
                qrCode, null, null, "s3qgSrVJ4PwJDYd86ssz", null);

        // Create an intent with extras
        Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
        intent.putExtra("event", (Parcelable) event);
        intent.putExtra("State", false);

        // Launch activity with the intent
        ActivityScenario<CreateEventActivity> scenario = launch(intent);

        // Setting title
        onView(withId(R.id.title_text)).perform(click());
        onView(withId(R.id.title_text)).perform(ViewActions.typeText("Title"));

        // Address text
        onView(withId(R.id.location_input_text)).perform(scrollTo(), click());
        onView(withId(R.id.location_input_text)).perform(ViewActions.typeText("Address"));

        // Description text
        onView(withId(R.id.description_input_text)).perform(scrollTo(), click());
        onView(withId(R.id.description_input_text)).perform(ViewActions.typeText("Description"));

        // QR Code
        onView(withId(R.id.qrcode_create_button)).perform(scrollTo(), click());

        // Create click
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
    }

    // Testing when the event was failed to be created due to some fields being empty
    @Test
    public void testCreateFail() {
        // Create click
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.create_event_button)).check(matches(isDisplayed()));
    }

    // Checking whether the back button navigates back to the home page or not
    @Test
    public void testBackButton() {
        ActivityScenario<CreateEventActivity> activityScenario = ActivityScenario.launchActivityForResult(CreateEventActivity.class);
        // Let's say MyActivity has a button that finishes itself.
        onView(withId(R.id.backButton)).perform(click());
        assertEquals(Activity.RESULT_CANCELED, activityScenario.getResult().getResultCode());
    }

    // Testing the update event functionality
    // Update event is triggered when an event is passed in as an intent to the page
    @Test
    public void testUpdateEvent() {
        // Have to create event to pass in
        QRCodes qrCode = new QRCodes("hotevents:checkin:3Ir78m6Dcf6ZKC9RnFfT");

        Event event = new Event(new Date(), new Date(), null, "4a2d37f1b5970890",
                null, "gs://hotevents-hotjava.appspot.com/Event Images/poster_3Ir78m6Dcf6ZKC9RnFfT.jpg",
                qrCode, "Description", "Title", "s3qgSrVJ4PwJDYd86ssz", "Address");

        // Create an intent with extras
        Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
        intent.putExtra("event", (Parcelable) event);
        intent.putExtra("State", false);

        // Launch activity with the intent
        ActivityScenario<CreateEventActivity> scenario = launch(intent);

        // Waiting for the fields to get populated
        onView(isRoot()).perform(waitFor(10000));

        // Create click for update
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
    }

    // Perform action of waiting for a specific time
    // Reference: https://stackoverflow.com/questions/21417954/thread-sleep-with-espresso
    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }


}
