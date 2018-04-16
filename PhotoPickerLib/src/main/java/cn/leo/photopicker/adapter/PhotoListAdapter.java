package cn.leo.photopicker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import cn.leo.photopicker.R;
import cn.leo.photopicker.bean.PhotoBean;
import cn.leo.photopicker.holder.PhotoListHolder;
import cn.leo.photopicker.pick.PhotoOptions;

/**
 * Created by Leo on 2018/4/16.
 */

public class PhotoListAdapter extends BaseAdapter {
    private ArrayList<PhotoBean> mAllPhotos = new ArrayList<>();
    private ArrayList<String> mSelectPhotos = new ArrayList<>();
    private PhotoOptions mPhotoOptions;
    private OnSelectChangeListener mOnSelectChangeListener;

    public PhotoListAdapter(PhotoOptions photoOptions,
                            OnSelectChangeListener onSelectChangeListener) {
        mPhotoOptions = photoOptions;
        mOnSelectChangeListener = onSelectChangeListener;
    }

    public void setData(ArrayList<PhotoBean> allPhotos) {
        mAllPhotos.clear();
        mAllPhotos.addAll(allPhotos);
        notifyDataSetChanged();
    }

    public void add(int index, PhotoBean bean) {
        mAllPhotos.add(index, bean);
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectPhotos() {
        return mSelectPhotos;
    }

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
            return mAllPhotos.get(position);
        }
        return null;
    }

    public PhotoBean getPhoto(int position) {
        if (mAllPhotos != null) {
            return mAllPhotos.get(position);
        }
        return null;
    }

    public ArrayList<String> getAllPhotoPaths() {
        ArrayList<String> list = new ArrayList<>();
        for (PhotoBean allPhoto : mAllPhotos) {
            list.add(allPhoto.path);
        }
        return list;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhotoListHolder holder;
        if (convertView == null) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            holder = new PhotoListHolder(view);
        } else {
            holder = (PhotoListHolder) convertView.getTag();
        }
        if (position == 0) {
            holder.setCameraPic();
        } else {
            holder.setData(mAllPhotos.get(position - 1), mSelectPhotos, mPhotoOptions, this);
        }
        return holder.itemView;
    }

    public void onSelectChanged() {
        if (mOnSelectChangeListener != null) {
            mOnSelectChangeListener.onSelectChange(mSelectPhotos);
        }
    }

    public interface OnSelectChangeListener {

        void onSelectChange(ArrayList<String> selectPhotos);

    }
}
