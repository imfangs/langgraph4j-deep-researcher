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
 * Reflection node
 * 
 * Responsible for analyzing current summary, identifying knowledge gaps and improvement directions
 * 
 * @author imfangs
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ReflectionNode implements NodeAction<ResearchState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearchState state) {
        try {
            log.info("🤔 Starting reflection analysis");

            // Mark node start
            Map<String, Object> nodeStart = state.markNodeStart();

            String researchTopic = state.researchTopic()
                .orElseThrow(() -> new IllegalStateException("Missing research topic"));

            String currentSummary = state.runningSummary().orElse("");
            if (currentSummary.isEmpty()) {
                log.warn("No summary available for reflection");
                return Map.of(
                    "current_node_start_time", nodeStart.get("current_node_start_time")
                );
            }

            // Build prompt
            String systemPrompt = PromptTemplates.REFLECTION_SYSTEM;
            String userMessage = buildUserMessage(researchTopic, currentSummary, state);

            log.debug("Reflection system prompt: {}", systemPrompt);
            log.debug("Reflection user message length: {} characters", userMessage.length());

            // Call LLM for reflection analysis
            // String reflectionResult = chatModel.chat(userMessage);
            String reflectionResult = chatModel.chat(
                SystemMessage.from(systemPrompt),
                UserMessage.from(userMessage)
            ).aiMessage().text();
            
            log.info("Reflection analysis completed, result length: {} characters", reflectionResult.length());
            log.debug("Reflection result: {}", reflectionResult);

            // Analyze reflection result, decide if more research is needed
            boolean needMoreResearch = analyzeReflectionResult(reflectionResult);
            
            log.info("Reflection conclusion: {}", needMoreResearch ? "Need more research" : "Information is relatively complete");

            // Return state updates (reflection results can be stored in metadata)
            return Map.of(
                "metadata", Map.of(
                    "last_reflection", reflectionResult,
                    "need_more_research", needMoreResearch,
                    "reflection_timestamp", System.currentTimeMillis()
                ),
                "current_node_start_time", nodeStart.get("current_node_start_time")
            );

        } catch (Exception e) {
            log.error("Reflection analysis failed", e);
            return state.setError("Reflection analysis failed: " + e.getMessage());
        }
    }

    /**
     * Build user message
     */
    private String buildUserMessage(String researchTopic, String currentSummary, ResearchState state) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append("Research topic: ").append(researchTopic);
        userMessage.append("\n\nCurrent loop count: ").append(state.researchLoopCount());
        userMessage.append("/").append(state.maxResearchLoops());
        
        userMessage.append("\n\nCurrent research summary:\n").append(currentSummary);

        // Add count of collected source information
        int sourcesCount = state.sourcesGathered().size();
        userMessage.append("\n\nNumber of collected source information: ").append(sourcesCount);

        userMessage.append("\n\nPlease analyze the completeness and accuracy of the current summary, identify possible knowledge gaps or information that needs to be supplemented.");

        return userMessage.toString();
    }

    /**
     * Analyze reflection result, determine if more research is needed
     */
    private boolean analyzeReflectionResult(String reflectionResult) {
        if (reflectionResult == null || reflectionResult.trim().isEmpty()) {
            return false;
        }

        String lowerResult = reflectionResult.toLowerCase();

        // Look for keywords indicating need for more information
        String[] needMoreKeywords = {
            "需要更多", "缺少", "不足", "不完整", "需要补充", "需要进一步",
            "更深入", "更详细", "gap", "missing", "incomplete", "need more",
            "further research", "additional information"
        };

        for (String keyword : needMoreKeywords) {
            if (lowerResult.contains(keyword)) {
                return true;
            }
        }

        // Look for keywords indicating sufficient information
        String[] sufficientKeywords = {
            "充足", "完整", "全面", "足够", "完善", "sufficient", "complete",
            "comprehensive", "adequate", "thorough"
        };

        for (String keyword : sufficientKeywords) {
            if (lowerResult.contains(keyword)) {
                return false;
            }
        }

        // By default, tend to think more research is needed (unless explicitly stated as sufficient)
        return true;
    }
}
