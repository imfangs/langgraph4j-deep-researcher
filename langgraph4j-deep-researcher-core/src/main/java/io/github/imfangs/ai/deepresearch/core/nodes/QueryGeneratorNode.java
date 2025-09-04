package io.github.imfangs.ai.deepresearch.core.nodes;

import io.github.imfangs.ai.deepresearch.api.state.ResearchState;
import io.github.imfangs.ai.deepresearch.config.PromptTemplates;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Query generator node
 * 
 * Responsible for generating query statements for web search
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class QueryGeneratorNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
        try {
            log.info("🔍 Starting to generate search query, loop count: {}", state.researchLoopCount());

            // Mark node start
            Map<String, Object> nodeStart = state.markNodeStart();

            String researchTopic = state.researchTopic()
                .orElseThrow(() -> new IllegalStateException("Missing research topic"));

            // Build prompt
            String systemPrompt = PromptTemplates.QUERY_GENERATION_SYSTEM;
            String userMessage = buildUserMessage(state, researchTopic);

            log.debug("System prompt: {}", systemPrompt);
            log.debug("User message: {}", userMessage);

            // Call LLM to generate query
            // String response = chatModel.chat(userMessage);
            String response = chatModel.chat(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userMessage)
            ).aiMessage().text();
            
            // Clean response, extract actual search query
            String searchQuery = cleanResponse(response);
            log.info("Generated search query: {}", searchQuery);

            // Return state updates
            return Map.of(
                "search_query", searchQuery,
                "current_node_start_time", nodeStart.get("current_node_start_time")
            );

        } catch (Exception e) {
            log.error("Query generation failed", e);
            return state.setError("Query generation failed: " + e.getMessage());
        }
    }

    /**
     * Build user message
     */
    private String buildUserMessage(ResearchState state, String researchTopic) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("Research topic: ").append(researchTopic);

        // If it's a subsequent loop, include previous summary
        if (state.researchLoopCount() > 0) {
            String previousSummary = state.runningSummary().orElse("");
            if (!previousSummary.isEmpty()) {
                userMessage.append("\n\nCurrent research progress:\n").append(previousSummary);
            }

            userMessage.append("\n\nPlease generate a new search query based on existing information to conduct in-depth research or supplement missing information.");
        } else {
            userMessage.append("\n\nThis is the first search, please generate a comprehensive search query to start the research.");
        }

        return userMessage.toString();
    }

    /**
     * Clean LLM response, extract search query
     */
    private String cleanResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalStateException("LLM returned empty response");
        }

        // Remove possible formatting markers
        String cleaned = response.trim()
            .replaceFirst("^搜索查询[:：]?\\s*", "")
            .replaceFirst("^查询[:：]?\\s*", "")
            .replaceFirst("^Query[:：]?\\s*", "")
            .replaceAll("^[\"']|[\"']$", ""); // Remove quotes

        // Take first line as query (prevent LLM from returning multiple lines)
        String[] lines = cleaned.split("\n");
        String query = lines[0].trim();

        if (query.isEmpty()) {
            throw new IllegalStateException("Unable to extract valid query from LLM response");
        }

        return query;
    }
}
