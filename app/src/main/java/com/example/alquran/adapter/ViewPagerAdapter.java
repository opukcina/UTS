package com.example.alquran.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.alquran.ui.QuranFragment;
import com.example.alquran.ui.LoginInfoFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new QuranFragment();
            case 1:
                return new LoginInfoFragment();
            default:
                return new QuranFragment(); // fallback
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
