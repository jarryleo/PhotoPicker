package cn.leo.photopicker.pick;

import android.database.ContentObserver;
import android.os.Handler;

import cn.leo.photopicker.activity.TakePhotoActivity;

/**
 * Created by Leo on 2018/1/19.
 */

public class MediaStoreContentObserver extends ContentObserver {
    private TakePhotoActivity mActivity;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MediaStoreContentObserver(TakePhotoActivity activity, Handler handler) {
        super(handler);
        mActivity = activity;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        mActivity.refreshData();
    }
}
