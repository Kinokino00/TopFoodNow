package com.example.topfoodnow.controller.log.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetApplicationLogsRequest {
  @Min(value = 0, message = "lines must be greater than or equal to 0")
  private int lines;
}
