package io.github.imfangs.ai.deepresearch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Langgraph4j Deep Research Application main startup class
 *
 * @author imfangs
 */
@Slf4j
@SpringBootApplication(scanBasePackages = {"io.github.imfangs.ai.deepresearch", "org.bsc.langgraph4j.agui" })
@EnableConfigurationProperties
public class DeepResearchApplication {

    public static void main(String[] args) {
        try {
            ConfigurableApplicationContext context = SpringApplication.run(DeepResearchApplication.class, args);
            
            Environment env = context.getEnvironment();
            String protocol = "http";
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https";
            }
            
            String serverPort = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String hostAddress = "localhost";
            
            try {
                hostAddress = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                log.warn("Unable to get host address", e);
            }
            
            log.info("""
                
                🚀 Langgraph4j Deep Researcher started successfully!
                
                ===================================
                🌐 Application Access URLs:
                   Local:    {}://localhost:{}{}
                   External: {}://{}:{}{}
                   
                📚 API Documentation:
                   Health Check: {}/api/v1/research/health
                   Research Interface: {}/api/v1/research/execute
                   
                🔧 Configuration Information:
                   Profile: {}
                   Java Version: {}
                ===================================
                """,
                protocol, serverPort, contextPath,
                protocol, hostAddress, serverPort, contextPath,
                protocol + "://localhost:" + serverPort + contextPath,
                protocol + "://localhost:" + serverPort + contextPath,
                String.join(",", env.getActiveProfiles()),
                System.getProperty("java.version")
            );
            
        } catch (Exception e) {
            log.error("Application startup failed", e);
            System.exit(1);
        }
    }
}
