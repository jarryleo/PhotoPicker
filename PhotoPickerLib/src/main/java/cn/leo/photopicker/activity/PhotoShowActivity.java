package cn.leo.photopicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.leo.photopicker.R;
import cn.leo.photopicker.utils.PositionUtil;
import cn.leo.photopicker.utils.ToastUtil;
import cn.leo.photopicker.view.BannerView;

public class PhotoShowActivity extends Activity {
    private static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    private static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";
    private static final String STATE_CURRENT_PAGE_POSITION = "state_current_page_position";
    private BannerView mBannerView;
    private int mCurrentPosition;
    private int mStartingPosition;
    private CheckBox mCheckBox;
    private ArrayList<String> mImages;
    private ArrayList<String> mChecks;
    private int mMax;

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
        //boolean check = intent.getBooleanExtra("check", false);
        mImages = intent.getStringArrayListExtra("images");
        mChecks = intent.getStringArrayListExtra("check");
        mMax = intent.getIntExtra("max", 0);
        mCurrentPosition = intent.getIntExtra(EXTRA_STARTING_ALBUM_POSITION, 0);
        mBannerView = findViewById(R.id.carouselView);
        //设置共享名称
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && mImages != null) {
            //mBannerView.setTransitionName(mImages.get(mCurrentPosition));
            mBannerView.setTransitionName("share");
        }
        mCheckBox = findViewById(R.id.cb_check);
        mCheckBox.setChecked(mChecks.contains(mImages.get(mCurrentPosition)));

        mBannerView.initImageLoader(new BannerView.ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String imagePath) {
                Glide.with(imageView.getContext())
                        .load(imagePath)
                        .crossFade()
                        .into(imageView);
            }
        });
        mBannerView.setImageList(mImages, mImages.size() > 1);
        mBannerView.setCurrentItem(mCurrentPosition);
        mBannerView.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position % mImages.size();
                mCheckBox.setChecked(mChecks.contains(mImages.get(mCurrentPosition)));
            }
        });
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String e = mImages.get(mCurrentPosition);
                if (isChecked) {
                    if (mChecks.contains(e)) return;
                    if (mChecks.size() >= mMax) {
                        ToastUtil.showToast(PhotoShowActivity.this, "您最多只能选择" + mMax + "张照片！");
                        mCheckBox.setChecked(false);
                    } else {
                        mChecks.add(e);
                    }
                } else {
                    mChecks.remove(e);
                }
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
        PositionUtil.pohotoPosition = mCurrentPosition + 1;
        Intent data = new Intent();
        data.putExtra(EXTRA_STARTING_ALBUM_POSITION, mStartingPosition);
        data.putExtra(EXTRA_CURRENT_ALBUM_POSITION, mCurrentPosition);
        data.putStringArrayListExtra("check", mChecks);
        setResult(RESULT_OK, data);
        super.finishAfterTransition();
    }
}
