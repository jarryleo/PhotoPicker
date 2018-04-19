package cn.leo.photopicker.pick;

import android.support.v4.app.FragmentActivity;

/**
 * Created by JarryLeo on 2017/5/20.
 */

public class PhotoPicker {

    public static TakePhoto takePhoto(FragmentActivity context) {
        return new TakePhoto(context);
    }

    public static TakeVideo takeVideo(FragmentActivity context) {
        return new TakeVideo(context);
    }

    public interface PhotoCallBack {
        void onPicSelected(String[] path);
    }
}
