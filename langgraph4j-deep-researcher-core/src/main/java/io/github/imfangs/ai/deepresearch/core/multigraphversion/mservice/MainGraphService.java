package io.github.imfangs.ai.deepresearch.core.multigraphversion.mservice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.github.imfangs.ai.deepresearch.api.dto.ResearchRequest;
import io.github.imfangs.ai.deepresearch.api.dto.ResearchResponse;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mgraph.MainGraph;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.MainGraphState;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@Service
public class MainGraphService {

    private final MainGraph maingraph;

    @Autowired
    public MainGraphService(MainGraph maingraph) {
        this.maingraph = maingraph;
    }

    public ResearchResponse executeResearch(ResearchRequest request) {
        String requestId = request.getRequestId() != null ? 
                request.getRequestId() : UUID.randomUUID().toString();

        log.info("🚀 Starting deep research execution, request ID: {}, research topic: {}", requestId, request.getResearchTopic());

        LocalDateTime startTime = LocalDateTime.now();
        
        try {

            log.info("📊 Building research state graph...");
            var researchGraph = maingraph.createResearchGraph();

            log.info("⚙️ Compiling research graph...");
            CompileConfig compileConfig = CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .build();
            
            CompiledGraph<MainGraphState> compiledGraph = researchGraph.compile(compileConfig);

            Map<String, Object> initialState = maingraph.createInitialState(
                request.getResearchTopic(), requestId, request.getUserId()
            );

            RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId(requestId)
                .build();

            log.info("🎯 Starting research graph execution, initial state: {}", initialState.keySet());

            MainGraphState finalState = null;
            int nodeCount = 0;
            
            for (var nodeOutput : compiledGraph.stream(initialState, runnableConfig)) {
                nodeCount++;
                finalState = nodeOutput.state();
                
                boolean isSuccess = finalState.success();
                
                if (!isSuccess) {
                    String errorMsg = finalState.errorMessage().orElse("Unknown error");
                    log.warn("⚠️ Error occurred during research: {}", errorMsg);
                    break;
                }
                
                if (nodeCount > 50) {
                    log.warn("⚠️ Too many node executions, forcing exit");
                    break;
                }
            }

            if (finalState == null) {
                throw new IllegalStateException("Graph execution did not return any state");
            }

            log.info("✅ Research graph execution completed, executed {} nodes", nodeCount);
            log.info("Final research report: {}", finalState.finalReport().orElse("No report generated"));

            return buildSuccessResponse(request, requestId, finalState, startTime);

        } catch (GraphStateException e) {
            log.error("❌ Graph state exception, request ID: " + requestId, e);
            return buildErrorResponse(request, requestId, "Graph state exception: " + e.getMessage(), startTime);
        } catch (Exception e) {
            log.error("❌ Deep research execution failed, request ID: " + requestId, e);
            return buildErrorResponse(request, requestId, "Research execution failed: " + e.getMessage(), startTime);
        }
    }

    private ResearchResponse buildSuccessResponse(
            ResearchRequest request, 
            String requestId, 
            MainGraphState finalState, 
            LocalDateTime startTime) {

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        String finalSummary = finalState.finalReport().orElse("Research failed to generate summary");
        Boolean success = finalState.success();

        return ResearchResponse.builder()
                .requestId(requestId)
                .researchTopic(request.getResearchTopic())
                .finalSummary(finalSummary)
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .success(success)
                .status(success ? ResearchResponse.ResearchStatus.COMPLETED : ResearchResponse.ResearchStatus.FAILED)
                .errorMessage(finalState.errorMessage().orElse(null))
                .build();
    }

    private ResearchResponse buildErrorResponse(
            ResearchRequest request, 
            String requestId, 
            String errorMessage, 
            LocalDateTime startTime) {

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        return ResearchResponse.builder()
                .requestId(requestId)
                .researchTopic(request.getResearchTopic())
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .success(false)
                .errorMessage(errorMessage)
                .status(ResearchResponse.ResearchStatus.FAILED)
                .sourcesGathered(List.of())
                .actualLoops(0)
                .build();
    }
}
