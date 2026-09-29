package com.senddart.resources;

import com.senddart.ListParams;
import com.senddart.SendDartResponse;
import com.senddart.http.ApiClient;
import com.senddart.requests.CreateTemplateRequest;
import com.senddart.requests.DuplicateTemplateRequest;
import com.senddart.requests.UpdateTemplateRequest;

import java.util.Collections;

/**
 * Templates — {@code senddart.templates()}.
 *
 * <p>Every {@code /templates/:id} route accepts either the template UUID
 * <em>or</em> its {@code alias}; no other resource does. New templates start
 * as a {@code draft} and sends always use the published snapshot, so call
 * {@link #publish(String)} before referencing one from a send.
 */
public final class Templates extends Resource {
    public Templates(ApiClient api) { super(api); }

    /** Returns the slim ack {@code { object: 'template', id }}. {@code POST /templates} */
    public SendDartResponse create(CreateTemplateRequest request) {
        return api.request("POST", "/templates", request);
    }

    /** Retrieve by id OR alias. {@code GET /templates/:id} */
    public SendDartResponse get(String id) {
        return api.request("GET", "/templates/" + enc(id));
    }

    /**
     * List templates. This route always applies a limit — with no pagination
     * params you get the first 20. Items use a reduced shape (no
     * {@code object}, {@code from}, {@code text} or {@code variables}); fetch
     * one with {@link #get(String)} for the full object. {@code GET /templates}
     */
    public SendDartResponse list() { return list(null); }

    public SendDartResponse list(ListParams params) {
        return api.request("GET", "/templates" + paginate(params));
    }

    /** Returns the slim ack. {@code PATCH /templates/:id} */
    public SendDartResponse update(String id, UpdateTemplateRequest request) {
        return api.request("PATCH", "/templates/" + enc(id), request);
    }

    /** Duplicate a template. {@code POST /templates/:id/duplicate} */
    public SendDartResponse duplicate(String id) {
        return api.request("POST", "/templates/" + enc(id) + "/duplicate", Collections.emptyMap());
    }

    public SendDartResponse duplicate(String id, DuplicateTemplateRequest request) {
        return api.request("POST", "/templates/" + enc(id) + "/duplicate", request);
    }

    /** Publish a template (make its latest draft live). {@code POST /templates/:id/publish} */
    public SendDartResponse publish(String id) {
        return api.request("POST", "/templates/" + enc(id) + "/publish");
    }

    /** {@code DELETE /templates/:id} */
    public SendDartResponse remove(String id) {
        return api.request("DELETE", "/templates/" + enc(id));
    }
}
