package com.senddart.resources;

import com.senddart.SendDartResponse;
import com.senddart.http.ApiClient;
import com.senddart.http.Query;
import com.senddart.requests.ListLogsParams;

/** API request logs — {@code senddart.logs()} (read-only). */
public final class Logs extends Resource {
    public Logs(ApiClient api) { super(api); }

    /** {@code GET /logs} */
    public SendDartResponse list() { return list(null); }

    /**
     * List API request logs. Cursor-paginated with optional server-side
     * {@code method} / {@code status} filters.
     */
    public SendDartResponse list(ListLogsParams params) {
        Query q = new Query();
        if (params != null) {
            q.add("limit", params.getLimit())
             .add("after", params.getAfter())
             .add("before", params.getBefore())
             .add("method", params.getMethod())
             .add("status", params.getStatus());
        }
        return api.request("GET", "/logs" + q);
    }

    /** Retrieve one log entry (includes request/response bodies). {@code GET /logs/:id} */
    public SendDartResponse get(String id) {
        return api.request("GET", "/logs/" + enc(id));
    }
}
