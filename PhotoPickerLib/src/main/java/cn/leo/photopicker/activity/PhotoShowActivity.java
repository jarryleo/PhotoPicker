package cn.leo.photopicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.leo.photopicker.R;
import cn.leo.photopicker.view.CarouselView;

public class PhotoShowActivity extends Activity {
    private static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    private static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";
    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";
    private CarouselView mCarouselView;
    private int mCurrentPosition;
    private int mStartingPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        /*getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setAllowEnterTransitionOverlap(true);
        }*/
        setContentView(R.layout.activity_photo_show);
        mStartingPosition = getIntent().getIntExtra(EXTRA_STARTING_ALBUM_POSITION, 0);
        if (savedInstanceState == null) {
            mCurrentPosition = mStartingPosition;
        } else {
            mCurrentPosition = savedInstanceState.getInt(STATE_CURRENT_PAGE_POSITION);
        }

        initView();
    }

    private void initView() {
        Intent intent = getIntent();
        final ArrayList<String> images = intent.getStringArrayListExtra("images");
        //int index = intent.getIntExtra("index", 0);
        mCarouselView = (CarouselView) findViewById(R.id.carouselView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && images != null) {
            mCarouselView.setTransitionName(images.get(mCurrentPosition));
        }
        mCarouselView.initImageLoader(new CarouselView.ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String imagePath) {
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setTransitionName(imagePath);
                }*/
                Glide.with(imageView.getContext())
                        .load(imagePath)
                        .crossFade()
                        .into(imageView);
            }
        });

        mCarouselView.setImageList(images, images.size() > 1);
        mCarouselView.setCurrentItem(mCurrentPosition);
        mCarouselView.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position % 50000;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_CURRENT_PAGE_POSITION, mCurrentPosition);
    }

    @Override
    public void finishAfterTransition() {
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_ALBUM_POSITION, mStartingPosition);
        data.putExtra(EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }
}
