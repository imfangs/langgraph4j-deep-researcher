package io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bsc.langgraph4j.state.AgentState;
import org.bsc.langgraph4j.state.Channel;
import org.bsc.langgraph4j.state.Channels;
import org.bsc.langgraph4j.state.Reducer;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
public class SupervisorState extends AgentState {

    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            Map.entry("research_topic", Channels.base(null, null)),
            Map.entry("research_brief", Channels.base(null, null)),        
            Map.entry("supervisor_messages", Channels.appender(() -> new ArrayList<ChatMessage>())),
            Map.entry("notes", Channels.appender(() -> new ArrayList<String>())),
            Map.entry("raw_notes", Channels.appender(() -> new ArrayList<String>())),
            Map.entry("task_list", Channels.base((Reducer<List<String>>) (oldValue, newValue) -> newValue)),

            Map.entry("is_continue", Channels.base(null, null)),
            Map.entry("research_iterations", Channels.base(null, null)),            
            Map.entry("supervision_start_time", Channels.base(null, null)),
            Map.entry("current_node_start_time", Channels.base(null, null)),
            Map.entry("last_iteration_time", Channels.base(null, null)),

            Map.entry("error_message", Channels.base(null, null)),
            Map.entry("success", Channels.base(null, null))

    );

    public SupervisorState(Map<String, Object> initData) {
        super(initData);
    }

    public Optional<String> researchTopic() {
        return this.value("research_topic");
    }

    public Optional<String> researchBrief() {
        return this.value("research_brief");
    }

    public Boolean isContinue() {
        return this.<Boolean>value("is_continue").orElse(true);
    }

    @SuppressWarnings("unchecked")
    public List<ChatMessage> supervisorMessages() {
        return this.<List<ChatMessage>>value("supervisor_messages").orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public List<String> taskList() {
        return this.<List<String>>value("task_list").orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public List<String> notes() {
        return this.<List<String>>value("notes").orElse(new ArrayList<>());
    }

    public Integer researchIterations() {
        return this.<Integer>value("research_iterations").orElse(0);
    }

    @SuppressWarnings("unchecked")
    public List<String> rawNotes() {
        return this.<List<String>>value("raw_notes").orElse(new ArrayList<>());
    }


    public Optional<LocalDateTime> supervisionStartTime() {
        return this.value("supervision_start_time");
    }

    public Optional<LocalDateTime> lastIterationTime() {
        return this.value("last_iteration_time");
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

    public Map<String, Object> markIteration() {
        return Map.of(
                "last_iteration_time", LocalDateTime.now(),
                "research_iterations", this.researchIterations() + 1);
    }

    public Map<String, Object> setError(String errorMessage) {
        return Map.of(
                "success", false,
                "error_message", errorMessage);
    }

    public boolean isSupervisionComplete() {
        return this.success() && this.researchIterations() > 0;
    }

    public String getProgressSummary() {
        return String.format(
                "Supervision Progress: Brief='%s', Iterations=%d, Messages=%d, Notes=%d, RawNotes=%d, Complete=%b",
                researchIterations(),
                supervisorMessages().size(),
                notes().size(),
                rawNotes().size(),
                isSupervisionComplete());
    }

    public static Map<String, Object> createInitialState(
            String researchTopic, String researchBrief) {

        return Map.of(
                "research_topic", researchTopic,
                "research_brief", researchBrief,
                "is_continue", true,
                "supervision_start_time", LocalDateTime.now());
    }
}
