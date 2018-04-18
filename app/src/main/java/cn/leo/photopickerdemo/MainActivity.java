package cn.leo.photopickerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import cn.leo.photopicker.pick.FragmentCallback;
import cn.leo.photopicker.pick.PhotoPicker;

public class MainActivity extends AppCompatActivity implements PhotoLoader.OnPhotoLoadFinishListener {

    private ImageView mImageView;
    private ImageShow mLlContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(cn.leo.photopicker.R.layout.activity_main);
        mImageView = (ImageView) findViewById(cn.leo.photopicker.R.id.iv_img);
        mLlContainer = (ImageShow) findViewById(cn.leo.photopicker.R.id.ll_container);
        mLlContainer.setOnImageClickListener(new ImageShow.OnImageClickListener() {
            @Override
            public void onClick(String tag, ImageView imageView) {
                Toast.makeText(MainActivity.this, tag, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void selectSinglePhotoCrop(View v) {
        //选择一张图片并裁剪
        PhotoPicker.selectPhoto(this)
                .crop(600, 600)
                .take(new PhotoPicker.PhotoCallBack() {
            @Override
            public void onPicSelected(String[] path) {
                Glide.with(MainActivity.this)
                        .load(path[0])
                        .centerCrop()
                        .into(mImageView);
                Toast.makeText(MainActivity.this, "" + Arrays.toString(path), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void selectSinglePhoto(View v) {
        //选择一张图片不裁剪
        PhotoPicker.selectPhoto(this)
                .compress(50, 50)
                .sizeLimit(10 * 1024)
                .take(new PhotoPicker.PhotoCallBack() {
                    @Override
                    public void onPicSelected(String[] path) {
                        Glide.with(MainActivity.this)
                                .load(path[0])
                                .centerCrop()
                                .into(mImageView);
                        Toast.makeText(MainActivity.this, "" + Arrays.toString(path), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void multiSelect(View v) {
        //选择多张图片
        PhotoPicker.selectPhoto(this)
                .multi(3)
                .sizeLimit(500 * 1024)
                .compress(1080, 1920)
                .take(new PhotoPicker.PhotoCallBack() {

                    @Override
                    public void onPicSelected(String[] path) {
                        //mLlContainer.removeAllViews();
                        mLlContainer.removeAllImageView();
                        for (int i = 0; i < path.length; i++) {
                            ImageView iv = new ImageView(MainActivity.this);
                            mLlContainer.addImageView(iv, "image" + i);
                            RelativeLayout.LayoutParams mParams = (RelativeLayout.LayoutParams) iv.getLayoutParams();
                            if (i > 0) {
                                mParams.leftMargin = 5;
                            }
                            iv.setLayoutParams(mParams);
                            Glide.with(MainActivity.this)
                                    .load(path[i])
                                    .listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String s, Target<GlideDrawable> target, boolean b) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable glideDrawable, String s, Target<GlideDrawable> target, boolean b, boolean b1) {
                                            return false;
                                        }
                                    })
                                    .centerCrop()
                                    .into(iv);
                        }

                    }
                });
    }


    private void testLoader() {
        getLoaderManager().initLoader(0, null, new PhotoLoader(this, this));
    }

    @Override
    public void onPhotoLoadFinish(HashMap<String, ArrayList<String>> photos) {

    }
}
