package cn.leo.photopicker.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

/**
 * Created by JarryLeo on 2018/2/6.
 * 安卓8.0以下申请一个权限，用户同意后整个权限组的权限都不用申请可以直接使用
 * 8.0后每个权限都要单独申请，不能一次申请通过后,整个权限组都不用申请;
 * 但是用户同意权限组内一个之后，其它申请直接通过(之前是不申请可以直接用，现在是申请直接过(但是必须申请))
 */

public class PermissionUtil {
    private static String tag = "fragmentRequestPermissionCallBack";
    private static final int REQUEST_CODE = 110;
    private FragmentCallback mFragmentCallback;

    /**
     * 权限中文翻译，需要特殊权限自己添加
     */
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public enum permission {
        READ_CONTACTS("读取联系人", Manifest.permission.READ_CONTACTS),
        READ_PHONE_STATE("读取电话信息", Manifest.permission.READ_PHONE_STATE),
        READ_CALENDAR("读取日历", Manifest.permission.READ_CALENDAR),
        CAMERA("相机", Manifest.permission.CAMERA),
        BODY_SENSORS("传感器", Manifest.permission.BODY_SENSORS),
        ACCESS_FINE_LOCATION("精确定位", Manifest.permission.ACCESS_FINE_LOCATION),
        ACCESS_COARSE_LOCATION("粗略定位", Manifest.permission.ACCESS_COARSE_LOCATION),
        READ_EXTERNAL_STORAGE("读取存储卡", Manifest.permission.READ_EXTERNAL_STORAGE),
        WRITE_EXTERNAL_STORAGE("写入存储卡", Manifest.permission.WRITE_EXTERNAL_STORAGE),
        RECORD_AUDIO("录音", Manifest.permission.RECORD_AUDIO),
        READ_SMS("读取短信", Manifest.permission.READ_SMS);
        private String permissionCh;
        private String permission;

        permission(String permissionCh, String permission) {
            this.permissionCh = permissionCh;
            this.permission = permission;
        }

        public String getPermissionCh() {
            return permissionCh;
        }

        public String getPermission() {
            return permission;
        }
    }

    public interface Result {
        void onSuccess();

        void onFailed();
    }

    /**
     * fragment，作为权限回调监听，和从设置界面返回监听
     */
    public static class FragmentCallback extends Fragment {
        private Result mResult;
        private permission[] mPermissions;
        private long mRequestTime;
        public void setRequestTime() {
            mRequestTime = SystemClock.elapsedRealtime();
        }
        public void setResult(Result result) {
            mResult = result;
        }
        public void setPermissions(permission[] permissions) {
            mPermissions = permissions;
        }
        @Override
        public void onRequestPermissionsResult(int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            boolean result = true;
            switch (requestCode) {
                case REQUEST_CODE:
                    for (int i = 0; i < permissions.length; i++) {
                        if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                            result = false;
                            break;
                        }
                    }
                    break;
            }
            if (mResult != null) {
                if (result) {
                    detach();
                    mResult.onSuccess();
                } else {
                    if (SystemClock.elapsedRealtime() - mRequestTime < 300) {
                        StringBuilder sb = new StringBuilder();
                        for (permission mPermission : mPermissions) {
                            if (!PermissionUtil.checkPermission(getActivity(), mPermission)) {
                                sb.append(" [")
                                        .append(mPermission.getPermissionCh())
                                        .append("] ");
                            }
                        }
                        openSettingActivity("本次操作需要" + sb.toString() + "权限,是否前往设置开启?");
                    } else {
                        mResult.onFailed();
                    }
                }
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE) {
                if (mResult != null && mPermissions != null) {
                    boolean result = true;
                    for (permission mPermission : mPermissions) {
                        if (!checkPermission(getActivity(), mPermission)) {
                            result = false;
                            break;
                        }
                    }
                    if (result) {
                        detach();
                        mResult.onSuccess();
                    } else {
                        mResult.onFailed();
                    }
                }
            }
        }

        //解绑fragment
        private void detach() {
            if (!isAdded()) return;
            FragmentTransaction fragmentTransaction =
                    getFragmentManager().beginTransaction();
            fragmentTransaction.detach(this);
            fragmentTransaction.remove(this);
            fragmentTransaction.commitAllowingStateLoss();
        }

