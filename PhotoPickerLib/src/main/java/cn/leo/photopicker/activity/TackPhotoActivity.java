package cn.leo.photopicker.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.leo.photopicker.R;
import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.pick.PermissionUtil;
import cn.leo.photopicker.pick.PhotoFolderPopupWindow;
import cn.leo.photopicker.pick.PhotoPicker;
import cn.leo.photopicker.pick.PhotoProvider;
import cn.leo.photopicker.pick.PhotoOptions;

public class TackPhotoActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ImageView mIvBack;
    private TextView mTvTitle;
    private ImageView mIvArrow;
    private GridView mGvPhotos;
    private RelativeLayout mRlBar;
    private HashMap<String, List<String>> mDiskPhotos;
    private List<String> mAllPhotos;
    private List<String> mSelectPhotos = new ArrayList<>();
    private GVAdapter mAdapter;
    private String mCamImageName;

    private static PhotoOptions photoOptions;
    private static PhotoPicker.PicCallBack picCallBack;
    private TextView mBtnComplete;
    private LinearLayout mLlTitleContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_tack_photo);
        initView();
        initEvent();
        initPermission();
    }

    /**
     * 检查权限
     */
    private void initPermission() {
        boolean SDpermission = PermissionUtil.checkPremission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!SDpermission) {
            PermissionUtil.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        } else {
            initData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean result = PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!result) {
            //Toast.makeText(this, "你不同意我读取图片，那我还处理个卵！", Toast.LENGTH_SHORT).show();
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
                                   PhotoPicker.PicCallBack callBack) {
        photoOptions = options;
        picCallBack = callBack;
        context.startActivity(new Intent(context, TackPhotoActivity.class));
    }

    private void initData() {
        mAdapter = new GVAdapter();
        mGvPhotos.setAdapter(mAdapter);
        new Thread() {
            @Override
            public void run() {
                mDiskPhotos = PhotoProvider.getDiskPhotos(TackPhotoActivity.this);
                mAllPhotos = PhotoProvider.getAllPhotos(mDiskPhotos);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
        mBtnComplete.setVisibility(photoOptions.takeNum > 1 ? View.VISIBLE : View.GONE);
        mBtnComplete.setText("完成(" + mSelectPhotos.size() + "/" + photoOptions.takeNum + ")");
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
            //完成图片多选
            String[] p = new String[mSelectPhotos.size()];
            mSelectPhotos.toArray(p);
            picCallBack.onPicSelected(p);
            finish();
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
                        if ("全部照片".equals(folder)) {
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
            //单张并开启裁剪
            if (photoOptions.crop && photoOptions.takeNum < 2) {
                CropActivity.startSelect(this, path, photoOptions, picCallBack);
                finish();
            } else if (photoOptions.takeNum < 2) {
                //单张不开启裁剪
                picCallBack.onPicSelected(new String[]{path});
                finish();
            } else {
                //多张
                GVAdapter.ViewHolder tag = (GVAdapter.ViewHolder) view.getTag();
                tag.mCbCheck.setChecked(!tag.mCbCheck.isChecked());
            }
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

        mCamImageName = CropUtil.getSaveImageFullName();
        File out = new File(savePath, mCamImageName);
        /**
         * android N 系统适配
         */
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //String provider = getPackageName() + ".take.PhotoPickerProvider";
            //uri = FileProvider.getUriForFile(this, provider, out);
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, out.getAbsolutePath());
            uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            uri = Uri.fromFile(out);
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
                mCbCheck.setOnCheckedChangeListener(this);
            }

            public void setData(String path) {
                mPath = path;
                mCbCheck.setChecked(mSelectPhotos.contains(mPath)); //勾选框复用问题
                mCbCheck.setVisibility(photoOptions.takeNum > 1 ? View.VISIBLE : View.GONE);
                Glide.with(itemView.getContext())
                        .load(path)
                        .centerCrop()
                        .into(mIvPhoto);
            }

            public void setCameraPic() {
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
                        Toast.makeText(TackPhotoActivity.this,
                                "您最多只能选择" + photoOptions.takeNum + "张照片！",
                                Toast.LENGTH_SHORT).show();
                        mCbCheck.setChecked(false);
                    }
                } else {
                    mSelectPhotos.remove(mPath);
                }
                mBtnComplete.setText("完成(" + mSelectPhotos.size() + "/" + photoOptions.takeNum + ")");
            }
        }
    }
}
