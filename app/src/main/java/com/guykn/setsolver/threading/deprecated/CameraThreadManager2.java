package com.guykn.setsolver.threading.deprecated;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleObserver;

import com.guykn.setsolver.FpsCounter;
import com.guykn.setsolver.ImageFileManager;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.CameraProcessingAction;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;
import com.guykn.setsolver.imageprocessing.ImageProcessingManager;
import com.guykn.setsolver.imageprocessing.image.ByteArrayImage;
import com.guykn.setsolver.imageprocessing.image.JpegByteArrayImage;
import com.guykn.setsolver.imageprocessing.image.MatImage;
import com.guykn.setsolver.set.SetBoardPosition;

import org.opencv.core.Mat;

@SuppressWarnings("deprecation")
public class CameraThreadManager2 implements SurfaceHolder.Callback, LifecycleObserver {

    private enum CameraThreadType {
        CAMERA_PREVIEW{
            @Override
            CameraPreviewThread createCameraThread(CameraThreadManager2 manager) {
                return manager.new CameraPreviewThread();
            }
        },
        CAMERA_PROCESSING{
            @Override
            CameraPreviewThread createCameraThread(CameraThreadManager2 manager) {
                return null;//todo
            }
        };

        abstract CameraPreviewThread createCameraThread(CameraThreadManager2 manager);
    }

    final private CameraPreviewThread mPreviewThread;

    @Nullable
    private final ImageProcessingManager mProcessingManager;
    @Nullable
    final private Context mContext;
    @Nullable
    final private FpsCounter mFpsCounter;
    @Nullable
    final private ImageFileManager mFileManager;


    private CameraThreadManager2(ImageProcessingConfig config,
                                 CameraThreadType cameraThreadType,
                                 @Nullable ImageProcessingManager processingManager,
                                 @Nullable Context context,
                                 @Nullable FpsCounter fpsCounter) {
        mContext = context;
        mProcessingManager = processingManager;
        mFpsCounter = fpsCounter;
        if (mContext != null) {
            mFileManager = new ImageFileManager(context);
        } else {
            mFileManager = null;
        }
        this.mConfig = config;

        mPreviewThread = cameraThreadType.createCameraThread(this);
        mPreviewThread.start();

    }

    private CameraThreadManager2(ImageProcessingConfig config,
                                 CameraThreadType cameraThreadType,
                                 @Nullable Context context,
                                 @Nullable FpsCounter fpsCounter) {
        this(config, cameraThreadType, null, context, fpsCounter);
    }

    public static CameraThreadManager2 createPreviewThreadManager(ImageProcessingConfig config,
                                                                  @Nullable Context context,
                                                                  @Nullable FpsCounter fpsCounter){
        return new CameraThreadManager2(config, CameraThreadType.CAMERA_PREVIEW, context, fpsCounter);
    }

