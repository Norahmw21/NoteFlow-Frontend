package com.example.noteflowfrontend.core;

import com.example.noteflowfrontend.core.dto.NoteDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteApi {

    private static volatile Long cachedUserId = null;

    private static String base(long userId) {
        return "/users/" + userId + "/notes";
    }

    private static synchronized long uid() throws Exception {
        if (cachedUserId != null) return cachedUserId;

        Long fromJwt = JwtUtil.extractUserIdFromBearer();
        if (fromJwt != null) {
            cachedUserId = fromJwt;
            return cachedUserId;
        }

        Map me = ApiClient.get("/me", Map.class);
        Object id = (me == null) ? null : me.get("id");
        if (id == null) throw new IllegalStateException("Unable to resolve user id from JWT or /api/me");

        cachedUserId = Long.parseLong(String.valueOf(id));
        return cachedUserId;
    }

    public static void clearCache() { cachedUserId = null; }

    // ================= Lists =================
    public static List<NoteDto> list() throws Exception {
        long userId = uid();
        NoteDto[] arr = ApiClient.get(base(userId), NoteDto[].class);
        return Arrays.asList(arr);
    }

    // NEW: list with filters
    public static List<NoteDto> list(String tagName, String tagColor) throws Exception {
        long userId = uid();
        String q = "";
        if (tagName != null && !tagName.isBlank()) q += (q.isEmpty() ? "?" : "&") + "tagName=" + encode(tagName);
        if (tagColor != null && !tagColor.isBlank()) q += (q.isEmpty() ? "?" : "&") + "tagColor=" + encode(tagColor);
        NoteDto[] arr = ApiClient.get(base(userId) + q, NoteDto[].class);
        return Arrays.asList(arr);
    }

    public static NoteDto get(long id) throws Exception {
        long userId = uid();
        return ApiClient.get(base(userId) + "/" + id, NoteDto.class);
    }

    // NoteApi.java
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

    // ================= Create / Update =================
    public static NoteDto create(String title, String textHtml, String drawingJson,
                                 String tagName, String tagColor) throws Exception {
        long userId = uid();
        Map<String, Object> body = new HashMap<>();
        body.put("title", (title == null || title.isBlank()) ? "Untitled" : title);
        body.put("textHtml", textHtml);
        body.put("drawingJson", drawingJson);
        body.put("tagName", tagName);
        body.put("tagColor", tagColor);
        return ApiClient.post(base(userId), body, NoteDto.class);
    }

    // Overload to keep old call sites working
    public static NoteDto create(String title, String textHtml, String drawingJson) throws Exception {
        return create(title, textHtml, drawingJson, null, null);
    }

    public static NoteDto update(long id, String title, String textHtml, String drawingJson,
                                 String tagName, String tagColor) throws Exception {
        long userId = uid();
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("textHtml", textHtml);
        body.put("drawingJson", drawingJson);
        body.put("tagName", tagName);
        body.put("tagColor", tagColor);
        return ApiClient.put(base(userId) + "/" + id, body, NoteDto.class);
    }

    // Overload to keep old call sites working
    public static NoteDto update(long id, String title, String textHtml, String drawingJson) throws Exception {
        return update(id, title, textHtml, drawingJson, null, null);
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

    private static String encode(String v) {
        return java.net.URLEncoder.encode(v, java.nio.charset.StandardCharsets.UTF_8);
    }
}
