package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.researchernode;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.Result;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
public interface IResearcherAgent {

    Result<String> research(String query);

    @Tool
    String tavilySearchTool(String query);

    @Tool
    String researchCompleteTool(String findings);

    @Tool
    String thinkTool(String reflection);

}
