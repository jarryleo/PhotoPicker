package cn.leo.photopicker.bean;

import android.annotation.SuppressLint;

/**
 * Created by Leo on 2018/4/16.
 */

public class PhotoBean {
    public String path;
    public int size;
    public int duration;
    public boolean checked;

    @SuppressLint("DefaultLocale")
    public String getTime() {
        int i = duration / 1000;
        int minute = i / 60;
        int second = i % 60;
        return String.format("%1$d:%2$02d", minute, second);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PhotoBean && ((PhotoBean) obj).path.equals(path);
    }
}
