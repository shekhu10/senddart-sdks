package com.senddart.resources;

import com.senddart.ListParams;
import com.senddart.SendDartResponse;
import com.senddart.http.ApiClient;
import com.senddart.http.Query;
import com.senddart.requests.AutomationAiRequest;
import com.senddart.requests.AutomationStep;
import com.senddart.requests.CreateAutomationRequest;
import com.senddart.requests.ListAutomationRunsParams;
import com.senddart.requests.UpdateAutomationRequest;
import com.senddart.requests.UpdateAutomationStepRequest;

/**
 * Automations (DOMAIN-FIRST) — {@code domain} is REQUIRED on create; only
 * {@code events().send(...)} calls carrying the same domain trigger them.
 */
public final class Automations extends Resource {
    public Automations(ApiClient api) { super(api); }

    /** {@code POST /automations} — {@code domain} is required on the request. */
    public SendDartResponse create(CreateAutomationRequest request) {
        return api.request("POST", "/automations", request);
    }

    /** {@code GET /automations/:id} */
    public SendDartResponse get(String id) {
        return api.request("GET", "/automations/" + enc(id));
    }

    /** {@code GET /automations} */
    public SendDartResponse list() { return list(null); }

    public SendDartResponse list(ListParams params) {
        return api.request("GET", "/automations" + paginate(params));
    }

    /** {@code PATCH /automations/:id} */
    public SendDartResponse update(String id, UpdateAutomationRequest request) {
        return api.request("PATCH", "/automations/" + enc(id), request);
    }

    /**
     * Append a step. The automation must be DISABLED first, and the trigger is
     * set on the automation rather than added as a step (both are 422s).
     * {@code POST /automations/:id/steps}
     */
    public SendDartResponse addStep(String id, AutomationStep step) {
        return api.request("POST", "/automations/" + enc(id) + "/steps", step);
    }

    /**
     * Replace a step's type and config. The automation must be DISABLED first,
     * and {@code type} is required even when only the config changes — the
     * step's graph {@code key} and {@code position} are NOT editable here, see
     * {@link UpdateAutomationStepRequest}.
     * {@code PATCH /automations/:id/steps/:stepId}
     */
    public SendDartResponse updateStep(String id, String stepId, UpdateAutomationStepRequest step) {
        return api.request("PATCH", "/automations/" + enc(id) + "/steps/" + enc(stepId), step);
    }

    /** Delete a step. The automation must be DISABLED first. {@code DELETE /automations/:id/steps/:stepId} */
    public SendDartResponse deleteStep(String id, String stepId) {
        return api.request("DELETE", "/automations/" + enc(id) + "/steps/" + enc(stepId));
    }

    /** List an automation's runs. {@code GET /automations/:id/runs} */
    public SendDartResponse runs(String id) { return runs(id, (ListParams) null); }

    public SendDartResponse runs(String id, ListParams params) {
        return api.request("GET", "/automations/" + enc(id) + "/runs" + paginate(params));
    }

    /**
     * List an automation's runs, optionally filtered by one or more run
     * statuses (filtering is applied before pagination).
     * {@code GET /automations/:id/runs?status=}
     */
    public SendDartResponse runs(String id, ListAutomationRunsParams params) {
        Query q = new Query();
        if (params != null) {
            q.add("limit", params.getLimit())
             .add("after", params.getAfter())
             .add("before", params.getBefore())
             .add("status", params.getStatus());
        }
        return api.request("GET", "/automations/" + enc(id) + "/runs" + q);
    }

    /** Retrieve a single run with its step trace. {@code GET /automations/:id/runs/:runId} */
    public SendDartResponse getRun(String id, String runId) {
        return api.request("GET", "/automations/" + enc(id) + "/runs/" + enc(runId));
    }

    /**
     * Build the automation's steps from a natural-language prompt. Requires a
     * STOPPED automation and spends AI credits.
     * {@code POST /automations/:id/ai}
     */
    public SendDartResponse createWithAi(String id, AutomationAiRequest request) {
        return api.request("POST", "/automations/" + enc(id) + "/ai", request);
    }

    /** Shorthand for {@link #createWithAi(String, AutomationAiRequest)} with just a prompt. */
    public SendDartResponse createWithAi(String id, String prompt) {
        return createWithAi(id, AutomationAiRequest.builder().prompt(prompt).build());
    }

    /** Stop an automation — prevents new runs; in-progress runs finish. {@code POST /automations/:id/stop} */
    public SendDartResponse stop(String id) {
        return api.request("POST", "/automations/" + enc(id) + "/stop");
    }

    /** {@code DELETE /automations/:id} */
    public SendDartResponse remove(String id) {
        return api.request("DELETE", "/automations/" + enc(id));
    }
}
