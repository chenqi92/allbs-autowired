package cn.allbs.auto.config;

import cn.allbs.auto.annotation.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author ChenQi
 */
@Getter
@RequiredArgsConstructor
public enum BootAutoType {

    /**
     * Component，组合注解，添加到 spring.factories
     */
    COMPONENT("org.springframework.stereotype.Component", "org.springframework.boot.autoconfigure.EnableAutoConfiguration"),
    /**
     * ApplicationContextInitializer 添加到 spring.factories
     */
    CONTEXT_INITIALIZER(AutoContextInitializer.class.getName(), "org.springframework.context.ApplicationContextInitializer"),
    /**
     * ApplicationListener 添加到 spring.factories
     */
    LISTENER(AutoListener.class.getName(), "org.springframework.context.ApplicationListener"),
    /**
     * SpringApplicationRunListener 添加到 spring.factories
     */
    RUN_LISTENER(AutoRunListener.class.getName(), "org.springframework.boot.SpringApplicationRunListener"),
    /**
     * AUTO_RESOURCE 添加到 spring.factories
     */
    AUTO_RESOURCE(AutoResource.class.getName(), "org.springframework.boot.env.EnvironmentPostProcessor"),
    /**
     * FailureAnalyzer 添加到 spring.factories
     */
    FAILURE_ANALYZER(AutoFailureAnalyzer.class.getName(), "org.springframework.boot.diagnostics.FailureAnalyzer"),
    /**
     * AutoConfigurationImportFilter spring.factories
     */
    AUTO_CONFIGURATION_IMPORT_FILTER(AutoConfigImportFilter.class.getName(), "org.springframework.boot.autoconfigure.AutoConfigurationImportFilter"),
    /**
     * TemplateAvailabilityProvider 添加到 spring.factories
     */
    TEMPLATE_AVAILABILITY_PROVIDER(AutoTemplateProvider.class.getName(), "org.springframework.boot.autoconfigure.template.TemplateAvailabilityProvider");

    private final String annotation;
    private final String configureKey;

}
