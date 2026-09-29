package com.senddart.resources;

import com.senddart.ListParams;
import com.senddart.SendDartResponse;
import com.senddart.http.ApiClient;
import com.senddart.http.Query;
import com.senddart.requests.ClaimDomainRequest;
import com.senddart.requests.CreateDomainRequest;
import com.senddart.requests.UpdateDomainRequest;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Sending/receiving domains — {@code senddart.domains()}. */
public final class Domains extends Resource {
    public Domains(ApiClient api) { super(api); }

    /** {@code POST /domains} */
    public SendDartResponse create(CreateDomainRequest request) {
        return api.request("POST", "/domains", request);
    }

    /** {@code GET /domains/:id} */
    /** HTTPS readiness; an unavailable host schedules server-side repair. */
    public SendDartResponse trackingHealth(String id) {
        return api.request("GET", "/domains/" + enc(id) + "/tracking-health");
    }

    public SendDartResponse get(String id) {
        return api.request("GET", "/domains/" + enc(id));
    }

    /**
     * List domains. With no pagination params the route skips paging and
     * answers with all of them — capped at <strong>1,000</strong> rows, with
     * {@code has_more} true if that ceiling bites. Rows still awaiting a
     * DNS-TXT ownership claim ({@code status: "claim"}) are excluded — reach
     * those through {@link #getClaim(String)}. {@code GET /domains}
     */
    public SendDartResponse list() { return list(null); }

    public SendDartResponse list(ListParams params) {
        return api.request("GET", "/domains" + paginate(params));
    }

    /**
     * Check a domain's live MX records before enabling inbound.
     * Returns {@code { has_mx, ours, records }}; DNS failures fail open with
     * {@code has_mx:false}. {@code GET /domains/mx-check?name=}
     */
    public SendDartResponse mxCheck(String name) {
        Query q = new Query().add("name", name);
        return api.request("GET", "/domains/mx-check" + q);
    }

    /**
     * Download the domain's DNS records as CSV text (the route returns
     * {@code text/csv}, not JSON). {@code GET /domains/:id/records.csv}
     */
    public String recordsCsv(String id) {
        byte[] bytes = api.requestRaw("GET", "/domains/" + enc(id) + "/records.csv");
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Returns the slim ack {@code { object: 'domain', id }}. {@code PATCH /domains/:id} */
    public SendDartResponse update(String id, UpdateDomainRequest request) {
        return api.request("PATCH", "/domains/" + enc(id), request);
    }

    /** Trigger DNS verification. {@code POST /domains/:id/verify} */
    public SendDartResponse verify(String id) {
        return api.request("POST", "/domains/" + enc(id) + "/verify");
    }

    /** Claim a domain already verified elsewhere. {@code POST /domains/claim} */
    public SendDartResponse claim(ClaimDomainRequest request) {
        return api.request("POST", "/domains/claim", request);
    }

    /** Retrieve a domain's claim record. {@code GET /domains/:id/claim} */
    public SendDartResponse getClaim(String id) {
        return api.request("GET", "/domains/" + enc(id) + "/claim");
    }

    /** Verify a domain claim. {@code POST /domains/:id/claim/verify} */
    public SendDartResponse verifyClaim(String id) {
        return api.request("POST", "/domains/" + enc(id) + "/claim/verify");
    }

    /**
     * Detect the domain's DNS provider and the one-click apply methods
     * available. {@code GET /domains/:id/dns/detect}
     */
    public SendDartResponse detectDns(String id) {
        return api.request("GET", "/domains/" + enc(id) + "/dns/detect");
    }

    /** Apply DNS records via the Cloudflare API, then auto-verify. {@code POST /domains/:id/dns/cloudflare} */
    public SendDartResponse applyCloudflareDns(String id, String token) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("token", token);
        return api.request("POST", "/domains/" + enc(id) + "/dns/cloudflare", body);
    }

    /** Apply DNS records via the GoDaddy API, then auto-verify. {@code POST /domains/:id/dns/godaddy} */
    public SendDartResponse applyGoDaddyDns(String id, String key, String secret) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("key", key);
        body.put("secret", secret);
        return api.request("POST", "/domains/" + enc(id) + "/dns/godaddy", body);
    }

    /**
     * Apply DNS records via the Namecheap API (existing records preserved),
     * then auto-verify. Namecheap must have the calling server's IP
     * whitelisted. {@code POST /domains/:id/dns/namecheap}
     */
    public SendDartResponse applyNamecheapDns(String id, String apiUser, String apiKey) {
        return applyNamecheapDns(id, apiUser, apiKey, null);
    }

    public SendDartResponse applyNamecheapDns(String id, String apiUser, String apiKey, String userName) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("apiUser", apiUser);
        body.put("apiKey", apiKey);
        if (userName != null) body.put("userName", userName);
        return api.request("POST", "/domains/" + enc(id) + "/dns/namecheap", body);
    }

    /** {@code DELETE /domains/:id} */
    public SendDartResponse remove(String id) {
        return api.request("DELETE", "/domains/" + enc(id));
    }
}
