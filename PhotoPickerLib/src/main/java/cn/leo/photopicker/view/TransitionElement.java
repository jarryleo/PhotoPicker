package cn.leo.photopicker.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;

/**
 * Created by Leo on 2017/7/14.
 */

public class TransitionElement {

    public static void transitonStart(Activity srcActivity, Class<?> destClass, View srcView, String transtionName) {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(srcActivity, srcView, transtionName);
        Bundle bundle = optionsCompat.toBundle();
        Intent intent = new Intent(srcActivity, destClass);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            srcActivity.startActivityForResult(intent, 100, bundle);
        } else {
            srcActivity.startActivity(intent);
        }
    }

    public static void transitonStart(Activity srcActivity, Intent intent, View srcView, String transtionName) {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(srcActivity, srcView, transtionName);
        Bundle bundle = optionsCompat.toBundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            srcActivity.startActivityForResult(intent, 100, bundle);
        } else {
            srcActivity.startActivity(intent);
        }
    }

    public static void transitonStart(Activity srcActivity, Intent intent, Pair<View, String>... sharedElements) {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(srcActivity, sharedElements);
        Bundle bundle = optionsCompat.toBundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            srcActivity.startActivity(intent, bundle);
            srcActivity.startActivityForResult(intent, 100, bundle);
        } else {
            srcActivity.startActivity(intent);
        }
    }
}
