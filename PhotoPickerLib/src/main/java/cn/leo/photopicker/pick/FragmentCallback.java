package cn.leo.photopicker.pick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import cn.leo.photopicker.activity.TakePhotoActivity;

/**
 * Created by Leo on 2018/2/2.
 */

public class FragmentCallback extends Fragment {
    public static final int REQUEST_CODE = 796;
    private PhotoPicker.PhotoCallBack mPhotoCallBack;
    private PhotoOptions mOptions;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK &&
                requestCode == REQUEST_CODE &&
                data != null) {
            String[] imgList = data.getStringArrayExtra("imgList");
            if (mPhotoCallBack != null) {
                mPhotoCallBack.onPicSelected(imgList);
            } else {
                FragmentActivity activity = getActivity();
                if (activity instanceof PhotoPicker.PhotoCallBack) {
                    ((PhotoPicker.PhotoCallBack) activity).onPicSelected(imgList);
                }
            }
        }
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.detach(this);
        fragmentTransaction.remove(this);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void setPhotoCallBack(PhotoOptions options,
                                 PhotoPicker.PhotoCallBack photoCallBack) {
        mPhotoCallBack = photoCallBack;
        mOptions = options;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTake();
    }

    public void startTake() {
        if (mOptions != null) {
            TakePhotoActivity.startSelect(this, mOptions);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("options", mOptions);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            PhotoOptions options = savedInstanceState.getParcelable("options");
            if (options != null) {
                mOptions = options;
            }
        }
    }

}
