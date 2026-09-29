package com.senddart.resources;

import com.senddart.ListParams;
import com.senddart.SendDartResponse;
import com.senddart.http.ApiClient;

/** Read-only results of the in-email poll widget — {@code senddart.polls()}. */
public final class Polls extends Resource {
    public Polls(ApiClient api) { super(api); }

    /** One summary row per email that has poll responses. {@code GET /polls} */
    public SendDartResponse list() { return list(null); }

    public SendDartResponse list(ListParams params) {
        return api.request("GET", "/polls" + paginate(params));
    }

    /** The aggregated answer breakdown for one email. {@code GET /polls/:emailId} */
    public SendDartResponse get(String emailId) {
        return api.request("GET", "/polls/" + enc(emailId));
    }
}
