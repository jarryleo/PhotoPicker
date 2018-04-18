package cn.leo.photopicker.activity;

import android.annotation.TargetApi;
import android.app.SharedElementCallback;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import java.util.List;
import java.util.Map;

import cn.leo.photopicker.utils.PositionUtil;

/**
 * Created by Leo on 2018/4/18.
 */

public abstract class TransitionAnimActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTransition();
        super.onCreate(savedInstanceState);
        setSHaredCallback();
    }

    @TargetApi(21)
    private void setSHaredCallback() {
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                View itemByPosition = getItemByPosition(PositionUtil.pohotoPosition);
                if (itemByPosition != null) {
                    sharedElements.put("share", itemByPosition);
                }
            }
        });
    }

    public abstract View getItemByPosition(int currentPosition);


    @TargetApi(21)
    private void setTransition() {
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        getWindow().setAllowEnterTransitionOverlap(true);
        getWindow().setAllowReturnTransitionOverlap(true);
        /*Fade fade = new Fade();
        fade.setDuration(200);
        getWindow().setExitTransition(fade);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
