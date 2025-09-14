package io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;

import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
public class ResearcherState extends AgentState {

    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            Map.entry("researcher_messages", Channels.appender(() -> new ArrayList<ChatMessage>())),
            Map.entry("tool_call_iterations", Channels.base(null, null)),
            Map.entry("research_task", Channels.base(null, null)),
            Map.entry("compressed_research", Channels.base(null, null)),
            Map.entry("raw_notes", Channels.appender(() -> new ArrayList<String>())),

            Map.entry("request_id", Channels.base(null, null)),
            Map.entry("user_id", Channels.base(null, null)),   

            Map.entry("research_start_time", Channels.base(null, null)),
            Map.entry("last_tool_call_time", Channels.base(null, null)),
            Map.entry("current_node_start_time", Channels.base(null, null)),
            Map.entry("error_message", Channels.base(null, null)),
            Map.entry("success", Channels.base(null, null)));

    public ResearcherState(Map<String, Object> initData) {
        super(initData);
    }

    @SuppressWarnings("unchecked")
    public List<ChatMessage> researcherMessages() {
        return this.<List<ChatMessage>>value("researcher_messages").orElse(new ArrayList<>());
    }

    public Integer toolCallIterations() {
        return this.<Integer>value("tool_call_iterations").orElse(0);
    }

    public Optional<String> researchTask() {
        return this.value("research_task");
    }

    public Optional<String> compressedResearch() {
        return this.value("compressed_research");
    }

    @SuppressWarnings("unchecked")
    public List<String> rawNotes() {
        return this.<List<String>>value("raw_notes").orElse(new ArrayList<>());
    }

    public Optional<String> requestId() {
        return this.value("request_id");
    }

    public Optional<String> userId() {
        return this.value("user_id");
    }    

    public boolean isInitialized() {
        return this.researchTask().isPresent() &&
                this.toolCallIterations() >= 0;
    }

    public boolean isResearchComplete() {
        return this.compressedResearch().isPresent() &&
                !this.compressedResearch().get().trim().isEmpty();
    }

    public String getProgressSummary() {
        return String.format(
                "Research Progress: Topic='%s', Iterations=%d, Messages=%d, Notes=%d, Complete=%b",
                researchTask().orElse("N/A"),
                toolCallIterations(),
                researcherMessages().size(),
                rawNotes().size(),
                isResearchComplete());
    }

    public Optional<LocalDateTime> currentNodeStartTime() {
        return this.value("current_node_start_time");
    }

    public Optional<String> errorMessage() {
        return this.value("error_message");
    }

    public Boolean success() {
        return this.<Boolean>value("success").orElse(true);
    }

    public Map<String, Object> markNodeStart() {
        return Map.of("current_node_start_time", LocalDateTime.now());
    }

    public Map<String, Object> setError(String errorMessage) {
        return Map.of(
                "success", false,
                "error_message", errorMessage);
    }

    public static Map<String, Object> createInitialState(String researchTask) {
        return Map.of(
                "research_task", researchTask,
                "tool_call_iterations", 0,
                "research_start_time", java.time.LocalDateTime.now(),
                "success", true);
    }

    public static Map<String, Object> createInitialState(
            String researchTask,
            String requestId,
            String userId) {

        return Map.of(
                "research_task", researchTask,
                "request_id", requestId,
                "user_id", userId,
                "success", true,
                "research_start_time", LocalDateTime.now());
    }

}
