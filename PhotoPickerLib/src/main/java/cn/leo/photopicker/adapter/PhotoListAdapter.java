package cn.leo.photopicker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import cn.leo.photopicker.R;
import cn.leo.photopicker.bean.PhotoBean;
import cn.leo.photopicker.holder.BaseRVHolder;
import cn.leo.photopicker.holder.PhotoListHolder;
import cn.leo.photopicker.pick.PhotoOptions;

/**
 * Created by Leo on 2018/4/16.
 */

public class PhotoListAdapter extends BaseRVAdapter<PhotoBean> implements PhotoListHolder.OnItemClickListener {
    private ArrayList<String> mSelectPhotos = new ArrayList<>();
    private PhotoOptions mPhotoOptions;
    private OnSelectChangeListener mOnSelectChangeListener;

    public PhotoListAdapter(PhotoOptions photoOptions,
                            OnSelectChangeListener onSelectChangeListener) {
        mPhotoOptions = photoOptions;
        mOnSelectChangeListener = onSelectChangeListener;
    }

    public void add(int index, PhotoBean bean) {
        mList.add(index, bean);
        notifyDataSetChanged();
    }

    public ArrayList<String> getSelectPhotos() {
        return mSelectPhotos;
    }

    @Override
    public int getItemCount() {
        return mList.size() + 1;
    }

    public PhotoBean getPhoto(int position) {
        return mList.get(position);
    }

    public ArrayList<String> getAllPhotoPaths(int position) {
        ArrayList<String> list = new ArrayList<>();
        int min = position > 500 ? position - 500 : 0;
        int max = min + 1000 > mList.size() ? mList.size() : min + 1000;
        for (int i = min; i < max; i++) {
            list.add(mList.get(i).path);
        }
        return list;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    protected BaseRVHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoListHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        PhotoListHolder photoListHolder = (PhotoListHolder) holder;
        if (position == 0) {
            photoListHolder.setCameraPic();
        } else {
            photoListHolder.setData(mList.get(position - 1), mSelectPhotos, mPhotoOptions, this);
        }
        photoListHolder.setOnItemClickListener(this);
    }

    public void onSelectChanged() {
        if (mOnSelectChangeListener != null) {
            mOnSelectChangeListener.onSelectChange(mSelectPhotos);
        }
    }

    @Override
    public void onItemOnClick(View view, int position) {
        if (mOnSelectChangeListener != null) {
            mOnSelectChangeListener.onItemClick(view, position);
        }
    }

    public interface OnSelectChangeListener {

        void onSelectChange(ArrayList<String> selectPhotos);

        void onItemClick(View view, int position);

    }
}
