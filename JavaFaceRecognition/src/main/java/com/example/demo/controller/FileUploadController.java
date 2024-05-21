package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opencv.core.Core;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.io.InputStream;

import java.util.concurrent.Executors;

import com.example.demo.controller.webdto.FaceRecognitionWebDto;
import com.example.demo.model.dto.FaceRecognitionInOutDto;
import com.example.demo.model.service.FaceComparisonService;

@Controller
public class FileUploadController {

	@Autowired
	FaceComparisonService faceRecognitionService;
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@GetMapping("/test")
	public String show(@ModelAttribute FaceRecognitionWebDto webDto) {

		return "test";
	}

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@GetMapping("/video")
	public ModelAndView index() {
		        
		return new ModelAndView("common/video");
	}
	
	@PostMapping("/compare")
    public ResponseEntity<FaceRecognitionWebDto> compareFaces(
        @RequestParam("image1") MultipartFile file1
    ) throws IOException {
		
        FaceRecognitionInOutDto outDto = faceRecognitionService.compareFaces(file1);
        
        FaceRecognitionWebDto webDto = new FaceRecognitionWebDto();
        
        webDto.setHasFound(outDto.getHasFound());
        
        webDto.setPersonName(outDto.getPersonName());
        
        return ResponseEntity.ok(webDto);
	    
    }
	
	@GetMapping()
	public String showInitialScreen() {
		
		return "initial";
	}
}
