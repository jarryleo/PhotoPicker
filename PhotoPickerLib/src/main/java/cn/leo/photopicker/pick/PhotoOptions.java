package cn.leo.photopicker.pick;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by JarryLeo on 2017/5/20.
 */

public class PhotoOptions implements Parcelable{
    public static final int TYPE_PHOTO = 0;
    public static final int TYPE_VIDEO = 1;
    public boolean crop;
    public int takeNum = 1;
    public int cropWidth;
    public int cropHeight;
    public int type;
    public int duration;
    public int size;
    public int compressWidth;
    public int compressHeight;

    public PhotoOptions() {
    }

    protected PhotoOptions(Parcel in) {
        crop = in.readByte() != 0;
        takeNum = in.readInt();
        cropWidth = in.readInt();
        cropHeight = in.readInt();
        type = in.readInt();
        duration = in.readInt();
        size = in.readInt();
        compressWidth = in.readInt();
        compressHeight = in.readInt();
    }

    public static final Creator<PhotoOptions> CREATOR = new Creator<PhotoOptions>() {
        @Override
        public PhotoOptions createFromParcel(Parcel in) {
            return new PhotoOptions(in);
        }

        @Override
        public PhotoOptions[] newArray(int size) {
            return new PhotoOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (crop ? 1 : 0));
        dest.writeInt(takeNum);
        dest.writeInt(cropWidth);
        dest.writeInt(cropHeight);
        dest.writeInt(type);
        dest.writeInt(duration);
        dest.writeInt(size);
        dest.writeInt(compressWidth);
        dest.writeInt(compressHeight);
    }
}
