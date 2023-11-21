package com.example.s3rekognition;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@Data
@RequiredArgsConstructor
public class FaceViolation {

    private String ppeArea = "FACE";
    private String id;
    private boolean isInViolation;

    public boolean isInViolation() {
        return isInViolation;
    }

}
