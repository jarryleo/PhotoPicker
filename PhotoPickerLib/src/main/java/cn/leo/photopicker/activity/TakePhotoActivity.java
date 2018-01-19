package cn.leo.photopicker.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.leo.photopicker.R;
import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.pick.PermissionUtil;
import cn.leo.photopicker.pick.PhotoFolderPopupWindow;
import cn.leo.photopicker.pick.PhotoOptions;
import cn.leo.photopicker.pick.PhotoPicker;
import cn.leo.photopicker.pick.PhotoProvider;
import cn.leo.photopicker.pick.VideoUtil;

public class TakePhotoActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    private static final String EXTRA_CURRENT_ALBUM_POSITION = "extra_current_item_position";
    private ImageView mIvBack;
    private TextView mTvTitle;
    private ImageView mIvArrow;
    private GridView mGvPhotos;
    private RelativeLayout mRlBar;
    private HashMap<String, ArrayList<String>> mDiskPhotos;
    private ArrayList<String> mAllPhotos;
    private ArrayList<String> mSelectPhotos = new ArrayList<>();
    private GVAdapter mAdapter;
    private String mCamImageName;
    private static PhotoOptions photoOptions;
    private static PhotoPicker.PhotoCallBack picCallBack;
    private TextView mBtnComplete;
    private LinearLayout mLlTitleContainer;
    private HashMap<String, ImageView> imageViews = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initStatusBar();
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_tack_photo);
        initView();
        initEvent();
        initPermission();
    }

    private void initStatusBar() {
        Window win = getWindow();
        //KITKAT也能满足，只是SYSTEM_UI_FLAG_LIGHT_STATUS_BAR（状态栏字体颜色反转）只有在6.0才有效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN /*|
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR*/);
            //win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN); //不调节状态栏文字颜色
            // 部分机型的statusbar会有半透明的黑色背景
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(Color.TRANSPARENT);// SDK21
        }
    }

    /**
     * 检查权限
     */
    private void initPermission() {
        boolean SDPermission = PermissionUtil.checkPremission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!SDPermission) {
            PermissionUtil.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            initData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean result = PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!result) {
            Toast.makeText(this, "缺少权限，图片加载失败！", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            initData();
        }
    }

    /**
     * 开启选择
     *
     * @param context
     * @param options
     */
    public static void startSelect(Activity context, PhotoOptions options,
                                   PhotoPicker.PhotoCallBack callBack) {
        photoOptions = options;
        picCallBack = callBack;
        context.startActivity(new Intent(context, TakePhotoActivity.class));
    }

    private void initData() {
        mAdapter = new GVAdapter();
        mGvPhotos.setAdapter(mAdapter);
        new Thread() {
            @Override
            public void run() {
                String title = "全部";
                if (photoOptions.type == PhotoOptions.TYPE_PHOTO) {
                    mDiskPhotos = PhotoProvider.getDiskPhotos(TakePhotoActivity.this);
                } else {
                    mDiskPhotos = PhotoProvider.getDiskVideos(TakePhotoActivity.this);
                }
                if (mDiskPhotos.containsKey("Camera")) {
                    mAllPhotos = mDiskPhotos.get("Camera");
                    title = "Camera";
                } else {
                    mAllPhotos = PhotoProvider.getAllPhotos(mDiskPhotos);
                }
                final String s = title;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvTitle.setText(s);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }


    private void initView() {
        mRlBar = (RelativeLayout) findViewById(R.id.photo_picker_rl_bar);
        mIvBack = (ImageView) findViewById(R.id.photo_picker_iv_back);
        mIvArrow = (ImageView) findViewById(R.id.photo_picker_iv_arrow);
        mTvTitle = (TextView) findViewById(R.id.photo_picker_tv_title);
        mGvPhotos = (GridView) findViewById(R.id.photo_picker_gv_photos);
        mLlTitleContainer = (LinearLayout) findViewById(R.id.photo_picker_ll_titlecontainer);
        mBtnComplete = (TextView) findViewById(R.id.tv_btn_complete);
        //mBtnComplete.setVisibility(photoOptions.takeNum > 1 ? View.VISIBLE : View.GONE);
        String text = "完成(" + mSelectPhotos.size() + "/" + photoOptions.takeNum + ")";
        if (photoOptions.crop || photoOptions.takeNum < 2) {
            text = "完成";
        }
        mBtnComplete.setText(text);
    }

    private void initEvent() {
        mIvBack.setOnClickListener(this);
        //mIvArrow.setOnClickListener(this);
        //mTvTitle.setOnClickListener(this);
        mBtnComplete.setOnClickListener(this);
        mGvPhotos.setOnItemClickListener(this);
        mLlTitleContainer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == mIvBack) {
            finish();
        }
        if (v == mLlTitleContainer) {
            //弹出图片选择文件夹
            showDirPopWindow();
        }
        if (v == mBtnComplete) {
            String[] p = new String[mSelectPhotos.size()];
            mSelectPhotos.toArray(p);
            finish();
            //单选并裁剪
            if (photoOptions.crop && photoOptions.takeNum < 2) {
                if (p.length == 1)
                    CropActivity.startSelect(this, p[0], photoOptions, picCallBack);
            } else if (photoOptions.takeNum < 2) {
                //单选不开启裁剪
                picCallBack.onPicSelected(p);
            } else {
                //完成图片多选
                picCallBack.onPicSelected(p);
            }
        }
    }

    //图片文件夹选择
    private void showDirPopWindow() {
        Rect rectangle = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int height = rectangle.bottom - rectangle.top - mRlBar.getMeasuredHeight();
        PhotoFolderPopupWindow popupWindow =
                new PhotoFolderPopupWindow(this, height, new PhotoFolderPopupWindow.Callback() {
                    @Override
                    public void onSelect(PhotoFolderPopupWindow popupWindow, String folder) {
                        popupWindow.dismiss();

                        mAllPhotos = mDiskPhotos.get(folder);
                        if ("全部".equals(folder)) {
                            mAllPhotos = PhotoProvider.getAllPhotos(mDiskPhotos);
                        }
                        mTvTitle.setText(folder);
                        mAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onDismiss() {
                        mIvArrow.setImageResource(R.mipmap.ic_arrow_bottom);
                    }

                    @Override
                    public void onShow() {
                        mIvArrow.setImageResource(R.mipmap.ic_arrow_top);
                    }
                });
        popupWindow.setAdapter(mDiskPhotos);
        popupWindow.showAsDropDown(mRlBar);
    }

    //图片点击事件
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            //打开相机拍照，并返回相片
            toOpenCamera();

        } else {
            //点击一张照片
            String path = mAllPhotos.get(position - 1);
            ArrayList<String> pathList = new ArrayList<>();
            pathList.add(path);
            //照片预览
            Intent intent = new Intent(this, PhotoShowActivity.class);
            intent.putStringArrayListExtra("images", pathList); //预览一张
            //预览所有
            //intent.putExtra(EXTRA_STARTING_ALBUM_POSITION, position - 1);
            //intent.putStringArrayListExtra("images", mAllPhotos);

            //共享动画
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                        view, path).toBundle());
            } else {
                startActivity(intent);
            }

           /* //单张并开启裁剪
            if (photoOptions.crop && photoOptions.takeNum < 2) {
                CropActivity.startSelect(this, mPath, photoOptions, picCallBack);
                finish();
            } else if (photoOptions.takeNum < 2) {
                //单张不开启裁剪
                picCallBack.onPicSelected(new String[]{mPath});
                finish();
            } else {
                //多张
                GVAdapter.ViewHolder tag = (GVAdapter.ViewHolder) view.getTag();
                tag.mCbCheck.setChecked(!tag.mCbCheck.isChecked());
            }*/
        }
    }


    /**
     * 开启相机拍照
     */
    private void toOpenCamera() {
        // 判断是否挂载了SD卡
        mCamImageName = null;
        String savePath = "";
        if (CropUtil.hasSDCard()) {
            savePath = CropUtil.getCameraPath();
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
        }

        // 没有挂载SD卡，无法保存文件
        if (TextUtils.isEmpty(savePath)) {
            Toast.makeText(this, "无法保存照片，请检查SD卡是否挂载", Toast.LENGTH_LONG).show();
            return;
        }
        if (photoOptions.type == PhotoOptions.TYPE_PHOTO) {
            mCamImageName = CropUtil.getSaveImageFullName();
        } else {
            mCamImageName = CropUtil.getSaveVideoFullName();
        }
        File out = new File(savePath, mCamImageName);
        /**
         * android N 系统适配
         */
        Intent intent = new Intent();
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String provider = getPackageName() + ".provider";
            uri = FileProvider.getUriForFile(this, provider, out);
        } else {
            uri = Uri.fromFile(out);
        }
        if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
            intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
        } else {
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 0x03);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            switch (requestCode) {
                case 0x03:
                    if (mCamImageName == null) return;
                    //刷新显示
                    String path = CropUtil.getCameraPath() + mCamImageName;
                    mAllPhotos.add(0, path);
                    mAdapter.notifyDataSetChanged();
                    //通知系统相册更新
                    // 其次把文件插入到系统图库
                    try {
                        if (photoOptions.type == PhotoOptions.TYPE_PHOTO) {
                            MediaStore.Images.Media.insertImage(getContentResolver(),
                                    path, mCamImageName, null);
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Uri localUri = Uri.fromFile(new File(path));
                    Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
                    sendBroadcast(localIntent);
                    //需要裁剪
                    if (photoOptions.crop && photoOptions.takeNum < 2) {
                        CropActivity.startSelect(this, path, photoOptions, picCallBack);
                        finish();
                    }
                    break;
                /*case 0x04:
                    if (data == null) return;
                    mOption.getCallback().doSelected(new String[]{data.getStringExtra("crop_path")});
                    finish();
                    break;*/
            }
        }
    }


    private class GVAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mAllPhotos != null) {
                return mAllPhotos.size() + 1;
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mAllPhotos != null) {
                mAllPhotos.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
                holder = new ViewHolder(view);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (position == 0) {
                holder.setCameraPic();
            } else {
                holder.setData(mAllPhotos.get(position - 1));
            }
            return holder.itemView;
        }

        class ViewHolder implements CompoundButton.OnCheckedChangeListener {
            View itemView;
            ImageView mIvPhoto;
            CheckBox mCbCheck;
            TextView mTvDuration;
            String mPath;

            public ViewHolder(View itemView) {
                this.itemView = itemView;
                int screenWidth = CropUtil.getScreenWidth(itemView.getContext());
                int itemSize = (int) ((screenWidth - CropUtil.dip2Px(itemView.getContext(), 4) + 0.5f) / 3);
                ViewGroup.LayoutParams params = itemView.getLayoutParams();
                params.height = itemSize;
                params.width = itemSize;
                itemView.setLayoutParams(params);
                itemView.setTag(this);
                mIvPhoto = (ImageView) itemView.findViewById(R.id.item_iv_photo);
                mCbCheck = (CheckBox) itemView.findViewById(R.id.item_cb_check);
                mTvDuration = (TextView) itemView.findViewById(R.id.tv_video_duration);
                mCbCheck.setOnCheckedChangeListener(this);
            }

            public void setData(String path) {
                mPath = path;
                mCbCheck.setChecked(mSelectPhotos.contains(mPath)); //勾选框复用问题
                mCbCheck.setVisibility(View.VISIBLE);
                //mCbCheck.setVisibility(photoOptions.takeNum > 1 ? View.VISIBLE : View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mIvPhoto.setTransitionName(path); //设置共享名称
                    imageViews.put(path, mIvPhoto);
                }
                Glide.with(itemView.getContext())
                        .load(path)
                        .crossFade()
                        .centerCrop()
                        .into(mIvPhoto);
                if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                    mTvDuration.setVisibility(View.VISIBLE);
                    VideoUtil.VideoInfo videoInfo = VideoUtil.videoInfo(TakePhotoActivity.this, path);
                    mTvDuration.setText(videoInfo.getTime());
                } else {
                    mTvDuration.setVisibility(View.GONE);
                }
            }

            public void setCameraPic() {
                mTvDuration.setVisibility(View.GONE);
                mCbCheck.setVisibility(View.GONE);
                mIvPhoto.setImageResource(R.mipmap.ic_tweet_select_picture_camera);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mSelectPhotos.contains(mPath)) {
                        return;
                    }
                    if (mSelectPhotos.size() < photoOptions.takeNum) {
                        mSelectPhotos.add(mPath);
                    } else {
                        String s = "张照片！";
                        if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                            s = "个视频！";
                        }
                        Toast.makeText(TakePhotoActivity.this,
                                "您最多只能选择" +
                                        photoOptions.takeNum +
                                        s,
                                Toast.LENGTH_SHORT).show();
                        mCbCheck.setChecked(false);
                    }
                } else {
                    mSelectPhotos.remove(mPath);
                }
                String text = "完成(" + mSelectPhotos.size() + "/" + photoOptions.takeNum + ")";
                if (photoOptions.crop || photoOptions.takeNum < 2) {
                    text = "完成";
                }
                mBtnComplete.setText(text);
            }
        }
    }
}
