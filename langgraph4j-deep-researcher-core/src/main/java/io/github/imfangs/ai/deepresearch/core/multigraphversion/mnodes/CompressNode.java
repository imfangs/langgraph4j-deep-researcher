package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes;

import java.util.List;
import java.util.Map;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt.PromptTemplates;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.ResearcherState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class CompressNode implements NodeAction<ResearcherState> {

    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(ResearcherState state) {
        try {
            log.info("🗜️ Starting CompressNode");

            Map<String, Object> nodeStart = state.markNodeStart();

            List<ChatMessage> researcherMessages = state.researcherMessages();

            if (researcherMessages == null || researcherMessages.isEmpty()) {
                log.warn("No researcher messages available for compression");
                return Map.of(
                        "current_node_start_time", nodeStart.get("current_node_start_time"));
            }

            String systemPrompt = PromptTemplates.formatCompressResearchSystemPrompt(
                PromptTemplates.getCurrentDate());
            String userMessage = buildUserMessage(researcherMessages);

            String compressedResearch = chatModel.chat(
                    SystemMessage.from(systemPrompt),
                    UserMessage.from(userMessage)).aiMessage().text();

            String rawNotesContent = extractRawNotes(researcherMessages);

            log.info("Compression completed, compressed research length: {} characters",
                    compressedResearch.length());

            return Map.of(
                    "compressed_research", compressedResearch,
                    "raw_notes", List.of(rawNotesContent),
                    "current_node_start_time", nodeStart.get("current_node_start_time"));

        } catch (Exception e) {
            log.error("Research compression failed", e);
            return state.setError("Research compression failed: " + e.getMessage());
        }
    }

    private String buildUserMessage(List<ChatMessage> researcherMessages) {
        StringBuilder userMessage = new StringBuilder(PromptTemplates.getCompressResearchHumanMessage());
        userMessage.append("\n\n--- Research Messages ---\n");

        for (ChatMessage message : researcherMessages) {
            if (message != null) {
                String messageContent = getMessageText(message);
                userMessage.append(messageContent).append("\n");
            }
        }

        return userMessage.toString();
    }

    private String extractRawNotes(List<ChatMessage> researcherMessages) {
        StringBuilder rawNotes = new StringBuilder();

        for (ChatMessage message : researcherMessages) {
            if (message != null) {
                String messageContent = getMessageText(message);
                rawNotes.append(messageContent).append("\n");
            }
        }

        return rawNotes.toString();
    }

    private String getMessageText(ChatMessage message) {
        switch (message.type()) {
            case SYSTEM:
                return "SYSTEM: " + ((SystemMessage) message).text();
            case USER:
                return "USER: " + ((UserMessage) message).contents().toString();
            case AI:
                return "AI: " + ((AiMessage) message).text();
            case TOOL_EXECUTION_RESULT:
                return "TOOL_EXECUTION_RESULT: " + ((ToolExecutionResultMessage) message).text();
            default:
                return message.toString();
        }
    }


}