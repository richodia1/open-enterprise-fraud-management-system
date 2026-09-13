package org.openefms.frauddetection.controller;

import org.openefms.common.models.ml.MlRuntimeStatus;
import org.openefms.frauddetection.service.MlScoringClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/fraud/ml/runtime")
public class MlRuntimeController {
    private final MlScoringClient scoringClient;

    public MlRuntimeController(MlScoringClient scoringClient) {
        this.scoringClient = scoringClient;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public MlRuntimeStatus status() {
        return scoringClient.status();
    }
}
