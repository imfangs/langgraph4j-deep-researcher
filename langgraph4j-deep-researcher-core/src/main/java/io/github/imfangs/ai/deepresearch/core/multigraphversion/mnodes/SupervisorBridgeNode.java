package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.stereotype.Component;

import io.github.imfangs.ai.deepresearch.core.multigraphversion.mgraph.SupervisorSubgraph;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.MainGraphState;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.SupervisorState;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SupervisorBridgeNode implements NodeAction<MainGraphState> {

    private final SupervisorSubgraph supervisorSubgraph;
    private StateGraph<SupervisorState> cachedSupervisorGraph;
    
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing cached SupervisorSubgraph...");
            this.cachedSupervisorGraph = supervisorSubgraph.createResearchGraph();
            log.info("SupervisorSubgraph cached successfully");
        } catch (Exception e) {
            log.error("Failed to initialize cached SupervisorSubgraph", e);
            throw new RuntimeException("Failed to initialize SupervisorSubgraph", e);
        }
    }    

    @Override
    public Map<String, Object> apply(MainGraphState state) {
        log.debug("Starting supervisor data bridge for research brief: {}",
                state.researchBrief().orElse(null));

        String researchTopic = state.researchTopic().orElse(null);
        String researchBrief = state.researchBrief().orElse(null);

        Map<String, Object> supervisorInitialState = supervisorSubgraph.createInitialState(researchTopic,
                researchBrief);

        try {
            SupervisorState finalState = executeSupervisorGraph(supervisorInitialState);

            if (finalState.success()) {
                log.info("Supervisor subgraph execution completed successfully");
                log.info("Notes count: {}, Research iterations: {}",
                        finalState.notes().size(), finalState.researchIterations());

                Map<String, Object> result = Map.of(
                        "notes", finalState.notes(),
                        "raw_notes", finalState.rawNotes(),
                        "supervisor_messages", finalState.supervisorMessages());

                return result;
            } else {
                String errorMessage = finalState.errorMessage().orElse("Unknown error");
                log.error("Supervisor subgraph execution failed: {}", errorMessage);
                return Map.of(
                        "notes", List.of("Research execution failed: " + errorMessage),
                        "final_report", "An error occurred during execution: " + errorMessage,
                        "success", false);
            }

        } catch (Exception e) {
            log.error("Supervisor data bridge execution failed", e);
            return Map.of(
                    "notes", List.of("Data transfer failed: " + e.getMessage()),
                    "final_report", "An error occurred during execution: " + e.getMessage(),
                    "success", false);
        }
    }

    private SupervisorState executeSupervisorGraph(Map<String, Object> initialState) throws Exception {
        CompileConfig compileConfig = CompileConfig.builder()
                .checkpointSaver(new MemorySaver())
                .build();
        CompiledGraph<SupervisorState> compiledGraph = cachedSupervisorGraph.compile(compileConfig);

        RunnableConfig runnableConfig = RunnableConfig.builder()
                .threadId("supervisor-bridge-" + UUID.randomUUID().toString())
                .build();

        SupervisorState finalState = null;
        for (var nodeOutput : compiledGraph.stream(initialState, runnableConfig)) {
            finalState = nodeOutput.state();
            if (!finalState.success())
                break;
        }

        return finalState;
    }

}