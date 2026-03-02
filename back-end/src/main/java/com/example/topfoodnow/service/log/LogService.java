package com.example.topfoodnow.service.log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface LogService {

  /**
   * 讀取日誌檔案的內容
   * @param maxLines 最大讀取行數，-1表示全部
   * @return 日誌內容的列表
   * @throws IOException 如果讀取檔案失敗
   */
  default List<String> readLogFile(int maxLines) throws IOException {
    java.nio.file.Path path = Paths.get(getLogFilePath());
    if (!Files.exists(path)) {
      return Collections.singletonList("Log file not found at: " + getLogFilePath());
    }

    try (Stream<String> lines = Files.lines(path)) {
      if (maxLines > 0) {
        return lines.limit(maxLines).toList();
      } else {
        return lines.toList();
      }
    }
  }

  /**
   * 讀取日誌檔案的最新N行
   * @param numberOfLines 要讀取的行數
   * @return 日誌最新內容的列表
   * @throws IOException 如果讀取檔案失敗
   */
  default List<String> readLastNLinesOfLogFile(int numberOfLines) throws IOException {
    java.nio.file.Path path = Paths.get(getLogFilePath());
    if (!Files.exists(path)) {
      return Collections.singletonList("Log file not found at: " + getLogFilePath());
    }

    List<String> allLines;
    try (Stream<String> lines = Files.lines(path)) {
      allLines = lines.toList();
    }

    if (allLines.size() <= numberOfLines) {
      return allLines;
    } else {
      return allLines.subList(allLines.size() - numberOfLines, allLines.size());
    }
  }

  String getLogFilePath();
}
