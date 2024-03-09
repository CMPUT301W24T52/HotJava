package com.example.hotevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

//@RunWith(AndroidJUnit4.class)
@LargeTest
public class CreateEventActivityTest {
    @Rule
    public ActivityScenarioRule<CreateEventActivity> scenario =
            new ActivityScenarioRule<CreateEventActivity>(CreateEventActivity.class);


    //Test start date
    @Test
    public void A_testStartDate(){
        //onView(withParent(withId(R.id.start_cal_button)), withId()
        onView(withId(R.id.start_cal_button)).perform(click());
        onView(withClassName(Matchers.equalTo(DatePicker.class.getName()))).
                perform(PickerActions.setDate(2024, 3, 7));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withClassName(Matchers.equalTo(TimePicker.class.getName()))).
                perform(PickerActions.setTime(3, 30));
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.start_date_text)).check(matches(withText("02/07/2024")));
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
        onView(withId(R.id.end_date_text)).check(matches(withText("03/07/2024")));
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
    public void D_testCreateEvent(){
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

    //Checking whether the back button navigates back to the home page or not
    @Test
    public void E_testBackButton(){
        onView(withId(R.id.back_button)).perform(click());
        onView(withId(R.id.back_button)).check(doesNotExist());
    }

}
