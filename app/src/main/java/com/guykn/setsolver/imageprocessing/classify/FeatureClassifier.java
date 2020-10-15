package com.guykn.setsolver.imageprocessing.classify;

import android.content.Context;
import android.graphics.Bitmap;

import com.guykn.setsolver.imageprocessing.detect.ContourBasedCardDetector;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;

public class FeatureClassifier {
    //todo: maybe use anodroid studio's built in tensorflow lite models
    private static final int MOBILENET_IMAGE_SIZE =224;
    private static final DataType INPUT_DATA_TYPE = DataType.FLOAT32;
    private static final String MODEL_FILE_NAME = "model.tflite";
    private static final String LABELS_FILE_NAME = "labels.txt";
    public static final int NUM_CATEGORIES =3;

    private Context context;
    private ContourBasedCardDetector.Config config;
    private String modelFilePath;
    private String labelsFilePath;
    private ImageProcessor imageProcessor;
    private Interpreter interpreter;
    TensorProcessor probabilityProcessor;
    List<String> labelValues;

    public FeatureClassifier(Context context, ContourBasedCardDetector.Config config, String modelDirectory) throws IOException {
        this.context = context;
        this.config = config;
        this.modelFilePath = modelDirectory + MODEL_FILE_NAME;
        this.labelsFilePath = modelDirectory + LABELS_FILE_NAME;
        initClassifier();
    }
    private void initClassifier() throws IOException{
        Interpreter.Options options = new Interpreter.Options();
        imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(MOBILENET_IMAGE_SIZE, MOBILENET_IMAGE_SIZE, ResizeOp.ResizeMethod.BILINEAR)).build();
        MappedByteBuffer buffer = FileUtil.loadMappedFile(context, modelFilePath);
        interpreter = new Interpreter(buffer, options);
        probabilityProcessor = new TensorProcessor.Builder().add(new NormalizeOp(0, 1)).build();

    }

    ClassificationResult classify(Bitmap image){
        TensorBuffer probabilityBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CATEGORIES}, INPUT_DATA_TYPE);
        TensorImage tImage = new TensorImage(INPUT_DATA_TYPE);
        tImage.load(image);
        tImage = imageProcessor.process(tImage);
        interpreter.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
        return ClassificationResult.getClassificationResultFromProbabilityTensor(probabilityBuffer);
    }

    static public class ClassificationResult {
        private int resultID;
        private double resultProbability;
        public int getResultID() {
            return resultID;
        }

        public double getResultProbability() {
            return resultProbability;
        }


        private ClassificationResult(int resultID, double resultProbability){
            this.resultID = resultID;
            this.resultProbability = resultProbability;
        }
        //finds te highest probability in a tensorBuffer, and creates a new Classification resault showing its ID and the probability of being right
        public static ClassificationResult getClassificationResultFromProbabilityTensor(TensorBuffer probabilityBuffer){
            double currentMaxProbability =0;
            int currentMaxIndex=0;
            for(int i=0;i<NUM_CATEGORIES;i++){
                double probability = probabilityBuffer.getFloatValue(i);
                if(probability > currentMaxProbability){
                    currentMaxProbability = probability;
                    currentMaxIndex = i;
                }
            }
            return new ClassificationResult(currentMaxIndex, currentMaxProbability);
        }
    }
}
