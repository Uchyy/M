package msc.uen1.M.Main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;

import org.opencv.android.OpenCVLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import msc.uen1.M.MainActivity;
import msc.uen1.M.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetectFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View view;
    Button previewButton, uplButton, detButton;
    ImageView previewImg;
    Bitmap imgBitmap, inputimage;
    Bitmap temp2;
    Canvas canvas;
    Paint paint = new Paint();
    InputImage image;
    int imgHeight, imgWidth;
    private FragmentViewModel fragmentViewModel;
    private StorageReference sRef;
    private DatabaseReference dRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    ProgressBar pBar;
    EditText angleText;
    ImageButton rotButton;
    String angle;
    Integer number;
    Boolean clicked = false;


    public DetectFragment() {
        // Required empty public constructor
    }

    public static DetectFragment newInstance(String param1, String param2) {
        DetectFragment fragment = new DetectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        previewButton = view.findViewById(R.id.preview3);
        previewImg = view.findViewById(R.id.imageView4);

        fragmentViewModel = new ViewModelProvider(getActivity()).get(FragmentViewModel.class);
        previewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentViewModel.getBitmap().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
                    @Override
                    public void onChanged(Bitmap bitmap) {
                        imgBitmap = bitmap;
                        previewImg.setImageBitmap(imgBitmap);
                        clicked = true;
                    }
                });
            }
        });

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
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_detect, container, false);
        OpenCVLoader.initDebug();

        uplButton = view.findViewById(R.id.uplButton);
        detButton = view.findViewById(R.id.detButton);
        rotButton = view.findViewById(R.id.imageButton2);
        angleText = view.findViewById(R.id.angleText2);
        sRef = storage.getReference();
        pBar = view.findViewById(R.id.progressBar5);
        pBar.setVisibility(View.GONE);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);


        rotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!clicked) {
                    fragmentViewModel.getBitmap().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
                        @Override
                        public void onChanged(Bitmap bitmap) {
                            imgBitmap = bitmap;
                        }
                    });
                }
                angle = angleText.getText().toString();
                number = Integer.valueOf(angle);

                if (checkfields()) {
                    //rotateImage(image, angle);
                    imgBitmap = rotateBitmap(imgBitmap, number);
                    previewImg.setImageBitmap(imgBitmap);
                }
                
            }
        });

        uplButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!clicked) {
                    fragmentViewModel.getBitmap().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
                        @Override
                        public void onChanged(Bitmap bitmap) {
                            imgBitmap = bitmap;
                        }
                    });
                }
                    getUsername();
            }
        });

        detButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    objectDetector();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        return view;
    }

    private Bitmap rotateBitmap(Bitmap original, float degrees) {
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        original.recycle();
        return rotatedBitmap;
    }

    public  Boolean checkfields() {
        if (angle.isEmpty()) {
            angleText.setError("Invalid Syntax: Field cannot be empty");
            return  false;
        }
        if (!angle.matches("[0-9]+")) {
            angleText.setError("Invalid Syntax: Field must contain digits!");
            return  false;
        }
        if (number < 0) {
            angleText.setError("Invalid Syntax: Angle can't be less than zero");
            return  false;
        }
        if (number > 360) {
            angleText.setError("Invalid Syntax: Angle can't be greater than 360");
            return  false;
        }

        return true;
    }

    private void getUsername() {
        //Cited from: https://techicaltutorial.blogspot.com/2021/02/android-studio-popup-window-with-input.html
        String userN;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Enter a Username");
        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pBar.setVisibility(View.VISIBLE);
                String user = input.getText().toString();

                //Upload to gs://meshach-uen1.appspot.com/files
                 if (imgBitmap != null){

                    StorageReference userPath = sRef.child(user+ "/" + System.currentTimeMillis() + ".jpeg" );
                    // Get the data from an ImageView as bytes
                     previewImg.setDrawingCacheEnabled(true);
                     previewImg.buildDrawingCache();
                     ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                     byte[] data = baos.toByteArray();

                    UploadTask uploadTask = userPath.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle unsuccessful uploads
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            Toast.makeText(getContext(), "File Uploaded successfully!!!", Toast.LENGTH_LONG).show();
                            pBar.setVisibility(View.GONE);
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Image is Null!", Toast.LENGTH_LONG).show();
                    pBar.setVisibility(View.GONE);
                }
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

    private void objectDetector() throws IOException {
        //Bitmap bitmap = BitmapFactory.decodeFile(imageUri.get);
        if (!clicked) {
            fragmentViewModel.getBitmap().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
                @Override
                public void onChanged(Bitmap bitmap) {
                    imgBitmap = bitmap;
                }
            });
        }

        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("footprints_model_uen1.tflite")
                        .build();

        Log.d("Model: ", localModel.toString());

        CustomObjectDetectorOptions customObjectDetectorOptions =
                new CustomObjectDetectorOptions.Builder(localModel)
                        .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .setClassificationConfidenceThreshold(0.8f)
                        .build();

        ObjectDetector detector = com.google.mlkit.vision.objects.ObjectDetection.getClient(customObjectDetectorOptions);

         if (imgBitmap != null){
            inputimage = imgBitmap;
            image = InputImage.fromBitmap(inputimage, 0);
            imgWidth = image.getWidth();
            imgHeight = image.getHeight();

        } else {
            Toast.makeText(getContext(), "No image was detected!!", Toast.LENGTH_LONG).show();
            return;
        }

        detector
                .process(image)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),  "Model Failed:" + e.getMessage() , Toast.LENGTH_LONG).show();
                        Log.d("Model Error: ", e.getMessage() );

                    }
                }).addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                    @Override
                    public void onSuccess(List<DetectedObject> detectedObjects) {
                        if (detectedObjects.size() > 0){

                            for (DetectedObject detectedObject : detectedObjects) {

                                Rect boundingBox = detectedObject.getBoundingBox();
                                Integer trackingId = detectedObject.getTrackingId();

                                temp2 =  Bitmap.createBitmap(inputimage.getWidth(), inputimage.getHeight(), Bitmap.Config.RGB_565);
                                canvas = new Canvas(temp2);
                                canvas.drawBitmap(inputimage, 0, 0 , null);
                                canvas.drawRect(boundingBox, paint);
                                previewImg.setImageBitmap(temp2);

                                Toast.makeText(getContext(), "Footprint was detected!!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            previewImg.setImageBitmap(imgBitmap);
                            Toast.makeText(getContext(), "No Footprint was detected!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}