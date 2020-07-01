package com.example.stepcounterapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionPagerAdapter extends FragmentPagerAdapter {

    // this adapter to display three fragments next to each other on the MainActivity.

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new StepsFragment();
            case 1:
                return new StepConverterFragment();
            case 2:
                return new ReportFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Steps";
            case 1:
                return "Converter";
            case 2:
                return "Report";

            default:
                return null;
        }
    }
}
