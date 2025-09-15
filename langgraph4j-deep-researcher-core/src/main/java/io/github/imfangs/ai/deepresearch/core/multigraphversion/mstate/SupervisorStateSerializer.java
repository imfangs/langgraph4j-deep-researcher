package io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate;

import io.github.imfangs.ai.deepresearch.api.dto.SearchResult;
import org.bsc.langgraph4j.serializer.Serializer;
import org.bsc.langgraph4j.serializer.std.ObjectStreamStateSerializer;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
public class SupervisorStateSerializer extends ObjectStreamStateSerializer<SupervisorState> {

    public SupervisorStateSerializer() {
        super(SupervisorState::new);

        mapper().register(SearchResult.class, new SearchResultSerializer());
        mapper().register(LocalDateTime.class, new LocalDateTimeSerializer());

        mapper().register(AiMessage.class, new AiMessageSerializer());
        mapper().register(UserMessage.class, new UserMessageSerializer());
        mapper().register(SystemMessage.class, new SystemMessageSerializer());
        mapper().register(ToolExecutionResultMessage.class, new ToolExecutionResultMessageSerializer());
    }

    private static class SearchResultSerializer implements Serializer<SearchResult> {

        @Override
        public void write(SearchResult object, ObjectOutput out) throws IOException {
            out.writeObject(object.getTitle());
            out.writeObject(object.getUrl());
            out.writeObject(object.getContent());
            out.writeObject(object.getRawContent());
            out.writeObject(object.getScore());
            out.writeObject(object.getMetadata());
            out.writeObject(object.getSourceEngine());
        }

        @Override
        public SearchResult read(ObjectInput in) throws IOException, ClassNotFoundException {
            String title = (String) in.readObject();
            String url = (String) in.readObject();
            String content = (String) in.readObject();
            String rawContent = (String) in.readObject();
            Double score = (Double) in.readObject();
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) in.readObject();
            String sourceEngine = (String) in.readObject();

            return SearchResult.builder()
                    .title(title)
                    .url(url)
                    .content(content)
                    .rawContent(rawContent)
                    .score(score)
                    .metadata(metadata)
                    .sourceEngine(sourceEngine)
                    .build();
        }
    }

    private static class LocalDateTimeSerializer implements Serializer<LocalDateTime> {

        @Override
        public void write(LocalDateTime object, ObjectOutput out) throws IOException {
            out.writeObject(object.toString());
        }

        @Override
        public LocalDateTime read(ObjectInput in) throws IOException, ClassNotFoundException {
            String isoString = (String) in.readObject();
            return LocalDateTime.parse(isoString);
        }
    }

    private static class AiMessageSerializer implements Serializer<AiMessage> {

        @Override
        public void write(AiMessage message, ObjectOutput out) throws IOException {
            out.writeObject(message.text());
            out.writeObject(message.toolExecutionRequests());
        }

        @Override
        public AiMessage read(ObjectInput in) throws IOException, ClassNotFoundException {
            String text = (String) in.readObject();
            @SuppressWarnings("unchecked")
            List<ToolExecutionRequest> toolRequests = (List<ToolExecutionRequest>) in.readObject();
            return AiMessage.from(text, toolRequests);
        }
    }

    private static class UserMessageSerializer implements Serializer<UserMessage> {

        @Override
        public void write(UserMessage message, ObjectOutput out) throws IOException {
            out.writeObject(message.singleText());
        }

        @Override
        public UserMessage read(ObjectInput in) throws IOException, ClassNotFoundException {
            String text = (String) in.readObject();
            return UserMessage.from(text);
        }
    }

    private static class SystemMessageSerializer implements Serializer<SystemMessage> {

        @Override
        public void write(SystemMessage message, ObjectOutput out) throws IOException {
            out.writeObject(message.text());
        }

        @Override
        public SystemMessage read(ObjectInput in) throws IOException, ClassNotFoundException {
            String text = (String) in.readObject();
            return SystemMessage.from(text);
        }
    }

    private static class ToolExecutionResultMessageSerializer implements Serializer<ToolExecutionResultMessage> {

        @Override
        public void write(ToolExecutionResultMessage message, ObjectOutput out) throws IOException {
            out.writeObject(message.id());
            out.writeObject(message.toolName());
            out.writeObject(message.text());
        }

        @Override
        public ToolExecutionResultMessage read(ObjectInput in) throws IOException, ClassNotFoundException {
            String id = (String) in.readObject();
            String toolName = (String) in.readObject();
            String text = (String) in.readObject();
            return ToolExecutionResultMessage.from(id, toolName, text);
        }
    }
}