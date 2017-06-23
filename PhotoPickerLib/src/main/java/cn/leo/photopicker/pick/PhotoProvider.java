package cn.leo.photopicker.pick;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by JarryLeo on 2017/5/20.
 */

public class PhotoProvider {
    /**
     * 获取含有文件夹层级的所有图片列表
     *
     * @param context
     * @return
     */
    public static HashMap<String, List<String>> getDiskPhotos(Activity context) {
        HashMap<String, List<String>> allPic = new LinkedHashMap<>();
        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();
        Cursor mCursor = mContentResolver.query(mImageUri, null, MediaStore.Images.Media.MIME_TYPE
                        + " in ('image/jpeg','image/png','image/jpg') and " + MediaStore.Images.Media.SIZE + " >0 ",
                null, MediaStore.Images.Media.DATE_ADDED + " desc");
        if (mCursor != null) {
            while (mCursor.moveToNext()) {
                // 获取图片的路径
                String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                File picFile = new File(path);
                if (picFile.exists()) {
                    String parentName = picFile.getParentFile().getName();

                    if (allPic.containsKey(parentName)) {
                        allPic.get(parentName).add(path);
                    } else {
                        ArrayList<String> chileList = new ArrayList<String>();
                        chileList.add(path);
                        allPic.put(parentName, chileList);
                    }
                }
            }
            mCursor.close();
        }
        return allPic;
    }

    /**
     * 获取所有图片的文件夹名字
     *
     * @param photos
     * @return
     */
    public static List<String> getDirList(HashMap<String, List<String>> photos) {
        if (photos == null) return null;
        List<String> dirs = new ArrayList<>();
        Set<String> set = photos.keySet();
        dirs.add("全部照片");
        for (String dir : set
                ) {
            dirs.add(dir);
        }
        return dirs;
    }

    /**
     * 拿到不包含文件夹的所有图片
     *
     * @param photos
     * @return
     */
    public static List<String> getAllPhotos(HashMap<String, List<String>> photos) {
        List<String> allPhotos = new ArrayList<>();
        Set<String> set = photos.keySet();
        for (String dir : set
                ) {
            List<String> list = photos.get(dir);
            allPhotos.addAll(list);
        }
        return allPhotos;
    }
}
