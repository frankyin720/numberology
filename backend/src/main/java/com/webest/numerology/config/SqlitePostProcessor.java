package com.webest.numerology.config;

import ch.qos.logback.core.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * 在 DataSource 初始化之前创建 SQLite 父目录，避免 {@code ./data} 不存在导致启动失败。
 */
public class SqlitePostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("spring.datasource.url");
        if (StringUtils.isBlank(url)) {
            url = "jdbc:sqlite:./data/numerology.db";
        }
        try {
            String pathPart = url.substring("jdbc:sqlite:".length()).trim();
            if (StringUtils.isBlank(pathPart) || ":memory".equals(pathPart)) {
                return;
            }
            if (StringUtils.startsWith(pathPart, "file:")) {
                pathPart = pathPart.substring("file:".length());
            }
            if (pathPart.contains("?")) {
                pathPart = pathPart.substring(0, pathPart.indexOf("?"));
            }
            Path dbFile = Path.of(pathPart).toAbsolutePath().normalize();
            Path parentDir = dbFile.getParent();
            if (Objects.nonNull(parentDir)) {
                Files.createDirectories(parentDir);
            }
        } catch (Exception e) {
            throw new IllegalStateException("无法创建 SQLite 目录，url=" + url + ": " + e.getMessage(), e);
        }
    }
}
