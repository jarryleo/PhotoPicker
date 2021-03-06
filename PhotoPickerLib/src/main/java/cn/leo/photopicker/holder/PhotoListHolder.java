package cn.leo.photopicker.holder;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import cn.leo.photopicker.R;
import cn.leo.photopicker.adapter.PhotoListAdapter;
import cn.leo.photopicker.bean.PhotoBean;
import cn.leo.photopicker.crop.CropUtil;
import cn.leo.photopicker.pick.PhotoOptions;
import cn.leo.photopicker.utils.ToastUtil;

/**
 * Created by Leo on 2018/4/16.
 */

public class PhotoListHolder extends
        BaseRVHolder<PhotoBean>
        implements
        CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private ImageView mIvPhoto;
    private CheckBox mCbCheck;
    private TextView mTvDuration;
    private PhotoBean mPhotoBean;
    private ArrayList<String> mSelectPhotos;
    private PhotoOptions mPhotoOptions;
    private PhotoListAdapter mPhotoListAdapter;
    private OnItemClickListener mOnItemClickListener;


    public PhotoListHolder(View itemView) {
        super(itemView);
        int screenWidth = CropUtil.getScreenWidth(itemView.getContext());
        int itemSize = screenWidth / 3;
        ViewGroup.LayoutParams params = itemView.getLayoutParams();
        params.height = itemSize;
        params.width = itemSize;
        itemView.setLayoutParams(params);
        itemView.setTag(this);
        mIvPhoto = itemView.findViewById(R.id.item_iv_photo);
        mCbCheck = itemView.findViewById(R.id.item_cb_check);
        mTvDuration = itemView.findViewById(R.id.tv_video_duration);
        mCbCheck.setOnCheckedChangeListener(this);
        itemView.setOnClickListener(this);
    }

    @Override
    public void setData(PhotoBean photoBean, int position) {

    }

    public void setData(PhotoBean photoBean,
                        ArrayList<String> selectPhotos,
                        PhotoOptions photoOptions,
                        PhotoListAdapter photoListAdapter) {
        mPhotoBean = photoBean;
        mSelectPhotos = selectPhotos;
        mPhotoOptions = photoOptions;
        mPhotoListAdapter = photoListAdapter;
        mCbCheck.setChecked(selectPhotos.contains(photoBean.path)); //勾选框复用问题
        mCbCheck.setVisibility(View.VISIBLE);
        Glide.with(mContext)
                .load(photoBean.path)
                .crossFade()
                .centerCrop()
                .into(mIvPhoto);
        if (mPhotoOptions.type == PhotoOptions.TYPE_VIDEO) {
            mTvDuration.setVisibility(View.VISIBLE);
            //读取时长和大小
            mTvDuration.setText(photoBean.getTime());
            if (mPhotoOptions.duration != 0 &&
                    photoBean.duration > mPhotoOptions.duration + 500) {
                mTvDuration.setTextColor(Color.RED);
            } else {
                mTvDuration.setTextColor(Color.WHITE);
            }
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
            if (mSelectPhotos.contains(mPhotoBean.path)) {
                return;
            }
            if (mPhotoOptions.type == PhotoOptions.TYPE_VIDEO) {
                if (mPhotoOptions.duration > 0 &&
                        mPhotoBean.duration > mPhotoOptions.duration + 500) {
                    ToastUtil.showToast(itemView.getContext(),
                            "您选择的视频时长超出限制");
                    mCbCheck.setChecked(false);
                    return;
                }
                if (mPhotoOptions.size > 0 &&
                        mPhotoBean.size > mPhotoOptions.size) {
                    ToastUtil.showToast(itemView.getContext(),
                            "您选择的视频大小超出限制");
                    mCbCheck.setChecked(false);
                    return;
                }
            }
            if (mSelectPhotos.size() < mPhotoOptions.takeNum) {
                mSelectPhotos.add(mPhotoBean.path);
            } else {
                String s = "张照片！";
                if (mPhotoOptions.type == PhotoOptions.TYPE_VIDEO) {
                    s = "个视频！";
                }
                ToastUtil.showToast(itemView.getContext(),
                        "您最多只能选择" +
                                mPhotoOptions.takeNum +
                                s);
                mCbCheck.setChecked(false);
                return;
            }
        } else {
            mSelectPhotos.remove(mPhotoBean.path);
        }
        mPhotoListAdapter.onSelectChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemOnClick(itemView, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onItemOnClick(View view, int position);
    }
}
