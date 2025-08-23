package com.example.noteflowfrontend.core;

import com.example.noteflowfrontend.core.dto.*;

public class Auth {
    private static String token;
    private static UserDto me;

    public static boolean register(String u, String e, String p) {
        try {
            var resp = ApiClient.post("/auth/register",
                    new RegisterRequest(u, e, p),
                    AuthResponse.class);
            if (resp.ok() && resp.token() != null) {
                token = resp.token();
                ApiClient.setBearer(token);
                me = new UserDto(null, resp.username(), resp.email());
                return true;
            }
        } catch (Exception ex) {
            System.err.println("Register error: " + ex.getMessage());
        }
        return false;
    }

    public static boolean login(String userOrEmail, String password) {
        try {
            var resp = ApiClient.post("/auth/login",
                    new LoginRequest(userOrEmail, password),
                    AuthResponse.class);
            if (resp.ok() && resp.token() != null) {
                token = resp.token();
                ApiClient.setBearer(token);
                me = new UserDto(null, resp.username(), resp.email());
                return true;
            }
        } catch (Exception ex) {
            System.err.println("Login error: " + ex.getMessage());
        }
        return false;
    }

    public static void logout() {
        token = null; me = null; ApiClient.clearBearer();
    }
    public static boolean isLoggedIn() { return token != null; }
    public static UserDto currentUser() { return me; }
}