        /**
         * 打开应用权限设置界面
         */
        public void openSettingActivity(String message) {
            showMessageOKCancel(message, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mResult.onFailed();
                }
            });
        }

        /**
         * 弹出对话框
         *
         * @param message    消息内容
         * @param okListener 点击回调
         */
        private void showMessageOKCancel(String message,
                                         DialogInterface.OnClickListener okListener,
                                         DialogInterface.OnClickListener cancelListener) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(message)
                    .setPositiveButton("开启", okListener)
                    .setNegativeButton("拒绝", cancelListener)
                    .create()
                    .show();
        }
    }

    private FragmentActivity mActivity;
    private permission[] mPermissions;

    private PermissionUtil(FragmentActivity activity) {
        this.mActivity = activity;

    }

    /**
     * 获取请求权限实例
     *
     * @param activity FragmentActivity
     * @return 请求权限工具对象
     */
    public static PermissionUtil getInstance(FragmentActivity activity) {
        return new PermissionUtil(activity);
    }

    /**
     * 需要请求的权限列表
     *
     * @param permissions 权限列表
     * @return 返回自身链式编程
     */
    public PermissionUtil request(permission... permissions) {
        mPermissions = permissions;
        return this;
    }

    /**
     * 执行权限请求
     *
     * @param result 请求结果回调
     */
    public void execute(Result result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkPermissions()) {
            if (result != null) {
                result.onSuccess();
            }
            return;
        }
        //创建fragment回调
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        Fragment fragmentByTag = fragmentManager.findFragmentByTag(tag);
        if (fragmentByTag != null) {
            mFragmentCallback = (FragmentCallback) fragmentByTag;
            mFragmentCallback.setResult(result);
        } else {
            mFragmentCallback = new FragmentCallback();
            mFragmentCallback.setResult(result);
            fragmentManager
                    .beginTransaction()
                    .add(mFragmentCallback, tag)
                    .commit();
            fragmentManager.executePendingTransactions();
        }
        //开始请求
        requestPermission();
    }

    /**
     * 检查权限列表是否全部通过
     *
     * @return 权限列表是否全部通过
     */
    private boolean checkPermissions() {
        for (permission mPermission : mPermissions) {
            if (!checkPermission(mPermission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查权限
     *
     * @param permission 权限列表
     * @return 权限是否通过
     */
    private boolean checkPermission(permission permission) {
        //检查权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int checkSelfPermission =
                ContextCompat
                        .checkSelfPermission(mActivity, permission.getPermission());
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 静态检查权限
     *
     * @param context    上下文
     * @param permission 权限列表
     * @return 权限是否通过
     */
    private static boolean checkPermission(Context context, permission permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        int checkSelfPermission =
                ContextCompat
                        .checkSelfPermission(context, permission.getPermission());
        return checkSelfPermission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 申请权限
     */
    private void requestPermission() {

        if (mActivity.getSupportFragmentManager().findFragmentByTag(tag) == null) {
            throw new PermissionRequestException("一个权限申请工具类对象只能申请一次权限");
        }
        if (mFragmentCallback != null && mPermissions != null) {
            mFragmentCallback.setPermissions(mPermissions);
            //提取权限列表里面没通过的
            String[] per = new String[mPermissions.length];
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mPermissions.length; i++) {
                per[i] = mPermissions[i].getPermission();
                if (!checkPermission(mPermissions[i])) {
                    sb.append(" [")
                            .append(mPermissions[i].getPermissionCh())
                            .append("] ");
                }
            }
            //如果用户点了不提示(或者同时申请多个权限)，我们主动提示用户
            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, mPermissions[0].getPermission())) {
                mFragmentCallback.openSettingActivity("需要" + sb.toString() + "权限,前往开启?");
            } else {
                //申请权限
                try {
                    mFragmentCallback.setRequestTime();
                    mFragmentCallback.requestPermissions(per, REQUEST_CODE);
                } catch (Exception e) {
                    mFragmentCallback.openSettingActivity("需要" + sb.toString() + "权限,前往开启?");
                }
            }
        }
    }


    static class PermissionRequestException extends RuntimeException {
        PermissionRequestException(String message) {
            super(message);
        }
    }
}

/*•	Normal Permissions如下 （不需要动态申请，只需要在清单文件注册即可）

ACCESS_LOCATION_EXTRA_COMMANDS
ACCESS_NETWORK_STATE
ACCESS_NOTIFICATION_POLICY
ACCESS_WIFI_STATE
BLUETOOTH
BLUETOOTH_ADMIN
BROADCAST_STICKY
CHANGE_NETWORK_STATE
CHANGE_WIFI_MULTICAST_STATE
CHANGE_WIFI_STATE
DISABLE_KEYGUARD
EXPAND_STATUS_BAR
GET_PACKAGE_SIZE
INSTALL_SHORTCUT
INTERNET
KILL_BACKGROUND_PROCESSES
MODIFY_AUDIO_SETTINGS
NFC
READ_SYNC_SETTINGS
READ_SYNC_STATS
RECEIVE_BOOT_COMPLETED
REORDER_TASKS
REQUEST_INSTALL_PACKAGES
SET_ALARM
SET_TIME_ZONE
SET_WALLPAPER
SET_WALLPAPER_HINTS
TRANSMIT_IR
UNINSTALL_SHORTCUT
USE_FINGERPRINT
VIBRATE
WAKE_LOCK
WRITE_SYNC_SETTINGS


•Dangerous Permissions: (需要动态申请，当然也要在清单文件声明)

group:android.permission-group.CONTACTS
  permission:android.permission.WRITE_CONTACTS
  permission:android.permission.GET_ACCOUNTS
  permission:android.permission.READ_CONTACTS

group:android.permission-group.PHONE
  permission:android.permission.READ_CALL_LOG
  permission:android.permission.READ_PHONE_STATE
  permission:android.permission.CALL_PHONE
  permission:android.permission.WRITE_CALL_LOG
  permission:android.permission.USE_SIP
  permission:android.permission.PROCESS_OUTGOING_CALLS
  permission:com.android.voicemail.permission.ADD_VOICEMAIL

group:android.permission-group.CALENDAR
  permission:android.permission.READ_CALENDAR
  permission:android.permission.WRITE_CALENDAR

group:android.permission-group.CAMERA
  permission:android.permission.CAMERA

group:android.permission-group.SENSORS
  permission:android.permission.BODY_SENSORS

group:android.permission-group.LOCATION
  permission:android.permission.ACCESS_FINE_LOCATION
  permission:android.permission.ACCESS_COARSE_LOCATION

group:android.permission-group.STORAGE
  permission:android.permission.READ_EXTERNAL_STORAGE
  permission:android.permission.WRITE_EXTERNAL_STORAGE

group:android.permission-group.MICROPHONE
  permission:android.permission.RECORD_AUDIO

group:android.permission-group.SMS
  permission:android.permission.READ_SMS
  permission:android.permission.RECEIVE_WAP_PUSH
  permission:android.permission.RECEIVE_MMS
  permission:android.permission.RECEIVE_SMS
  permission:android.permission.SEND_SMS
  permission:android.permission.READ_CELL_BROADCASTS

*/
