package com.google.android.apps.photolab.storyboard.soloader;


import android.content.Context;
import android.util.Log;

import com.mp.android.apps.utils.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SoFileUtils {

    /**
     * 加载 so 文件
     *
     * @param context
     * @param fromPath 下载到得sdcard目录
     */
    public static void loadSoFile(Context context, String fromPath) {
        try {
            File dirs = context.getDir("libs", Context.MODE_PRIVATE);
            if (!isLoadSoFiles("libfacedetector_native.so", dirs) || !isLoadSoFiles("libobjectdetector_native.so", dirs)) {
                copyFile(fromPath, dirs.getAbsolutePath());
            }
            LoadLibraryUtil.installNativeLibraryPath(context.getApplicationContext().getClassLoader(), dirs);
        } catch (Throwable throwable) {
            Logger.e("loadSoFile", "loadSoFile error " + throwable.getMessage());
        }
    }

    public static boolean hasLoadSofile(Context context) {
        File dirs = context.getDir("libs", Context.MODE_PRIVATE);
        if (isLoadSoFiles("libobjectdetector_native.so", dirs) &&
                isLoadSoFiles("libobjectdetector_native.so", dirs)
        ) {
            try {
                LoadLibraryUtil.installNativeLibraryPath(context.getApplicationContext().getClassLoader(), dirs);
            } catch (Throwable throwable) {
                Logger.e("loadSoFile", "loadSoFile error " + throwable.getMessage());
            }
            return true;
        }
        return false;

    }

    /**
     * 判断immqy so 文件是否存在
     *
     * @param name "libimmqy" so库
     * @return boolean
     */
    private static boolean isLoadSoFiles(String name, File dirs) {
        boolean getSoLib = false;
        File[] currentFiles;
        currentFiles = dirs.listFiles();
        if (currentFiles == null) {
            return false;
        }
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].getName().contains(name)) {
                getSoLib = true;
            }
        }
        return getSoLib;
    }


    /**
     * 要复制的目录下的所有非子目录(文件夹)文件拷贝
     *
     * @param fromFiles 指定的下载目录
     * @param toFile    应用的包路径
     * @return int
     */
    private static int copySdcardFile(String fromFiles, String toFile) {
        Log.d("要复制到", toFile);
        try {
            FileInputStream fileInput = new FileInputStream(fromFiles);
            FileOutputStream fileOutput = new FileOutputStream(toFile);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024 * 1];
            int len = -1;
            while ((len = fileInput.read(buffer)) != -1) {
                byteOut.write(buffer, 0, len);
            }
            // 从内存到写入到具体文件
            fileOutput.write(byteOut.toByteArray());
            // 关闭文件流
            byteOut.close();
            fileOutput.close();
            fileInput.close();
            return 0;
        } catch (Exception ex) {
            return -1;
        }
    }


    /**
     * @param fromFiles 指定的下载目录
     * @param toFile    应用的包路径
     * @return int
     */
    private static int copyFile(String fromFiles, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFiles);
        //如同判断SD卡是否存在或者文件是否存在,如果不存在则 return出去
        if (!root.exists()) {
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();
        if (currentFiles == null) {
            Log.d("soFile---", "未获取到文件");
            return -1;
        }
        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {
                Log.d("当前项为子目录 进行递归", "copyFile: ");
                //如果当前项为子目录 进行递归
                copyFile(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");
            } else {
                //如果当前项为文件则进行文件拷贝
                if (currentFiles[i].getName().contains(".so")) {
                    Log.d("当前项为文件则进行文件拷贝", "copyFile: ");
                    int id = copySdcardFile(currentFiles[i].getPath(), toFile + File.separator + currentFiles[i].getName());
                }
            }
        }
        return 0;
    }

}