package cn.leo.photopicker.pick;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import cn.leo.photopicker.crop.CropUtil;

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
    public static long compressPx(String srcPath, String savePath, PhotoOptions ops) {
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
                bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = new File(savePath);
        return file.length();
    }

    /**
     * 在原路径对图片压缩处理
     *
     * @param path
     */
    public static long compressPx(String path, PhotoOptions ops, int quality) {

        BitmapFactory.Options options = getOptions(path, ops);
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        OutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null)
                    out.close();
                bitmap.recycle();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        File file = new File(path);
        return file.length();
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

    /**
     * 压缩缩略图
     *
     * @param bitmap
     * @param savePath
     */
    public static void compressThumb(Bitmap bitmap, String savePath) {
        BitmapFactory.Options options = getOptions(bitmap);
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

    private static BitmapFactory.Options getOptions(Bitmap bitmap) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        int bWidth = options.outWidth;
        int bHeight = options.outHeight;
        int toWidth = 320; //设定图片压缩目标宽高像素
        int toHeight = 240;
        int be = 1; //默认不变
        bWidth /= toWidth; //宽度大于设定值倍率
        bHeight /= toHeight; //高度大于设定值倍率
        be = bWidth < bHeight ? bWidth : bHeight; //设定最小倍率为压缩倍率
        be = be < 1 ? 1 : be;//小于1则等于1
        options.inSampleSize = be;
        return options;
    }

    /**
     * 改变文件路径后缀以及缓存路径
     *
     * @param path
     * @return
     */
    public static String changeFileType(String path) {
        String name = new File(path).getName();
        int i = name.lastIndexOf(".");
        String newPath = CropUtil.getCachePath() + name.substring(0, i) + ".jpg";
        return newPath;
    }
}
