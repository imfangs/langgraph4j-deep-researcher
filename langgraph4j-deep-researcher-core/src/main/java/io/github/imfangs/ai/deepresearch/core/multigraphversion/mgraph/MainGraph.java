package io.github.imfangs.ai.deepresearch.core.multigraphversion.mgraph;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import java.util.Map;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.FinalReportGenerationNode;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.SupervisorBridgeNode;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.WriteResearchBriefNode;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.MainGraphSerializer;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.MainGraphState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class MainGraph {
    private final WriteResearchBriefNode writeResearchBriefNode;
    private final SupervisorBridgeNode supervisorBridgeNode;
    private final FinalReportGenerationNode finalReportGenerationNode;

    public StateGraph<MainGraphState> createResearchGraph() throws GraphStateException {
        log.info("Creating deep research main graph...");

        StateGraph<MainGraphState> workflow = new StateGraph<>(MainGraphState.SCHEMA, new MainGraphSerializer())
                .addNode("writeResearchBrief", node_async(writeResearchBriefNode))
                .addNode("supervisorBridge", node_async(supervisorBridgeNode))
                .addNode("finalReportGeneration", node_async(finalReportGenerationNode))
                .addEdge(START, "writeResearchBrief")
                .addEdge("writeResearchBrief", "supervisorBridge")
                .addEdge("supervisorBridge", "finalReportGeneration")
                .addEdge("finalReportGeneration", END);

        log.info("Main graph creation completed");
        return workflow;
    }

    public Map<String, Object> createInitialState(String researchTopic, String requestId, String userId) {

        return MainGraphState.createInitialState(researchTopic, requestId, userId);
    }
}
