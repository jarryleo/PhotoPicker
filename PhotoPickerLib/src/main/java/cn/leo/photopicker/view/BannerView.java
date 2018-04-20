package cn.leo.photopicker.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import cn.leo.photopicker.photoview.PhotoView;

/**
 * Created by Leo on 2017/7/12.
 */

public class BannerView extends FrameLayout implements ViewPager.OnPageChangeListener {
    private List<String> mImageList;
    private innerViewPager mViewPager;
    private ImageAdapter mAdapter;
    private boolean mAutoScroll = false;
    private boolean mInfiniteMode = false;
    private ImageLoader mImageLoader;
    private Handler mHandler;
    private int mScrollInterval = 3000; //自动轮播间隔3秒
    private int mCurrentItem;
    private OnPageClickListener mOnPageClickListener;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;

    public BannerView(Context context) {
        this(context, null);
    }

    public BannerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 必须实现图片加载功能
     *
     * @param imageLoader
     * @return
     */
    public BannerView initImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
        return this;
    }

    public void setImageList(List<String> imageList, boolean infiniteMode) {
        mInfiniteMode = infiniteMode;
        setImageList(imageList);
    }


    public void setImageList(List<String> imageList) {
        if (imageList == null) return;
        mImageList = imageList;
        if (mViewPager == null) {
            mViewPager = new innerViewPager(getContext());
            this.addView(mViewPager);
            mViewPager.setLayoutParams(new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            mAdapter = new ImageAdapter();
            mViewPager.setAdapter(mAdapter);
            if (mInfiniteMode) {
                mCurrentItem = mImageList.size() * 50000;
            }
            mViewPager.setCurrentItem(mCurrentItem);
            mViewPager.addOnPageChangeListener(this);
            mHandler = new Handler(Looper.getMainLooper());
        } else {
            mAdapter.notifyDataSetChanged();
        }
        if (mAutoScroll) {
            openAutoScroll();
        }
    }

    public void setCurrentItem(int currentItem) {
        if (mViewPager == null) return;
        if (currentItem < mImageList.size() * 50000 && mInfiniteMode) {
            currentItem += mImageList.size() * 50000;
        }
        mCurrentItem = currentItem;
        mViewPager.setCurrentItem(mCurrentItem);
    }


    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mOnPageChangeListener = onPageChangeListener;
    }

    public void setOnPageClickListener(OnPageClickListener onPageClickListener) {
        mOnPageClickListener = onPageClickListener;
    }

    /**
     * 设置自动轮播间隔时长
     *
     * @param interval 单位 ms
     */
    public void setScrollInterval(int interval) {
        mScrollInterval = interval;
    }

    /**
     * 开启自动轮播
     */
    public void openAutoScroll() {
        mAutoScroll = true;
        autoScroll();
    }

    /**
     * 关闭自动轮播
     */
    public void closeAutoScroll() {
        mAutoScroll = false;
        mHandler.removeCallbacks(autoScrollMission);
    }

    private void autoScroll() {
        mHandler.removeCallbacks(autoScrollMission);
        mHandler.postDelayed(autoScrollMission, mScrollInterval);
    }

    private Runnable autoScrollMission = new Runnable() {

        @Override
        public void run() {
            mCurrentItem++;
            if (mCurrentItem > mImageList.size() * 100000L) {
                mCurrentItem = mImageList.size() * 50000 + 1;
            }
            mViewPager.setCurrentItem(mCurrentItem);
            autoScroll();
        }
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
        }
    }

    @Override
    public void onPageSelected(int position) {
        mCurrentItem = position;
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageScrollStateChanged(state);
        }
    }


    private class ImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            if (mImageList != null) {
                if (mInfiniteMode) {
                    return mImageList.size() * 100000;
                } else {
                    return mImageList.size();
                }
            }
            return 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            int i = position % mImageList.size();
            PhotoView photoView = new PhotoView(getContext());
            photoView.setLayoutParams(new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            container.addView(photoView);
            picLoader(photoView, mImageList.get(i));
            return photoView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        private void picLoader(ImageView imageView, String imagePath) {
            if (mImageLoader != null) {
                mImageLoader.loadImage(imageView, imagePath);
            }
        }

    }

    private class innerViewPager extends ViewPager {
        private GestureDetector mGestureDetector =
                new GestureDetector(getContext(),
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                if (mOnPageClickListener == null) return false;
                                int index = mCurrentItem % mImageList.size();
                                mOnPageClickListener.onClick(index, mImageList.get(index));
                                return true;
                            }
                        });

        public innerViewPager(Context context) {
            this(context, null);
        }

        public innerViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (!mAutoScroll) return super.onTouchEvent(ev);
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    //暂时关闭自动滑动
                    mHandler.removeCallbacks(autoScrollMission);
                    break;
                case MotionEvent.ACTION_UP:
                    //开启自动滑动
                    autoScroll();
                    break;
            }
            mGestureDetector.onTouchEvent(ev);
            return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 图片加载器用户自己实现
     */
    public interface ImageLoader {
        void loadImage(ImageView imageView, String imagePath);
    }

    public interface OnPageClickListener {
        void onClick(int index, String imagePath);
    }
}
