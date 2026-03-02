package com.example.topfoodnow.controller.log.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetApplicationLogsResponse {
  private List<String> logContent;
  private String message;
  private Boolean success;
}
