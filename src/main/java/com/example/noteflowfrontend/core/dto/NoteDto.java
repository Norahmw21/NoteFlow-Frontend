package com.example.noteflowfrontend.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NoteDto(
        Long id,
        String title,
        String textHtml,
        String drawingJson,
        boolean favorite,
        boolean trashed,
        String deletedAt,
        String createdAt,
        String updatedAt,
        String tagName,   // NEW
        String tagColor   // NEW (#RRGGBB)
) {}
