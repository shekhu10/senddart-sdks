package com.senddart.resources;

import com.senddart.ListParams;
import com.senddart.SendDartResponse;
import com.senddart.http.ApiClient;
import com.senddart.http.Query;
import com.senddart.requests.CreateSegmentRequest;
import com.senddart.requests.UpdateSegmentRequest;

/**
 * Segments (DOMAIN-FIRST) — {@code domain} is REQUIRED on create and list.
 * Segment names are unique within a domain; every domain carries an
 * auto-created "General" segment.
 */
public final class Segments extends Resource {
    public Segments(ApiClient api) { super(api); }

    /** {@code POST /segments} — {@code domain} is required on the request. */
    public SendDartResponse create(CreateSegmentRequest request) {
        return api.request("POST", "/segments", request);
    }

    /** {@code GET /segments/:id} */
    public SendDartResponse get(String id) {
        return api.request("GET", "/segments/" + enc(id));
    }

    /** List a domain's segments. {@code GET /segments?domain=} */
    public SendDartResponse list(String domain) { return list(domain, null); }

    public SendDartResponse list(String domain, ListParams params) {
        Query q = new Query().add("domain", domain);
        if (params != null) params.applyTo(q);
        return api.request("GET", "/segments" + q);
    }

    /**
     * Preview the contacts a segment currently resolves to (filter matches plus
     * explicit memberships). Items use a reduced contact shape — no
     * {@code object} key and no {@code properties}. With no pagination params
     * the route skips paging and answers with every match up to a ceiling of
     * <strong>1,000</strong> rows; {@code has_more} goes true when a segment is
     * larger than that, so page on with {@code after} instead of treating one
     * unpaged call as complete. {@code GET /segments/:id/contacts}
     */
    public SendDartResponse contacts(String id) { return contacts(id, null); }

    public SendDartResponse contacts(String id, ListParams params) {
        return api.request("GET", "/segments/" + enc(id) + "/contacts" + paginate(params));
    }

    /** {@code PATCH /segments/:id} */
    public SendDartResponse update(String id, UpdateSegmentRequest request) {
        return api.request("PATCH", "/segments/" + enc(id), request);
    }

    /** {@code DELETE /segments/:id} */
    public SendDartResponse remove(String id) {
        return api.request("DELETE", "/segments/" + enc(id));
    }
}
