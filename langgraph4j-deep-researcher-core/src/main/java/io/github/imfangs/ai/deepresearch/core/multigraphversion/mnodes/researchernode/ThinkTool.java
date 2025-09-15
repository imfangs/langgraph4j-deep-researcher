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
public class ThinkTool {

    private int searchCount = 0;

    @Tool("think_tool: Strategic reflection tool for research planning and decision making")
    public String thinkTool(@P("reflection") String reflection) {
        searchCount++;
        log.info("Reflection call #{}: {}", searchCount, reflection);

        if (searchCount >= 3) {
            return String.format("Reflection #%d: %s\n\nSuggestion: Multiple searches have been performed, consider calling research_complete_tool to end the task.",
                    searchCount, reflection);
        }

        return String.format("Reflection #%d recorded: %s", searchCount, reflection);
    }
}
