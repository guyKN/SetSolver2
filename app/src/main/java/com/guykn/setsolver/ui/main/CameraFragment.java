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
import android.widget.Toast;

import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.R;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.imageprocessing.Config;
import com.guykn.setsolver.threading.CameraThreadManager;
import com.guykn.setsolver.threading.ImageProcessingThreadManager;
import com.guykn.setsolver.threading.SimpleDelayChecker;
import com.guykn.setsolver.ui.views.CameraPreview;
import com.guykn.setsolver.ui.views.SetCardOutlineView;

public class CameraFragment extends Fragment implements ImageProcessingThreadManager.Callback {
    //todo: allow user to take picture, and save the sections of the picture
    //todo: use the MainViewModel for config, and add a config fragment
    //todo: use viewmodel instead of passing DrawableOnCanvas objects from CameraThreadManager to CameraFragment
    //todo: make screen stay awake
    //todo: put cameraPreview on seprate thread
    public static final String TAG = "CameraFragment";
    private MainViewModel mViewModel;
    private CameraThreadManager cameraThreadManager;
    private CameraPreview mCameraPreview;
    private SetCardOutlineView setCardOutlineView;
    private FrameLayout cameraFrame;

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
        Log.d(TAG, "onCreateView()");

        View view = inflater.inflate(R.layout.camera_fragment, container, false);
        cameraFrame = view.findViewById(R.id.camera_preview_frame);

        Context context = getContextFromParentActivity();
        if(context != null) {
            Log.d(TAG, "onCreateView(), context isn't null");
            mCameraPreview = new CameraPreview(context, cameraThreadManager);
            setCardOutlineView = new SetCardOutlineView(context, null);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(cameraFrame != null && mCameraPreview !=null && setCardOutlineView !=null){
            Log.d(TAG, "onResume, cameraFrame!=null");
            Log.d(TAG, "All view aren't null.");
            cameraFrame.removeAllViews();
            cameraFrame.addView(mCameraPreview);
            cameraFrame.addView(setCardOutlineView);
        }else {
            Log.i(TAG, "Some views are null. ");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        if(cameraFrame !=null) {
            Log.d(TAG, "onPause, cameraFrame!=null");
            cameraFrame.removeAllViews();
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        // TODO: Use the ViewModel
    }




    @Override
    public void onImageProcessingSuccess(DrawableOnCanvas drawable) {
        setCardOutlineView.setDrawable(drawable);
    }

    @Override
    public void onImageProcessingFailure(Exception e) {
        Activity parent = getActivity();
        if(parent == null) return;
        Toast.makeText(parent, "there was an error.", Toast.LENGTH_LONG).show();
        e.printStackTrace();
    }

    @Nullable
    private Context getContextFromParentActivity(){
        Activity activity = getActivity();
        if(activity == null) {
            return null;
        }
        return activity.getApplicationContext();
    }

    private boolean checkCameraHardware(Context context) {
        // this device has a camera
        // no camera on this device
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

}