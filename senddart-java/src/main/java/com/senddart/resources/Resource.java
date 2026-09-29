package com.senddart.resources;

import com.senddart.ListParams;
import com.senddart.http.ApiClient;
import com.senddart.http.Query;
import com.senddart.http.Urls;

/** Shared plumbing for all resource services. */
public abstract class Resource {
    protected final ApiClient api;

    protected Resource(ApiClient api) { this.api = api; }

    /** Percent-encode a path segment (ids, emails, …). */
    protected static String enc(String segment) { return Urls.encode(segment); }

    /** Build a {@code ?limit=&after=&before=} query string from pagination params. */
    protected static String paginate(ListParams params) {
        Query q = new Query();
        if (params != null) params.applyTo(q);
        return q.toString();
    }
}
