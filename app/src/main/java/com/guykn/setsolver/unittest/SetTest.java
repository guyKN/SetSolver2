package com.guykn.setsolver.unittest;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;

import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;

public class SetTest {
    GenericRotatedRectangle testRect = new GenericRotatedRectangle(new RotatedRect(new Point(1, 1), new Size(1, 1), 5), 20, 20);

}
