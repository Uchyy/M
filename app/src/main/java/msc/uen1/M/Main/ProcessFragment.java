package msc.uen1.M.Main;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY_INV;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

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

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import msc.uen1.M.MainActivity2;
import msc.uen1.M.R;

public class ProcessFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    View view;
    String bitmap;
    static Bitmap imgBitmap;
    private ImageView preview2;
    private Button button, processButton;
    private ImageButton rotButton;
    EditText angleText;
    private  FragmentViewModel fragmentViewModel;
    String angle;
    Integer number;
    ProgressBar progressBar;
    Boolean clicked = false;

    public ProcessFragment() {
        // Required empty public constructor
    }

    public static ProcessFragment newInstance(String param1, String param2) {
        ProcessFragment fragment = new ProcessFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preview2 = view.findViewById(R.id.preview2);
        button = view.findViewById(R.id.button2);

        fragmentViewModel = new ViewModelProvider(getActivity()).get(FragmentViewModel.class);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentViewModel.getBitmap().observe(getViewLifecycleOwner(), new Observer<Bitmap>() {
                    @Override
                    public void onChanged(Bitmap bitmap) {

                        if (bitmap != null) {
                            imgBitmap = bitmap;
                            preview2.setImageBitmap(bitmap);
                            clicked = true;
                        } else {
                            Toast.makeText(getContext(), "Image is Null!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_process, container, false);
        OpenCVLoader.initDebug();

        preview2 = view.findViewById(R.id.preview2);
        button = view.findViewById(R.id.button2);
        processButton = view.findViewById(R.id.processbutton);
        progressBar = view.findViewById(R.id.progressBar);
        //results = Bitmap.createBitmap(imgBitmap);


        processButton.setOnClickListener(new View.OnClickListener() {
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

                if (imgBitmap == null) {
                    Toast.makeText(getContext(), "No image was detected!!", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Select the level of processing required");// add a list
                String[] animals = {"EasyProcess", "HardProcess"};
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(10);
                builder.setItems(animals, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: // EAsyProceess
                                Mat src = new Mat();
                                //Mat dest = new Mat();
                                        Utils.bitmapToMat(imgBitmap, src);
                                src = easyProcess(src);
                                        Utils.matToBitmap(src, imgBitmap);
                                        //imgBitmap = results;
                                progressBar.setProgress(100);
                                progressBar.setVisibility(View.GONE);
                                preview2.setImageBitmap(imgBitmap);
                                break;
                            case 1: // HardProcess
                                Mat src2 = new Mat(); Mat dest2 = new Mat();
                                Utils.bitmapToMat(imgBitmap, src2);
                                dest2 = hardProcess(src2);
                                Utils.matToBitmap(dest2, imgBitmap);
                                progressBar.setProgress(100);
                                progressBar.setVisibility(View.GONE);
                                preview2.setImageBitmap(imgBitmap);
                                //Toast.makeText(getContext(), "Not implemented!!!!", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        fragmentViewModel.setBitmap(imgBitmap);
                    }
                });// create and show the alert dialog
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        return view;

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

        Mat dst = new Mat();
        Imgproc.blur(filder2d, dst, new Size(6,6));

        Mat binary = new Mat();
        //Imgproc.threshold(gray, binary, 150, 300, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(dst, binary, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 201, 10);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size (5,5));

        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, kernel);

        Core.bitwise_not(binary, binary);
        Imgproc.erode(binary, binary, kernel);

        //Mat result = new Mat(); //src.copyTo(result, binary);
        return binary;
        //return binary;
    }

    private Mat hardProcess (Mat src) {
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);

        Mat filder2d = new Mat();
        Mat kernel3 = Mat.ones(2,2, CvType.CV_32F);
        kernel3.put(-1, 0, 1,
                -2, 0, 2,
                -1, 0, 1);
        Imgproc.filter2D(gray, filder2d, src.depth(), kernel3);

        Mat dst = new Mat();
        Imgproc.blur(filder2d, dst, new Size(6,6));

        Mat binary = new Mat();
        //Imgproc.threshold(gray, binary, 150, 300, Imgproc.THRESH_BINARY);
        Imgproc.adaptiveThreshold(dst, binary, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY_INV, 201, 10);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size (5,5));
        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, kernel);

        Mat kernelCross = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,new Size (15
                ,15));
        Mat kernelRect = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size (12,12));
        Mat kernelEllipse = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,new Size (12,12));

        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, kernelCross);
        Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, kernelRect);
        //Imgproc.morphologyEx(binary, binary, Imgproc.MORPH_OPEN, kernelEllipse);

        Mat kernel2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size (12,12));

        Imgproc.dilate(binary, binary, kernel2);
        Imgproc.dilate(binary, binary, kernel2);
        Imgproc.erode(binary, binary, kernel2);

        Imgproc.GaussianBlur(binary, binary, new Size(5,7), 10);

        Mat result = new Mat();
        src.copyTo(result, binary);
        return result;
    }


}