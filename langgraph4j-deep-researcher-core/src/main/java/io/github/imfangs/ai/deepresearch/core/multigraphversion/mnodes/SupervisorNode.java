package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt.PromptTemplates;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.SupervisorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SupervisorNode implements NodeAction<SupervisorState> {

    private final ChatModel chatModel;
    private final int maxIterations = 3;

    @Override
    public Map<String, Object> apply(SupervisorState state) {
        log.info("🎯 SupervisorNode start");

        Integer iterations = state.researchIterations();

        if (iterations >= maxIterations) {
            return Map.of(
                    "is_continue", false);
        }

        try {
            String prompt = buildPrompt(state);
            String response = chatModel.chat(
                    SystemMessage.from(PromptTemplates.getSupervisorSystemPrompt()),  
                    UserMessage.from(prompt)).aiMessage().text();

            List<String> nextTasks = parseTasks(response);

            log.info("Parsed {} new tasks: {}", nextTasks.size(), nextTasks);

            if (nextTasks.isEmpty()) {
                return Map.of(
                        "is_continue", false,
                        "success",false,
                        "supervisor_messages", List.of(AiMessage.from("## finish research \n\n Research is sufficient")),
                        "task_list", new ArrayList<>(),
                        "research_iterations", iterations + 1,
                        "last_iteration_time", LocalDateTime.now());
            } else {
                return Map.of(
                        "is_continue", true,
                        "supervisor_messages", List.of(AiMessage.from("## Complete task decomposition \n\n" + formatTasks(nextTasks))),
                        "task_list", nextTasks,
                        "research_iterations", iterations + 1,
                        "last_iteration_time", LocalDateTime.now());
            }

        } catch (Exception e) {
            log.error("Supervisor decision failed: {}", e.getMessage());
            return Map.of(
                    "is_continue", false,
                    "success",false,
                    "supervisor_messages", List.of(AiMessage.from("## finish research \n\n some wrong happened in supervisor: " + e.getMessage())),
                    "task_list", new ArrayList<>(),
                    "research_iterations", iterations + 1,
                    "last_iteration_time", LocalDateTime.now());
        }
    }

    private String buildPrompt(SupervisorState state) {
        StringBuilder prompt = new StringBuilder();

        String researchTopic = state.researchTopic().orElse("");
        String researchBrief=state.researchBrief().orElse("");
        prompt.append("research topic: ").append(researchTopic).append("\n");
        prompt.append("research brief: ").append(researchBrief).append("\n");
        prompt.append("research progress: ").append(state.researchIterations()).append("cycle\n");

        List<ChatMessage> messages = state.supervisorMessages();
        if (messages != null && !messages.isEmpty()) {
            prompt.append("research process:\n");
            for (ChatMessage msg : messages) {
                String content = getMessageText(msg);
                prompt.append("- ").append(content.replace("\n", " ")).append("\n");
            }
        }

        return prompt.toString();
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
                return "TOOL: " + ((ToolExecutionResultMessage) message).text();
            default:
                return "UNKNOWN: " +message.toString();
        }
    }

    private List<String> parseTasks(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(extractJson(response), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("JSON parse failed, using empty list");
            return new ArrayList<>();
        }
    }

    private String extractJson(String response) {
        String text = response.trim();
        if (text.contains("```")) {
            int start = text.indexOf("```") + 3;
            if (text.substring(start).trim().startsWith("json"))
                start += 4;
            int end = text.indexOf("```", start);
            if (end > start)
                text = text.substring(start, end).trim();
        }

        int start = text.indexOf("[");
        int end = text.lastIndexOf("]");
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return "[]";
    }

    private String formatTasks(List<String> tasks) {
        StringBuilder sb = new StringBuilder("next task:\n");
        for (String task : tasks) {
            sb.append("- ").append(task).append("\n");
        }
        return sb.toString();
    }

}