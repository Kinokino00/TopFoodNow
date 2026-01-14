package com.example.topfoodnow.service;

import java.io.IOException;
import java.util.List;

public interface LogService {
    List<String> readLogFile(int maxLines) throws IOException;
    List<String> readLastNLinesOfLogFile(int numberOfLines) throws IOException;
}