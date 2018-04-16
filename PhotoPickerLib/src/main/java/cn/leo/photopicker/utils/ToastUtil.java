package cn.leo.photopicker.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtil {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Toast toast;

    /**
     * 强大的吐司，能够连续弹的吐司并且能在子线程弹吐司
     *
     * @param context  上下文
     * @param text     文本内容
     * @param duration 时长
     */
    @SuppressLint("ShowToast")
    public static void showToast(final Context context, final String text, final int duration) {
        if (Looper.myLooper() != Looper.getMainLooper()) {//线程切换
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showToast(context, text, duration);
                }
            });
            return;
        }
        if (toast == null) {//并不需要加锁双重校验，很少多个线程同时弹吐司
            toast = Toast.makeText(context.getApplicationContext(), text, duration);
        } else {
            toast.setDuration(duration);
            toast.setText(text);
        }
        toast.show();//同一个吐司对象不会出现一个接一个弹
    }

    /**
     * 短吐司
     *
     * @param context 上下文
     * @param text    文本
     */
    public static void showToast(final Context context, final String text) {
        showToast(context, text, Toast.LENGTH_SHORT);
    }
}
