package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes.researchernode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.ResearcherState;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt.PromptTemplates;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ResearcherNode implements NodeAction<ResearcherState> {

    private final ChatModel chatModel;
    private final TavilySearchTool searchTool;
    private final ResearchCompleteTool completeResearchTool;
    private final ThinkTool thinkTool;

    @Override
    public Map<String, Object> apply(ResearcherState state) {
        try {
            log.info("🔍 Starting researcher with AiServices React iteration");
            log.info("current prompt language: "+PromptTemplates.getCurrentLanguage());

            Map<String, Object> nodeStart = state.markNodeStart();

            List<ChatMessage> researcherMessages = state.researcherMessages();
            if (researcherMessages == null) {
                researcherMessages = new ArrayList<>();
            }

            String researchTask = state.researchTask().orElse("");

            log.info("📖 Read from state - messages: {}, task: {}",
                    researcherMessages.size(), researchTask);

            IResearcherAgent agent = AiServices.builder(IResearcherAgent.class)
                    .chatModel(chatModel)
                    .tools(searchTool, completeResearchTool, thinkTool)
                    .maxSequentialToolsInvocations(10)
                    .systemMessageProvider(chatMemoryId -> PromptTemplates.getResearcherSystemPrompt())
                    .build();

            Result<String> result = agent.research(researchTask);

            List<ChatMessage> updatedMessages = new ArrayList<>(researcherMessages);
            updatedMessages.add(AiMessage.from(result.content()));
            int toolExecutionCount = result.toolExecutions() != null ? result.toolExecutions().size() : 0;

            log.info("Research completed, total tool executions: {}, research result: {}",
            toolExecutionCount, result.content());

            return Map.of(
                    "researcher_messages", updatedMessages,
                    "tool_call_iterations", toolExecutionCount,
                    "current_node_start_time", nodeStart.get("current_node_start_time"));

        } catch (Exception e) {
            log.error("❌ Researcher execution failed", e);
            return state.setError("Researcher execution failed: " + e.getMessage());
        }
    }

}