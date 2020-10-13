package com.guykn.setsolver;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageFileManager {
    static String imageFilePrefix = ".jpg";
    private Context context;
    File currentImage;
    public ImageFileManager(Context context){
        this.context = context;
    }

    public File createTempImage() throws IOException {
        File storageDir = new File(context.getCacheDir(), "images");
        storageDir.mkdir();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        currentImage = File.createTempFile(
                imageFileName,
                imageFilePrefix,
                storageDir
        );
        return currentImage;
    }

    public void setCurrentImage(File image){
        this.currentImage = image;
    }
    public File getCurrentImage(){
        return currentImage;
    }
    public String getCurrentImagePath(){
        return currentImage.getAbsolutePath();
    }

    public void saveImage(Bitmap bmp) throws IOException {
        try (FileOutputStream out = new FileOutputStream(currentImage)) { // a try is need in order to automatic close the outputStream in case of failure
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out); //todo: check what quality does
        }
    }
    public void saveImage(Mat mat){
        Imgcodecs.imwrite(getCurrentImagePath(), mat);
    }


    public void saveToGallery(Bitmap bmp){
        MediaStore.Images.Media.insertImage(context.getContentResolver(), bmp, "An image", "longer description");
    }
}
