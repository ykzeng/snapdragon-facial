package com.lenss.yzeng.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by yukun on 1/3/2017.
 */

public class Utils {
    // yukun
    public static void requestPermisssions(Activity activity, String[] permissionList){
        ArrayList<String> permissionsToGet = new ArrayList<String>();
        for(int i = 0; i < permissionList.length; i++){
            int permissionFlag = ContextCompat.checkSelfPermission(activity, permissionList[i]);
            if (permissionFlag != PackageManager.PERMISSION_GRANTED){
                permissionsToGet.add(permissionList[i]);
            }
        }
        String[] permissionsRequestArray = new String[permissionList.length];
        permissionsToGet.toArray(permissionsRequestArray);
        ActivityCompat.requestPermissions(activity, permissionsRequestArray, -1);
    }

    public static void appendStringToFile(String output, String filePath){
        File file = new File(filePath);
        try {
            if (!file.exists())
                file.createNewFile();
            FileWriter writer = new FileWriter(file, true);
            writer.write(output);
            writer.flush();
            writer.close();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void appendByteArrayListToFile(ArrayList<byte[]> bytesArrayList, String filePath){
        File file = new File(filePath);
        try {
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            for (byte[] byteArray : bytesArrayList) {
                Log.e("Util.writeBytes", bytesArrayList.size() + " frames have been written to " + filePath);
                fos.write(byteArray);
                fos.flush();
            }
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void appendByteArrayToFile(byte[] byteArray, String filePath){
        File file = new File(filePath);
        try {
            if (!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
