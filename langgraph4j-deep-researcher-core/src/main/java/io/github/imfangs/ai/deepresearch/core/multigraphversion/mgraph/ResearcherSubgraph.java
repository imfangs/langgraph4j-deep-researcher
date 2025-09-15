package io.github.imfangs.ai.deepresearch.core.multigraphversion.mgraph;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import java.util.Map;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.CompressNode;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.researchernode.ResearcherNode;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.ResearcherState;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.ResearcherStateSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ResearcherSubgraph {

    private final ResearcherNode researchernode;
    private final CompressNode compressnode;

    public StateGraph<ResearcherState> createResearchGraph() throws GraphStateException {
        log.info("Creating ResearcherSubgraph...");

        StateGraph<ResearcherState> workflow = new StateGraph<>(ResearcherState.SCHEMA, new ResearcherStateSerializer())
                .addNode("researcher", node_async(researchernode))
                .addNode("compress", node_async(compressnode))
                .addEdge(START, "researcher")
                .addEdge("researcher", "compress")
                .addEdge("compress", END);

        log.info("ResearcherSubgraph creation completed");
        return workflow;
    }

    public Map<String, Object> createInitialState(
            String researchTask,
            String requestId,
            String userId) {

        return ResearcherState.createInitialState(
            researchTask,
                requestId,
                userId);
    }

}
