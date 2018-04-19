package cn.leo.photopicker.pick;

import android.support.v4.app.FragmentActivity;

import cn.leo.photopicker.utils.LifeCycleUtil;

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

    public static class TakePhoto {
        private FragmentActivity mActivity;
        private PhotoOptions options = new PhotoOptions();

        private TakePhoto(FragmentActivity activity) {
            mActivity = activity;
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

        public void take(PhotoCallBack callBack) {
            LifeCycleUtil.setLifeCycleListener(mActivity, options, callBack);
        }
    }

    public static class TakeVideo {
        private FragmentActivity mActivity;
        private PhotoOptions options = new PhotoOptions();

        private TakeVideo(FragmentActivity activity) {
            mActivity = activity;
            options.type = PhotoOptions.TYPE_VIDEO;
        }

        public TakeVideo multi(int multi) {
            options.takeNum = multi;
            return this;
        }

        public TakeVideo maxDuration(int duration) {
            options.duration = duration;
            return this;
        }

        public TakeVideo sizeLimit(int size) {
            options.size = size;
            return this;
        }

        public void take(PhotoCallBack callBack) {
            LifeCycleUtil.setLifeCycleListener(mActivity, options, callBack);
        }
    }
}
