package com.example.topfoodnow.service.log.impl;

import com.example.topfoodnow.controller.log.request.GetApplicationLogsRequest;
import com.example.topfoodnow.controller.log.response.GetApplicationLogsResponse;
import com.example.topfoodnow.service.log.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class GetApplicationLogsUseCase {

private LogService logService;

  @Value("${logging.file.name}")
  private String logFilePath;

  public GetApplicationLogsResponse execute(GetApplicationLogsRequest rq) {
    log.info(">>>> GetApplicationLogsUseCase : Request = {}", rq);

    try {
      List<String> logContent;
      if (rq.getLines() > 0) {
        logContent = logService.readLastNLinesOfLogFile(rq.getLines());
      } else {
        logContent = logService.readLogFile(-1);
      }

      GetApplicationLogsResponse response = createResponse(logContent);
      log.info(">>>> GetApplicationLogsUseCase : Response = {}", response);
      return response;
    } catch (IOException e) {
      log.error(">>>> GetApplicationLogsUseCase : Error reading log file: {}", e.getMessage(), e);
      return GetApplicationLogsResponse.builder()
              .logContent(List.of("Unable to read log file: " + e.getMessage()))
              .success(false)
              .message("Error reading log file: " + e.getMessage())
              .build();
    }
  }

  private GetApplicationLogsResponse createResponse(List<String> logContent) {
    return GetApplicationLogsResponse.builder()
            .logContent(logContent)
            .success(true)
            .message("Successfully retrieved application logs")
            .build();
  }
}



