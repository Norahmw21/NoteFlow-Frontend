package com.example.noteflowfrontend.core.dto;

public record AuthResponse(boolean ok, String token, String username, String email) {}
