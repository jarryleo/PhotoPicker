package cn.leo.photopicker.pick;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;

import cn.leo.photopicker.R;

/**
 * 图片选择器菜单选择界面
 */
public class PhotoFolderPopupWindow extends PopupWindow implements
        View.OnAttachStateChangeListener, AdapterView.OnItemClickListener {
    private ListView mFolderView;
    private Callback mCallback;
    private FolderAdapter mFolderAdapter;
    private List<String> mFolders;
    private HashMap<String, List<String>> mAllFile;

    public PhotoFolderPopupWindow(Context context, int height, Callback callback) {
        super(LayoutInflater.from(context).inflate(R.layout.popup_window_folder, null),
                ViewGroup.LayoutParams.MATCH_PARENT, height);

        mCallback = callback;

        // init
        //setAnimationStyle(R.style.popup_anim_style_alpha);
        setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setOutsideTouchable(true);
        setFocusable(true);

        // content
        View content = getContentView();
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        content.addOnAttachStateChangeListener(this);

        mFolderView = (ListView) content.findViewById(R.id.lv_popup_folder);

    }

    public void setAdapter(HashMap<String, List<String>> folders) {
        mAllFile = folders;
        mFolders = PhotoProvider.getDirList(folders);
        mFolderAdapter = new FolderAdapter();
        mFolderView.setAdapter(mFolderAdapter);
        mFolderView.setOnItemClickListener(this);
    }

    @Override
    public void onViewAttachedToWindow(View v) {
        final Callback callback = mCallback;
        if (callback != null)
            callback.onShow();
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        final Callback callback = mCallback;
        if (callback != null)
            callback.onDismiss();
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final Callback callback = mCallback;
        if (callback != null)
            callback.onSelect(this, mFolders.get(position));
    }

    public interface Callback {
        void onSelect(PhotoFolderPopupWindow popupWindow, String folder);

        void onDismiss();

        void onShow();
    }

    public class FolderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (mFolders != null) {
                return mFolders.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mFolders != null) {
                return mFolders.get(position);
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_folder, parent, false);
                holder = new ViewHolder(convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.setData(mFolders.get(position));
            return holder.itemView;
        }
    }

    private class ViewHolder {
        View itemView;
        ImageView iv_image;
        TextView tv_name, tv_size;

        public ViewHolder(View itemView) {
            this.itemView = itemView;
            itemView.setTag(this);
            iv_image = (ImageView) itemView.findViewById(R.id.iv_folder);
            tv_name = (TextView) itemView.findViewById(R.id.tv_folder_name);
            tv_size = (TextView) itemView.findViewById(R.id.tv_size);
        }

        public void setData(String s) {
            List<String> fnums = mAllFile.get(s);
            if ("全部照片".equals(s)) {
                fnums = PhotoProvider.getAllPhotos(mAllFile);
            }
            tv_name.setText(s);
            if (fnums != null && fnums.size() > 0) {
                tv_size.setText(String.format("(%s)", fnums.size()));
                String path = fnums.get(0);
                Glide.with(itemView.getContext()).load(path).centerCrop().into(iv_image);
            }
        }
    }
}
