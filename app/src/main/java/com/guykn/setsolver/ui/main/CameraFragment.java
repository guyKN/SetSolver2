package com.guykn.setsolver.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.R;
import com.guykn.setsolver.SettingsActivity;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.JavaImageProcessingManager;
import com.guykn.setsolver.ui.views.CameraOverlay;
import com.guykn.setsolver.ui.views.CameraPreview;

import java.util.Locale;

public class CameraFragment extends Fragment {
    //todo: have a visible FPS counter
    //todo: scale saved image down to fixed resolution
    //todo: gather data for ML
    //todo: make it save images on all phones

    public static final String TAG = "CameraFragment";
    private MainViewModel mViewModel;
    private CameraPreview mCameraPreview;
    private CameraOverlay mCameraOverlay;
    private FrameLayout cameraFrame;
    private Button captureButton;
    private TextView fpsView;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG,"inside of updated program");
        super.onAttach(context);
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

            fpsView = root.findViewById(R.id.fps_display);


            ImageProcessingConfig config = ImageProcessingConfig.getDefaultConfig();

            ImageFileManager fileManager = new ImageFileManager(context);
            ImageProcessingManager processingManager =
                    JavaImageProcessingManager.getDefaultManager(config);

            FpsCounter fpsCounter = new FpsCounter(mViewModel, 1000);

            mCameraPreview = new CameraPreview(
                    context, processingManager, mViewModel, fpsCounter, getLifecycle());
            mCameraOverlay = new CameraOverlay(context, getLifecycle());

            cameraFrame.addView(mCameraPreview);
            cameraFrame.addView(mCameraOverlay);

            mViewModel.getDrawableLiveData().observe(getViewLifecycleOwner(), drawable -> {
                mCameraOverlay.setDrawable(drawable);
            });

            mViewModel.getFpsLiveData().observe(getViewLifecycleOwner(), fps ->{
                String fpsText = String.format(Locale.US,
                        "%s FPS", fps);
                fpsView.setText(fpsText);
            });

            captureButton = root.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    (View v) -> {
                        mCameraPreview.takePicture();
                    }
            );

            View settingsButton = root.findViewById(R.id.button_settings);

            settingsButton.setOnClickListener(
                    (View v) -> openSettingsActivity()
            );

        }

        return root;
    }

    private void openSettingsActivity(){
        Intent intent = new Intent(requireContext(), SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}