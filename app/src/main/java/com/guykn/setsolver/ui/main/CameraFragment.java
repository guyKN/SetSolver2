package com.guykn.setsolver.ui.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.guykn.setsolver.threading.CameraPreviewThread.CameraUiState;

public class CameraFragment extends Fragment {
    //todo: allow both landscape and portrait
    //todo: keep image even after phone activity pauses
    //todo: handle unexpected user actions
    public static final String TAG = "CameraFragment";
    public static final int CAMERA_ACTIVE_UI_UPDATE_DELAY = 750;
    private final DecimalFormat processingTimeDecimalFormat = new DecimalFormat("0.00sec");
    private MainViewModel mainViewModel;
    private CameraPreview cameraPreview;
    private CameraOverlay cameraOverlay;
    private FrameLayout cameraFrame;
    private ImageButton captureButton;
    private TextView fpsView;
    private CameraThreadManager cameraThreadManager;
    private ProgressBar loadingIcon;
    private View loadingColorMask;
    private View loadingBlackScreen;
    private CameraUiState currentCameraUiState;

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
        if (context != null && root != null) {

            loadingIcon = root.findViewById(R.id.loading_icon);
            fpsView = root.findViewById(R.id.fps_display);
            loadingColorMask = root.findViewById(R.id.loading_color_mask);
            captureButton = root.findViewById(R.id.button_capture);
            loadingBlackScreen = root.findViewById(R.id.loading_black_screen);

            CameraThread cameraThread = new CameraPreviewThread(context.getApplicationContext(),
                    mainViewModel, mainViewModel.getConfigLiveData().getValue());

            cameraThreadManager = new CameraThreadManager(cameraThread, getLifecycle());
            cameraPreview = cameraThreadManager.getCameraPreview(context);
            cameraThreadManager.startCamera();
            cameraOverlay = new CameraOverlay(context, getLifecycle());

            cameraFrame.setOnClickListener((View v) -> {
                cameraThreadManager.startCamera();
            });

            cameraFrame.addView(cameraPreview);
            cameraFrame.addView(cameraOverlay);

            fpsView.bringToFront();
            loadingColorMask.bringToFront();
            loadingIcon.bringToFront();
            captureButton.bringToFront();
            loadingBlackScreen.bringToFront();

            mainViewModel.getDrawableLiveData().observe(getViewLifecycleOwner(), drawable -> {
                cameraOverlay.setDrawable(drawable);
                Log.d(TAG, "drawable changed");
            });

            mainViewModel.getFpsLiveData().observe(getViewLifecycleOwner(), fps -> {
                String fpsText = String.format(Locale.US, "%s FPS", fps);
                fpsView.setText(fpsText);
            });

            mainViewModel.getTotalProcessingTimeData().observe(getViewLifecycleOwner(),
                    milliseconds -> {
                        double seconds = ((double) milliseconds) / 1000.0;
                        String processingTimeText = processingTimeDecimalFormat.format(seconds);
                        fpsView.setText(processingTimeText);
                    }
            );

            mainViewModel.getCameraExceptionData().observe(getViewLifecycleOwner(),
                    errorMessage -> {
                        Activity activity = getActivity();
                        if (activity == null)
                            return;
                        String text = getString(R.string.camera_error_heading) + errorMessage;
                        Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
                    }
            );

            mainViewModel.getCameraUiStateLiveData().observe(getViewLifecycleOwner(),
                    cameraUiState -> {
                        currentCameraUiState = cameraUiState;
                        //noinspection DuplicateBranchesInSwitch
                        switch (cameraUiState) {
                            case CAMERA_INACTIVE:
                                fpsView.setVisibility(View.GONE);
                                captureButton.setVisibility(View.GONE);
                                loadingIcon.setVisibility(View.GONE);
                                loadingColorMask.setVisibility(View.GONE);
                                cameraOverlay.setVisibility(View.GONE);
                                loadingBlackScreen.setVisibility(View.VISIBLE);
                                break;
                            case CAMERA_LOADING:
                                fpsView.setVisibility(View.GONE);
                                captureButton.setVisibility(View.GONE);
                                loadingIcon.setVisibility(View.GONE);
                                loadingColorMask.setVisibility(View.GONE);
                                cameraOverlay.setVisibility(View.GONE);
                                loadingBlackScreen.setVisibility(View.VISIBLE);
                                break;
                            case CAMERA_ACTIVE:
                                fpsView.setVisibility(View.VISIBLE);
                                captureButton.setVisibility(View.VISIBLE);
                                loadingIcon.setVisibility(View.GONE);
                                loadingColorMask.setVisibility(View.GONE);
                                cameraOverlay.setVisibility(View.VISIBLE);
                                loadingBlackScreen.setVisibility(View.GONE);
                                break;
                            case CAMERA_CURRENTLY_PROCESSING:
                                fpsView.setVisibility(View.GONE);
                                captureButton.setVisibility(View.GONE);
                                loadingIcon.setVisibility(View.VISIBLE);
                                loadingColorMask.setVisibility(View.VISIBLE);
                                cameraOverlay.setVisibility(View.VISIBLE);
                                loadingBlackScreen.setVisibility(View.GONE);
                                break;
                            case CAMERA_DONE_PROCESSING:
                                fpsView.setVisibility(View.VISIBLE);
                                captureButton.setVisibility(View.GONE);
                                loadingIcon.setVisibility(View.GONE);
                                loadingColorMask.setVisibility(View.GONE);
                                cameraOverlay.setVisibility(View.VISIBLE);
                                loadingBlackScreen.setVisibility(View.GONE);
                                break;
                        }
                    }
            );

            captureButton.setOnClickListener(
                    (View v) -> {
                        cameraThreadManager.takePicture();
                    }
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