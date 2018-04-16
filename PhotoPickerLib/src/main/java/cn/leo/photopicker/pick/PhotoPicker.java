package cn.leo.photopicker.pick;

import android.support.v4.app.FragmentActivity;

import cn.leo.photopicker.utils.LifeCycleUtil;

/**
 * Created by JarryLeo on 2017/5/20.
 */

public class PhotoPicker {

    public static SelectPhoto selectPhoto(FragmentActivity context) {
        return new SelectPhoto(context);
    }

    public static SelectVideo selectVideo(FragmentActivity context) {
        return new SelectVideo(context);
    }

    public interface PhotoCallBack {
        void onPicSelected(String[] path);
    }

    public static class SelectPhoto {
        private FragmentActivity mActivity;
        private PhotoOptions options = new PhotoOptions();

        private SelectPhoto(FragmentActivity activity) {
            mActivity = activity;
        }

        public SelectPhoto crop(int cropWidth, int cropHeight) {
            options.crop = true;
            options.cropWidth = cropWidth;
            options.cropHeight = cropHeight;
            return this;
        }

        public SelectPhoto multi(int multi) {
            options.takeNum = multi;
            return this;
        }

        public SelectPhoto sizeLimit(int size) {
            options.size = size;
            return this;
        }

        public SelectPhoto compress(int width, int height) {
            options.compressWidth = width;
            options.compressHeight = height;
            return this;
        }

        public void take(PhotoCallBack callBack) {
            LifeCycleUtil.setLifeCycleListener(mActivity, options, callBack);
        }
    }

    public static class SelectVideo {
        private FragmentActivity mActivity;
        private PhotoOptions options = new PhotoOptions();

        private SelectVideo(FragmentActivity activity) {
            mActivity = activity;
            options.type = PhotoOptions.TYPE_VIDEO;
        }

        public SelectVideo multi(int multi) {
            options.takeNum = multi;
            return this;
        }

        public SelectVideo maxDuration(int duration) {
            options.duration = duration;
            return this;
        }

        public SelectVideo sizeLimit(int size) {
            options.size = size;
            return this;
        }

        public void take(PhotoCallBack callBack) {
            LifeCycleUtil.setLifeCycleListener(mActivity, options, callBack);
        }
    }
}
