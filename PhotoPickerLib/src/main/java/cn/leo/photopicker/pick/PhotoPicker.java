package cn.leo.photopicker.pick;

import android.app.Activity;

import cn.leo.photopicker.activity.TakePhotoActivity;

/**
 * Created by JarryLeo on 2017/5/20.
 */

public class PhotoPicker {

    public static SelectPhoto selectPhoto(Activity context) {
        return new SelectPhoto(context);
    }

    public static SelectVideo selectVideo(Activity context) {
        return new SelectVideo(context);
    }

    public interface PhotoCallBack {
        void onPicSelected(String[] path);
    }


    public static class SelectPhoto {
        private Activity mActivity;
        private PhotoOptions options = new PhotoOptions();

        private SelectPhoto(Activity activity) {
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

        public void take(PhotoCallBack callBack) {
            TakePhotoActivity.startSelect(mActivity, options, callBack);
        }
    }

    public static class SelectVideo {
        private Activity mActivity;
        private PhotoOptions options = new PhotoOptions();

        private SelectVideo(Activity activity) {
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
            TakePhotoActivity.startSelect(mActivity, options, callBack);
        }
    }
}
