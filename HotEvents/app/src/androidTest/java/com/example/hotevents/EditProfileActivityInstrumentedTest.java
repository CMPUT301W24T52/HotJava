package com.example.hotevents;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class EditProfileActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<EditProfileActivity> activityRule =
            new ActivityScenarioRule<>(EditProfileActivity.class);

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);

    @Test
    public void testProfilePhotoButtonDisplayed() {
        onView(withId(R.id.editProfilePhotoButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testProfilePhotoButtonClickable() {
        onView(withId(R.id.editProfilePhotoButton)).perform(click());
    }

    @Test
    public void testSaveButtonDisplayed() {
        onView(withId(R.id.saveProfile)).check(matches(isDisplayed()));
    }

    @Test
    public void testSaveButtonClickable() {
        onView(withId(R.id.saveProfile)).perform(click());
    }

    @Test
    public void testEditNameField() {
        onView(withId(R.id.edit_text_name)).perform(typeText("John Doe")).check(matches(withText("John Doe")));
    }

    @Test
    public void testEditEmailField() {
        onView(withId(R.id.edit_text_email)).perform(typeText("john@example.com")).check(matches(withText("john@example.com")));
    }

    @Test
    public void testEditContactField() {
        onView(withId(R.id.edit_text_contact)).perform(typeText("1234567890")).check(matches(withText("1234567890")));
    }

    @Test
    public void testEditLocationField() {
        onView(withId(R.id.edit_text_location)).perform(typeText("New York")).check(matches(withText("New York")));
    }
}
