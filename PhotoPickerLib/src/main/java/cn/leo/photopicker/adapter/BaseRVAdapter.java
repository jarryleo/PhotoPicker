package cn.leo.photopicker.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.leo.photopicker.holder.BaseRVHolder;

/**
 * Created by Leo on 2018/2/3.
 */

public abstract class BaseRVAdapter<T> extends RecyclerView.Adapter {
    public List<T> mList = new ArrayList<>();

    public void setData(List<T> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public void addData(List<T> list) {
        mList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return getViewHolder(parent, viewType);
    }

    //由子类选择ViewHolder
    protected abstract BaseRVHolder getViewHolder(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        BaseRVHolder commRVHolder = (BaseRVHolder) holder;
        commRVHolder.setData(mList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
