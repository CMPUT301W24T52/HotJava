package com.example.hotevents;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.content.Context;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class MyEventsAdapterTest {

    @Test
    public void getItemCount_withNonEmptyList_returnsListSize() {
        ArrayList<Event> events = new ArrayList<>();
        Date startDate = new Date(); // Example start date
        Date endDate = new Date(); // Example end date
        events.add(new Event(startDate, endDate, 50, "sdgwioegsjdgjsk",
                null, "gs://hotevents-hotjava.appspot.com/Event Images/poster_3Ir78m6Dcf6ZKC9RnFfT.jpg",
                mock(QRCodes.class), "this is the description", "Event title", "sdSDGJKSDsdjsjdk", "Address"));
        events.add(new Event(startDate, endDate, 50, "sdgwioegsjdgjsk",
                null, "gs://hotevents-hotjava.appspot.com/Event Images/poster_3Ir78m6Dcf6ZKC9RnFfT.jpg",
                mock(QRCodes.class), "this is the description", "Event title", "sdSDGJKSDsdjsjdk", "Address"));
        MyEventsAdapter adapter = new MyEventsAdapter(events, mock(Context.class));
        assertEquals(events.size(), adapter.getItemCount());
    }

    @Test
    public void getItemCount_withEmptyList_returnsZero() {
        MyEventsAdapter adapter = new MyEventsAdapter(new ArrayList<>(), mock(Context.class));
        assertEquals(0, adapter.getItemCount());
    }

}
