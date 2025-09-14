package io.github.imfangs.ai.deepresearch.core.multigraphversion.mprompt;

/**
 * @info: multi-graph version deep research
 * @author: zoey333
 */
public class PromptTemplateFactory {

    private static final String DEFAULT_LANGUAGE = "en";

    private static final String CURRENT_LANGUAGE = System.getenv().getOrDefault("PROMPT_LANGUAGE",
            System.getProperty("prompt.language", DEFAULT_LANGUAGE));

    private static volatile PromptTemplate instance;

    public static PromptTemplate getInstance() {
        if (instance == null) {
            synchronized (PromptTemplateFactory.class) {
                if (instance == null) {
                    instance = createInstance();
                }
            }
        }
        return instance;
    }

    private static PromptTemplate createInstance() {
        switch (CURRENT_LANGUAGE.toLowerCase()) {
            case "zh":
            case "chinese":
                return new PromptTemplateCN();
            case "en":
            case "english":
            default:
                return new PromptTemplateEN();    
        }
    }

    public static String getCurrentLanguage() {
        return CURRENT_LANGUAGE;
    }

    static void resetInstance() {
        instance = null;
    }

}