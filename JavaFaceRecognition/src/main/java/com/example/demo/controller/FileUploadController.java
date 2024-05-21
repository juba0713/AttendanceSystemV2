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

import com.example.demo.controller.webdto.FileUploadWebDto;
import com.example.demo.model.service.FaceComparisonService;

@Controller
public class FileUploadController {

	@Autowired
	FaceComparisonService faceComparisonService;
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	@GetMapping("/test")
	public String show(@ModelAttribute FileUploadWebDto webDto) {

		return "test";
	}

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	@GetMapping("/video")
	public ModelAndView index() {
		        
		return new ModelAndView("common/video");
	}
	
	@PostMapping("/compare")
    public ResponseEntity<Boolean> compareFaces(
        @RequestParam("image1") MultipartFile file1
    ) throws IOException {
		
		File realImage2 = new File("C:\\Users\\Manag\\ocv\\idea\\julius.jpg");

	    Mat image1 = Imgcodecs.imdecode(new MatOfByte(file1.getBytes()), Imgcodecs.IMREAD_UNCHANGED);

	    // Read bytes from realImage2
	    try (InputStream in = new FileInputStream(realImage2);
	         ByteArrayOutputStream out = new ByteArrayOutputStream()) {

	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = in.read(buffer)) != -1) {
	            out.write(buffer, 0, length);
	        }

	        Mat image2 = Imgcodecs.imdecode(new MatOfByte(out.toByteArray()), Imgcodecs.IMREAD_UNCHANGED);

	        if (file1.isEmpty()) {  // No need to check file2 (it's a local File object)
	            System.out.println("Image 1 is empty");
	            // Handle this error appropriately (e.g., return an error response)
	        }

	        boolean areSimilar = faceComparisonService.compareFaces(image1, image2);
	        
