package com.androidlittleboy.ttstest;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 */
public class AppUtils {
    private static final String TAG = AppUtils.class.getName();
    private boolean needFloat = true;

    private AppUtils() {
    }

    /**
     * 检测应用是否在运行
     *
     * @param context     上下文
     * @param packageName 包名
     * @return 运行结果
     */
    public boolean isAppRunning(Context context, String packageName) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(100);
        boolean isAppRunning = false;
        for (ActivityManager.RunningTaskInfo info : list) {
            if (info.topActivity.getPackageName().equals(packageName) || info.baseActivity.getPackageName().equals(packageName)) {
                isAppRunning = true;
                Log.d("TAG", info.topActivity.getPackageName() + " info.baseActivity.getPackageName()=" + info.baseActivity.getPackageName());
                Log.d("TAG", "正在运行中……");
                break;
            }
        }
        return isAppRunning;
    }

    /**
     * 检测app是否存在
     *
     * @param context     上下文
     * @param packageName 包名
     * @return app是否存在
     */
    public boolean isAppExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            context.getPackageManager()
                    .getApplicationInfo(packageName,
                            PackageManager.GET_UNINSTALLED_PACKAGES);

            //没抛出异常证明应用存在
            Log.d(TAG, "应用存在");
            return true;
        } catch (PackageManager.NameNotFoundException en) {
            Log.e(TAG, "应用不存在");
            return false;
        }
    }

    /**
     * 根据包名获取包信息
     *
     * @param context     上下文
     * @param packageName 包名
     * @return packInfo
     */
    public PackageInfo getPackageInfo(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager()
                    .getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "get packageInfo failed:" + e.toString());
        }
        return packageInfo;
    }


    /**
     * 启动app
     *
     * @param context     上下文对象
     * @param packageName 包名
     */
    public void launcherAct(Context context, String packageName) {
        PackageManager manager = context.getApplicationContext().getPackageManager();
        Intent ihIntent = manager.getLaunchIntentForPackage(packageName);
        context.getApplicationContext().startActivity(ihIntent);
    }


    /**
     * 检测语音引擎apk
     */
    public boolean checkDeamonApk(Context context) {
        //先检查apk是否已经安装
        if (context != null) {
            boolean appExist = AppUtils.getInstance().isAppExist(context, "com.iflytek.speechcloud");
            if (appExist) {
                Log.d(TAG, "语音引擎a已经存在");
                return true;
            } else {
                //未安装则检查本地是否有apk
                Log.d(TAG, "语音引擎不存在，进行本地检查");
                File file = new File(context.getExternalFilesDir("").getAbsolutePath() + "/语音引擎.apk");
                // 如果apk存在，则进行安装，如果不存在，则先push进去，再安装
                if (file.exists()) {
                    Log.d(TAG, "守护app本地存在，先删除");
                    file.delete();
                }
                FileUtils.getInstance().pushApk(context, "");
                Log.d(TAG, "开始安装语音引擎");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String authority = "独一无二的名字" + ".fileProvider";
                Uri fileUri = FileProvider.getUriForFile(context, authority, file);
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                ((MainActivity) context).startActivityForResult(intent, 666);
                return false;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                    AppUtils.silentInstallInP(context,context.getExternalFilesDir("").getAbsolutePath() + "/语音引擎.apk");
//                } else {
//                    AppUtils.silentInstallUnderP(context.getPackageManager(),context.getExternalFilesDir("").getAbsolutePath() + "/语音引擎.apk");
//                }
            }
        } else {
            return false;
        }
    }

//    public  void observerDeamonApp(final Context context, boolean needFloat) {
//        observerDeamonApp(context);
//        this.needFloat = needFloat;
//    }


    public static boolean silentInstallInP(Context context, String apkPath) {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        String pkgName = getApkPackageName(context, apkPath);
        if (pkgName == null) {
            return false;
        }
        params.setAppPackageName(pkgName);
        try {
            Method allowDowngrade = PackageInstaller.SessionParams.class.getMethod("setAllowDowngrade", boolean.class);
            allowDowngrade.setAccessible(true);
            allowDowngrade.invoke(params, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        OutputStream os = null;
        InputStream is = null;
        try {
            int sessionId = packageInstaller.createSession(params);
            PackageInstaller.Session session = packageInstaller.openSession(sessionId);
            os = session.openWrite(pkgName, 0, -1);
            is = new FileInputStream(apkPath);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            session.fsync(os);
            os.close();
            os = null;
            is.close();
            is = null;
            session.commit(PendingIntent.getBroadcast(context, sessionId,
                    new Intent(Intent.ACTION_MAIN), 0).getIntentSender());
        } catch (Exception e) {
            Log.e("INSTALL", "" + e.getMessage());
            return false;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static boolean silentInstallUnderP(PackageManager packageManager, String apkPath) {
        Class<?> pmClz = packageManager.getClass();
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                Class<?> aClass = Class.forName("android.app.PackageInstallObserver");
                Constructor<?> constructor = aClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object installObserver = constructor.newInstance();
                Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, aClass, int.class, String.class);
                method.setAccessible(true);
                method.invoke(packageManager, Uri.fromFile(new File(apkPath)), installObserver, 2, null);
            } else {
                Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class);
                method.setAccessible(true);
                method.invoke(packageManager, Uri.fromFile(new File(apkPath)), null, 2, null);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取apk的包名
     */
    private static String getApkPackageName(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, 0);
        if (info != null) {
            return info.packageName;
        } else {
            return null;
        }
    }

    /**
     * 强制停止应用程序
     *
     * @param pkgName 包名
     */
    public void forceStopPackage(String pkgName, Context context) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        Method method = Class.forName("android.app.ActivityManager").getMethod("forceStopPackage", String.class);
        method.invoke(am, pkgName);
        Log.e(TAG, "强行停止程序:" + pkgName);
    }


    public static AppUtils getInstance() {
        return ClassInner.appUtils;
    }

    static class ClassInner {
        static AppUtils appUtils = new AppUtils();
    }
}
