package com.example.demo.model.service;

import java.io.IOException;

import org.opencv.core.Mat;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FaceComparisonService {

	public boolean compareFaces(MultipartFile file1)throws IOException ;
	
}
