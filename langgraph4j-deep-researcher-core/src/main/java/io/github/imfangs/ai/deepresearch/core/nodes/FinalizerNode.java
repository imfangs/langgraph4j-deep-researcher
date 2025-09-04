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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Finalizer node
 * 
 * Responsible for generating final research report and summary
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FinalizerNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
            try {
                log.info("🏁 Starting to generate final research report");

                // Mark node start
                Map<String, Object> nodeStart = state.markNodeStart();

                String researchTopic = state.researchTopic()
                    .orElseThrow(() -> new IllegalStateException("Missing research topic"));

                String currentSummary = state.runningSummary().orElse("No research summary generated");

                // Build prompt
                String systemPrompt = PromptTemplates.FINALIZATION_SYSTEM;
                String userMessage = buildUserMessage(researchTopic, currentSummary, state);

                log.debug("Finalization system prompt: {}", systemPrompt);
                log.debug("Finalization user message length: {} characters", userMessage.length());

                // Call LLM to generate final report
                // String finalSummary = chatModel.chat(userMessage);
                String finalSummary = chatModel.chat(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userMessage)
                ).aiMessage().text();
                
                log.info("Final research report generation completed, length: {} characters", finalSummary.length());

                // Calculate execution statistics
                LocalDateTime endTime = LocalDateTime.now();
                long totalDuration = state.getTotalDuration();
                int totalLoops = state.researchLoopCount();
                int totalSources = state.sourcesGathered().size();

                log.info("Research completion statistics - Loop count: {}, Source count: {}, Total duration: {}ms", 
                    totalLoops, totalSources, totalDuration);

                // Return final state updates
                return Map.of(
                    "running_summary", finalSummary,
                    "success", true,
                    "metadata", Map.of(
                        "final_report_generated", true,
                        "completion_timestamp", System.currentTimeMillis(),
                        "total_duration_ms", totalDuration,
                        "total_loops_completed", totalLoops,
                        "total_sources_gathered", totalSources,
                        "final_summary_length", finalSummary.length()
                    ),
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );

            } catch (Exception e) {
                log.error("Finalization processing failed", e);
                return state.setError("Finalization processing failed: " + e.getMessage());
            }
    }

    /**
     * Build user message
     */
    private String buildUserMessage(String researchTopic, String currentSummary, ResearchState state) {
        StringBuilder userMessage = new StringBuilder();
        
        // Basic information
        userMessage.append("Research topic: ").append(researchTopic);
        userMessage.append("\nCompleted loop count: ").append(state.researchLoopCount());
        userMessage.append("\nCollected source count: ").append(state.sourcesGathered().size());
        
        // Execution statistics
        long duration = state.getTotalDuration();
        if (duration > 0) {
            userMessage.append("\nTotal execution time: ").append(formatDuration(duration));
        }

        // Current summary
        userMessage.append("\n\nResearch summary:\n").append(currentSummary);

        // Source information list
        List<String> sources = state.sourcesGathered();
        if (!sources.isEmpty()) {
            userMessage.append("\n\nReference sources:\n");
            for (int i = 0; i < sources.size() && i < 10; i++) { // Show at most 10 sources
                userMessage.append(i + 1).append(". ").append(sources.get(i)).append("\n");
            }
            if (sources.size() > 10) {
                userMessage.append("... (Total ").append(sources.size()).append(" sources)\n");
            }
        }

        userMessage.append("\nPlease generate a professional and complete final research report with clear structure and conclusions.");

        return userMessage.toString();
    }

    /**
     * Format duration
     */
    private String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        if (seconds < 60) {
            return seconds + " seconds";
        } else {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + " minutes " + remainingSeconds + " seconds";
        }
    }
}
