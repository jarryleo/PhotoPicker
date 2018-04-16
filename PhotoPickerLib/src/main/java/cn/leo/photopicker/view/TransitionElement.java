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
    public static int TRANSITION_REQUEST_CODE = 100;


    public static void transitonStart(Activity srcActivity, Class<?> destClass, View srcView, String sharedElementName) {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(srcActivity, srcView, sharedElementName);
        Bundle bundle = optionsCompat.toBundle();
        Intent intent = new Intent(srcActivity, destClass);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            srcActivity.startActivityForResult(intent, TRANSITION_REQUEST_CODE, bundle);
        } else {
            srcActivity.startActivity(intent);
        }
    }

    public static void transitonStart(Activity srcActivity, Intent intent, View srcView, String sharedElementName) {
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                .makeSceneTransitionAnimation(srcActivity, srcView, sharedElementName);
        Bundle bundle = optionsCompat.toBundle();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            srcActivity.startActivityForResult(intent, TRANSITION_REQUEST_CODE, bundle);
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
            srcActivity.startActivityForResult(intent, TRANSITION_REQUEST_CODE, bundle);
        } else {
            srcActivity.startActivity(intent);
        }
    }
}
