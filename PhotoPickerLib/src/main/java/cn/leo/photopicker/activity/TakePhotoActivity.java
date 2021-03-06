package cn.leo.photopicker.activity;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import java.util.List;

import cn.leo.photopicker.R;
import cn.leo.photopicker.adapter.PhotoListAdapter;
import cn.leo.photopicker.bean.PhotoBean;
import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.loader.OnLoadFinishListener;
import cn.leo.photopicker.loader.PhotoLoader;
import cn.leo.photopicker.loader.VideoLoader;
import cn.leo.photopicker.pick.CompressTask;
import cn.leo.photopicker.pick.FragmentCallback;
import cn.leo.photopicker.pick.ImageCompressUtil;
import cn.leo.photopicker.pick.PhotoFolderPopupWindow;
import cn.leo.photopicker.pick.PhotoOptions;
import cn.leo.photopicker.pick.PhotoPickerFileProvider;
import cn.leo.photopicker.pick.PhotoProvider;
import cn.leo.photopicker.utils.PermissionUtil;
import cn.leo.photopicker.utils.PositionUtil;
import cn.leo.photopicker.utils.ToastUtil;
import cn.leo.photopicker.view.TransitionElement;

public class TakePhotoActivity extends TransitionAnimActivity implements View.OnClickListener, PhotoListAdapter.OnSelectChangeListener, OnLoadFinishListener {
    private static final String EXTRA_STARTING_ALBUM_POSITION = "extra_starting_item_position";
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_CHECK = 2;
    public static final int REQUEST_CLIP = 3;
    private HashMap<String, ArrayList<PhotoBean>> mDiskPhotos;
    private ImageView mIvBack;
    private TextView mTvTitle;
    private ImageView mIvArrow;
    private RecyclerView mRvPhotos;
    private RelativeLayout mRlBar;
    private String mCamImageName;
    private TextView mBtnComplete;
    private PhotoListAdapter mAdapter;
    private static PhotoOptions photoOptions;
    private LinearLayout mLlTitleContainer;
    private ProgressDialog mProgressDialog;
    private View mViewBottom;
    private View mViewTop;

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
     * @param context 上下文
     * @param options 选择配置参数
     */

    public static void startSelect(Fragment context, PhotoOptions options) {
        photoOptions = options;
        Intent intent = new Intent(context.getActivity(), TakePhotoActivity.class);
        context.startActivityForResult(intent, FragmentCallback.REQUEST_CODE);
    }

    private void initData() {
        mAdapter = new PhotoListAdapter(photoOptions, this);
        mRvPhotos.setAdapter(mAdapter);
        refreshData();
    }


    @Override
    public void onSelectChange(ArrayList<String> selectPhotos) {
        String text = "完成(" + selectPhotos.size() + "/" + photoOptions.takeNum + ")";
        if (photoOptions.crop || photoOptions.takeNum < 2) {
            text = "完成";
        }
        mBtnComplete.setText(text);
    }

    //获取数据
    public void refreshData() {
        LoaderManager.LoaderCallbacks loader;
        if (photoOptions.type == PhotoOptions.TYPE_PHOTO) {
            loader = new PhotoLoader(this, this);
        } else {
            loader = new VideoLoader(this, this);
        }
        getLoaderManager().initLoader(0, null, loader);
    }

    @Override
    public void onPhotoLoadFinish(HashMap<String, ArrayList<PhotoBean>> photos) {
        String title = "全部";
        mDiskPhotos = photos;
        final ArrayList<PhotoBean> photoBeans;
        if (mDiskPhotos.containsKey("Camera")) {
            photoBeans = mDiskPhotos.get("Camera");
            title = "Camera";
        } else {
            photoBeans = PhotoProvider.getAllPhotos(mDiskPhotos);
        }
        mTvTitle.setText(title);
        mAdapter.setData(photoBeans);
    }

    private void initView() {
        mViewTop = findViewById(R.id.viewTop);
        mViewBottom = findViewById(R.id.viewBottom);
        mRlBar = findViewById(R.id.photo_picker_rl_bar);
        mIvBack = findViewById(R.id.photo_picker_iv_back);
        mIvArrow = findViewById(R.id.photo_picker_iv_arrow);
        mTvTitle = findViewById(R.id.photo_picker_tv_title);
        mBtnComplete = findViewById(R.id.tv_btn_complete);
        mLlTitleContainer = findViewById(R.id.photo_picker_ll_titlecontainer);
        initButtonText();
        mRvPhotos = findViewById(R.id.rv_photos);
        mRvPhotos.setLayoutManager(new GridLayoutManager(this, 3));
    }

    private void initButtonText() {
        if (photoOptions != null) {
            String text = "完成(0/" + photoOptions.takeNum + ")";
            if (photoOptions.crop || photoOptions.takeNum < 2) {
                text = "完成";
            }
            mBtnComplete.setText(text);
        }
    }

