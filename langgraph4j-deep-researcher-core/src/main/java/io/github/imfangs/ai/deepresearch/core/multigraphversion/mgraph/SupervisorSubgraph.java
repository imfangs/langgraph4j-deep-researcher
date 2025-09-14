package io.github.imfangs.ai.deepresearch.core.multigraphversion.mgraph;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;
import static org.bsc.langgraph4j.action.AsyncEdgeAction.edge_async;
import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;
import java.util.Map;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;
import org.springframework.stereotype.Component;

import io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.SupervisorNode;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.SupervisorToolsNode;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.SupervisorState;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.SupervisorStateSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SupervisorSubgraph {

    private final SupervisorNode supervisorNode;
    private final SupervisorToolsNode supervisorToolsNode;

    public StateGraph<SupervisorState> createResearchGraph() throws GraphStateException {
        log.info("Creating SupervisorSubgraph...");

        StateGraph<SupervisorState> workflow = new StateGraph<>(SupervisorState.SCHEMA, new SupervisorStateSerializer())
                .addNode("supervisor", node_async(supervisorNode))
                .addNode("supervisor_tools", node_async(supervisorToolsNode))
                .addEdge(START, "supervisor")
                .addEdge("supervisor_tools", "supervisor")
                .addConditionalEdges(
                        "supervisor",
                        edge_async(state -> {
                            SupervisorState supervisorState = new SupervisorState(state.data());

                            if (!supervisorState.isContinue()) {
                                return "finalize";
                            }

                            return "continue";
                        }),

                        Map.of(
                                "continue", "supervisor_tools",
                                "finalize", END));

        log.info("SupervisorSubgraph completed");
        return workflow;
    }

    public Map<String, Object> createInitialState(
            String researchTopic, String researchBrief) {

        return SupervisorState.createInitialState(
                researchTopic, researchBrief);
    }

}
