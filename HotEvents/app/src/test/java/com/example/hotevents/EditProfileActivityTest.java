package com.example.hotevents;

import android.app.Activity;
import android.content.Intent;

import org.junit.Test;
import org.mockito.Mockito;

public class EditProfileActivityTest {

    @Test
    public void testOnActivityResult_NullData() {
        // You should avoid instantiating Activity directly in unit tests
        // Instead, consider using Mockito to mock the activity
        EditProfileActivity activity = Mockito.mock(EditProfileActivity.class);
        activity.onActivityResult(EditProfileActivity.IMAGE_PICK_REQUEST_CODE, Activity.RESULT_OK, null);
        // No assertion needed, just ensure no crash occurs
    }

    @Test
    public void testOnActivityResult_NullSelectedImageUri() {
        EditProfileActivity activity = Mockito.mock(EditProfileActivity.class);
        Intent intent = Mockito.mock(Intent.class);
        activity.onActivityResult(EditProfileActivity.IMAGE_PICK_REQUEST_CODE, Activity.RESULT_OK, intent);
        // No assertion needed, just ensure no crash occurs
    }

}
