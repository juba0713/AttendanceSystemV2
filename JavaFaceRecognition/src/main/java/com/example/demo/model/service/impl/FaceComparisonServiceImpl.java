package com.example.demo.model.service.impl;

import java.util.Arrays;

import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;

import com.example.demo.model.service.FaceComparisonService;

@Service
public class FaceComparisonServiceImpl implements FaceComparisonService{

	private static final String CASCADE_CLASSIFIER_PATH = "C:\\Users\\Manag\\ocv\\idea\\haarcascade_frontalface_alt2.xml"; 
    private static final double SIMILARITY_THRESHOLD = 0.2; // Adjust as needed

    @Override
    public boolean compareFaces(Mat image1, Mat image2) {
        CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_CLASSIFIER_PATH);
        MatOfRect faces1 = new MatOfRect();
        MatOfRect faces2 = new MatOfRect();

        faceDetector.detectMultiScale(image1, faces1);
        faceDetector.detectMultiScale(image2, faces2);

        if (faces1.toArray().length == 0 || faces2.toArray().length == 0) {
            return false; // No faces detected
        }

        Mat face1 = image1.submat(faces1.toArray()[0]);
        Mat face2 = image2.submat(faces2.toArray()[0]);

        Mat hist1 = getHistogram(face1);
        Mat hist2 = getHistogram(face2);
        double distance = Imgproc.compareHist(hist1, hist2, Imgproc.CV_DIST_MASK_3);
        
        System.out.println((distance < SIMILARITY_THRESHOLD)+ " : " + distance);
        
        return distance < SIMILARITY_THRESHOLD;
    }

    private Mat getHistogram(Mat image) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);
        MatOfInt histSize = new MatOfInt(256);
        MatOfFloat ranges = new MatOfFloat(0, 256);
        Mat hist = new Mat();
        Imgproc.calcHist(Arrays.asList(grayImage), new MatOfInt(0), new Mat(), hist, histSize, ranges);
        return hist;
    }

}
