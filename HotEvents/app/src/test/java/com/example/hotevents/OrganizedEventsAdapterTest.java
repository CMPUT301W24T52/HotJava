package com.example.hotevents;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;

public class OrganizedEventsAdapterTest {

    private OrganizedEventsAdapter adapter;
    private ArrayList<Event> events;

    @Before
    public void setUp() {
        events = new ArrayList<>();
        events.add(new Event(new Date(), new Date(), 100, "organiserId123", null,
                null, mock(QRCodes.class), "Test description", "Test title",
                "eventId123", "Test location"));

        adapter = new OrganizedEventsAdapter(events, mock(Context.class));
    }

    @Test
    public void getItemCount_withNonEmptyList_returnsListSize() {
        assertEquals(events.size(), adapter.getItemCount());
    }

    @Test
    public void getItemCount_withEmptyList_returnsZero() {
        adapter = new OrganizedEventsAdapter(new ArrayList<>(), mock(Context.class));
        assertEquals(0, adapter.getItemCount());
    }
}