    public static CameraThreadManager2 createProcessingThreadManager(ImageProcessingConfig config,
                                                                     @Nullable Context context,
                                                                     @Nullable FpsCounter fpsCounter,
                                                                     @NonNull ImageProcessingManager processingManager){

        return new CameraThreadManager2(config, CameraThreadType.CAMERA_PROCESSING,
                processingManager, context, fpsCounter);
    }



    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mPreviewThread.getHandler().post(
                () -> mPreviewThread.surfaceCreated(holder)
        );
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        mPreviewThread.getHandler().post(
                () -> mPreviewThread.surfaceChanged(holder ,format, width, height)
        );
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mPreviewThread.getHandler().post(
                () -> mPreviewThread.surfaceDestroyed(holder)
        );
    }

    public void takePicture() {
        mPreviewThread.getHandler().post(
                mPreviewThread::internalTakePicture
        );
    }

    public void terminateThread() {
        mPreviewThread.getHandler().postAtFrontOfQueue( //todo: should I use postAtfrontOfQueue?
                mPreviewThread::terminateThread
        );
    }


    private class CameraPreviewThread extends HandlerThread implements Camera.PictureCallback,
            Camera.PreviewCallback, SurfaceHolder.Callback {

        private static final String THREAD_NAME = "CameraPreviewThread";

        private static final String TAG = CameraPreview.TAG;

        private Camera mCamera;
        private Handler mHandler;

        private static final int CAMERA_RESTART_DELAY = 0;


        public CameraPreviewThread() {
            super(THREAD_NAME);
        }


        protected Camera getCamera() {
            return mCamera;
        }



        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            Log.d(TAG, "surfaceCreated(). ");
            openCamera();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int w, int h) {
            Log.d(TAG, "surfaceChanged.");
            if (holder.getSurface() == null) {
                // preview surface does not exist
                return;
            }
            stopCamera();
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
            startCamera();
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed(). ");
            destroyCamera();
        }

        private void terminateThread() {
            onTerminateThread();
            quit();
        }


        protected void onTerminateThread() {}

        protected void openCamera() {
            Log.d(TAG, "openCamera()");
            mCamera = getCameraInstance();
            if (mCamera == null) {
                Log.w(TAG, "couldn't open camera");
            }
        }

        protected void startCamera() {
            Log.d(TAG, "startCamera()");
            try {
                mCamera.startPreview();
                mCamera.autoFocus(null);
                mCamera.setPreviewCallback(this);
                if (mFpsCounter != null) {
                    mFpsCounter.startCounting();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        protected void stopCamera() {
            Log.d(TAG, "stopCamera()");
            mCamera.stopPreview();
            mCamera.cancelAutoFocus();
            mCamera.setPreviewCallback(null);
            if (mFpsCounter != null) {
                mFpsCounter.startCounting();
            }
        }

        protected void destroyCamera() {
            Log.d(TAG, "destroyCamera()");
            if (mCamera == null) {
                return;
            }
            stopCamera();
            try {
                mCamera.release();
            } catch (Exception e) {
                e.printStackTrace();
                // ignore: tried to stop a non-existent preview
            }
            mCamera = null;
        }


        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mFpsCounter != null) {
                mFpsCounter.startCounting();
            }
        }

        @Override
        public final void onPictureTaken(byte[] data, Camera camera) {
            Camera.Size size = camera.getParameters().getPictureSize();
            int width = size.width;
            int height = size.height;

            JpegByteArrayImage image = new JpegByteArrayImage(data, width, height);
            pictureTakenMatAction(image.toMat());
            stopCamera();
            //startCamera();
            //startCameraDelayed();
        }

        protected void pictureTakenMatAction(Mat mat) {
            if (mFileManager != null) {
                mFileManager.saveToGallery(mat);
            }
        }

        private void internalTakePicture() {
            mCamera.takePicture(null, null, null, this);
        }

        @Nullable
        private Camera getCameraInstance() {
            Camera c = null;
            try {
                c = Camera.open(); // attempt to get a Camera instance
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                e.printStackTrace();
            }
            return c; // returns null if camera is unavailable
        }

        /**
         * After a certain amount of time, start the camera again.
         * Used to give the user a certain amount of time to look at the camera output
         * before it starts again
         *
         */

        private void startCameraDelayed() {
            getHandler().postDelayed(
                    () -> {
                        if (mCamera != null) {
                            startCamera();
                        }
                    },
                    CAMERA_RESTART_DELAY
            );
        }

    }




    @SuppressWarnings("deprecation")
    public class CameraProcessingThread extends CameraPreviewThread {

        //todo: use a byte buffer in the callback for better performance

        private final ImageProcessingManager mmProcessingManager;
        private final CameraProcessingAction.Callback mmCallback;

        public CameraProcessingThread(ImageProcessingManager processingManager, CameraProcessingAction.Callback callback){
            mmProcessingManager = processingManager;
            mmCallback = callback;
        }


        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            super.onPreviewFrame(data, camera);
            Camera.Size imageSize = camera.getParameters().getPreviewSize();
            int width = imageSize.width;
            int height = imageSize.height;

            ByteArrayImage byteImage = new ByteArrayImage(data, width, height);
            mmProcessingManager.setImage(byteImage);
            RotatedRectangleList cardPositions = mmProcessingManager.getCardPositions();
            mmCallback.onImageProcessingSuccess(cardPositions);
            mmProcessingManager.finish();
        }

        @Override
        protected void onTerminateThread(){
            mmProcessingManager.finish();
            super.onTerminateThread();
        }

        public void setConfig(ImageProcessingConfig config){
            mmProcessingManager.setConfig(config);
        }

        @Override
        public void pictureTakenMatAction(Mat mat){
            //super.pictureTakenMatAction(mat); //uncoment to also saved the original, full-sized image
            MatImage matImage = new MatImage(mat);
            mmProcessingManager.setImage(matImage);
            //processingManager.saveCardImagesToGallery(getFileManager());
    //        DrawableOnCanvas result =  processingManager.getCardPositions();
    //        callback.onImageProcessingSuccess(result);

            SetBoardPosition boardPosition = mmProcessingManager.getBoard();
            mmCallback.onImageProcessingSuccess(boardPosition);
            mmProcessingManager.finish();
        }

    }




}
