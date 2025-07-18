package com.example.topfoodnow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LogService {
    @Value("${logging.file.name}")
    private String logFilePath;

    /**
     * 讀取日誌檔案的內容
     * @param maxLines 最大讀取行數，-1表示全部
     * @return 日誌內容的列表
     * @throws IOException 如果讀取檔案失敗
     */
    public List<String> readLogFile(int maxLines) throws IOException {
        java.nio.file.Path path = Paths.get(logFilePath);
        if (!Files.exists(path)) {
            return Collections.singletonList("Log file not found at: " + logFilePath);
        }

        try (Stream<String> lines = Files.lines(path)) {
            if (maxLines > 0) {
                return lines.limit(maxLines).collect(Collectors.toList());
            } else {
                return lines.collect(Collectors.toList());
            }
        }
    }

    /**
     * 讀取日誌檔案的最新N行
     * @param numberOfLines 要讀取的行數
     * @return 日誌最新內容的列表
     * @throws IOException 如果讀取檔案失敗
     */
    public List<String> readLastNLinesOfLogFile(int numberOfLines) throws IOException {
        java.nio.file.Path path = Paths.get(logFilePath);
        if (!Files.exists(path)) {
            return Collections.singletonList("Log file not found at: " + logFilePath);
        }

        List<String> allLines;
        try (Stream<String> lines = Files.lines(path)) {
            allLines = lines.collect(Collectors.toList());
        }

        if (allLines.size() <= numberOfLines) {
            return allLines;
        } else {
            return allLines.subList(allLines.size() - numberOfLines, allLines.size());
        }
    }
}