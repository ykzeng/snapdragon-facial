package com.lenss.yzeng.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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
}
