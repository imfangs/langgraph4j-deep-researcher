package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.MainGraphState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt.PromptTemplates;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class WriteResearchBriefNode implements NodeAction<MainGraphState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(MainGraphState state) {
        log.info("📝 Generating research brief");

        try {
            String researchTopic = state.researchTopic().orElse("");
            if (researchTopic == null || researchTopic.isEmpty()) {
                log.warn("No research topic found, unable to generate research brief");
                return Map.of(
                        "research_brief", "Unable to generate research brief: Missing research topic");
            }

            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String prompt = PromptTemplates.formatResearchTopicPrompt(researchTopic, currentDate);

            String researchBrief = chatModel.chat(
                    UserMessage.from(prompt)).aiMessage().text().trim();

            return Map.of(
                    "research_brief", researchBrief);

        } catch (Exception e) {
            log.error("Failed to generate research summary: {}", e.getMessage(), e);
            return Map.of(
                    "research_brief", "Research summary generation failed: " + e.getMessage(),
                    "success", false);
        }
    }

}