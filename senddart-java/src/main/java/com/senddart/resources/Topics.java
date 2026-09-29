package com.senddart.resources;

import com.senddart.ListParams;
import com.senddart.SendDartResponse;
import com.senddart.http.ApiClient;
import com.senddart.http.Query;
import com.senddart.requests.CreateTopicRequest;
import com.senddart.requests.UpdateTopicRequest;

/** Topics (DOMAIN-FIRST) — {@code domain} is REQUIRED on create and list. */
public final class Topics extends Resource {
    public Topics(ApiClient api) { super(api); }

    /** {@code POST /topics} — {@code domain} is required on the request. */
    public SendDartResponse create(CreateTopicRequest request) {
        return api.request("POST", "/topics", request);
    }

    /** {@code GET /topics/:id} */
    public SendDartResponse get(String id) {
        return api.request("GET", "/topics/" + enc(id));
    }

    /**
     * List a domain's topics. With no pagination params the route skips paging
     * and answers with the whole pool — capped at <strong>1,000</strong> rows,
     * with {@code has_more} true if that ceiling bites.
     * {@code GET /topics?domain=}
     */
    public SendDartResponse list(String domain) { return list(domain, null); }

    public SendDartResponse list(String domain, ListParams params) {
        Query q = new Query().add("domain", domain);
        if (params != null) params.applyTo(q);
        return api.request("GET", "/topics" + q);
    }

    /** {@code PATCH /topics/:id} */
    public SendDartResponse update(String id, UpdateTopicRequest request) {
        return api.request("PATCH", "/topics/" + enc(id), request);
    }

    /** {@code DELETE /topics/:id} */
    public SendDartResponse remove(String id) {
        return api.request("DELETE", "/topics/" + enc(id));
    }
}
