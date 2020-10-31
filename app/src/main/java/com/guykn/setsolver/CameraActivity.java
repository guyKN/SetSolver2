package com.guykn.setsolver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.guykn.setsolver.ui.main.CameraFragment;

public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CAMERA_PERMISSION = 33;
    private static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 73;

    @Override
    protected void onCreate(Bundle savedInstanceState) {




        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        if (savedInstanceState == null) {
            checkPermissionsAndOpenFragment();
        }
    }

    private void checkPermissionsAndOpenFragment() {
        if (!activityHasCameraPermission()) {
            requestCameraPermission();
        } else if(!activityHasWriteStoragePermission()) {
            requestWriteStoragePermission();
        }else {
            createCameraFragment();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsAndOpenFragment();
            } else {
                Toast.makeText(this, "This app can't work without the camera.", Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == REQUEST_CODE_WRITE_STORAGE_PERMISSION){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionsAndOpenFragment();
            } else {
                Toast.makeText(this, "This app needs to access external storage.", Toast.LENGTH_LONG).show();
            }

        }
    }



    private void createCameraFragment(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, CameraFragment.newInstance())
                .commitNow();
    }

    private boolean activityHasCameraPermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private boolean activityHasWriteStoragePermission(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA_PERMISSION);
    }

    private void requestWriteStoragePermission(){
        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_CAMERA_PERMISSION);
    }


}