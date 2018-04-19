package cn.leo.photopicker.pick;

import android.support.v4.app.FragmentActivity;

import java.lang.ref.WeakReference;

import cn.leo.photopicker.utils.LifeCycleUtil;

/**
 * Created by Leo on 2018/4/19.
 */

public class TakePhoto {
    private WeakReference<FragmentActivity> mActivity;
    private PhotoOptions options = new PhotoOptions();

    TakePhoto(FragmentActivity activity) {
        mActivity = new WeakReference<>(activity);
        options.type = PhotoOptions.TYPE_PHOTO;
    }

    public TakePhoto crop(int cropWidth, int cropHeight) {
        options.crop = true;
        options.cropWidth = cropWidth;
        options.cropHeight = cropHeight;
        return this;
    }

    public TakePhoto multi(int multi) {
        options.takeNum = multi;
        return this;
    }

    public TakePhoto sizeLimit(int size) {
        options.size = size;
        return this;
    }

    public TakePhoto compress(int width, int height) {
        options.compressWidth = width;
        options.compressHeight = height;
        return this;
    }

    public void take(PhotoPicker.PhotoCallBack callBack) {
        LifeCycleUtil.setLifeCycleListener(mActivity.get(), options, callBack);
    }
}