	        return ResponseEntity.ok(areSimilar);
	    } 
    }

	/*
	 * @GetMapping(value = "/video_feed", produces = MediaType.IMAGE_JPEG_VALUE)
	 * public ResponseEntity<SseEmitter> videoFeed() { SseEmitter emitter = new
	 * SseEmitter(); executor.execute(() -> { VideoCapture camera = new
	 * VideoCapture(0); try { if (!camera.isOpened()) {
	 * emitter.completeWithError(new RuntimeException("Failed to open camera"));
	 * return; }
	 * 
	 * while (true) { Mat frame = new Mat(); if (camera.read(frame)) {
	 * Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB); MatOfByte mem = new
	 * MatOfByte(); Imgcodecs.imencode(".jpg", frame, mem); byte[] imageBytes =
	 * mem.toArray(); try { emitter.send(imageBytes, MediaType.IMAGE_JPEG); } catch
	 * (IOException e) { emitter.completeWithError(e); break; } } } } finally {
	 * camera.release(); } });
	 * 
	 * return ResponseEntity.ok() .header(HttpHeaders.CONTENT_TYPE,
	 * MediaType.IMAGE_JPEG_VALUE) .body(emitter); }
	 */

	@GetMapping(value = "/video_feed", produces = MediaType.IMAGE_JPEG_VALUE)
	public @ResponseBody byte[] videoFeed() throws InterruptedException, ExecutionException {
		return executor.submit(() -> {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
				VideoCapture camera = new VideoCapture(0);
				if (!camera.isOpened()) {
					throw new RuntimeException("Failed to open camera");
				}
				
				

				Mat frame = new Mat();
				camera.read(frame);
				MatOfByte mem = new MatOfByte();
				if (camera.read(frame)) {
					Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
					Imgcodecs.imencode(".jpg", frame, mem);
					baos.write(mem.toArray());
				}
				
				
				camera.release();

				return baos.toByteArray();

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).get();
	}

	
	 @PostMapping("/upload") public String handleFileUpload(@RequestParam("image") MultipartFile imageFile, Model model) {
	  
		 if (!imageFile.isEmpty()) {
		        try {
		            // 1. Determine the Save Location
		            String uploadDir = "C:\\temp\\"; // Or configure using a properties file
		            Path filePath = Paths.get(uploadDir).resolve(imageFile.getOriginalFilename());
		            
		            // Create the directory if it doesn't exist
		            Files.createDirectories(filePath.getParent());

		            // 2. Save the Image
		            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING); 

		            model.addAttribute("message", "Image uploaded successfully!");
		        } catch (IOException e) {
		            model.addAttribute("error", "Error uploading image: " + e.getMessage());
		        }
		    } else {
		        model.addAttribute("error", "Please select an image to upload.");
		    } 
	  
	  return "redirect:/test"; // Redirect to a success page 
	 }
	 
	 /* 
	 * @GetMapping(value = "/video_feed", produces = MediaType.IMAGE_JPEG_VALUE)
	 * public @ResponseBody byte[] videoFeed() throws InterruptedException,
	 * ExecutionException { return executor.submit(() -> { try
	 * (ByteArrayOutputStream baos = new ByteArrayOutputStream()) { VideoCapture
	 * camera = new VideoCapture(0); if (!camera.isOpened()) { throw new
	 * RuntimeException("Failed to open camera"); } Mat frame = new Mat(); MatOfByte
	 * mem = new MatOfByte(); if (camera.read(frame)) { Imgproc.cvtColor(frame,
	 * frame, Imgproc.COLOR_BGR2RGB); Imgcodecs.imencode(".jpg", frame, mem);
	 * baos.write(mem.toArray()); } camera.release(); return baos.toByteArray(); }
	 * catch (IOException e) { throw new RuntimeException(e); } }).get(); }
	 */
	/*
	 * @PostMapping("/upload") public String handleFileUpload(@ModelAttribute
	 * FileUploadWebDto webDto) {
	 * 
	 * try { // Save the file (e.g., to a local directory or cloud storage) // You
	 * can use the 'file' variable to access the uploaded image data
	 * System.out.println(webDto.getTest()); MultipartFile file = webDto.getImage();
	 * //run(file); } catch (IOException e) { // Handle exceptions
	 * System.out.println("ERROR"); }
	 * 
	 * return "redirect:/test"; // Redirect to a success page }
	 * 
	 * @GetMapping("/video_feed") public ResponseEntity<StreamingResponseBody>
	 * videoFeed() { return ResponseEntity.ok()
	 * .contentType(MediaType.parseMediaType(
	 * "multipart/x-mixed-replace;boundary=frame")) .body(this::processVideoFrames);
	 * }
	 * 
	 * @GetMapping("/video") public String index() { return "common/video"; //
	 * Render the Thymeleaf template }
	 * 
	 * private void processVideoFrames(OutputStream outputStream) { VideoCapture
	 * capture = new VideoCapture(0); // 0 for default webcam Mat frame = new Mat();
	 * 
	 * while (capture.read(frame)) { // Face detection MatOfRect faces = new
	 * MatOfRect(); faceCascade.detectMultiScale(frame, faces);
	 * 
	 * for (Rect rect : faces.toArray()) { // Face recognition (if needed) Mat face
	 * = new Mat(frame, rect); IntPointer label = new IntPointer(1); DoublePointer
	 * confidence = new DoublePointer(1); faceRecognizer.predict(face, label,
	 * confidence); int predictedLabel = label.get(0);
	 * 
	 * // Draw rectangle and label on the frame rectangle(frame, rect, new Scalar(0,
	 * 255, 0, 0), 2, LINE_8, 0); putText(frame, "Person " + predictedLabel, new
	 * Point(rect.x(), rect.y() - 10), FONT_HERSHEY_SIMPLEX, 0.9, new Scalar(0, 255,
	 * 0, 0), 2); }
	 * 
	 * // Encode frame to JPEG MatOfByte encodedImage = new MatOfByte();
	 * imencode(".jpg", frame, encodedImage); byte[] bytes = encodedImage.toArray();
	 * 
	 * try { outputStream.write(( "--frame\r\n" + "Content-Type: image/jpeg\r\n\r\n"
	 * ).getBytes()); outputStream.write(bytes);
	 * outputStream.write("\r\n".getBytes()); } catch (IOException e) {
	 * e.printStackTrace(); } } }
	 */
	/*
	 * @PostMapping("/process_encoding") public ResponseEntity<Map<String, Boolean>>
	 * processEncodings(@RequestBody Map<String, String> data) { // ... (Call the
	 * videoService to process encodings and get results) }
	 * 
	 * 
	 * public static void run(MultipartFile file) throws IOException {
	 * 
	 * loadLibrary( Core.NATIVE_LIBRARY_NAME ); CascadeClassifier faceDetector = new
	 * CascadeClassifier(); faceDetector.load(
	 * "C:\\Users\\Manag\\ocv\\idea\\haarcascade_frontalface_alt.xml" );
	 * 
	 * File images = new File("C:\\Users\\Manag\\ocv\\idea\\julius.jpg"); Mat image
	 * = Imgcodecs.imread(String.valueOf(images)); // Detecting faces MatOfRect
	 * faceDetections = new MatOfRect(); faceDetector.detectMultiScale( image,
	 * faceDetections );
	 * 
	 * // Creating a rectangular box showing faces detected for (Rect rect :
	 * faceDetections.toArray()) { rectangle( image, new Point( rect.x, rect.y ),
	 * new Point( rect.width + rect.x, rect.height + rect.y ), new Scalar( 0, 255, 0
	 * ) ); }
	 * 
	 * // Saving the output image String filename = "Ouput.jpg";
	 * System.out.println("Face Detected Successfully "); Imgcodecs.imwrite(
	 * "C:\\temp\\" + filename, image );
	 * 
	 * 
	 * 
	 * }
	 */
}
