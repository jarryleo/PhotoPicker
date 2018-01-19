package cn.leo.photopickerdemo;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Leo on 2017/7/11.
 */

public class ImageShow extends HorizontalScrollView implements View.OnClickListener {
    private OnImageClickListener mOnImageClickListener;
    private LinearLayout mContainer;
    private Map<String, RelativeLayout> mLayoutHashMap = new HashMap<>();

    public ImageShow(Context context) {
        this(context, null);
    }

    public ImageShow(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageShow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mContainer = new LinearLayout(getContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mContainer.setLayoutParams(layoutParams);
        this.addView(mContainer);
    }

    /**
     * 添加图片控件，返回本次添加的是第几张
     *
     * @param imageView
     * @return
     */
    public void addImageView(ImageView imageView, String tag) {

        int index = mLayoutHashMap.size();
        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        relativeLayout.setLayoutParams(layoutParams);
        relativeLayout.addView(imageView); //添加图片
        //添加删除按钮
        TextView textView = new TextView(getContext());
        textView.setText("删除");
        textView.setBackgroundColor(Color.RED);
        textView.setTag(tag);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = (String) v.getTag();
                removeImageView(tag); //删除控件
            }
        });

        relativeLayout.addView(textView);
        mLayoutHashMap.put(tag, relativeLayout);
        mContainer.addView(relativeLayout);
        imageView.setOnClickListener(this);
    }

    /**
     * 移除编号对应图片
     *
     * @param tag
     */
    public void removeImageView(String tag) {
        RelativeLayout remove = mLayoutHashMap.remove(tag);
        mContainer.removeView(remove);
    }

    /**
     * 清除所有图片
     */
    public void removeAllImageView() {
        mContainer.removeAllViews();
        mLayoutHashMap.clear();
    }


    @Override
    public void onClick(View v) {
        if (mOnImageClickListener == null) return;
        Set<Map.Entry<String, RelativeLayout>> entries = mLayoutHashMap.entrySet();
        Iterator<Map.Entry<String, RelativeLayout>> iterator = entries.iterator();
        for (Map.Entry<String, RelativeLayout> entry : entries) {
            String key = entry.getKey();
            RelativeLayout value = entry.getValue();
            ImageView view = (ImageView) value.getChildAt(0);
            if (view == v) {
                mOnImageClickListener.onClick(key, view);
            }
        }
    }

    /**
     * 设置图片点击事件
     *
     * @param onImageClickListener
     */
    public void setOnImageClickListener(OnImageClickListener onImageClickListener) {
        mOnImageClickListener = onImageClickListener;
    }

    public static interface OnImageClickListener {
        void onClick(String tag, ImageView imageView);
    }
}
