package msc.uen1.M.Main;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;

import msc.uen1.M.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ImageButton addBtn;
    private ImageView preview;
    private ActivityResultLauncher<Intent> getImageFromDevice;
    private ActivityResultLauncher<Intent> getImageFromCamera;
    View view;
    private Uri imageUri;
    private  Bitmap imgBitmap;
    private static Bitmap fragBitmap;
    private  FragmentViewModel fragmentViewModel;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EditFragment() {}

    public static EditFragment newInstance(String param1, String param2) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        //args.putParcelable("bitmap", fragBitmap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentViewModel = new ViewModelProvider(getActivity()).get(FragmentViewModel.class);
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_edit, container, false);

        addBtn = view.findViewById(R.id.addImage);
        preview = view.findViewById(R.id.previewImage);

        getImageFromDevice = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        //checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 101);
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            imageUri = data.getData();
                            try {
                                fragBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            preview.setImageURI(imageUri);
                            fragmentViewModel.setBitmap(fragBitmap);
                            //sendBundle2();
                        } //
                    }
                });
        getImageFromCamera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Bundle bundle = result.getData().getExtras();
                            imgBitmap = (Bitmap) bundle.get("data");
                            fragBitmap = imgBitmap;
                            preview.setImageBitmap(imgBitmap);
                            fragmentViewModel.setBitmap(fragBitmap);
                            //sendBundle2();
                        }
                    }
                });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select a way to Upload");// add a list
                String[] animals = {"Gallery", "Camera"};
                builder.setItems(animals, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // Gallery
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                                getImageFromDevice.launch(intent);
                                break;
                            case 1: // Camera
                                Intent intentCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                getImageFromCamera.launch(intentCam);
                                break;
                        }
                    }
                });// create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return view;
    }

    public void checkPermission() {
        //Cited: https://www.android--code.com/2017/08/android-request-multiple-permissions.html
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(

                    getActivity(),
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 123
            );
        } else {
            Toast.makeText(getContext(), "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }


}