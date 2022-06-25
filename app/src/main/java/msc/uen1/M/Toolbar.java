package msc.uen1.M;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Toolbar extends AppCompatActivity {

    private androidx.appcompat.widget.Toolbar tb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toolbar);

        // Set Toolbar
        tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        tb.inflateMenu(R.menu.main_menu);

    }

    public void initToolbar(int toolbarId) {
        androidx.appcompat.widget.Toolbar tb;
        tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        //tb.inflateMenu(R.menu.main_menu);

        tb.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.folder) {
                //Toast.makeText(getApplicationContext(), "You clicked Folder!!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(getApplicationContext(), Folder.class));
                return true;
            }
            else  if (id == R.id.app_bar_search) {
                Toast.makeText(getApplicationContext(), "Not Implemented !!", Toast.LENGTH_LONG).show();
                return true;
            }
            else  if (id == R.id.help ) {
                startActivity(new Intent(getApplicationContext(), Help.class));
                return true;
            }
            else {  return false; }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    //public void onOptionsItemSelected(MenuItem item) {}
}
