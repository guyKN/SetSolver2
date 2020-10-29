package com.guykn.setsolver.imageprocessing.verify.indirect;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;

import org.christopherfrantz.dbscan.DBSCANClusteringException;
import org.christopherfrantz.dbscan.DistanceMetric;

public class RectangleAreaDistanceMetric implements DistanceMetric<GenericRotatedRectangle> {

    @Override
    public double calculateDistance(GenericRotatedRectangle rect1,
                                    GenericRotatedRectangle rect2) throws DBSCANClusteringException{

        return Math.pow(rect1.getArea()-rect2.getArea(), 2);
    }
}