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

import java.util.List;
import java.util.Map;

/**
 * Summarizer node
 * 
 * Responsible for summarizing search results and updating the running summary
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SummarizerNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
        try {
            log.info("📝 Starting to summarize search results");

            // Mark node start
            Map<String, Object> nodeStart = state.markNodeStart();

            List<String> searchResults = state.webSearchResults();
            if (searchResults.isEmpty()) {
                log.warn("No search results available for summarization");
                return Map.of(
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );
            }

            String researchTopic = state.researchTopic()
                .orElseThrow(() -> new IllegalStateException("Missing research topic"));

            // Build prompt
            String systemPrompt = PromptTemplates.SUMMARIZATION_SYSTEM;
            String userMessage = buildUserMessage(state, researchTopic, searchResults);

            log.debug("Summarization system prompt: {}", systemPrompt);
            log.debug("Summarization user message length: {} characters", userMessage.length());

            // Call LLM to generate summary
            String newSummary = chatModel.chat(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userMessage)
            ).aiMessage().text();

            // Increment loop count
            Integer newLoopCount = state.researchLoopCount() + 1;

            log.info("Summarization completed, loop count updated to: {}, summary length: {} characters", 
                newLoopCount, newSummary.length());

            // Add source information to collection list
            List<String> newSources = extractSources(searchResults);

            // Return state updates
            return Map.of(
                "running_summary", newSummary,
                "research_loop_count", newLoopCount,
                "sources_gathered", newSources,
                "current_node_start_time", nodeStart.get("current_node_start_time")
            );

        } catch (Exception e) {
            log.error("Summary generation failed", e);
            return state.setError("Summary generation failed: " + e.getMessage());
        }
    }

    /**
     * Build user message
     */
    private String buildUserMessage(ResearchState state, String researchTopic, List<String> searchResults) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("Research topic: ").append(researchTopic);

        // If there is a previous summary, include it
        String previousSummary = state.runningSummary().orElse("");
        if (!previousSummary.isEmpty()) {
            userMessage.append("\n\nPrevious research summary:\n").append(previousSummary);
        }

        // Add new search results
        userMessage.append("\n\nLatest search results:\n");
        for (int i = 0; i < searchResults.size(); i++) {
            userMessage.append(i + 1).append(". ").append(searchResults.get(i)).append("\n");
        }

        userMessage.append("\nPlease combine the previous summary and new search results to generate a more comprehensive and accurate research summary.");

        return userMessage.toString();
    }

    /**
     * Extract source information from search results
     */
    private List<String> extractSources(List<String> searchResults) {
        return searchResults.stream()
            .map(result -> {
                // Extract URL part as source
                if (result.contains(" - ")) {
                    String[] parts = result.split(" - ", 2);
                    if (parts.length > 0 && parts[0].contains("] ")) {
                        String[] titleUrl = parts[0].split("] ", 2);
                        if (titleUrl.length > 1) {
                            return titleUrl[1]; // Return URL part
                        }
                    }
                }
                return result; // If cannot parse, return original result
            })
            .toList();
    }
}
