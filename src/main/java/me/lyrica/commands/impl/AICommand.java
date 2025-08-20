package me.lyrica.commands.impl;

import me.lyrica.commands.Command;
import me.lyrica.commands.RegisterCommand;
import me.lyrica.Lyrica;
import net.minecraft.text.Text;

@RegisterCommand(name = "ai", description = "Gemini AI ile sohbet.", syntax = "<[prompt]>")
public class AICommand extends Command {
    public static String GEMINI_API_KEY = "AIzaSyCJ6m5jckjG-qynYQd_w9n66L-vUud6t3g";
    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            messageSyntax();
            return;
        }
        StringBuilder prompt = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            prompt.append(args[i]);
            if (i + 1 != args.length) prompt.append(" ");
        }
        new Thread(() -> {
            try {
                String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
                String body = """
                {\n  \"contents\": [\n    {\n      \"parts\": [\n        { \"text\": \"%s\" }\n      ]\n    }\n  ]\n}\n""".formatted(prompt.toString().replace("\"", "\\\""));
                java.net.URL url = new java.net.URL(endpoint);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                }
                String response = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(response).getAsJsonObject();
                String answer = obj.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();
                mc.execute(() -> mc.inGameHud.getChatHud().addMessage(Text.literal("[Gemini] " + answer)));
            } catch (Exception e) {
                mc.execute(() -> mc.inGameHud.getChatHud().addMessage(Text.literal("[Gemini] Hata: " + e.getClass().getSimpleName())));
            }
        }).start();
    }
} 