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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.guykn.setsolver.R;
import com.guykn.setsolver.SettingsActivity;
import com.guykn.setsolver.threading.CameraPreviewThread;
import com.guykn.setsolver.threading.CameraThread;
import com.guykn.setsolver.threading.CameraThreadManager;
import com.guykn.setsolver.ui.views.CameraOverlay;
import com.guykn.setsolver.ui.views.CameraPreview;

import java.text.DecimalFormat;
import java.util.Locale;

public class CameraFragment extends Fragment {
    //todo: allow both landscape and portrait
    //todo: keep image even after phone activity pauses
    public static final String TAG = "CameraFragment";
    private MainViewModel mainViewModel;
    private CameraPreview mCameraPreview;
    private CameraOverlay mCameraOverlay;
    private FrameLayout cameraFrame;
    private Button captureButton;
    private TextView fpsView;
    private CameraThreadManager cameraThreadManager;
    private ProgressBar loadingIcon;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "inside of updated program");
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
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        if (context != null) {

            loadingIcon = root.findViewById(R.id.loading_icon);

            fpsView = root.findViewById(R.id.fps_display);

            Log.d(TAG, mainViewModel.getConfigLiveData().getValue() == null ? "null" : "not null");

            CameraThread cameraThread = new CameraPreviewThread(context.getApplicationContext(),
                    mainViewModel, mainViewModel.getConfigLiveData().getValue());

            cameraThreadManager = new CameraThreadManager(cameraThread, getLifecycle());
            mCameraPreview = cameraThreadManager.getCameraPreview(context);
            cameraThreadManager.startCamera();
            mCameraOverlay = new CameraOverlay(context, getLifecycle());

            cameraFrame.setOnClickListener((View v) -> {
                cameraThreadManager.startCamera();
            });

            cameraFrame.addView(mCameraPreview);
            cameraFrame.addView(mCameraOverlay);

            fpsView.bringToFront();
            loadingIcon.bringToFront();

            mainViewModel.getDrawableLiveData().observe(getViewLifecycleOwner(), drawable -> {
                mCameraOverlay.setDrawable(drawable);
                Log.d(TAG, "drawable changed");
            });

            mainViewModel.getFpsLiveData().observe(getViewLifecycleOwner(), fps -> {
                String fpsText = String.format(Locale.US,
                        "%s FPS", fps);
                fpsView.setVisibility(View.VISIBLE);
                fpsView.setText(fpsText);
            });

            mainViewModel.getTotalProcessingTimeData().observe(getViewLifecycleOwner(),
                    milliseconds -> {
                        String processingTimeText =
                                new DecimalFormat("Processing Time: 0.00sec")
                                        .format(
                                                ((double) milliseconds) / 1000.0
                                        );
                        fpsView.setVisibility(View.VISIBLE);
                        fpsView.setText(processingTimeText);
                    }
            );

            mainViewModel.getShouldHaveLoadingIconData().observe(getViewLifecycleOwner(),
                    shouldHaveLoadingIcon -> {
                        if (shouldHaveLoadingIcon) {
                            loadingIcon.setVisibility(View.VISIBLE);
                            fpsView.setVisibility(View.GONE);

                        } else {
                            loadingIcon.setVisibility(View.GONE);
                        }
                    }
            );

            captureButton = root.findViewById(R.id.button_capture);
            captureButton.setOnClickListener(
                    (View v) -> {
                        cameraThreadManager.takePicture();
                    }
            );

            View settingsButton = root.findViewById(R.id.button_settings);

            settingsButton.setOnClickListener(
                    (View v) -> openSettingsActivity()
            );
        }

        return root;
    }

    private void openSettingsActivity() {
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