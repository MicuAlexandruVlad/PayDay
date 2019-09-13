package com.example.payday;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import java.util.List;

public class MainPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {

    public static final int NUM_ITEMS = 2;
    private String tabTitles[] = new String[] {"Dashboard", "Reserves"};
    private Bundle dashboardBundle, reservesBundle;
    private String symbol;
    private User currentUser;

    public MainPagerAdapter(FragmentManager fragmentManager, Bundle dashboardBundle, Bundle reservesBundle, User user) {
        super(fragmentManager);
        this.dashboardBundle = dashboardBundle;
        this.currentUser = user;
        this.reservesBundle = reservesBundle;
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0: {
                return DashboardFragment.newInstance(0, "Dashboard", dashboardBundle, currentUser);
            }
            case 1: {
                return ReserveFragment.newInstance(0, "Reserves", reservesBundle, currentUser);
            }
            default: return null;

        }
    }



    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}
