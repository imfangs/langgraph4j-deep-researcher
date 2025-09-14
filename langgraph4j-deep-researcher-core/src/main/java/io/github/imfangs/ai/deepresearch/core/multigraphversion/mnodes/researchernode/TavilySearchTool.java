/*
 * @Atuthor: zoey333
 */
package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.researchernode;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import io.github.imfangs.ai.deepresearch.config.ResearchConfig;
import io.github.imfangs.ai.deepresearch.tools.search.SearchEngineManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.P;
import java.util.List;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Component
@Slf4j
public class TavilySearchTool {

    private final SearchEngineManager searchEngineManager;
    private final ResearchConfig researchConfig;

    @Autowired
    public TavilySearchTool(SearchEngineManager searchEngineManager, ResearchConfig researchConfig) {
        this.searchEngineManager = searchEngineManager;
        this.researchConfig = researchConfig;
    }

    @Tool("tavily_search_tool: Perform web search using the default search engine.")
    public String tavilySearchTool(@P("query") String query) {
        try {
            log.info("Execute default search engine search: query={}", query);

            List<SearchResult> searchResults = searchEngineManager.searchWithDefault(query);

            if (searchResults.isEmpty()) {
                return "No relevant search results found";
            }

            String formattedResults = searchEngineManager.formatSearchResults(
                    searchResults,
                    researchConfig.getFlow().getMaxTokensPerSource());

            log.info("Default search engine search completed, obtained {} results", searchResults.size());
            return formattedResults;

        } catch (Exception e) {
            log.error("Default search engine search execution failed: query=" + query, e);
            return "Search execution failed: " + e.getMessage();
        }
    }
}