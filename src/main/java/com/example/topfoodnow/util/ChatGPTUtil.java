package com.example.topfoodnow.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class ChatGPTUtil {
    public static String askGpt(String prompt, String apiKey) throws IOException {
        URL url = new URL("https://api.openai.com/v1/chat/completions");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + apiKey);
        con.setDoOutput(true);

        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.put(message);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", messages);

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
                throw new IOException("OpenAI API 錯誤，HTTP " + status + ":\n" + error);
            }
        }
    }
}
