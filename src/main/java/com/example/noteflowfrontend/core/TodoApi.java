package com.example.noteflowfrontend.core;

import com.example.noteflowfrontend.core.dto.ToDoListDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoApi {

    private static final String BASE = "/todo";

    /** Resolve userId directly from JWT or fallback /me */
    private static long uid() throws Exception {
        Long fromJwt = JwtUtil.extractUserIdFromBearer();
        if (fromJwt != null) return fromJwt;

        Map<?, ?> me = ApiClient.get("/me", Map.class);
        Object id = (me == null) ? null : me.get("id");
        if (id == null) throw new IllegalStateException("Unable to resolve user id from JWT or /me");
        return Long.parseLong(String.valueOf(id));
    }

    /** Get all tasks for the current user */
    public static List<ToDoListDto> list() throws Exception {
        long userId = uid();
        ToDoListDto[] arr = ApiClient.get(BASE + "/user/" + userId, ToDoListDto[].class);
        return Arrays.asList(arr);
    }

    /** Create a new task */
    public static ToDoListDto create(String taskName, String status, String importance,
                                     LocalDateTime startDate, LocalDateTime endDate) throws Exception {
        long userId = uid(); // get current user
        Map<String, Object> body = new HashMap<>();
        body.put("taskName", taskName);
        body.put("status", status);
        body.put("taskImportance", importance);
        body.put("startDate", startDate != null ? startDate.toString() : null);
        body.put("endDate", endDate != null ? endDate.toString() : null);

        // append userId as query param
        return ApiClient.post(BASE + "?userId=" + userId, body, ToDoListDto.class);
    }


    public static ToDoListDto update(ToDoListDto dto) throws Exception {
        long userId = uid();
        return ApiClient.put(BASE + "/" + dto.getTaskId() + "?userId=" + userId, dto, ToDoListDto.class);
    }



    /** Delete a task */
    public static void delete(Long taskId) throws Exception {
        ApiClient.delete(BASE + "/" + taskId, Void.class);
    }
}
