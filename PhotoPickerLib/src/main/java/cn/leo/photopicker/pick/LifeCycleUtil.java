package cn.leo.photopicker.pick;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Created by Leo on 2018/1/4.
 */

public class LifeCycleUtil {
    public static void setLifeCycleListener(FragmentActivity activity,
                                            PhotoOptions options,
                                            PhotoPicker.PhotoCallBack callBack) {
        String tag = "fragmentCallBack";
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(tag) != null) {
            return;
        }
        FragmentCallback fragment = new FragmentCallback();
        fragment.setPhotoCallBack(options, callBack);
        fragmentManager
                .beginTransaction()
                .add(fragment, tag)
                .commit();
        fragmentManager.executePendingTransactions();
    }
}
