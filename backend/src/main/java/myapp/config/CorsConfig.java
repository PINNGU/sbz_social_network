
package myapp.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(CorsConfig.class);

    public CorsConfig() {
        logger.debug("[CorsConfig] Constructor called");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        logger.debug("[CorsConfig] addCorsMappings called");
        try {
            logger.info("[CorsConfig] Adding CORS mapping for /api/**");
            registry.addMapping("/api/**") // Apply CORS to all /api endpoints
                    .allowedOrigins("http://localhost:5173") // Allow your Vue.js frontend origin
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            logger.info("[CorsConfig] CORS mapping added: origin=http://localhost:5173, methods=GET,POST,PUT,DELETE,OPTIONS, allowedHeaders=*, allowCredentials=true");
        } catch (Exception e) {
            logger.error("[CorsConfig] Exception in addCorsMappings", e);
        }
    }
}
