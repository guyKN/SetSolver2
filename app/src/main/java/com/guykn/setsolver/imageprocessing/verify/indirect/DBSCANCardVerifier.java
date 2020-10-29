package com.guykn.setsolver.imageprocessing.verify.indirect;

import com.guykn.setsolver.drawing.GenericRotatedRectangle;
import com.guykn.setsolver.drawing.RotatedRectangleList;
import com.guykn.setsolver.imageprocessing.ImageProcessingConfig;

import org.christopherfrantz.dbscan.DBSCANClusterer;
import org.christopherfrantz.dbscan.DBSCANClusteringException;

import java.util.ArrayList;
import java.util.List;

public class DBSCANCardVerifier implements IndirectCardVerifier {

    //todo: in addition, add a noise detector that prevents rectangles from being there for only 1 frame
    //todo: see what happens with cards that are too close to each other
    //todo: find the best way to handle cards that are also outliers

    private static final int MIN_SIZE_TO_DO_CLUSTERING =3;
    private static final int MIN_CLUSTER_SIZE = 2;



    @Override
    public RotatedRectangleList removeFalsePositives(RotatedRectangleList rectangleList,
                                                     ImageProcessingConfig config) {

        List<GenericRotatedRectangle> rectangleArray = rectangleList.getDrawables();

        if(rectangleArray.size()<MIN_SIZE_TO_DO_CLUSTERING){
            // If we are given too few elements in the list, we just return the input,
            // since clusterring is imposible with so few elements
            return rectangleList;
        }

        DBSCANClusterer<GenericRotatedRectangle> clusterer;
        try {
            clusterer = new DBSCANClusterer<>(rectangleArray,
                    MIN_CLUSTER_SIZE,
                    config.contourVerification.maxDistance,
                    new RectangleAreaDistanceMetric()
            );
            ArrayList<ArrayList<GenericRotatedRectangle>> clusters = clusterer.performClustering();

            ArrayList<GenericRotatedRectangle> largestCluster = findLargestCluster(clusters);

            return new RotatedRectangleList(largestCluster);

        } catch (DBSCANClusteringException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    private ArrayList<GenericRotatedRectangle> findLargestCluster(
            ArrayList<ArrayList<GenericRotatedRectangle>> clusters){

        ArrayList<GenericRotatedRectangle> largestCluster = new ArrayList<>();
        int largestClusterSize = 0;
        for(ArrayList<GenericRotatedRectangle> cluster: clusters){
            int clusterSize = cluster.size();
            if(clusterSize >= largestClusterSize){
                largestCluster = cluster;
                largestClusterSize = clusterSize;
            }
        }
        return largestCluster;
    }
}
