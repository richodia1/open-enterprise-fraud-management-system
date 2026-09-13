package org.openefms.frauddetection.service;

import org.openefms.common.models.ml.MlRuntimeStatus;
import org.openefms.common.models.ml.MlScoreRequest;
import org.openefms.common.models.ml.MlScoreResponse;

public interface MlScoringClient {
    public MlRuntimeStatus status();

    public MlScoreResponse score(MlScoreRequest request);
}
