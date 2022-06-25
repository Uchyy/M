package msc.uen1.M.Main;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FragmentViewModel extends ViewModel {

    private MutableLiveData <Bitmap> imgBitmap = new MutableLiveData<>();

    public void setBitmap (Bitmap bitmap) {     imgBitmap.setValue(bitmap); }

    public LiveData <Bitmap> getBitmap () {     return imgBitmap;   }
}
