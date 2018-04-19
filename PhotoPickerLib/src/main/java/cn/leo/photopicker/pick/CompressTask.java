package cn.leo.photopicker.pick;

import android.os.AsyncTask;

import java.io.File;

import cn.leo.photopicker.adapter.PhotoListAdapter;
import cn.leo.photopicker.crop.CropUtil;

/**
 * Created by Leo on 2018/4/19.
 */

public class CompressTask extends AsyncTask<PhotoListAdapter, Void, String[]> {
    private PhotoOptions mOptions;
    private onCompressResultListener mResult;

    public interface onCompressResultListener {
        void onCompressResult(String paths[]);
    }

    public CompressTask(PhotoOptions options, onCompressResultListener result) {
        mOptions = options;
        mResult = result;
    }

    @Override
    protected String[] doInBackground(PhotoListAdapter... photoListAdapters) {
        PhotoListAdapter listAdapter = photoListAdapters[0];
        String[] compressPaths = new String[listAdapter.getSelectPhotos().size()];
        for (int i = 0; i < listAdapter.getSelectPhotos().size(); i++) {
            String selectPhoto = listAdapter.getSelectPhotos().get(i);
            File file = new File(selectPhoto);
            if (mOptions.size > 0 && file.length() < mOptions.size) {
                compressPaths[i] = selectPhoto;
            } else {
                String descPath = CropUtil.getCachePath() + file.getName();
                long compressPx = ImageCompressUtil.compressPx(selectPhoto, descPath, mOptions);
                int quality = 90;
                while (mOptions.size > 0 && compressPx > mOptions.size && quality > 10) {
                    compressPx = ImageCompressUtil.compressPx(descPath, mOptions, quality);
                    quality -= 10;
                }
                compressPaths[i] = descPath;
            }
        }
        return compressPaths;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        if (mResult != null) {
            mResult.onCompressResult(strings);
        }
    }
}
