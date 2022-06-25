package msc.uen1.M.Main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterTabLayout extends FragmentStateAdapter {

    public AdapterTabLayout(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new EditFragment();
                break;
            case 1:
                fragment = new ProcessFragment();
                break;
            case 2:
                fragment = new DetectFragment();
                break;
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
