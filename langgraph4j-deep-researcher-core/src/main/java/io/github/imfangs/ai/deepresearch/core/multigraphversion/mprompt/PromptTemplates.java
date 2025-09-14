package io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
public class PromptTemplates {

    private static final PromptTemplate INSTANCE = PromptTemplateFactory.getInstance();

    public static String getResearcherTaskHumanPrompt() {
        return INSTANCE.getResearcherTaskHumanPrompt();
    }

    public static String formatResearcherTaskHumanPrompt(String researchTask) {
        return INSTANCE.formatResearcherTaskHumanPrompt(researchTask);
    }
    
    public static String getResearchTopicPrompt() {
        return INSTANCE.getResearchTopicPrompt();
    }

    public static String getSupervisorSystemPrompt() {
        return INSTANCE.getSupervisorSystemPrompt();
    }

    public static String getFinalReportGenerationPrompt() {
        return INSTANCE.getFinalReportGenerationPrompt();
    }

    public static String getResearcherSystemPrompt() {
        return INSTANCE.getResearcherSystemPrompt();
    }

    public static String getCompressResearchSystemPrompt() {
        return INSTANCE.getCompressResearchSystemPrompt();
    }

    public static String getCompressResearchHumanMessage() {
        return INSTANCE.getCompressResearchHumanMessage();
    }


    public static String formatResearchTopicPrompt(String researchTopic, String currentDate) {
        return INSTANCE.formatResearchTopicPrompt(researchTopic, currentDate);
    }

    public static String formatFinalReportGenerationPrompt(String researchBrief, String findings, String currentDate) {
        return INSTANCE.formatFinalReportGenerationPrompt(researchBrief, findings, currentDate);
    }

    public static String formatCompressResearchSystemPrompt(String currentDateAmerican) {
        return INSTANCE.formatCompressResearchSystemPrompt(currentDateAmerican);
    }


    public static String getCurrentDate() {
        return INSTANCE.getCurrentDate();
    }

    public static String getCurrentDateAmerican() {
        return INSTANCE.getCurrentDateAmerican();
    }

    public static String getCurrentLanguage() {
        return PromptTemplateFactory.getCurrentLanguage();
    }
}