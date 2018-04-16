package cn.leo.photopicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.leo.photopicker.R;
import cn.leo.photopicker.view.BannerView;

public class PhotoShowActivity extends Activity {
    private static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    private static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";
    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";
    private BannerView mCarouselView;
    private int mCurrentPosition;
    private int mStartingPosition;
    private CheckBox mCheckBox;
    private ArrayList<String> mImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
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
        //图片是否选中
        boolean check = intent.getBooleanExtra("check", false);
        mCheckBox = (CheckBox) findViewById(R.id.cb_check);
        mCheckBox.setChecked(check);

        mImages = intent.getStringArrayListExtra("images");
        //int index = intent.getIntExtra("index", 0);
        mCarouselView = (BannerView) findViewById(R.id.carouselView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mImages != null) {
            mCarouselView.setTransitionName(mImages.get(mCurrentPosition));
        }
        mCarouselView.initImageLoader(new BannerView.ImageLoader() {
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

        mCarouselView.setImageList(mImages, mImages.size() > 1);
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
        data.putExtra("path", mImages.get(mCurrentPosition));
        data.putExtra("check", mCheckBox.isChecked());
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }
}
