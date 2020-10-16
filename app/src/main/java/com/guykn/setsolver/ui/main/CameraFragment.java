package com.guykn.setsolver.ui.main;

import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.guykn.setsolver.R;
import com.guykn.setsolver.threading.ImageProcessingThreadCallback;
import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;
import com.guykn.setsolver.threading.ImageProcessingThreadManager;
import com.guykn.setsolver.ui.views.CameraPreview;

public class CameraFragment extends Fragment implements ImageProcessingThreadCallback {
    private static final String TAG = "CameraFragment";
    private MainViewModel mViewModel;
    private ImageProcessingThreadManager mThreadManager;
    private CameraPreview mCameraPreview;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mThreadManager = new ImageProcessingThreadManager(context, this, ContourBasedCardDetector.Config.getDefaultConfig());
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        Camera camera = getCameraInstance();
        Context context = getContextFromParentActivity();
        Log.d(TAG, "Creating camera.");
        if(camera !=null && context != null){
            Log.d(TAG, "both context and camera aren't null.");
            mCameraPreview = new CameraPreview(context, camera);
            FrameLayout cameraFrame =  view.findViewById(R.id.camera_preview_frame);
            cameraFrame.addView(mCameraPreview);
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
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
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
    public void onImageProcessingSuccess(ImageProcessingThreadManager.ImageProcessingThreadMessage message) {

    }

    @Override
    public void onImageProcessingFailure(ImageProcessingThreadManager.ImageProcessingThreadMessage message) {

    }
}