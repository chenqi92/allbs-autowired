package cn.allbs.auto.factories;


import cn.allbs.auto.config.MultiSetMap;
import lombok.experimental.UtilityClass;

import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ChenQi
 */
@UtilityClass
class FactoriesFiles {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /**
     * 读取 spring.factories 文件
     *
     * @param fileObject FileObject
     * @return MultiSetMap
     * @throws IOException 异常信息
     */
    protected static MultiSetMap<String, String> readFactoriesFile(FileObject fileObject, Elements elementUtils) throws IOException {
        // 读取 spring.factories 内容
        Properties properties = new Properties();
        try (InputStream input = fileObject.openInputStream()) {
            properties.load(input);
        }
        MultiSetMap<String, String> multiSetMap = new MultiSetMap<>();
        Set<Map.Entry<Object, Object>> entrySet = properties.entrySet();
        for (Map.Entry<Object, Object> objectEntry : entrySet) {
            String key = (String) objectEntry.getKey();
            String value = (String) objectEntry.getValue();
            if (value == null || value.trim().isEmpty()) {
                continue;
            }
            // 解析 spring.factories
            String[] values = value.split(",");
            Set<String> valueSet = Arrays.stream(values)
                    .filter(v -> !v.isEmpty())
                    .map(String::trim)
                    // 校验是否删除文件
                    .filter((v) -> Objects.nonNull(elementUtils.getTypeElement(v)))
                    .collect(Collectors.toSet());
            multiSetMap.putAll(key.trim(), valueSet);
        }
        return multiSetMap;
    }

    /**
     * 写出 spring.factories 文件
     *
     * @param factories factories 信息
     * @param output    输出流
     * @throws IOException 异常信息
     */
    protected static void writeFactoriesFile(MultiSetMap<String, String> factories,
                                             OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, UTF_8));
        Set<String> keySet = factories.keySet();
        for (String key : keySet) {
            Set<String> values = factories.get(key);
            if (values == null || values.isEmpty()) {
                continue;
            }
            writer.write(key);
            writer.write("=\\\n  ");
            StringJoiner joiner = new StringJoiner(",\\\n  ");
            for (String value : values) {
                joiner.add(value);
            }
            writer.write(joiner.toString());
            writer.newLine();
        }
        writer.flush();
    }

    /**
     * 写出 spring-devtools.properties
     *
     * @param projectName 项目名
     * @param output      输出流
     * @throws IOException 异常信息
     */
    protected static void writeDevToolsFile(String projectName,
                                            OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, UTF_8));
        String format = "restart.include.%s=/%s[\\\\w\\\\d-.]+\\\\.jar";
        writer.write(String.format(format, projectName, projectName));
        writer.flush();
    }

}