    private void initEvent() {
        mIvBack.setOnClickListener(this);
        mBtnComplete.setOnClickListener(this);
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

    //完成选择返回结果
    private void complete() {
        String[] p = new String[mAdapter.getSelectPhotos().size()];
        mAdapter.getSelectPhotos().toArray(p);
        //单选并裁剪
        if (photoOptions.crop && photoOptions.takeNum < 2) {
            if (p.length == 1) {
                CropActivity.startSelect(this, p[0], photoOptions);
            }
        } else {
            //完成选择
            if (mAdapter.getSelectPhotos().size() > 0) {
                if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                    //如果是视频，需要创建缩略图
                    getThumb(p);
                } else if (photoOptions.compressWidth > 0 && photoOptions.compressHeight > 0) {
                    //需要压缩
                    compress();
                } else {
                    result(p);
                }
            }
        }
    }

    /**
     * 返回结果
     *
     * @param paths 返回的路径数组
     */
    private void result(String paths[]) {
        Intent data = new Intent();
        data.putExtra("imgList", paths);
        setResult(RESULT_OK, data);
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        finish();
    }


    //提取视频缩略图
    private void getThumb(final String[] videoPaths) {
        showDialog("视频处理中，请稍候...");
        mProgressDialog.show();
        for (int i = 0; i < mAdapter.getSelectPhotos().size(); i++) {
            final String path = mAdapter.getSelectPhotos().get(i);
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
        result(videoPaths);
    }


    private void showDialog(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(TakePhotoActivity.this);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    //压缩图片
    private void compress() {
        showDialog("图片压缩中，请稍候...");
        new CompressTask(photoOptions, new CompressTask.onCompressResultListener() {
            @Override
            public void onCompressResult(String[] paths) {
                result(paths);
            }
        }).execute(mAdapter);
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

                        ArrayList<PhotoBean> photoBeans = mDiskPhotos.get(folder);
                        if ("全部".equals(folder)) {
                            photoBeans = PhotoProvider.getAllPhotos(mDiskPhotos);
                        }
                        mTvTitle.setText(folder);
                        mAdapter.setData(photoBeans);
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
    public void onItemClick(View view, int position) {
        if (position == 0) {
            //打开相机拍照，并返回相片
            initCameraPermission();

        } else {
            String path = mAdapter.getPhoto(position - 1).path;
            //视频预览
            if (photoOptions.type == PhotoOptions.TYPE_VIDEO) {
                File file = new File(path);
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
            PositionUtil.pohotoPosition = position;
            //照片预览
            ArrayList<String> allPhotoPaths = mAdapter.getAllPhotoPaths(position - 1);
            int p = allPhotoPaths.indexOf(path);
            Intent intent = new Intent(this, PhotoShowActivity.class);
            intent.putExtra(EXTRA_STARTING_ALBUM_POSITION, p);
            intent.putStringArrayListExtra("images", allPhotoPaths);
            intent.putStringArrayListExtra("check", mAdapter.getSelectPhotos());
            intent.putExtra("max", photoOptions.takeNum);
            //共享动画
            TransitionElement.transitionStart(this, intent, view, "share", REQUEST_CHECK);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getItemByPosition(int currentPosition) {
        //mRvPhotos.scrollToPosition(currentPosition);
        currentPosition += mAdapter.getStart();
        GridLayoutManager layoutManager = (GridLayoutManager) mRvPhotos.getLayoutManager();
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        RecyclerView.ViewHolder viewHolder = mRvPhotos.findViewHolderForAdapterPosition(currentPosition);
        if (viewHolder == null) {
            if (currentPosition < first) {
                return mViewTop;
            } else if (currentPosition > last) {
                return mViewBottom;
            }
            return mBtnComplete;
        }
        return viewHolder.itemView;
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
        //android N 系统适配
        Intent intent = new Intent();
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String provider = getPackageName() + "." + PhotoPickerFileProvider.class.getSimpleName();
            uri = FileProvider.getUriForFile(this, provider, out);
            //加入uri权限
            List<ResolveInfo> infoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : infoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                grantUriPermission(packageName, uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        } else {
            uri = Uri.fromFile(out);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA:
                    if (mCamImageName == null) return;
                    //刷新显示
                    String path = CropUtil.getCameraPath() + mCamImageName;
                    //通知系统相册更新
                    MediaScannerConnection.scanFile(this, new String[]{path}, null, null);
                    //需要裁剪
                    if (photoOptions.crop && photoOptions.takeNum < 2) {
                        CropActivity.startSelect(this, path, photoOptions);
                    }
                    break;
                case REQUEST_CHECK:
                    ArrayList<String> checks = data.getStringArrayListExtra("check");
                    mAdapter.getSelectPhotos().clear();
                    mAdapter.getSelectPhotos().addAll(checks);
                    mAdapter.notifyDataSetChanged();
                    onSelectChange(mAdapter.getSelectPhotos());
                    break;
                case REQUEST_CLIP:
                    String cropPath = data.getStringExtra("path");
                    String[] paths = new String[]{cropPath};
                    result(paths);
                    break;
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
        if (mAdapter.getItemCount() == 1) {
            initPermission();
        }
    }

    @Override
    protected void onDestroy() {
        mDiskPhotos.clear();
        mAdapter.mList.clear();
        super.onDestroy();
    }
}
