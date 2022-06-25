package msc.uen1.M;

import static org.opencv.imgproc.Imgproc.ADAPTIVE_THRESH_MEAN_C;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.adaptiveThreshold;

import android.graphics.Bitmap;
import android.media.Image;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Preprocessor {

    public Preprocessor() {
    }
    //int DELAY_CAPTION = 1500;
    //int DELAY_BLUR = 100;
    //int MAX_KERNEL_LENGTH = 31;

    public static Mat Adpthreshold(Mat src) {
        Mat dest = new Mat();
        adaptiveThreshold(ColorToGray (src), dest, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 15, 40);
        return dest;
    }

    public static Mat ColorToGray (Mat src) {
        Mat dest = new Mat();
        Imgproc.cvtColor(src, dest, Imgproc.COLOR_BGR2GRAY);
        return dest;
    }

    public static Mat GrayToColor (Mat src) {
        Mat dest = new Mat();
        Imgproc.cvtColor(src, dest, Imgproc.COLOR_GRAY2BGR);
        return dest;
    }

    public static Bitmap denoiseImg (Bitmap filename){
        Mat dest, src = new Mat();

        if (filename == null ) {
            return null;
        } else {
            Utils.bitmapToMat(filename, src);
        }
        return null;
    }

}
