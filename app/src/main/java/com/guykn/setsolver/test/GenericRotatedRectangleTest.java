package com.guykn.setsolver.test;

import android.graphics.Point;
import android.util.Log;

import com.guykn.setsolver.MainActivity;
import com.guykn.setsolver.drawing.GenericRotatedRectangle;

import java.util.Arrays;

public class GenericRotatedRectangleTest {
    static String TAG = MainActivity.TAG;
    public static void test(){
        GenericRotatedRectangle rect = new GenericRotatedRectangle(0.5,0.5,0.25,0.25, 0);
        Point[] corners = rect.getCornersTest(100, 100);
        Log.d(TAG, Arrays.toString(corners));
    }
}
