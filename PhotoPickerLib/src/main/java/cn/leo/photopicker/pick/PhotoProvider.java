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

import cn.leo.photopicker.utils.VideoUtil;

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
    public static HashMap<String, ArrayList<String>> getDiskPhotos(Activity context) {
        HashMap<String, ArrayList<String>> allPic = new LinkedHashMap<>();
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
     * 获取含有文件夹层级的所有视频列表
     *
     * @param context
     * @return
     */
    public static HashMap<String, ArrayList<String>> getDiskVideos(Activity context, VideoUtil videoUtil) {
        HashMap<String, ArrayList<String>> allPic = new LinkedHashMap<>();
        Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver mContentResolver = context.getContentResolver();
        Cursor cursor = mContentResolver.query(mImageUri, null, MediaStore.Video.Media.MIME_TYPE
                        + " in ('video/mp4') and " + MediaStore.Video.Media.SIZE + " >0 ",
                null, MediaStore.Video.Media.DATE_ADDED + " desc");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获取视频的路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
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
                int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                videoUtil.put(path, new VideoUtil.VideoInfo(size, duration));
            }
            cursor.close();
        }
        return allPic;
    }

    /**
     * 获取所有图片的文件夹名字
     *
     * @param photos
     * @return
     */
    public static ArrayList<String> getDirList(HashMap<String, ArrayList<String>> photos) {
        if (photos == null) return null;
        ArrayList<String> dirs = new ArrayList<>();
        Set<String> set = photos.keySet();
        dirs.add("全部");
        for (String dir : set) {
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
    public static ArrayList<String> getAllPhotos(HashMap<String, ArrayList<String>> photos) {
        ArrayList<String> allPhotos = new ArrayList<>();
        Set<String> set = photos.keySet();
        for (String dir : set
                ) {
            List<String> list = photos.get(dir);
            allPhotos.addAll(list);
        }
        return allPhotos;
    }
}
