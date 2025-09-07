package io.github.imfangs.ai.deepresearch.core.agui;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import io.github.imfangs.ai.deepresearch.core.graph.ResearchGraphBuilder;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.NodeOutput;
import org.bsc.langgraph4j.action.InterruptionMetadata;
import org.bsc.langgraph4j.agui.AGUILangGraphAgent;
import org.bsc.langgraph4j.agui.AGUIMessage;
import org.bsc.langgraph4j.agui.AGUIType;
import org.bsc.langgraph4j.state.AgentState;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Component("AGUIAgent")
public class AGUIDeepResearchAgent extends AGUILangGraphAgent {
    //private final ResearchConfig researchConfig;
    private final ResearchGraphBuilder graphBuilder;

    protected AGUIDeepResearchAgent( ResearchGraphBuilder graphBuilder) {
        // this.researchConfig = researchConfig;
        this.graphBuilder = graphBuilder;
    }

    @Override
    protected GraphData buildStateGraph() throws GraphStateException {

        var researchGraph = graphBuilder.createResearchGraph();

        var compiledGraph = researchGraph.compile();

        compiledGraph.setMaxIterations(50);

        return new GraphData(researchGraph.compile());
    }

    @Override
    protected Map<String, Object> buildGraphInput(AGUIType.RunAgentInput runAgentInput) {

        var researchTopic = runAgentInput.lastUserMessage()
                .map(AGUIMessage.TextMessage::content)
                .orElseThrow( () -> new IllegalStateException("last user message not found"));

        var userId = "test";
        var maxResearchLoops = 3;
        var searchEngine = "tavily";
        var maxSearchResults = 3;
        var fetchFullPage = true;

        return graphBuilder.createInitialState(
                researchTopic,
                runAgentInput.threadId(),
                userId,
                maxResearchLoops,
                searchEngine,
                maxSearchResults,
                fetchFullPage
        );
    }

    @Override
    protected <State extends AgentState> List<Approval> onInterruption(AGUIType.RunAgentInput runAgentInput, InterruptionMetadata<State> interruptionMetadata) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    Set<String> searchResultCache = new HashSet<>();

    @Override
    protected Optional<String> nodeOutputToText(NodeOutput<? extends AgentState> nodeOutput) {
        if( nodeOutput.isSTART() ) {
            return Optional.empty();
        }
        if( nodeOutput.isEND() ) {
            searchResultCache.clear();
            if( nodeOutput.state() instanceof ResearchState state ) {
                return state.runningSummary();
            }
            return Optional.empty();
        }
        if( nodeOutput.state() instanceof ResearchState state ) {

            if( state.detailedSearchResults().isEmpty() ) {
                return Optional.empty();
            }

            var cacheDiffs = state.detailedSearchResults().stream()
                                    .map(SearchResult::getUrl)
                                    .filter(url -> !searchResultCache.contains(url))
                                    .toList()
                                    ;
            if(cacheDiffs.isEmpty()) {
                return Optional.empty();
            }

            var result =  state.detailedSearchResults().stream()
                    .filter( r -> !searchResultCache.contains(r.getUrl()) )
                    .map( detail -> {
                    var decodedUrl = URLDecoder.decode(detail.getUrl(), StandardCharsets.UTF_8);
                    return format( "* %s - %s", detail.getTitle(), decodedUrl );
                })
                .collect(Collectors.joining("\n"));

            searchResultCache.addAll(cacheDiffs);

            return Optional.of( format( "*Searching in* ....\n%s",result) );

        }

        return Optional.empty();
    }
}
