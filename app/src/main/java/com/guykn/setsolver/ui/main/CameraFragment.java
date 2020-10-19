package com.guykn.setsolver.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.R;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.threading.CameraThreadManager;
import com.guykn.setsolver.threading.ImageProcessingThreadManager;
import com.guykn.setsolver.threading.SimpleDelayChecker;
import com.guykn.setsolver.ui.views.CameraPreview;
import com.guykn.setsolver.ui.views.CameraOverlay;

public class CameraFragment extends Fragment implements ImageProcessingThreadManager.Callback {
    //todo: allow user to take picture, and save the sections of the picture
    //todo: use the MainViewModel for config
    public static final String TAG = "CameraFragment";
    private MainViewModel mViewModel;
    private CameraThreadManager cameraThreadManager;
    private CameraPreview mCameraPreview;
    private CameraOverlay cameraOverlay;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG,"inside of updated program");
        super.onAttach(context);
        CameraThreadManager.DelayChecker delayChecker = new SimpleDelayChecker(200,50);
        cameraThreadManager = new CameraThreadManager(context, this, delayChecker);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        Context context = getContextFromParentActivity();
        if(context != null){
            Log.d(TAG, "both context and camera aren't null.");

            mCameraPreview = new CameraPreview(context, cameraThreadManager);
            FrameLayout cameraFrame =  view.findViewById(R.id.camera_preview_frame);
            cameraFrame.addView(mCameraPreview);

            cameraOverlay = new CameraOverlay(context, null);
            cameraFrame.addView(cameraOverlay);
        }
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }

    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }



    @Nullable
    private Context getContextFromParentActivity(){
        Activity activity = getActivity();
        if(activity == null) {
            return null;
        }
        return activity.getApplicationContext();
    }


    @Override
    public void onImageProcessingSuccess(DrawableOnCanvas drawable) {
        Log.i(MainActivity.TAG, "Message recieved in CameraFragment.");
        cameraOverlay.setDrawable(drawable);
    }

    @Override
    public void onImageProcessingFailure(Exception e) {
        Activity parent = getActivity();
        if(parent == null) return;
        Toast.makeText(parent, "there was an error.", Toast.LENGTH_LONG).show();
        e.printStackTrace();
    }
}