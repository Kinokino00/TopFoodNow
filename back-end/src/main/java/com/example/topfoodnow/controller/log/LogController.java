package com.example.topfoodnow.controller.log;

import com.example.topfoodnow.controller.log.request.GetApplicationLogsRequest;
import com.example.topfoodnow.controller.log.response.GetApplicationLogsResponse;
import com.example.topfoodnow.service.log.impl.GetApplicationLogsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/log")
@Tag(name = "log 相關")
public class LogController {

  private final GetApplicationLogsUseCase getApplicationLogsUseCase;

  @Operation(summary = "查看應用程式日誌", description = "只有具有 'ADMIN' 角色的用戶才能訪問。返回應用程式的日誌內容")
  @PostMapping("/logs")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<GetApplicationLogsResponse> getApplicationLogs(
      @RequestBody @Valid GetApplicationLogsRequest rq) {
    return ResponseEntity.ok(getApplicationLogsUseCase.execute(rq));
  }
}