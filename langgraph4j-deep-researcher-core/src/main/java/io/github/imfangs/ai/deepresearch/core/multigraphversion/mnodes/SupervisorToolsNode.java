package io.github.imfangs.ai.deepresearch.core.multigraphversion.mnodes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.bsc.langgraph4j.CompileConfig;
import org.bsc.langgraph4j.RunnableConfig;
import org.bsc.langgraph4j.StateGraph;
import org.bsc.langgraph4j.action.NodeAction;
import org.bsc.langgraph4j.checkpoint.MemorySaver;
import org.springframework.stereotype.Component;
import dev.langchain4j.data.message.AiMessage;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mgraph.ResearcherSubgraph;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.ResearcherState;
import io.github.imfangs.ai.deepresearch.core.multigraphversion.mstate.SupervisorState;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SupervisorToolsNode implements NodeAction<SupervisorState> {

    private final ResearcherSubgraph researcherSubgraph;
    private final int MaxConcurrentResearch = 3;

    private final ExecutorService researchExecutor = Executors.newCachedThreadPool();
    private StateGraph<ResearcherState> cachedResearchSubgraph;
    
    @PostConstruct
    public void init() {
        try {
            log.info("Initializing cached ResearchSubgraph...");
            this.cachedResearchSubgraph = researcherSubgraph.createResearchGraph();
            log.info("ResearchSubgraph cached successfully");
        } catch (Exception e) {
            log.error("Failed to initialize cached ResearchSubgraph", e);
            throw new RuntimeException("Failed to initialize ResearchSubgraph", e);
        }
    } 
    
    @Override
    public Map<String, Object> apply(SupervisorState state) {
        log.info("🔧 Starting SupervisorToolsNode");

        List<String> researchTasks = state.taskList();

        if (researchTasks == null || researchTasks.isEmpty()) {
            log.warn("⚠️ No research tasks to execute");
            return Map.of(
                    "supervisor_messages", List.of(AiMessage.from("## Research task execution \n\n⚠️ No research tasks pending execution.")),
                    "research_iterations", state.researchIterations() + 1,
                    "last_iteration_time", LocalDateTime.now());
        }

        log.info("🚀 Executing {} research tasks in batches", researchTasks.size());

        List<String> remainingTasks = new ArrayList<>(researchTasks);
        List<String> compressedReports = new ArrayList<>();
        List<String> successfulTasks = new ArrayList<>();
        List<String> failedTasks = new ArrayList<>();
        List<String> rawNotes = new ArrayList<>();

        int totalBatches = (int) Math.ceil((double) researchTasks.size() / MaxConcurrentResearch);
        int currentBatch = 0;

        while (!remainingTasks.isEmpty()) {
            currentBatch++;
            int batchSize = Math.min(remainingTasks.size(), MaxConcurrentResearch);
            List<String> currentBatchTasks = remainingTasks.subList(0, batchSize);

            log.info("📦 Executing batch {}/{} with {} tasks", currentBatch, totalBatches, currentBatchTasks.size());

            List<CompletableFuture<ResearcherState>> batchFutures = currentBatchTasks.stream()
                    .map(this::executeResearchTaskAsync)
                    .collect(Collectors.toList());

            for (int i = 0; i < batchFutures.size(); i++) {
                CompletableFuture<ResearcherState> future = batchFutures.get(i);
                String task = currentBatchTasks.get(i);
                
                try {
                    ResearcherState result = future.join();
                    
                    if (result.success()) {
                        successfulTasks.add(task);
                        result.compressedResearch().ifPresent(compressedReports::add);
                        rawNotes.addAll(result.rawNotes());
                    } else {
                        failedTasks.add(task);
                        String errorMsg = result.errorMessage().orElse("Unknown error");
                        log.warn("Research failed for task: {} - {}", task, errorMsg);
                        rawNotes.add(String.format("error - %s: %s", task, errorMsg));
                    }
                    
                } catch (Exception e) {
                    failedTasks.add(task);
                    log.error("Task execution failed for task: {}", task, e);
                    rawNotes.add(String.format("error - %s: Task execution failed - %s", task, e.getMessage()));
                }
            }

            remainingTasks.subList(0, batchSize).clear();
        }

        StringBuilder resultMessage = new StringBuilder();
        resultMessage.append("## 🔬 Batch research task execution completed \n\n");

        resultMessage.append(String.format("**📊 Execution statistics: %d successful, %d failed, %d total batches \n\n",
                successfulTasks.size(), failedTasks.size(), totalBatches));

        if (!successfulTasks.isEmpty()) {
            resultMessage.append("**✅ Successful research tasks **:\n");
            for (String task : successfulTasks) {
                resultMessage.append(String.format("- %s\n", task));
            }
            resultMessage.append("\n");
        }

        if (!failedTasks.isEmpty()) {
            resultMessage.append("**❌ Failed tasks **:\n");
            for (String task : failedTasks) {
                resultMessage.append(String.format("- %s\n", task));
            }
            resultMessage.append("\n");
        }

        if (!compressedReports.isEmpty()) {
            resultMessage.append("**📋 Research report summary **:\n");
            for (int i = 0; i < compressedReports.size(); i++) {
                resultMessage.append(String.format("### report %d\n%s\n\n", i + 1, compressedReports.get(i)));
            }
        } else {
            resultMessage.append("**📋 report **: No available reports \n\n");
        }

        log.info("✅ All batches completed - {} successful, {} failed",
                successfulTasks.size(), failedTasks.size());

        return Map.of(
                "supervisor_messages", List.of(AiMessage.from(resultMessage.toString())),
                "notes", compressedReports,
                "raw_notes", rawNotes,
                "task_list",new ArrayList<>(),
                "research_iterations", state.researchIterations() + 1,
                "last_iteration_time", LocalDateTime.now());
    }

    private CompletableFuture<ResearcherState> executeResearchTaskAsync(String researchTask) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Starting research for task: {}", researchTask);

                Map<String, Object> initialState = researcherSubgraph.createInitialState(
                        researchTask,
                        "supervisor-tools-" + UUID.randomUUID().toString(),
                        "supervisor-user");

                CompileConfig compileConfig = CompileConfig.builder()
                        .checkpointSaver(new MemorySaver())
                        .build();
                CompiledGraph<ResearcherState> compiledGraph = cachedResearchSubgraph.compile(compileConfig);

                RunnableConfig runnableConfig = RunnableConfig.builder()
                        .threadId("supervisor-tools-" + UUID.randomUUID().toString())
                        .build();

                ResearcherState finalState = null;
                for (var nodeOutput : compiledGraph.stream(initialState, runnableConfig)) {
                    finalState = nodeOutput.state();
                    if (!finalState.success())
                        break;
                }

                log.info("finished researchTask: "+researchTask);
                return finalState != null ? finalState
                        : new ResearcherState(Map.of("success", false, "error_message", "No result returned"));

            } catch (Exception e) {
                log.error("Research task execution failed for task: {}", researchTask, e);
                return new ResearcherState(Map.of(
                        "research_task", researchTask,
                        "success", false,
                        "error_message", e.getMessage()));
            }
        }, researchExecutor);
    }
}