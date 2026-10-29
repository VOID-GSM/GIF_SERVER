package com.example.gifserverv2.domain.assessment.roadmaking.exception;

public class RoadMakingException extends RuntimeException {

    private final RoadMakingErrorCode errorCode;

    public RoadMakingException(RoadMakingErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public RoadMakingErrorCode getErrorCode() {
        return errorCode;
    }
}