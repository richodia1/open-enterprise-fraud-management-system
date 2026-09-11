package org.openefms.frauddetection.ml;

import org.openefms.common.models.ml.MlRuntimeStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/fraud/ml/runtime")
class MlRuntimeController {
    private final MlScoringClient scoringClient;

    MlRuntimeController(MlScoringClient scoringClient) {
        this.scoringClient = scoringClient;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    MlRuntimeStatus status() {
        return scoringClient.status();
    }
}
