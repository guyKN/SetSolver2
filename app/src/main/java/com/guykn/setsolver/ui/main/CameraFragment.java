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
    //todo: actually make the CameraPreview look at the CameraFragment's lifecycle
    //todo: have a visible FPS counter
    public static final String TAG = "CameraFragment";
    private MainViewModel mViewModel;
    private CameraThreadManager cameraThreadManager;
    private CameraPreview mCameraPreview;
    private CameraOverlay mCameraOverlay;
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

        View root = inflater.inflate(R.layout.camera_fragment, container, false);

        cameraFrame = root.findViewById(R.id.camera_preview_frame);
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

            mCameraPreview = new CameraPreview(context, processingThread, getLifecycle());
            mCameraOverlay = new CameraOverlay(context, getLifecycle());

            cameraFrame.addView(mCameraPreview);
            cameraFrame.addView(mCameraOverlay);

            mViewModel.getDrawableLiveData().observe(getViewLifecycleOwner(), drawable -> {
                mCameraOverlay.setDrawable(drawable);
            });

            captureButton = root.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    (View v) -> {
                        mCameraPreview.takePicture();
                    }
            );

        }


        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }




    @Override
    public void onImageProcessingSuccess(DrawableOnCanvas drawable) {
        mCameraOverlay.setDrawable(drawable);
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
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}