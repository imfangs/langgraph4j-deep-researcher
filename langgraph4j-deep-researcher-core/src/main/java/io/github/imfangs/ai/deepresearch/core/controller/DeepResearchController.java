package io.github.imfangs.ai.deepresearch.core.controller;

import io.github.imfangs.ai.deepresearch.api.dto.ApiResponse;
import io.github.imfangs.ai.deepresearch.api.dto.ResearchRequest;
import io.github.imfangs.ai.deepresearch.api.dto.ResearchResponse;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mservice.MainGraphService;
import io.github.imfangs.ai.deepresearch.core.service.DeepResearchService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * Deep research controller
 * 
 * Provides REST API interfaces for deep research
 * 
 * @author imfangs
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/research")
@CrossOrigin(origins = "*")
public class DeepResearchController {

    private final DeepResearchService deepResearchService;
    private final MainGraphService mainGraphService;

    @Autowired
    public DeepResearchController(DeepResearchService deepResearchService, MainGraphService mainGraphService) {
        this.deepResearchService = deepResearchService;
        this.mainGraphService=mainGraphService;
    }

    /**
     * Execute deep research
     * 
     * @param request Research request
     * @return Research response
     */
    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<ResearchResponse>> executeResearch(
            @Valid @RequestBody ResearchRequest request) {
        
        log.info("Received deep research request, topic: {}, user ID: {}", 
                request.getResearchTopic(), request.getUserId());
        
        try {
            ResearchResponse response = deepResearchService.executeResearch(request);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(response, "Research execution successful"));
            } else {
                return ResponseEntity.ok(ApiResponse.error(
                        500, 
                        response.getErrorMessage(), 
                        response.getRequestId()));
            }
            
        } catch (Exception e) {
            log.error("Deep research execution exception", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Internal system error: " + e.getMessage()));
        }
    }

    @PostMapping("/executemultigraph")
    public ResponseEntity<ApiResponse<ResearchResponse>> executeMultiGraph(
            @Valid @RequestBody ResearchRequest request) {
        
        log.info("Received deep research request, topic: {}, user ID: {}", 
                request.getResearchTopic(), request.getUserId());
        
        try {
            ResearchResponse response = mainGraphService.executeResearch(request);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(ApiResponse.success(response, "Research execution successful"));
            } else {
                return ResponseEntity.ok(ApiResponse.error(
                        500, 
                        response.getErrorMessage(), 
                        response.getRequestId()));
            }
            
        } catch (Exception e) {
            log.error("Deep research execution exception", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "Internal system error: " + e.getMessage()));
        }
    }


    /**
     * Health check endpoint
     * 
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("OK", "Deep research service running normally"));
    }

}