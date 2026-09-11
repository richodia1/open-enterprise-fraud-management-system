package org.openefms.frauddetection.ml;

public class MlScoringUnavailableException extends RuntimeException {
    private final String provider;
    private final String modelName;

    public MlScoringUnavailableException(String provider, String modelName, String reason) {
        super(reason);
        this.provider = provider;
        this.modelName = modelName;
    }

    public String provider() {
        return provider;
    }

    public String modelName() {
        return modelName;
    }
}
