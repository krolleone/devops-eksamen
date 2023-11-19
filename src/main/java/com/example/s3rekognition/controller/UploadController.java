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

    @GetMapping("")
    public String viewHomePage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUploadForm(Model model, String description, @RequestParam("file") MultipartFile multipart) {

        String[] extensions = {".jpg", ".jpeg", ".png"};
        String fileName = multipart.getOriginalFilename();

        boolean checkExtension = Arrays.stream(extensions)
                .anyMatch(ext -> fileName.endsWith(ext));

        String msg = "";

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