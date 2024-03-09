package com.example.hotevents;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {

    Event myEvent;
    ImageButton backButton;
    ImageView eventImage;
    TextView eventTitle;
    TextView startDate;
    TextView endDate;
    TabLayout tabLayout;
    ViewPager2 viewPager2;
    EventPagerAdapter eventPagerAdapter;
    TextView organiserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        myEvent = (Event) getIntent().getSerializableExtra("event");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        setViews();

        if (myEvent.getTitle() != null) {
            eventTitle.setText(myEvent.getTitle());
        }


        if (myEvent.getStartDateTime() != null) {
            startDate.setText(myEvent.getStartDateTime().toString());
        }

        if (myEvent.getEndDateTime() != null) {
            endDate.setText(myEvent.getEndDateTime().toString());
        }

//        Location missing
        if (myEvent.getOrganiserId() != null && !myEvent.getOrganiserId().trim().isEmpty()) {
            db.collection("Users").document(myEvent.getOrganiserId()).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    organiserName.setText(document.getString("Name"));
                }
            });
        }
//        Notifications not implemented yet



        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

    }

    private void setViews() {
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        eventTitle = findViewById(R.id.event_title);
        startDate = findViewById(R.id.event_start_date);
        endDate = findViewById(R.id.event_end_date);
        organiserName = findViewById(R.id.organiser_name);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager2 = findViewById(R.id.view_pager);
        eventPagerAdapter = new EventPagerAdapter(this);
        viewPager2.setAdapter(eventPagerAdapter);
    }
    private class EventPagerAdapter extends FragmentStateAdapter {

        public EventPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 1) {
                return new EventDetailsAnnouncementFragment();
            }
            return EventDetailsAboutFragment.newInstance(myEvent.getDescription());
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}