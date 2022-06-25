package msc.uen1.M;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Help extends Toolbar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        initToolbar(R.id.toolbar);


    }
}