package cn.leo.photopicker.pick;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;

import java.util.HashMap;

/**
 * Created by Leo on 2018/1/9.
 */

public class VideoUtil implements LifecycleObserver {
    private static HashMap<String, VideoInfo> videoInfoHashMap = new HashMap<>();

    public VideoUtil(LifecycleOwner activity) {
        activity.getLifecycle().addObserver(this);
    }

    public void put(String path, VideoInfo info) {
        videoInfoHashMap.put(path, info);
    }

    public String getTime(String path) {
        return videoInfoHashMap.get(path).getTime();
    }

    public VideoInfo getVideoInfo(String path) {
        return videoInfoHashMap.get(path);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        videoInfoHashMap.clear();
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
            return String.format("%1$d:%2$02d", minute, second);
        }
    }
}
