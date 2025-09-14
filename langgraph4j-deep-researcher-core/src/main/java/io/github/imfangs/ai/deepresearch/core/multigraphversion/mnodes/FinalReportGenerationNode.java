package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt.PromptTemplates;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.MainGraphState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class FinalReportGenerationNode implements NodeAction<MainGraphState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(MainGraphState state) {
        log.info("📄 Generating final research report");

        try {
            List<String> notes = state.notes();
            String findings = String.join("\n", notes);

            String researchBrief = state.researchBrief().orElse("");

            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            String prompt = PromptTemplates.formatFinalReportGenerationPrompt(
                    researchBrief, findings, currentDate);

            String finalReport = chatModel.chat(
                    UserMessage.from(prompt)).aiMessage().text().trim();

            return Map.of(
                    "final_report", finalReport,
                    "notes", new ArrayList<String>());

        } catch (Exception e) {
            log.error("Failed to generate final report: {}", e.getMessage(), e);
            return Map.of(
                    "final_report", "Final report generation failed: " + e.getMessage(),
                    "success", false);
        }
    }

}