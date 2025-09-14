package io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
public abstract class PromptTemplate {


    public abstract String getResearchTopicPrompt();

    public abstract String getSupervisorSystemPrompt();

    public abstract String getFinalReportGenerationPrompt();

    public abstract String getResearcherSystemPrompt();

    public abstract String getCompressResearchSystemPrompt();

    public abstract String getCompressResearchHumanMessage();
    
    public abstract String getResearcherTaskHumanPrompt();

    public String formatResearcherTaskHumanPrompt(String researchTask) {
        return String.format(getResearcherTaskHumanPrompt(), researchTask);
    }
    
    public String formatResearchTopicPrompt(String researchTopic, String currentDate) {
        return String.format(getResearchTopicPrompt(), researchTopic, currentDate);
    }

    public String formatFinalReportGenerationPrompt(String researchBrief, String findings, String currentDate) {
        return String.format(getFinalReportGenerationPrompt(), researchBrief, findings, currentDate);
    }

    public String formatCompressResearchSystemPrompt(String currentDateAmerican) {
        return getCompressResearchSystemPrompt().replace("{date}", currentDateAmerican);
    }


    protected String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    protected String getCurrentDateAmerican() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("EEE MMM dd, yyyy"));
    }
}