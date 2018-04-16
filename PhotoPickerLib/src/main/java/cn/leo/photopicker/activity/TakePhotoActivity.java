package cn.leo.photopicker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import cn.leo.photopicker.R;
import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.pick.FragmentCallback;
import cn.leo.photopicker.pick.ImageCompressUtil;
import cn.leo.photopicker.pick.MediaStoreContentObserver;
import cn.leo.photopicker.pick.PhotoFolderPopupWindow;
import cn.leo.photopicker.pick.PhotoOptions;
import cn.leo.photopicker.pick.PhotoPicker;
import cn.leo.photopicker.pick.PhotoPickerFileProvider;
import cn.leo.photopicker.pick.PhotoProvider;
import cn.leo.photopicker.utils.PermissionUtil;
import cn.leo.photopicker.utils.ToastUtil;
import cn.leo.photopicker.utils.VideoUtil;
import cn.leo.photopicker.view.TransitionElement;

public class TakePhotoActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static final int REQUEST_CLIP = 0x03;
    public static final int REQUEST_CHECK = 0x04;
    private ImageView mIvBack;
    private TextView mTvTitle;
    private ImageView mIvArrow;
    private GridView mGvPhotos;
    private RelativeLayout mRlBar;
    private ArrayList<String> mAllPhotos;
    private HashMap<String, ArrayList<String>> mDiskPhotos;
    private ArrayList<String> mSelectPhotos = new ArrayList<>();
    private GVAdapter mAdapter;
    private String mCamImageName;
    private static PhotoOptions photoOptions;
    private static PhotoPicker.PhotoCallBack picCallBack;
    private TextView mBtnComplete;
    private LinearLayout mLlTitleContainer;
    private HashMap<String, ImageView> imageViews = new HashMap<>();
    private VideoUtil mVideoUtil;
    private MediaStoreContentObserver mMediaStoreContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        initStatusBar();
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            PhotoOptions options = savedInstanceState.getParcelable("options");
            if (options != null) {
                photoOptions = options;
            }
        }
        setContentView(R.layout.activity_tack_photo);
        initView();
        initEvent();
        initPermission();
    }

    /**
     * 沉浸式状态栏
     */
    private void initStatusBar() {
        Window win = getWindow();
        //KITKAT也能满足，只是SYSTEM_UI_FLAG_LIGHT_STATUS_BAR（状态栏字体颜色反转）只有在6.0才有效
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            win.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//透明状态栏
            // 状态栏字体设置为深色，SYSTEM_UI_FLAG_LIGHT_STATUS_BAR 为SDK23增加
            win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN /*|
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR*/);
            //不调节状态栏文字颜色
            //win.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
        PermissionUtil.getInstance(this)
                .request(PermissionUtil.permission.READ_EXTERNAL_STORAGE,
                        PermissionUtil.permission.WRITE_EXTERNAL_STORAGE)
                .execute(new PermissionUtil.Result() {
                    @Override
                    public void onSuccess() {
                        initData();
                    }

                    @Override
                    public void onFailed() {
                        ToastUtil.showToast(TakePhotoActivity.this, "缺少权限，图片加载失败！");
                    }
                });
    }

    /**
     * 获取相机权限
     */
    private void initCameraPermission() {
        PermissionUtil.getInstance(this)
                .request(PermissionUtil.permission.CAMERA)
                .execute(new PermissionUtil.Result() {
                    @Override
                    public void onSuccess() {
                        toOpenCamera();
                    }

                    @Override
                    public void onFailed() {
                        ToastUtil.showToast(TakePhotoActivity.this, "缺少权限，打开相机失败！");
                    }
                });
    }

    /**
     * 开启选择
     *
     * @param context
     * @param options
     */

    public static void startSelect(Fragment context, PhotoOptions options) {
        photoOptions = options;
        Intent intent = new Intent(context.getActivity(), TakePhotoActivity.class);
        context.startActivityForResult(intent, FragmentCallback.REQUEST_CODE);
    }

    private void initData() {
        mVideoUtil = new VideoUtil(this);
        mAdapter = new GVAdapter();
        mGvPhotos.setAdapter(mAdapter);
        refreshData();
        mMediaStoreContentObserver = new MediaStoreContentObserver(this, new Handler());
        Uri imageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        getContentResolver().registerContentObserver(imageUri, false,
                mMediaStoreContentObserver);
    }

    @Override
    protected void onDestroy() {
        if (mMediaStoreContentObserver != null) {
            getContentResolver().unregisterContentObserver(mMediaStoreContentObserver);
        }
        super.onDestroy();
    }

    public void refreshData() {
        new Thread() {
            @Override
            public void run() {
                String title = "全部";
                if (photoOptions.type == PhotoOptions.TYPE_PHOTO) {
                    mDiskPhotos = PhotoProvider.getDiskPhotos(TakePhotoActivity.this);
                } else {
                    mDiskPhotos = PhotoProvider.getDiskVideos(TakePhotoActivity.this, mVideoUtil);
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
        if (photoOptions != null) {
            String text = "完成(" + mSelectPhotos.size() + "/" + photoOptions.takeNum + ")";
            if (photoOptions.crop || photoOptions.takeNum < 2) {
                text = "完成";
            }
            mBtnComplete.setText(text);
        }
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
            //返回
            finish();
        }
        if (v == mLlTitleContainer) {
            //弹出图片选择文件夹
            showDirPopWindow();
        }
        if (v == mBtnComplete) {
            //完成选择
            complete();
        }
    }

    private void complete() {
        String[] p = new String[mSelectPhotos.size()];
        mSelectPhotos.toArray(p);
        //单选并裁剪
        if (photoOptions.crop && photoOptions.takeNum < 2) {
            if (p.length == 1) {
                CropActivity.startSelect(this, p[0], photoOptions/*, picCallBack*/);
            }
            //finish();
        } else {
            //完成选择
            if (mSelectPhotos.size() > 0) {
                if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                    //如果是视频，需要创建缩略图
                    getThumb(p);
                } else if (photoOptions.compressWidth > 0 && photoOptions.compressHeight > 0) {
                    //需要压缩
                    compress();
                } else {
                    mHandler.obtainMessage(0, p).sendToTarget();
                }
            }
        }
    }


    Handler mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String[] compressPaths = (String[]) msg.obj;
            if (picCallBack == null) {
                Intent data = new Intent();
                data.putExtra("imgList", compressPaths);
                setResult(RESULT_OK, data);
            } else {
                picCallBack.onPicSelected(compressPaths);
            }
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            finish();
            return true;
        }
    });
    ProgressDialog mProgressDialog;

    //提取视频缩略图
    private void getThumb(final String[] videoPaths) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("视频处理中，请稍候...");
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
        for (int i = 0; i < mSelectPhotos.size(); i++) {
            final String path = mSelectPhotos.get(i);
            Glide.with(TakePhotoActivity.this)
                    .load(path)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                            String savePath = ImageCompressUtil.changeFileType(path);
                            if (!new File(savePath).exists()) {
                                ImageCompressUtil.compressThumb(bitmap, savePath);
                            }
                        }
                    });
        }
        mHandler.obtainMessage(0, videoPaths).sendToTarget();
    }

    //压缩图片
    private void compress() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("图片压缩中，请稍候...");
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
        new Thread() {
            @Override
            public void run() {
                String[] compressPaths = new String[mSelectPhotos.size()];
                for (int i = 0; i < mSelectPhotos.size(); i++) {
                    String selectPhoto = mSelectPhotos.get(i);
                    File file = new File(selectPhoto);
                    if (photoOptions.size > 0 && file.length() < photoOptions.size) {
                        compressPaths[i] = selectPhoto;
                    } else {
                        String descPath = CropUtil.getCachePath() + file.getName();
                        long compressPx = ImageCompressUtil.compressPx(selectPhoto, descPath, photoOptions);
                        int quality = 90;
                        while (photoOptions.size > 0 && compressPx > photoOptions.size && quality > 10) {
                            compressPx = ImageCompressUtil.compressPx(descPath, photoOptions, quality);
                            quality -= 10;
                        }
                        compressPaths[i] = descPath;
                    }
                }
                mHandler.obtainMessage(0, compressPaths).sendToTarget();
            }
        }.start();
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
            initCameraPermission();

        } else {
            //视频预览
            if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                File file = new File(mAllPhotos.get(position - 1));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(this,
                            getPackageName() + "." +
                                    PhotoPickerFileProvider.class.getSimpleName(),
                            file);
                    intent.setDataAndType(contentUri, "video/mp4");
                } else {
                    uri = Uri.fromFile(file);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(uri, "video/mp4");
                }
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "没有默认播放器,无法预览视频", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            //点击一张照片
            String path = mAllPhotos.get(position - 1);
            ArrayList<String> pathList = new ArrayList<>();
            pathList.add(path);
            //照片预览
            Intent intent = new Intent(this, PhotoShowActivity.class);
            intent.putExtra("check", mSelectPhotos.contains(path)); //传递选中
            intent.putStringArrayListExtra("images", pathList); //预览一张
            //预览所有
            //intent.putExtra(EXTRA_STARTING_ALBUM_POSITION, position - 1);
            //intent.putStringArrayListExtra("images", mAllPhotos);
            //共享动画
            TransitionElement.transitionStart(this, intent, view, path, REQUEST_CHECK);
        }
    }


    /**
     * 开启相机拍照/录像
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
            Toast.makeText(this, "无法保存图像，请检查SD卡是否挂载", Toast.LENGTH_LONG).show();
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
            String provider = getPackageName() + "." + PhotoPickerFileProvider.class.getSimpleName();
            uri = FileProvider.getUriForFile(this, provider, out);
        } else {
            uri = Uri.fromFile(out);
        }
        //视频选取条件限制
        if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
            intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
            if (photoOptions.duration > 0) {
                //限制时长
                intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, photoOptions.duration / 1000);
            }
            if (photoOptions.size > 0) {
                //限制大小
                intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, photoOptions.size);
            }
        } else {
            intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CLIP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CLIP:
                    if (mCamImageName == null) return;
                    //刷新显示
                    String path = CropUtil.getCameraPath() + mCamImageName;
                    mAllPhotos.add(0, path);
                    mAdapter.notifyDataSetChanged();
                    //通知系统相册更新
                    MediaScannerConnection.scanFile(this, new String[]{path}, null, null);
                    //需要裁剪
                    if (photoOptions.crop && photoOptions.takeNum < 2) {
                        CropActivity.startSelect(this, path, photoOptions);
                    }
                    break;
                case REQUEST_CHECK:
                    boolean check = data.getBooleanExtra("check", false);
                    String path1 = data.getStringExtra("path");
                    if (check) {
                        mSelectPhotos.add(path1);
                    } else {
                        mSelectPhotos.remove(path1);
                    }
                    mAdapter.notifyDataSetChanged();
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

            ViewHolder(View itemView) {
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
                Glide.with(itemView.getContext())
                        .load(path)
                        .crossFade()
                        .centerCrop()
                        .into(mIvPhoto);
                if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                    mTvDuration.setVisibility(View.VISIBLE);
                    //读取时长和大小
                    mTvDuration.setText(mVideoUtil.getTime(path));
                    if (mVideoUtil.getDuration(path) > photoOptions.duration + 500) {
                        mTvDuration.setTextColor(Color.RED);
                    } else {
                        mTvDuration.setTextColor(Color.WHITE);
                    }
                } else {
                    mTvDuration.setVisibility(View.GONE);
                }
            }

            void setCameraPic() {
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
                    if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                        VideoUtil.VideoInfo videoInfo = mVideoUtil.getVideoInfo(mPath);
                        if (photoOptions.duration > 0 &&
                                videoInfo.duration > photoOptions.duration + 500) {
                            Toast.makeText(TakePhotoActivity.this,
                                    "您选择的视频时长超出限制",
                                    Toast.LENGTH_SHORT).show();
                            mCbCheck.setChecked(false);
                            return;
                        }
                        if (photoOptions.size > 0 &&
                                videoInfo.size > photoOptions.size) {
                            Toast.makeText(TakePhotoActivity.this,
                                    "您选择的视频大小超出限制",
                                    Toast.LENGTH_SHORT).show();
                            mCbCheck.setChecked(false);
                            return;
                        }
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
                        return;
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("options", photoOptions);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            PhotoOptions options = savedInstanceState.getParcelable("options");
            if (options != null) {
                photoOptions = options;
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mAllPhotos == null || mAllPhotos.size() == 0) {
            initPermission();
        }
    }
}
