package msc.uen1.M.Main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import msc.uen1.M.MainActivity;
import msc.uen1.M.R;


public class MainActivity3 extends msc.uen1.M.Toolbar {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FragmentStateAdapter adapterTablayout;
    String[] titles = new String[]{"Input", "Analyze", "Detect"};
    Toolbar toolbar;
    private FragmentViewModel fragmentViewModel;
    Bitmap b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        initToolbar(R.id.toolbar);

        fragmentViewModel = new ViewModelProvider(this).get(FragmentViewModel.class);
        //fragmentViewModel.getBitmap().observe(this, bitmap -> );

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager2);

        adapterTablayout = new AdapterTabLayout(this);
        viewPager.setAdapter(adapterTablayout);

        viewPager.setOffscreenPageLimit(3);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) ->
        tab.setText(titles[position])
        ).attach();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}