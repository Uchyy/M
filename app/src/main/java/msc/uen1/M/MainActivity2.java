
package msc.uen1.M;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

import static org.opencv.core.CvType.CV_32F;
import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.MORPH_CLOSE;
import static org.opencv.imgproc.Imgproc.MORPH_OPEN;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Feature2D;
import org.opencv.features2d.Params;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {

    ImageView imgview, imgview2;
    Button startBtn, finishBtn, rotBtn;
    EditText angleText;
    Integer angle;
    Bitmap image, result, rotated;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        OpenCVLoader.initDebug();

        imgview = findViewById(R.id.imageView);
        imgview2 = findViewById(R.id.imageView2);
        startBtn = findViewById(R.id.startbtn);
        finishBtn = findViewById(R.id.finishbtn);
        rotBtn = (Button) findViewById(R.id.rotateBtn);
        angleText = findViewById(R.id.angleText);

        String a = "color_4_1.jpeg";
        String b = "gray_30_4.jpeg";
        String c = "rough.jpg";
        String d = "rough_1.jpg";
        String e = "rough_3.jpg";
        String f = "rough_4.jpg";

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                image = BitmapFactory.decodeResource(MainActivity2.this.getResources(), R.drawable.rough);
                imgview.setImageBitmap(image);
            }
        });

        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (image == null) {
                    // Toast.makeText(this, "Image is null", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(),  "Image is null" , Toast.LENGTH_LONG).show();
                    return;
                } else {
                    result = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                }
                Mat src = new Mat();
                Utils.bitmapToMat(image, src);

                //Run EasyProcess: Get Binary Image
                Mat binary = easyProcess(src);

                //Utils.matToBitmap(dest, result);
                Utils.matToBitmap(binary, result);
                imgview2.setImageBitmap(result);
            }
        });

        rotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                angle = Integer.valueOf(angleText.getText().toString());
                if (!angle.equals(null) ) {
                    //rotateImage(image, angle);
                    image = rotateBitmap(image,  angle);
                    imgview.setImageBitmap(image);
                } else {
                    angleText.setError("Please enter angle!");
                }
            }
        });

    }

    private Bitmap rotateBitmap(Bitmap original, float degrees) {

        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        original.recycle();
        return rotatedBitmap;
    }

    private Mat easyProcess(Mat src) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Mat filder2d = new Mat();
        Mat kernel3 = Mat.ones(2,2, CvType.CV_32F);
        kernel3.put(-1, 0, 1,
                -2, 0, 2,
                -1, 0, 1);
        Imgproc.filter2D(gray, filder2d, src.depth(), kernel3);

        Mat binary = new Mat();
        //Imgproc.threshold(gray, binary, 150, 300, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(filder2d, binary, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 201, 10);

        //return binary;
        return  hardProcess(binary);
    }

    private Mat hardProcess (Mat src) {
        //Mat binary = easyProcess(src);

        //Find Contours
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> desired = new ArrayList<>();

        Mat hierarchy = new Mat();
        Imgproc.findContours(src, contours, hierarchy, RETR_TREE, CHAIN_APPROX_NONE);

        Mat biggest = Mat.zeros(src.size(), CvType.CV_8UC3);
        Scalar scalar = new Scalar(255, 255, 255);
        //Scalar white = new Scalar();

        double maxVal = 500;
        int maxValIdx = 0;
        Log.d("Conturs.size()", Integer.toString(contours.size()));
        //Get biggestblob
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (contourArea > maxVal)
            {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }
        Imgproc.drawContours(biggest, contours, maxValIdx, scalar, 2, Imgproc.LINE_8, hierarchy);

        Core.bitwise_not(biggest, biggest);

        return biggest;
    }


}