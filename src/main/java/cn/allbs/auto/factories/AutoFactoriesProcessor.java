package cn.allbs.auto.factories;

import cn.allbs.auto.annotation.AutoIgnore;
import cn.allbs.auto.config.AbstractDefaultProcessor;
import cn.allbs.auto.config.BootAutoType;
import cn.allbs.auto.config.MultiSetMap;
import com.google.auto.service.AutoService;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ChenQi
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedOptions("debug")
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
public class AutoFactoriesProcessor extends AbstractDefaultProcessor {
    /**
     * The location to look for factories.
     * <p>Can be present in multiple JAR files.
     */
    private static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";
    /**
     * devtools，有 Configuration 注解的 jar 一般需要 devtools 配置文件
     */
    private static final String DEVTOOLS_RESOURCE_LOCATION = "META-INF/spring-devtools.properties";
    /**
     * 数据承载
     */
    private MultiSetMap<String, String> factories = new MultiSetMap<>();
    /**
     * 元素辅助类
     */
    private Elements elementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    @Override
    protected boolean processImpl(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            generateFactoriesFiles();
        } else {
            processAnnotations(annotations, roundEnv);
        }
        return false;
    }

    private void processAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // 日志 打印信息 gradle build --debug
        log(annotations.toString());
        Set<? extends Element> elementSet = roundEnv.getRootElements();
        log("All Element set: " + elementSet.toString());

        // 过滤 TypeElement
        Set<TypeElement> typeElementSet = elementSet.stream()
                .filter(this::isClassOrInterface)
                .filter(e -> e instanceof TypeElement)
                .map(e -> (TypeElement) e)
                .collect(Collectors.toSet());
        // 如果为空直接跳出
        if (typeElementSet.isEmpty()) {
            log("Annotations elementSet is isEmpty");
            return;
        }

        for (TypeElement typeElement : typeElementSet) {
            // ignore @AutoIgnore Element
            if (isAnnotation(elementUtils, typeElement, AutoIgnore.class.getName())) {
                log("Found @AutoIgnore annotation, ignore Element: " + typeElement.toString());
            } else {
                for (BootAutoType autoType : BootAutoType.values()) {
                    String annotation = autoType.getAnnotation();
                    if (isAnnotation(elementUtils, typeElement, annotation)) {
                        log("Found @" + annotation + " Element: " + typeElement.toString());

                        String factoryName = typeElement.getQualifiedName().toString();
                        if (factories.containsVal(factoryName)) {
                            continue;
                        }

                        log("读取到新配置 spring.factories factoryName：" + factoryName);
                        factories.put(autoType.getConfigureKey(), factoryName);
                    }
                }
            }
        }
    }

    private void generateFactoriesFiles() {
        if (factories.isEmpty()) {
            return;
        }
        Filer filer = processingEnv.getFiler();
        try {
            // 1. spring.factories
            MultiSetMap<String, String> allFactories = new MultiSetMap<>();
            try {
                FileObject existingFactoriesFile = filer.getResource(StandardLocation.CLASS_OUTPUT, "", FACTORIES_RESOURCE_LOCATION);
                // 查找是否已经存在 spring.factories
                log("Looking for existing spring.factories file at " + existingFactoriesFile.toUri());
                MultiSetMap<String, String> existingFactories = FactoriesFiles.readFactoriesFile(existingFactoriesFile, elementUtils);
                log("Existing spring.factories entries: " + existingFactories);
                allFactories.putAll(existingFactories);
            } catch (IOException e) {
                log("spring.factories resource file did not already exist.");
            }
            // 原有配置 + 增量
            allFactories.putAll(factories);
            log("New spring.factories file contents: " + allFactories);
            FileObject factoriesFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", FACTORIES_RESOURCE_LOCATION);
            try (OutputStream out = factoriesFile.openOutputStream()) {
                FactoriesFiles.writeFactoriesFile(allFactories, out);
            }

            // 2. devtools 配置，因为有 @Configuration 注解的需要 devtools
            String classesPath = factoriesFile.toUri().toString().split("classes")[0];
            Path projectPath = Paths.get(new URI(classesPath)).getParent();
            String projectName = projectPath.getFileName().toString();
            FileObject devToolsFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", DEVTOOLS_RESOURCE_LOCATION);
            try (OutputStream out = devToolsFile.openOutputStream()) {
                FactoriesFiles.writeDevToolsFile(projectName, out);
            }
        } catch (IOException | URISyntaxException e) {
            fatalError(e);
        }
    }
}
