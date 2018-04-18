package cn.leo.photopicker.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import cn.leo.photopicker.R;
import cn.leo.photopicker.crop.CropLayout;
import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.crop.ZoomImageView;
import cn.leo.photopicker.pick.PhotoOptions;

public class CropActivity extends Activity implements View.OnClickListener {

    private static PhotoOptions photoOptions;
    private CropLayout mGl;
    private Button btn1;
    private Button btn2;
    private ImageView mIv;
    private static String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_crop);
        initView();
        initEvent();
        initData();
    }

    /**
     * 开启裁剪
     *
     * @param context
     * @param options
     */
    public static void startSelect(Activity context, String url,
                                   PhotoOptions options) {
        mUrl = url;
        photoOptions = options;
        context.startActivityForResult(
                new Intent(context, CropActivity.class),
                TakePhotoActivity.REQUEST_CLIP);
    }

    private void initView() {
        mGl = findViewById(R.id.gl);
        btn1 = findViewById(R.id.take_btn1);
        btn2 = findViewById(R.id.take_btn2);
        mIv = findViewById(R.id.take_iv);
    }

    private void initEvent() {
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btn1) {
            Bitmap bitmap = mGl.cropBitmap();
            //mIv.setImageBitmap(bitmap);
            String path = CropUtil.getCachePath() + new File(mUrl).getName();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File(path));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Intent data = new Intent();
                data.putExtra("path", path);
                setResult(RESULT_OK, data);
                finish();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            finish();
        }
        if (v == btn2) {
            finish();
        }
    }

    private void initData() {
        ZoomImageView imageView = mGl.getImageView();
        //Intent intent = getIntent();
        //String url = intent.getStringExtra("url");
        //int screenSize = CropUtil.getScreenWidth(this);
        //Bitmap bitmap = CropUtil.decodeSampledBitmapFromFile(mUrl);
        //imageView.setImageBitmap(bitmap);
        new BitmapWorkerTask(imageView).execute(mUrl);
        mGl.setCropWidth(photoOptions.cropWidth);
        mGl.setCropHeight(photoOptions.cropHeight);
        mGl.start();
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String data = null;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
            data = params[0];
            return CropUtil.decodeSampledBitmapFromFile(data,
                    CropUtil.getScreenWidth(CropActivity.this),
                    CropUtil.getScreenWidth(CropActivity.this));
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
}



