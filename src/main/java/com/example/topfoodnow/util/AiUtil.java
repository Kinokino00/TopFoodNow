package com.example.topfoodnow.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class AiUtil {
    public static String askAi(String prompt, String apiKey) throws IOException {
        URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        JSONObject textPart = new JSONObject();
        textPart.put("text", prompt);

        JSONArray parts = new JSONArray();
        parts.put(textPart);

        JSONObject content = new JSONObject();
        content.put("role", "user");
        content.put("parts", parts);

        JSONArray contents = new JSONArray();
        contents.put(content);

        JSONObject requestBody = new JSONObject();
        requestBody.put("contents", contents);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = requestBody.toString().getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = con.getResponseCode();

        if (status >= 200 && status < 300) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                return br.lines().collect(Collectors.joining());
            }
        } else {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getErrorStream(), "utf-8"))) {
                String error = br.lines().collect(Collectors.joining());
                throw new IOException("Gemini API 錯誤，HTTP " + status + ":\n" + error);
            }
        }
    }
}