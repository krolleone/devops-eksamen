package com.example.s3rekognition.controller;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.logging.Logger;


@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {

    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;

    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());


    private Map<String, PPEClassificationResponse> ppeFaceScans = new HashMap();
    private Map<String, PPEClassificationResponse> ppeAllRegionsScans = new HashMap();

    private MeterRegistry meterRegistry;

    @Autowired
    public RekognitionController(MeterRegistry meterRegistry) {
        this.s3Client = AmazonS3ClientBuilder.standard().build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard().build();
        this.meterRegistry = meterRegistry;
    }

    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(value = "/scan-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("FACE_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result);


            logger.info("scanning " + image.getKey() + ", violation result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);

            ppeFaceScans.put(image.getKey(), classification);
        }
        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }

    @GetMapping(value = "/scan-all-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanAllPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("FACE_COVER", "HAND_COVER", "HEAD_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, hands or head, it's a violation of regulations
            boolean violation = isTotalViolation(result);


            logger.info("scanning " + image.getKey() + ", violation result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);

            ppeAllRegionsScans.put(image.getKey(), classification);
        }
        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }

    private static boolean isTotalViolation(DetectProtectiveEquipmentResult result) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> {
                    String bodyPartName = bodyPart.getName();
                    List<String> requiredParts = Arrays.asList("FACE", "HAND_COVER", "HEAD_COVER");
                    return requiredParts.contains(bodyPartName) &&
                            bodyPart.getEquipmentDetections().isEmpty();
                });
    }

    /**
     * Detects if the image has a protective gear violation for the FACE bodypart-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.
     *
     * @param result
     * @return
     */
    private static boolean isViolation(DetectProtectiveEquipmentResult result) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals("FACE")
                        && bodyPart.getEquipmentDetections().isEmpty());
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        // Total Face-scans
        Gauge.builder("total_scans", ppeFaceScans,
                b -> b.values().size())
                .register(meterRegistry);
        // Total non-violations in Face-region
        Gauge.builder("non_violations", ppeFaceScans,
                b -> b.values().stream()
                        .filter(v -> !v.isViolation())
                        .count())
                .register(meterRegistry);
        // Total violations in Face-region
        Gauge.builder("violations", ppeFaceScans,
                b -> b.values().stream()
                        .filter(v -> v.isViolation())
                        .count())
                .register(meterRegistry);

        // Total scans all regions
        Gauge.builder("total_full_scans", ppeAllRegionsScans,
                b -> b.values().size())
                .register(meterRegistry);
        // Total non-violations in all regions
        Gauge.builder("non_violations_all_regions", ppeAllRegionsScans,
                b -> b.values().stream()
                        .filter(v -> !v.isViolation())
                        .count())
                .register(meterRegistry);
        // Total violations in all regions
        Gauge.builder("violations_all_regions", ppeAllRegionsScans,
                b -> b.values().stream()
                        .filter(v -> v.isViolation())
                        .count())
                .register(meterRegistry);

        // Totalt antall personer scannet
        Gauge.builder("people_count", ppeFaceScans,
                p -> p.values()
                        .stream()
                        .mapToInt(PPEClassificationResponse::getPersonCount)
                        .sum())
                .register(meterRegistry);
    }
}
