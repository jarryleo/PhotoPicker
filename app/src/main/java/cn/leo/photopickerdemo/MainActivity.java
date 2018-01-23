package cn.leo.photopickerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Arrays;

import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.pick.PhotoPicker;

public class MainActivity extends AppCompatActivity {

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
        PhotoPicker.selectPhoto(this).crop(600, 600).take(new PhotoPicker.PhotoCallBack() {
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
        PhotoPicker.selectVideo(this).take(new PhotoPicker.PhotoCallBack() {
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

    public void multSelect(View v) {
        //选择多张图片
        PhotoPicker.selectPhoto(this)
                .multi(3)
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
                                    .centerCrop()
                                    .into(iv);
                        }

                    }
                });
    }


}
