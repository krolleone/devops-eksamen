package com.example.s3rekognition.controller;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.Account;
import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;
import com.netflix.spectator.atlas.AtlasConfig;
import io.micrometer.atlas.AtlasMeterRegistry;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.math.BigDecimal.valueOf;
import static java.util.Optional.ofNullable;


@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {

    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;

    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());

    private Map<String, PPEClassificationResponse> ppeFaceScans = new HashMap();
    private Map<String, Account> theBank = new HashMap();

    private MeterRegistry meterRegistry;
    AtlasConfig atlasConfig = new AtlasConfig() {

        @Override
        public Duration step() {
            return Duration.ofSeconds(10);
        }

        @Override
        public String get(String s) {
            return null;
        }
    };

    MeterRegistry registry = new AtlasMeterRegistry(atlasConfig, Clock.SYSTEM);


    @Autowired
    public RekognitionController(MeterRegistry meterRegistry) {
        this.s3Client = AmazonS3ClientBuilder.standard().build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard().build();
        //this.meterRegistry = meterRegistry;
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

        Gauge.builder("account_count", theBank,
                b -> b.values().size()).register(registry);


        /*
        // Total Face-scans
        Gauge.builder("total_scans", ppeFaceScans,
                b -> b.values().size()).register(meterRegistry);
         */

        /*
        // Total amount of violations
        Gauge.builder("total_face_violations", inViolation,
                b -> b.values().stream()
                        .filter(v -> v)
                        .count()).register(meterRegistry);

        // Total amount of non-violations
        Gauge.builder("total_face_non_violations", inViolation,
                b -> b.values().stream()
                        .filter(v -> !v)
                        .count()).register(meterRegistry);
         */
    }

    @ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "account not found")
    public static class AccountNotFoundException extends RuntimeException {
    }

    private Account getOrCreateAccount(String accountId) {
        if (theBank.get(accountId) == null) {
            Account a = new Account();
            a.setId(accountId);
            theBank.put(accountId, a);
        }
        return theBank.get(accountId);
    }

    /**
     * Saves an account. Will create a new account if one does not exist.
     *
     * @param a the account Object
     * @return
     */
    @PostMapping(path = "/account", consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Account> updateAccount(@RequestBody Account a) {
        registry.counter("update_account").increment();
        Account account = getOrCreateAccount(a.getId());
        account.setBalance(a.getBalance());
        account.setCurrency(a.getCurrency());
        theBank.put(a.getId(), a);
        return new ResponseEntity<>(a, HttpStatus.OK);
    }

    /**
     * Gets account info for an account
     *
     * @param accountId the account ID to get info from
     * @return
     */
    @Timed
    @GetMapping(path = "/account/{accountId}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Account> balance(@PathVariable String accountId) {
        registry.counter("balance").increment();
        Account account = ofNullable(theBank.get(accountId)).orElseThrow(AccountNotFoundException::new);

        // Random timer to simulate dely
        try {
            Thread.sleep((long) (250 * Math.random()));
        } catch (InterruptedException ignored) {
        }
        return new ResponseEntity<>(account, HttpStatus.OK);
    }
}
