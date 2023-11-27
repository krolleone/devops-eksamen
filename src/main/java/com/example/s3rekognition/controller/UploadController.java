package com.example.s3rekognition.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.s3rekognition.S3Util;

import java.util.Arrays;

@Controller
public class UploadController {

    /**
     * This endpoint is a visual entrypoint to allow a user to upload images to the S3 bucket.
     *
     * @return
     */
    @GetMapping("")
    public String viewHomePage() {
        return "upload";
    }

    /**
     * This endpoint takes the file that the user wants to upload, and does some bare minimum checks
     * before it runs the logic behind the upload to S3 bucket.
     *
     * @param model
     * @param multipart
     * @return
     */
    @PostMapping("/upload")
    public String handleUploadForm(Model model, @RequestParam("file") MultipartFile multipart) {

        String[] extensions = {".jpg", ".jpeg", ".png"};
        String fileName = multipart.getOriginalFilename();

        boolean checkExtension = Arrays.stream(extensions)
                .anyMatch(ext -> fileName.endsWith(ext));

        String msg;

        if (checkExtension){
            try {
                S3Util.uploadFile(fileName, multipart.getInputStream());
                msg = "Your file has been uploaded successfully!";
            } catch (Exception ex) {
                msg = "Error uploading file: " + ex.getMessage();
            }
        } else {
            msg = "Not a supported file-type";
        }
        model.addAttribute("message", msg);

        return "message";
    }
}