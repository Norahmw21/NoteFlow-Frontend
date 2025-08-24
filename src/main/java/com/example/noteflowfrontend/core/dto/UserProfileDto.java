package com.example.noteflowfrontend.core.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserProfileDto(
        @JsonAlias({"userId", "id"}) Long id,
        String username,
        String email,
        String avatarUrl,
        String phone
) {
}
