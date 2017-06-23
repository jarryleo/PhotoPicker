package cn.leo.photopickerdemo2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Arrays;

import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.pick.PhotoPicker;

public class MainActivity extends AppCompatActivity {

    private ImageView mImageView;
    private LinearLayout mLlContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(cn.leo.photopicker.R.layout.activity_main);
        mImageView = (ImageView) findViewById(cn.leo.photopicker.R.id.iv_img);
        mLlContainer = (LinearLayout) findViewById(cn.leo.photopicker.R.id.ll_container);
    }

    public void selectSinglePhotoCrop(View v) {
        //选择一张图片并裁剪
        PhotoPicker.selectPic(this, 1, true, 600, 600, new PhotoPicker.PicCallBack() {
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
        PhotoPicker.selectPic(this, 1, false, 0, 0, new PhotoPicker.PicCallBack() {
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
        PhotoPicker.selectPic(this, 3, false, 0, 0, new PhotoPicker.PicCallBack() {

            @Override
            public void onPicSelected(String[] path) {
                mLlContainer.removeAllViews();
                for (int i = 0; i < path.length; i++) {
                    ImageView iv = new ImageView(MainActivity.this);


                    mLlContainer.addView(iv);
                    LinearLayout.LayoutParams mParams = (LinearLayout.LayoutParams) iv.getLayoutParams();
                    if (i > 0) {
                        mParams.leftMargin = 5;
                    }
                    mParams.width = CropUtil.dip2Px(MainActivity.this, 200);
                    mParams.height = CropUtil.dip2Px(MainActivity.this, 150);
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
