package com.example.topfoodnow.controller;

import com.example.topfoodnow.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
@Tag(name = "log 相關")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    private final LogService logService;

    @Operation(
        summary = "查看應用程式日誌",
        description = "只有具有 'ADMIN' 角色的用戶才能訪問。返回應用程式的日誌內容",
        parameters = {
            @Parameter(name = "lines", description = "要讀取的日誌行數（負數或0表示全部，預設為100行）", example = "50")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "成功取得日誌內容"),
            @ApiResponse(responseCode = "401", description = "未經認證"),
            @ApiResponse(responseCode = "403", description = "無權限訪問 (需要 ADMIN 角色)"),
            @ApiResponse(responseCode = "500", description = "讀取日誌檔案失敗")
        }
    )
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getApplicationLogs(@RequestParam(defaultValue = "100") int lines) {
        logger.info("管理員嘗試訪問應用程式日誌，請求行數: {}", lines);
        try {
            List<String> logContent;
            if (lines > 0) {
                logContent = logService.readLastNLinesOfLogFile(lines);
            } else {
                logContent = logService.readLogFile(-1);
            }
            return ResponseEntity.ok(logContent);
        } catch (IOException e) {
            logger.error("讀取日誌檔案失敗: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of("無法讀取日誌檔案: " + e.getMessage()));
        }
    }
}