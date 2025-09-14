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

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
public class MainGraphState extends AgentState {

    public static final Map<String, Channel<?>> SCHEMA = Map.ofEntries(
            Map.entry("supervisor_messages", Channels.appender(() -> new ArrayList<ChatMessage>())),
            Map.entry("research_brief", Channels.base(null, null)),
            Map.entry("raw_notes", Channels.appender(() -> new ArrayList<String>())),
            Map.entry("notes", Channels.appender(() -> new ArrayList<String>())),
            Map.entry("final_report", Channels.base(null, null)),

            Map.entry("request_id", Channels.base(null, null)),
            Map.entry("user_id", Channels.base(null, null)),   
            Map.entry("research_topic", Channels.base(null, null)),

            Map.entry("current_node_start_time", Channels.base(null, null)),
            Map.entry("error_message", Channels.base(null, null)),
            Map.entry("success", Channels.base(null, null)));

    public MainGraphState(Map<String, Object> initData) {
        super(initData);
    }

    public Optional<String> requestId() {
        return this.value("request_id");
    }

    public Optional<String> userId() {
        return this.value("user_id");
    }    

    public Optional<String> researchBrief() {
        return this.value("research_brief");
    }

    public Optional<String> researchTopic() {
        return this.value("research_topic");
    }

    public Optional<String> finalReport() {
        return this.value("final_report");
    }

    @SuppressWarnings("unchecked")
    public List<ChatMessage> supervisorMessages() {
        return this.<List<ChatMessage>>value("supervisor_messages").orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public List<String> notes() {
        return this.<List<String>>value("notes").orElse(new ArrayList<>());
    }

    @SuppressWarnings("unchecked")
    public List<String> rawNotes() {
        return this.<List<String>>value("raw_notes").orElse(new ArrayList<>());
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

    public static Map<String, Object> createInitialState(
            String researchTopic,
            String requestId,
            String userId) {

        return Map.of(
                "research_topic", researchTopic,
                "request_id", requestId,
                "user_id", userId,
                "start_time", LocalDateTime.now());
    }

}