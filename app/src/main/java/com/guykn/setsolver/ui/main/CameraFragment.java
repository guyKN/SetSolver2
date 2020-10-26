package com.guykn.setsolver.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.R;
import com.guykn.setsolver.drawing.DrawableOnCanvas;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.JavaImageProcessingManager;
import com.guykn.setsolver.threading.CameraProcessingThread;
import com.guykn.setsolver.threading.deprecated.CameraThreadManager;
import com.guykn.setsolver.threading.deprecated.SimpleDelayChecker;
import com.guykn.setsolver.ui.views.CameraOverlay;
import com.guykn.setsolver.ui.views.CameraPreview;

public class CameraFragment extends Fragment implements CameraProcessingThread.Callback {
    //todo: use the MainViewModel for config, and add a config fragment
    //todo: make screen stay awake
    //todo: use a lifeCycleObserver for the camera
    public static final String TAG = "CameraFragment";
    private MainViewModel mViewModel;
    private CameraThreadManager cameraThreadManager;
    private CameraPreview mCameraPreview;
    private CameraOverlay cameraOverlay;
    private FrameLayout cameraFrame;
    private Button captureButton;

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
        Context context = getActivity();
        mViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if(context != null && mViewModel !=null) {
            Log.d(TAG, "onCreateView(), context and viewModel aren't null");

            ImageProcessingConfig config = ImageProcessingConfig.getDefaultConfig();

            ImageFileManager fileManager = new ImageFileManager(context);
            ImageProcessingManager processingManager =
                    JavaImageProcessingManager.getDefaultManager(config);

            CameraProcessingThread processingThread = new CameraProcessingThread(
                    processingManager, mViewModel, fileManager);

            mCameraPreview = new CameraPreview(context, processingThread);
            cameraOverlay = new CameraOverlay(context, null);

            mViewModel.getDrawableLiveData().observe(getViewLifecycleOwner(), drawable -> {
                cameraOverlay.setDrawable(drawable);
                Log.d(TAG, "changing UI");
            });

            captureButton = view.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    (View v) -> {
                        mCameraPreview.takePicture();
                    }
            );

        }


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if(cameraFrame != null && mCameraPreview !=null && cameraOverlay !=null){
            Log.d(TAG, "onResume, cameraFrame!=null");
            Log.d(TAG, "All view aren't null.");
            cameraFrame.removeAllViews();
            cameraFrame.addView(mCameraPreview);
            cameraFrame.addView(cameraOverlay);
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
    }




    @Override
    public void onImageProcessingSuccess(DrawableOnCanvas drawable) {
        cameraOverlay.setDrawable(drawable);
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