package cn.leo.photopicker.pick;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Leo on 2018/1/9.
 */

public class VideoUtil {
    /**
     * @param context
     * @param path    视频路径
     * @return
     */
    public static VideoInfo videoInfo(Context context, String path) {

        Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] columns = {MediaStore.Video.Media._ID, // ID
                MediaStore.Video.Media.DURATION,// 时长
                MediaStore.Video.Media.SIZE //文件大小
        };
        ContentResolver mResolver = context.getContentResolver();
        String selection = MediaStore.Video.Media.DATA + "=?";
        String selectionArgs[] = {path};
        Cursor cursor = mResolver.query(contentUri, columns, selection, selectionArgs, null);
        int duration = 0;
        int size = 0;
        if (cursor != null && cursor.moveToFirst()) {
            duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
            size = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
            cursor.close();
        }
        return new VideoInfo(size, duration);
    }

    public static class VideoInfo {
        public int size;
        public int duration;
        private String time;

        public VideoInfo() {
        }

        public VideoInfo(int size, int duration) {
            this.size = size;
            this.duration = duration;
        }

        public String getTime() {
            int i = duration / 1000;
            int minute = i / 60;
            int second = i % 60;
            return String.format("%1$d:%2$02d",minute,second);
        }
    }
}
