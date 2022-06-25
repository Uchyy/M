package msc.uen1.M;

import static androidx.camera.core.CameraX.getContext;
import static com.google.mlkit.vision.objects.ObjectDetection.getClient;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.ImageCapture;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.interfaces.Detector;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.squareup.picasso.Picasso;

import org.opencv.android.OpenCVLoader;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Toolbar implements AdapterView.OnItemSelectedListener {
    Toolbar tb = new Toolbar();
    ImageView img, processedImg;
    Button btn, processBtn;
    EditText processText, messageText;
    Spinner spinner;
    private ActivityResultLauncher<Intent> getImageFromDevice;
    private ActivityResultLauncher<Intent> getImageFromCamera;
    String username;
    private StorageReference sRef;
    private DatabaseReference dRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    Uri imageUri;
    ProgressBar pBar;
    Preprocessor preprocessor;
    Bitmap imgBitmap, inputimage;
    Canvas canvas;
    Paint paint = new Paint();
    InputImage image;
    int imgHeight, imgWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermission();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(10);

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "Unable to load OpenCV");
        } else {
            Log.e("OpenCV", "OpenCV Loaded!!");
        }

        img = findViewById(R.id.img);
        processedImg = findViewById(R.id.procesedImage); ///
        btn = findViewById(R.id.uplBtn);
        processBtn = findViewById(R.id.processBtn);///
        processText = findViewById(R.id.processtext);//
        messageText = findViewById(R.id.msgText);///
        spinner = findViewById(R.id.spinner);
        spinner.setPrompt("ATTACH");
        spinner.setOnItemSelectedListener(this);
        sRef = storage.getReference();
        pBar = findViewById(R.id.progressBar2);
        pBar.setVisibility(View.GONE);
        preprocessor = new Preprocessor();

        initToolbar(R.id.toolbar);

        String message = getIntent().getStringExtra("ImageUri");
        if (message != null) {
            img.setImageURI(Uri.parse(message));
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getUsername();
            }
        });

        processBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    objectDetector();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

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
                            //Picasso.load(imageUri).resize(50, 50).centerCrop().into(img);
                            img.setImageURI(imageUri);
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
                        img.setImageBitmap(imgBitmap);
                }
            }
        });
    }

    private void objectDetector() throws IOException {
       //Bitmap bitmap = BitmapFactory.decodeFile(imageUri.get);
        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("footprints_model_uen1.tflite")
                        .build();

        Log.d("Model: ", localModel.toString());

        CustomObjectDetectorOptions customObjectDetectorOptions =
                new CustomObjectDetectorOptions.Builder(localModel)
                        .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .setClassificationConfidenceThreshold(0.5f)
                        .build();

        ObjectDetector detector = com.google.mlkit.vision.objects.ObjectDetection.getClient(customObjectDetectorOptions);

        if (imageUri != null && imgBitmap == null) {

            //image = InputImage.fromFilePath(getApplicationContext(), imageUri);
            Bitmap uritoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            inputimage = uritoBitmap;
            image = InputImage.fromBitmap(uritoBitmap, 0);

            Toast.makeText(getApplicationContext(),  "ImageUri is set" , Toast.LENGTH_LONG).show();
        } else if (imgBitmap != null &&  imageUri == null){

            inputimage = imgBitmap;
            image = InputImage.fromBitmap(imgBitmap, 0);
            imgWidth = image.getWidth();
            imgHeight = image.getHeight();

            Toast.makeText(getApplicationContext(),  "ImageBitmap is set" , Toast.LENGTH_LONG).show();
        } else if (imgBitmap == null &&  imageUri == null) {
            Toast.makeText(getApplicationContext(), "No image was detected!!", Toast.LENGTH_LONG).show();
            return;
        }

        detector
                .process(image)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(),  "Model Failed:" + e.getMessage() , Toast.LENGTH_LONG).show();
                        Log.d("Model Error: ", e.getMessage() );
                    }
                }).addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
                    @Override
                    public void onSuccess(List<DetectedObject> detectedObjects) {
                        Toast.makeText(getApplicationContext(),  " SUCCESS!!!" , Toast.LENGTH_LONG).show();

                        if (detectedObjects.size() > 0){

                            Log.d("Detected Objects: ", String.valueOf(detectedObjects.size()));
                            Toast.makeText(getApplicationContext(),  " SUCCESS 2!!!" , Toast.LENGTH_LONG).show();
                            for (DetectedObject detectedObject : detectedObjects) {

                                Rect boundingBox = detectedObject.getBoundingBox();
                                Integer trackingId = detectedObject.getTrackingId();
                                Toast.makeText(getApplicationContext(),  " SUCCESS 3!!!" , Toast.LENGTH_LONG).show();

                                //Bitmap resized = Bitmap.createScaledBitmap(inputimage, 512, 512, true);

                                Bitmap temp2 =  Bitmap.createBitmap(inputimage.getWidth(), inputimage.getHeight(), Bitmap.Config.RGB_565);
                                canvas = new Canvas(temp2);
                                canvas.drawBitmap(inputimage, 0, 0 , null);
                                canvas.drawRect(boundingBox, paint);
                                processedImg.setImageBitmap(temp2);
                                //img.setImageDrawable(new BitmapDrawable(getResources(), temp2));
                                img.setImageBitmap(imgBitmap);

                                Toast.makeText(getApplicationContext(), "Footprint was detected!!", Toast.LENGTH_LONG).show();

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "No Footprint was detected!!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void getUsername() {
        //Cited from: https://techicaltutorial.blogspot.com/2021/02/android-studio-popup-window-with-input.html
        String userN;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Enter a Username");
        EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pBar.setVisibility(View.VISIBLE);
                String user = input.getText().toString();
                Toast.makeText(getApplicationContext(), "Username is: " + user,
                        Toast.LENGTH_LONG).show();


                //Upload to gs://meshach-uen1.appspot.com/files
                if (imageUri != null) {
                    //StorageReference username = sRef.child(user + "/");
                    StorageReference userPath = sRef.child(user+ "/" + System.currentTimeMillis() + "." + getExtension(imageUri));
                    userPath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(MainActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                            pBar.setVisibility(View.GONE);
                            spinner.setSelection(0);
                            recreate();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            pBar.setVisibility(View.GONE);
                        }
                    });
                }else if (imgBitmap != null){

                    StorageReference userPath = sRef.child(user+ "/" + System.currentTimeMillis() + ".jpeg" );
                    // Get the data from an ImageView as bytes
                    img.setDrawingCacheEnabled(true);
                    img.buildDrawingCache();
                    //Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();

                    UploadTask uploadTask = userPath.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle unsuccessful uploads
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                            Toast.makeText(MainActivity.this, "File Uploaded succesfully!!!", Toast.LENGTH_LONG).show();
                            pBar.setVisibility(View.GONE);
                            spinner.setSelection(0);
                            recreate();
                        }
                    });

                } else {
                    Toast.makeText(MainActivity.this, "Image is Null!", Toast.LENGTH_LONG).show();
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

        //return userN;
    }

    public void onItemSelected(@NonNull AdapterView<?> parent, View view, int pos, long id) {
        String item = parent.getItemAtPosition(pos).toString();
        switch (pos) {
            case 1:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                getImageFromDevice.launch(intent);
                break;
            case 2:
                Intent intentCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Toast.makeText(MainActivity.this, "Step 1", Toast.LENGTH_LONG).show();
                getImageFromCamera.launch(intentCam);
                Toast.makeText(MainActivity.this, "Step 2", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void checkPermission() {
        //Cited: https://www.android--code.com/2017/08/android-request-multiple-permissions.html
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) + ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {

            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    }, 123
            );
        } else {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //Cited: https://www.android--code.com/2017/08/android-request-multiple-permissions.html
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {// When request is cancelled, the results array are empty
            if (
                    (grantResults.length > 0) &&
                            (grantResults[0]
                                    + grantResults[1]
                                    + grantResults[2]
                                    == PackageManager.PERMISSION_GRANTED
                            )
            ) {
                // Permissions are granted
                Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show();
            } else {
                // Permissions are denied
                Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }
    //CIted from: https://www.youtube.com/watch?v=lPfQN-Sfnjw
    private String getExtension (Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // Read the picture from the specified path and obtain its EXIF information
            ExifInterface exifInterface = new ExifInterface(path);
            // Get rotation information for pictures
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

}
