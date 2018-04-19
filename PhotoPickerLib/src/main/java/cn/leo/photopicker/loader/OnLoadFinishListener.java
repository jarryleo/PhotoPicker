package cn.leo.photopicker.loader;

import java.util.ArrayList;
import java.util.HashMap;

import cn.leo.photopicker.bean.PhotoBean;

/**
 * Created by Leo on 2018/4/19.
 */

public interface OnLoadFinishListener {
    void onPhotoLoadFinish(HashMap<String, ArrayList<PhotoBean>> photos);
}
