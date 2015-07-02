package com.padeoe.officex86installer;

import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Created by Kangkang on 2015/5/22.
 */
public class DataPackageManager {
    public static void createFolder(String folderName) {
        File folder = new File(folderName);
        boolean success = false;
        //目录不存在，创建目录
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        //目录存在，删除目录及内部文件
        else{
            deleteFolder(folderName);
            success = folder.mkdir();
        }
        if (success) {
            Log.i("TestCreateFolder", "数据包文件夹创建成功");
        } else {
            Log.i("TestCreateFolder", "数据包文件夹创建失败");
        }
    }

    /**
     * 删除文件夹自身及其内部的所有内容
     * @param folderName 文件夹路径，是否以分隔符结尾均可
     */
    public static void deleteFolder(String folderName) {
        //如果路径末尾不是分隔符，则添加
        if (!folderName.endsWith(File.separator)) {
            folderName = folderName + File.separator;
        }
        File folder = new File(folderName);
        File[] files = folder.listFiles();
        if(files!=null){
            for (int i = 0; i < files.length; i++) {
                //删除子文件
                if (files[i].isFile()) {
                    boolean result = files[i].delete();
                }
                //删除子目录下文件与自身
                else {
                    deleteFolder(files[i].getPath());
                }
            }
        }
        folder.delete();
    }

}
