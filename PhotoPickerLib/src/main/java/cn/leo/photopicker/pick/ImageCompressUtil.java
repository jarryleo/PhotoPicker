package cn.leo.photopicker.pick;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Leo on 2018/1/19.
 */

public class ImageCompressUtil {
    /**
     * 把路径srcPath的图片压缩后存储到savepath
     *
     * @param srcPath
     * @param savePath
     */
    public static void compressPx(String srcPath, String savePath, PhotoOptions ops) {
        BitmapFactory.Options options = getOptions(srcPath, ops);
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, options);
        OutputStream out = null;
        try {
            out = new FileOutputStream(savePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * 图片尺寸大小压缩比率计算
     *
     * @param srcPath
     * @return
     */
    @NonNull
    private static BitmapFactory.Options getOptions(String srcPath, PhotoOptions ops) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(srcPath, options);
        options.inJustDecodeBounds = false;
        int bWidth = options.outWidth;
        int bHeight = options.outHeight;
        int toWidth = ops.compressWidth; //设定图片压缩目标宽高像素
        int toHeight = ops.compressHeight;
        int be = 1; //默认不变
        bWidth /= toWidth; //宽度大于设定值倍率
        bHeight /= toHeight; //高度大于设定值倍率
        be = bWidth < bHeight ? bWidth : bHeight; //设定最小倍率为压缩倍率
        be = be < 1 ? 1 : be;//小于1则等于1
        options.inSampleSize = be;
        return options;
    }
}
