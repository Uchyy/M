package msc.uen1.M;

import static androidx.camera.core.CameraX.getContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import msc.uen1.M.Main.MainActivity3;

public class Folder extends AppCompatActivity implements  ItemClickListener {

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference sRef;
    ImageView img;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    ArrayList<String> listitems;
    TextView textView;
    DividerItemDecoration dividerItemDecoration;
    String username;
    Button back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        back = findViewById(R.id.backButton);
        sRef = storage.getReference();
        recyclerView = findViewById(R.id.reView);
        textView = findViewById(R.id.photoText);
        listitems = new ArrayList<String>();
        adapter = new MyAdapter(listitems, this, this::onClick);
        //adapter.setC
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                new LinearLayoutManager(this).getOrientation());
        dividerItemDecoration.setDrawable(
                Objects.requireNonNull(ContextCompat.getDrawable(Folder.this, R.drawable.divider))
        );
        recyclerView.addItemDecoration(dividerItemDecoration);

        getUsername();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MainActivity3.class));
            }
        });
    }

    private void getUsername() {
        //Cited from: https://techicaltutorial.blogspot.com/2021/02/android-studio-popup-window-with-input.html
        AlertDialog.Builder builder = new AlertDialog.Builder(Folder.this);
        builder.setTitle("Enter a Username");
        EditText input = new EditText(Folder.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //pBar.setVisibility(View.VISIBLE);
                username = input.getText().toString().trim();
                Toast.makeText(getApplicationContext(), "Username is: " + username, Toast.LENGTH_LONG).show();

                //Get Userfiles from Cloud Storage and download into device.  Cited from https://firebase.google.com/docs/storage/android/list-files
                StorageReference userPath = storage.getReference().child(username);
                userPath.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        //for (StorageReference prefix : listResult.getPrefixes()) { // All the prefixes under listRef. // You may call listAll() recursively on them.}
                        for (StorageReference item : listResult.getItems()) {
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    listitems.add(uri.toString());
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    recyclerView.setAdapter(adapter);
                                    String size =  Integer.toString(listitems.size());
                                    textView.setText("Photos: " + size);
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    @Override
    public void onClick(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Folder.this);
        builder.setTitle("Select an Action: ");

        builder.setPositiveButton("DOWNLOAD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String urlString = listitems.get(position);
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/M";
                File directory = new File(path);
                if (!directory.exists()) directory.mkdirs();
                String filename = directory + "/" + System.currentTimeMillis() + ".png";

                try {
                    downloadImageToDevice(path, urlString);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //dialog.cancel();
                //int pos = recyclerView.getChildAdapterPosition(recyclerView.getFocusedChild());
                String string = listitems.get(position);
                Log.e("url", string);
                StorageReference userPath = storage.getReferenceFromUrl(string);
                userPath.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(getApplicationContext(),"File Deleted Successfully!!", Toast.LENGTH_LONG).show();
                        //Cited from: https://www.tutorialspoint.com/how-to-reload-activity-in-android
                        //Restart Activity
                        recreate();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        builder.show();
    }

    private void downloadImageToDevice(String filename, String urlString) throws MalformedURLException {
        //URL url = new URL(urlString);
        try{
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(urlString);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle("Downloading M....")
                    .setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,File.separator + System.currentTimeMillis() + ".jpg");
            downloadManager.enqueue(request);
            Toast.makeText(this, "Image download started.", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}