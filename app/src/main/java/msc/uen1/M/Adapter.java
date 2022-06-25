package msc.uen1.M;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends FragmentStateAdapter {

    private List <Fragment> fLIst = new ArrayList<Fragment>();


    public Adapter(@NonNull FragmentActivity fragmentActivity, List <Fragment> fLIst) {
        super(fragmentActivity);
        this.fLIst = fLIst;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fLIst.get(position);
    }

    @Override
    public int getItemCount() {
        return fLIst.size();
    }
}


