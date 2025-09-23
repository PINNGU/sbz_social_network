package myapp.config;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.KieServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class DroolsConfig {
    @Bean
    public KieContainer kieContainer() {
        return KieServices.Factory.get().getKieClasspathContainer();
    }

    @Bean
    @Scope("prototype")
    public KieSession kieSession(KieContainer kieContainer) {
        return kieContainer.newKieSession("ksession-posts");
    }
}
