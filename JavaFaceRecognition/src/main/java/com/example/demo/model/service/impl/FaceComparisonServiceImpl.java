package com.example.demo.model.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.io.InputStream;
import java.io.FileInputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.dto.FaceRecognitionInOutDto;
import com.example.demo.model.service.FaceComparisonService;

@Service
public class FaceComparisonServiceImpl implements FaceComparisonService{

	private static final String CASCADE_CLASSIFIER_PATH = "C:\\Users\\Julius Basas\\ocv\\idea\\haarcascade_frontalface_alt.xml"; 
    private static final double SIMILARITY_THRESHOLD = 0.12; 
    
    private static final String FACES_PATH = "C:\\Users\\Julius Basas\\ocv\\faces\\";
    
	@Override
	public FaceRecognitionInOutDto compareFaces(MultipartFile file1) throws IOException {
		
		FaceRecognitionInOutDto outDto = new FaceRecognitionInOutDto();
		
		File facesDirectory = new File(FACES_PATH);
        File[] imageFiles = facesDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        
        if (imageFiles == null || imageFiles.length == 0) {
            outDto.setHasFound(false);
        }
		 
        

        for (File imageFile : imageFiles) {
        	try (InputStream in = new FileInputStream(imageFile);
        			ByteArrayOutputStream out = new ByteArrayOutputStream()) {
        		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME); 

        		CascadeClassifier faceDetector = new CascadeClassifier();
        		faceDetector.load(CASCADE_CLASSIFIER_PATH);
                MatOfRect faces1 = new MatOfRect();
                MatOfRect faces2 = new MatOfRect();
                
        		byte[] buffer = new byte[1024];
    	        int length;
    	        while ((length = in.read(buffer)) != -1) {
    	            out.write(buffer, 0, length);
    	        }
        		
    	        Mat image1 = Imgcodecs.imdecode(new MatOfByte(file1.getBytes()), Imgcodecs.IMREAD_UNCHANGED);
        		Mat image2 = Imgcodecs.imdecode(new MatOfByte(out.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);
        		
        		faceDetector.detectMultiScale(image1, faces1);
                faceDetector.detectMultiScale(image2, faces2);
                
                if (faces1.toArray().length == 0 || faces2.toArray().length == 0) {
                    continue; // Skip if no faces detected in either image
                }

                Mat face1 = image1.submat(faces1.toArray()[0]);
                Mat face2 = image2.submat(faces2.toArray()[0]);
                
                Mat hist1 = getHistogram(face1);
                Mat hist2 = getHistogram(face2);
                double distance = Imgproc.compareHist(hist1, hist2, Imgproc.CV_DIST_MASK_3);

                System.out.println("Comparing with " + imageFile.getName() + ": " + (distance < SIMILARITY_THRESHOLD) + " : " + distance);

                if (distance <= SIMILARITY_THRESHOLD) {
                    outDto.setHasFound(true); 
                    outDto.setPersonName(imageFile.getName());
                    
                    return outDto;
                }
        	}        
        }
        
        outDto.setHasFound(false);

        return outDto;
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
