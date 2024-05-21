package com.example.demo.controller.webdto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class FileUploadWebDto {
	
	String test;
	
	MultipartFile image1;
	
	MultipartFile image2;
	
	public Boolean hasFound;
	
	public String personName;
}
