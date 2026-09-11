package org.openefms.frauddetection.ml;

import org.openefms.common.models.ml.MlRuntimeStatus;
import org.openefms.common.models.ml.MlScoreRequest;
import org.openefms.common.models.ml.MlScoreResponse;

public interface MlScoringClient {
    MlRuntimeStatus status();

    MlScoreResponse score(MlScoreRequest request);
}
