package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.researchernode;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Component
@Slf4j
public class ResearchCompleteTool {

    @Tool("research_complete_tool: Terminate the current task and provide the final answer. Call this tool when the task is completed or no further progress can be made.")
    public String researchCompleteTool(@P("reason") String reason) {
        String message = reason != null && !reason.trim().isEmpty() ? reason : "The task was terminated by the agent.";

        log.info("The tool 'research_complete_tool' was called, reason: {}", message);

        return "Execution has been terminated. Reason:" + message;
    }
}
