package com.example.noteflowfrontend.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class JwtUtil {
    private static final ObjectMapper M = new ObjectMapper();

    /** Try to extract a numeric user id from the JWT. Supports common claim names. */
    public static Long extractUserIdFromBearer() {
        try {
            String token = ApiClient.getBearer();
            if (token == null || token.isBlank()) return null;

            // Strip "Bearer " if someone passed that
            if (token.startsWith("Bearer ")) token = token.substring(7);

            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<?,?> p = M.readValue(payloadJson, Map.class);

            Object[] candidates = new Object[] {
                    p.get("userId"), p.get("uid"), p.get("id"), p.get("sub")
            };
            for (Object c : candidates) {
                if (c == null) continue;
                String s = String.valueOf(c);
                // accept pure numeric ids
                if (s.matches("\\d+")) return Long.valueOf(s);
            }
            return null;
        } catch (Exception ignore) {
            return null;
        }
    }
}
