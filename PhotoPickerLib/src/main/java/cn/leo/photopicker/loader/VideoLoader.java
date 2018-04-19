package cn.leo.photopicker.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import cn.leo.photopicker.bean.PhotoBean;

/**
 * Created by Leo on 2018/3/6.
 */

public class VideoLoader extends CursorLoader implements LoaderManager.LoaderCallbacks<Cursor> {
    private OnLoadFinishListener mOnPhotoLoadFinishListener;

    public VideoLoader(Context context, OnLoadFinishListener loadFinishListener) {
        super(context,
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Video.Media.MIME_TYPE
                        + " in ('video/mp4') and "
                        + MediaStore.Video.Media.SIZE + " >0 ",
                null,
                MediaStore.Video.Media.DATE_ADDED + " desc");
        mOnPhotoLoadFinishListener = loadFinishListener;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return this;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            HashMap<String, ArrayList<PhotoBean>> allPic = new LinkedHashMap<>();
            while (data.moveToNext()) {
                // 获取视频的路径
                String path = data.getString(data.getColumnIndex(MediaStore.Video.Media.DATA));
                PhotoBean bean = new PhotoBean();
                bean.path = path;
                File picFile = new File(path);
                if (picFile.exists()) {
                    String parentName = picFile.getParentFile().getName();
                    if (allPic.containsKey(parentName)) {
                        allPic.get(parentName).add(bean);
                    } else {
                        ArrayList<PhotoBean> chileList = new ArrayList<>();
                        chileList.add(bean);
                        allPic.put(parentName, chileList);
                    }
                }
                bean.size = data.getInt(data.getColumnIndex(MediaStore.Video.Media.SIZE));
                bean.duration = data.getInt(data.getColumnIndex(MediaStore.Video.Media.DURATION));
            }
            if (mOnPhotoLoadFinishListener != null) {
                mOnPhotoLoadFinishListener.onPhotoLoadFinish(allPic);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
