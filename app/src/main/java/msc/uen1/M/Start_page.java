package msc.uen1.M;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import msc.uen1.M.Fragments.fragment_one;
import msc.uen1.M.Fragments.fragment_three;
import msc.uen1.M.Fragments.fragment_two;
import msc.uen1.M.Fragments.fragment_zero;

public class Start_page extends FragmentActivity {

    private ViewPager2 vPage;
    private FragmentStateAdapter adapter;
    private Button btn;
    //private Toolbar toolbar;

    String prevStarted = "yes";
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedpreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        if (!sharedpreferences.getBoolean(prevStarted, false)) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(prevStarted, Boolean.TRUE);
            editor.apply();
        } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        // Add Fragments
        List<Fragment> fLIst = new ArrayList<Fragment>();
        fLIst.add(new fragment_zero());
        fLIst.add(new fragment_one());
        fLIst.add(new fragment_two());
        fLIst.add(new fragment_three());

        vPage = findViewById(R.id.vPager2);
        adapter = new Adapter(this, fLIst);
        vPage.setAdapter(adapter);

        btn = findViewById(R.id.btnStart);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

    }
    @Override
    public void onBackPressed() {
        if (vPage.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            vPage.setCurrentItem(vPage.getCurrentItem() - 1);
        }
    }
}