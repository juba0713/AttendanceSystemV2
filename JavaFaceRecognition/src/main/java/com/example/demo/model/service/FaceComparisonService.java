package com.example.demo.model.service;

import org.opencv.core.Mat;
import org.springframework.stereotype.Service;

@Service
public interface FaceComparisonService {

	public boolean compareFaces(Mat image1, Mat image2);
	
}
