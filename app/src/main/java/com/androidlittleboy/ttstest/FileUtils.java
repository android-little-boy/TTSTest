package com.androidlittleboy.ttstest;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author yey
 * @date 2020/8/4
 * @description
 */
public class FileUtils {

    public static FileUtils getInstance() {
        return InnerClass.fileUtils;
    }

    private final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private final int EOF = -1;

    /**
     * 把apk从asset目录push到SD卡
     */
    public void pushApk(Context context, String fileUrl) {
        File file = new File(fileUrl);
        if (file.exists()) {
            copyAssetsFileToSDCard(context, fileUrl, context.getExternalFilesDir("").getAbsolutePath() + "/语音引擎.apk");
        } else {
            File assetsFontBack = new File(context.getExternalFilesDir("").getAbsolutePath() + "/语音引擎.apk");
            if (!assetsFontBack.exists()){
                copyAssetsFileToSDCard(context, "kdxfyyyq_downcc.apk", context.getExternalFilesDir("").getAbsolutePath() + "/语音引擎.apk");
            }
        }
    }

    /**
     * 把assets目录的文件夹拷贝到sdcard
     *
     * @param context        上下文
     * @param assetsFileName assets路径
     * @param destination    目标sd卡路径
     */
    private void copyAssetsFileToSDCard(Context context, String assetsFileName, String destination) {
        InputStream is = null;
        try {
            is = context.getAssets().open(assetsFileName);
            File desFile = new File(destination);
            copyInputStreamToFile(is, desFile);
        } catch (IOException e) {
            Log.e("拷贝文件时发生异常", e.toString());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.e("close stream failed:" , e.toString());
            }
        }
    }

    private void copyInputStreamToFile(final InputStream source, final File destination) throws IOException {
        try (InputStream in = source) {
            copyToFile(in, destination);
        }
    }

    private void copyToFile(final InputStream source, final File destination) throws IOException {
        try (InputStream in = source; OutputStream out = openOutputStream(destination)) {
            copy(in, out);
        }
    }

    private int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private long copy(final InputStream input, final OutputStream output, final int bufferSize)
            throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    private long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    private long copyLarge(final InputStream input, final OutputStream output)
            throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }

    private FileOutputStream openOutputStream(final File file) throws IOException {
        return openOutputStream(file, false);
    }

    private FileOutputStream openOutputStream(final File file, final boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (!file.canWrite()) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

    private static class InnerClass {
        static FileUtils fileUtils = new FileUtils();
    }
}
