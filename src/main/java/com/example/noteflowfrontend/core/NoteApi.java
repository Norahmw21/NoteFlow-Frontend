package com.example.noteflowfrontend.core;

import com.example.noteflowfrontend.core.dto.NoteDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteApi {

    /**
     * Cached user ID to prevent repeated lookups within a session.
     * It's volatile to ensure visibility across threads, though the synchronized uid() method also guarantees this.
     */
    private static volatile Long cachedUserId = null;

    private static String base(long userId) {
        return "/users/" + userId + "/notes";
    }

    /**
     * Resolves the user ID from the JWT or the /api/me endpoint, with caching.
     * This method is synchronized to ensure that in a multi-threaded scenario,
     * the lookup is only performed once.
     *
     * @return The user's ID.
     * @throws Exception if the user ID cannot be resolved.
     */
    private static synchronized long uid() throws Exception {
        // 1. Return the cached ID if it's already available.
        if (cachedUserId != null) {
            return cachedUserId;
        }

        // 2. If not cached, try to extract it from the JWT.
        Long fromJwt = JwtUtil.extractUserIdFromBearer();
        if (fromJwt != null) {
            cachedUserId = fromJwt; // Cache the ID.
            return cachedUserId;
        }

        // 3. Fallback: ask the backend who the current user is.
        Map me = ApiClient.get("/me", Map.class);
        Object id = (me == null) ? null : me.get("id");
        if (id == null) {
            throw new IllegalStateException("Unable to resolve user id from JWT or /api/me");
        }

        // 4. Parse and cache the ID from the API response.
        cachedUserId = Long.parseLong(String.valueOf(id));
        return cachedUserId;
    }

    /**
     * Clears the cached user ID. This should be called when the user logs out
     * to ensure the next user doesn't inadvertently use the previous user's ID.
     */
    public static void clearCache() {
        cachedUserId = null;
        // It's also a good practice to clear the JWT token itself from its storage location.
        // For example: JwtUtil.clearToken();
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
    public static NoteDto create(String title, String textHtml, String drawingJson) throws Exception {
        long userId = uid();
        Map<String, Object> body = new HashMap<>();
        body.put("title", (title == null || title.isBlank()) ? "Untitled" : title);
        body.put("textHtml", textHtml);
        body.put("drawingJson", drawingJson);
        return ApiClient.post(base(userId), body, NoteDto.class);
    }

    public static NoteDto update(long id, String title, String textHtml, String drawingJson) throws Exception {
        long userId = uid();
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("textHtml", textHtml);
        body.put("drawingJson", drawingJson);
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
