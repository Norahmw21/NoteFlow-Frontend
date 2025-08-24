package com.example.noteflowfrontend.core;

import com.example.noteflowfrontend.core.dto.NoteDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteApi {

    private static String base(long userId) { return "/users/" + userId + "/notes"; }

    /** Resolve userId directly from JWT; fallback to /api/me if token lacks numeric id. */
    private static long uid() throws Exception {
        Long fromJwt = JwtUtil.extractUserIdFromBearer();
        if (fromJwt != null) return fromJwt;

        // Fallback: ask backend who I am (returns { id, username, email, ... })
        Map me = ApiClient.get("/me", Map.class);
        Object id = (me == null) ? null : me.get("id");
        if (id == null) throw new IllegalStateException("Unable to resolve user id from JWT or /api/me");
        return Long.parseLong(String.valueOf(id));
    }

    // ================= Lists =================
    public static List<NoteDto> list() throws Exception {
        long userId = uid();
        NoteDto[] arr = ApiClient.get(base(userId), NoteDto[].class);
        return Arrays.asList(arr);
    }
    public static List<NoteDto> listFavorites() throws Exception {
        long userId = uid();
        NoteDto[] arr = ApiClient.get(base(userId) + "/favorites", NoteDto[].class);
        return Arrays.asList(arr);
    }
    public static List<NoteDto> listTrash() throws Exception {
        long userId = uid();
        NoteDto[] arr = ApiClient.get(base(userId) + "/trash", NoteDto[].class);
        return Arrays.asList(arr);
    }

    public static NoteDto get(long id) throws Exception {
        long userId = uid();
        return ApiClient.get(base(userId) + "/" + id, NoteDto.class);
    }

    // ================= Create / Update =================
    /** Your backend Note entity uses title, textHtml, drawingJson — send exactly those. */
    public static NoteDto create(String title, String textHtml, String drawingJson) throws Exception {
        long userId = uid();
        Map<String, Object> body = new HashMap<>();
        body.put("title", (title == null || title.isBlank()) ? "Untitled" : title);
        body.put("textHtml",    textHtml);
        body.put("drawingJson", drawingJson);
        return ApiClient.post(base(userId), body, NoteDto.class);
    }

    public static NoteDto update(long id, String title, String textHtml, String drawingJson) throws Exception {
        long userId = uid();
        Map<String, Object> body = new HashMap<>();
        body.put("title",        title);
        body.put("textHtml",     textHtml);
        body.put("drawingJson",  drawingJson);
        return ApiClient.put(base(userId) + "/" + id, body, NoteDto.class);
    }

    // ================= Favorite / Trash / Delete =================
    public static NoteDto setFavorite(long id, boolean value) throws Exception {
        long userId = uid();
        return ApiClient.put(base(userId) + "/" + id + "/favorite?value=" + value, Map.of(), NoteDto.class);
    }

    public static NoteDto setTrashed(long id, boolean value) throws Exception {
        long userId = uid();
        return ApiClient.put(base(userId) + "/" + id + "/trash?value=" + value, Map.of(), NoteDto.class);
    }

    public static void deletePermanent(long id) throws Exception {
        long userId = uid();
        ApiClient.delete(base(userId) + "/" + id, null);
    }
}
