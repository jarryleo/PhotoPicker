package cn.leo.photopicker.pick;

import android.support.v4.app.FragmentActivity;

import cn.leo.photopicker.utils.LifeCycleUtil;

/**
 * Created by Leo on 2018/4/19.
 */

public class TakeVideo {
    private FragmentActivity mActivity;
    private PhotoOptions options = new PhotoOptions();

    TakeVideo(FragmentActivity activity) {
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

    public void take(PhotoPicker.PhotoCallBack callBack) {
        LifeCycleUtil.setLifeCycleListener(mActivity, options, callBack);
    }
}