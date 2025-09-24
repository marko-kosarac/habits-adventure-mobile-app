package com.example.mobilnaaplikacija.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mobilnaaplikacija.fragments.friends.TabFriendsFragment;
import com.example.mobilnaaplikacija.fragments.friends.TabAllUsersFragment;
import com.example.mobilnaaplikacija.fragments.friends.FriendsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FriendsFragment fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new TabFriendsFragment(); // Tab za prijatelje
            case 1:
                return new TabAllUsersFragment(); // Tab za sve korisnike
            default:
                return new TabFriendsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Imamo dva taba
    }
}
