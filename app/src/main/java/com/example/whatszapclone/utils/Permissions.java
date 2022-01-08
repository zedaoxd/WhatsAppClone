package com.example.whatszapclone.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissions {

    public static boolean validatePermission(String[] permissions, Activity activity, int requestCode){
        if (Build.VERSION.SDK_INT >= 23){
            List<String> listPermission = new ArrayList<>();

            for(String permission : permissions){
                Boolean  havePermission = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
                if(!havePermission) listPermission.add(permission);
            }

            if(listPermission.isEmpty()) return true;

            String[] missingPermits = new String[listPermission.size()];
            listPermission.toArray(missingPermits);
            ActivityCompat.requestPermissions(activity, missingPermits, requestCode);
        }


        return true;
    }
}
