package com.example.hotevents;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

//@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventActivityTest {
    @Rule
    public ActivityScenarioRule<CreateEventActivity> scenario =
            new ActivityScenarioRule<CreateEventActivity>(CreateEventActivity.class);


    //Test start date
    @Test
    public void A_testStartDate(){
        onView(withId(R.id.start_cal_button)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).
                perform(PickerActions.setDate(2024, 3, 7));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).
                perform(PickerActions.setTime(3, 30));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.start_date_text)).check(matches(withText("03/07/2024")));
        onView(withId(R.id.start_time_text)).check(matches(withText("03:30")));
    }

    //Test end date
    @Test
    public void B_testEndDate(){
        onView(withId(R.id.end_cal_button)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).
                perform(PickerActions.setDate(2024, 4, 7));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).
                perform(PickerActions.setTime(3, 30));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.end_date_text)).check(matches(withText("04/07/2024")));
        onView(withId(R.id.end_time_text)).check(matches(withText("03:30")));
    }

    //Test max attendees
    @Test
    public void C_testMaxAttendees(){
        onView(withId(R.id.max_attendee_switch)).perform(click());
        onView(withId(R.id.max_attendee_input_text)).check(matches(isDisplayed()));
        onView(withId(R.id.max_attendee_switch)).perform(click());
        onView(withId(R.id.max_attendee_input_text)).check(matches(not(isDisplayed())));
    }

    //Test create event success
    @Test
    public void F_testCreateEvent(){
        //Setting title
        onView(withId(R.id.title_text)).perform(click());
        onView(withId(R.id.title_text)).perform(ViewActions.typeText("Title"));

        //Setting start date
        onView(withId(R.id.start_cal_button)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).
                perform(PickerActions.setDate(2024, 3, 7));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).
                perform(PickerActions.setTime(3, 30));
        onView(withId(android.R.id.button1)).perform(click());

        //Setting end date
        onView(withId(R.id.end_cal_button)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).
                perform(PickerActions.setDate(2024, 4, 7));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).
                perform(PickerActions.setTime(3, 30));
        onView(withId(android.R.id.button1)).perform(click());

        //Address text
        onView(withId(R.id.location_input_text)).perform(click());
        onView(withId(R.id.location_input_text)).perform(ViewActions.typeText("Address"));

        //Description text
        onView(withId(R.id.description_input_text)).perform(scrollTo(), click());
        onView(withId(R.id.description_input_text)).perform(ViewActions.typeText("Title"));

        //QR Code
        onView(withId(R.id.qrcode_create_button)).perform(scrollTo(), click());

        //Create click
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
    }

    //Test choose QR code
    @Test
    public void E_testChooseQRCode(){
        //Waiting for firebase to return the user details
        //onView(isRoot()).perform(waitFor(10000));

        onView(withId(R.id.qrcode_choose_spinner)).perform(scrollTo(), click());
        onData(anything()).atPosition(1).perform(scrollTo(), click());
        onView(withId(R.id.qrcode_choose_spinner)).check(matches(withSpinnerText(containsString("hotevents:checkin"))));
    }

    //Testing when the event was failed to be created due to some fields being empty
    @Test public void F_testCreateFail(){
        //Create click
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
        onView(withId(R.id.create_event_button)).check(matches(isDisplayed()));
    }

    //Checking whether the back button navigates back to the home page or not
    @Test
    public void G_testBackButton(){
        onView(withId(R.id.back_button)).perform(click());
        onView(withId(R.id.back_button)).check(doesNotExist());
    }

    //Testing the update event functionality
    //Update event is triggered when an event is passed in as an intent to the page
    @Test
    public void H_testUpdateEvent(){
        //Have to create event to pass in
        QRCodes qrCode = new QRCodes("hotevents:checkin:3Ir78m6Dcf6ZKC9RnFfT");

        Event event = new Event(new Date(), new Date(), null, "4a2d37f1b5970890",
                null, "gs://hotevents-hotjava.appspot.com/Event Images/poster_3Ir78m6Dcf6ZKC9RnFfT.jpg",
                qrCode, "Description", "Title", "s3qgSrVJ4PwJDYd86ssz", "Address");

        // Create an intent with extras
        Intent intent = new Intent(getApplicationContext(), CreateEventActivity.class);
        intent.putExtra("event", (Parcelable) event);

        // Launch activity with the intent
        ActivityScenario<CreateEventActivity> scenario = ActivityScenario.launch(intent);

        //Waiting for the fields to get populated
        onView(isRoot()).perform(waitFor(10000));

        //Create click for update
        onView(withId(R.id.create_event_button)).perform(scrollTo(), click());
    }

    //Perform action of waiting for a specific time
    //Reference: https://stackoverflow.com/questions/21417954/thread-sleep-with-espresso
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
