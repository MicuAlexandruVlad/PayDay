package com.example.payday;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ReserveFragment.SendReserves {
    private ViewPager pager;
    //private TabLayout tabLayout;

    private User user;
    private List<Operation> operations;
    private List<Reserve> finalisedReserves, unfinalisedReseves;
    private Intent parentIntent;
    private MainPagerAdapter adapter;
    private DBLinks dbLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Bundle dashboardBundle = new Bundle();
        Bundle reservesBundle = new Bundle();

        pager = findViewById(R.id.viewpager);
        //tabLayout = findViewById(R.id.tab_layout);

        parentIntent = getIntent();
        dbLinks = new DBLinks();

        operations = new ArrayList<>();
        user = (User) parentIntent.getSerializableExtra("currentUser");
        operations = (List<Operation>) parentIntent.getSerializableExtra("operations");
        finalisedReserves = (List<Reserve>) parentIntent.getSerializableExtra("finalised_reserves");
        unfinalisedReseves = (List<Reserve>) parentIntent.getSerializableExtra("unfinalised_reserves");
        // send data to pager adapter using bundles
        dashboardBundle.putSerializable("operations", (Serializable) operations);
        dashboardBundle.putSerializable("finalised_reserves", (Serializable) finalisedReserves);
        dashboardBundle.putSerializable("unfinalised_reserves", (Serializable) unfinalisedReseves);
        reservesBundle.putSerializable("operations", (Serializable) operations);
        reservesBundle.putSerializable("finalised_reserves", (Serializable) finalisedReserves);
        reservesBundle.putSerializable("unfinalised_reserves", (Serializable) unfinalisedReseves);


        adapter = new MainPagerAdapter(getSupportFragmentManager(), dashboardBundle, reservesBundle, user);
        pager.setAdapter(adapter);
        //tabLayout.setupWithViewPager(pager);
    }

    // send new reserves to dashboardFragment
    @Override
    public void sendData(List<Reserve> reserves) {
        String tag = "android:switcher:" + R.id.viewpager + ":" + 0;
        DashboardFragment dashboardFragment = (DashboardFragment) getSupportFragmentManager()
                .findFragmentByTag(tag);
        dashboardFragment.receiveReserves(reserves);
    }
}
