package com.example.demo.model.service;

import java.io.IOException;

import org.opencv.core.Mat;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.dto.FaceRecognitionInOutDto;

@Service
public interface FaceComparisonService {

	public FaceRecognitionInOutDto compareFaces(MultipartFile file1)throws IOException ;
	
}
