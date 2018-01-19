package cn.leo.photopicker.pick;

import android.app.Activity;
import android.support.annotation.NonNull;

import cn.leo.photopicker.activity.TakePhotoActivity;

/**
 * Created by JarryLeo on 2017/5/20.
 */

public class PhotoPicker {

    /**
     * @param context
     * @param picNum     选择图片张数 大于1位多选，1 为单选
     * @param crop       是否裁剪 ，只对单选有效
     * @param cropWidth  裁剪宽度，只对单选有效
     * @param cropHeight 裁剪高度，只对单选有效
     * @param callBack   选择图片回调
     */
    public static void selectPic(Activity context, int picNum, boolean crop, int cropWidth, int cropHeight, @NonNull PicCallBack callBack) {
        PhotoOptions options = new PhotoOptions();
        options.crop = crop;
        options.takeNum = picNum;
        options.cropWidth = cropWidth;
        options.cropHeight = cropHeight;
        TakePhotoActivity.startSelect(context, options, callBack);

    }

    public interface PicCallBack {
        void onPicSelected(String[] path);

        //void onFailed();
    }
}
