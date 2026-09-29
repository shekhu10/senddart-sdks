import senddart

from .helpers import RecordingTestCase


class TestAudiences(RecordingTestCase):
    def test_crud(self):
        senddart.Audiences.create({"name": "General"})
        self.assertCall("POST", "/audiences", {"name": "General"})
        senddart.Audiences.get("aud_1")
        self.assertCall("GET", "/audiences/aud_1")
        senddart.Audiences.list({"limit": 5})
        self.assertCall("GET", "/audiences?limit=5")
        senddart.Audiences.update("aud_1", {"name": "Renamed"})
        self.assertCall("PATCH", "/audiences/aud_1", {"name": "Renamed"})
        senddart.Audiences.remove("aud_1")
        self.assertCall("DELETE", "/audiences/aud_1")

    def test_import_sheet(self):
        payload = {"url": "https://docs.google.com/spreadsheets/d/x", "segment_name": "Sheet"}
        senddart.Audiences.import_sheet("aud_1", payload)
        self.assertCall("POST", "/audiences/aud_1/contacts/import-sheet", payload)


class TestContactProperties(RecordingTestCase):
    def test_crud(self):
        senddart.ContactProperties.create({"key": "plan", "type": "string"})
        self.assertCall("POST", "/contact-properties", {"key": "plan", "type": "string"})
        senddart.ContactProperties.get("prop_1")
        self.assertCall("GET", "/contact-properties/prop_1")
        senddart.ContactProperties.list()
        self.assertCall("GET", "/contact-properties")
        senddart.ContactProperties.update("prop_1", {"fallback_value": "free"})
        self.assertCall("PATCH", "/contact-properties/prop_1", {"fallback_value": "free"})
        senddart.ContactProperties.remove("prop_1")
        self.assertCall("DELETE", "/contact-properties/prop_1")


class TestTemplates(RecordingTestCase):
    def test_crud(self):
        params = {"name": "Welcome", "subject": "Hi {{first_name}}", "html": "<p>Hi</p>"}
        senddart.Templates.create(params)
        self.assertCall("POST", "/templates", params)
        senddart.Templates.get("tmpl_1")
        self.assertCall("GET", "/templates/tmpl_1")
        senddart.Templates.list({"limit": 10})
        self.assertCall("GET", "/templates?limit=10")
        senddart.Templates.update("tmpl_1", {"name": "Welcome v2"})
        self.assertCall("PATCH", "/templates/tmpl_1", {"name": "Welcome v2"})
        senddart.Templates.remove("tmpl_1")
        self.assertCall("DELETE", "/templates/tmpl_1")

    def test_duplicate_defaults_to_empty_body(self):
        senddart.Templates.duplicate("tmpl_1")
        self.assertCall("POST", "/templates/tmpl_1/duplicate", {})
        senddart.Templates.duplicate("tmpl_1", {"name": "Copy"})
        self.assertCall("POST", "/templates/tmpl_1/duplicate", {"name": "Copy"})

    def test_publish(self):
        senddart.Templates.publish("tmpl_1")
        self.assertCall("POST", "/templates/tmpl_1/publish")


class TestAutomations(RecordingTestCase):
    def test_create_requires_domain_in_params(self):
        params = {"name": "Welcome series", "domain": "acme.com", "trigger": "contact.created"}
        senddart.Automations.create(params)
        self.assertCall("POST", "/automations", params)

    def test_steps(self):
        step = {"type": "send_email", "config": {"template_id": "tmpl_1"}}
        senddart.Automations.add_step("auto_1", step)
        self.assertCall("POST", "/automations/auto_1/steps", step)
        # `type` is required on every PATCH: the server re-validates the whole
        # step, so a config-only body is a 422. `key` is create-only.
        patch = {"type": "send_email", "config": {"template_id": "tmpl_2"}}
        senddart.Automations.update_step("auto_1", "step_1", patch)
        self.assertCall("PATCH", "/automations/auto_1/steps/step_1", patch)
        senddart.Automations.delete_step("auto_1", "step_1")
        self.assertCall("DELETE", "/automations/auto_1/steps/step_1")

    def test_ai(self):
        params = {"prompt": "Wait 2 days then send the welcome email"}
        senddart.Automations.create_with_ai("auto_1", params)
        self.assertCall("POST", "/automations/auto_1/ai", params)

    def test_runs(self):
        senddart.Automations.runs("auto_1", {"limit": 25})
        self.assertCall("GET", "/automations/auto_1/runs?limit=25")
        senddart.Automations.get_run("auto_1", "run_1")
        self.assertCall("GET", "/automations/auto_1/runs/run_1")

    def test_runs_status_filter(self):
        senddart.Automations.runs("auto_1", {"status": "running,failed"})
        self.assertCall("GET", "/automations/auto_1/runs?status=running%2Cfailed")

    def test_runs_no_params(self):
        senddart.Automations.runs("auto_1")
        self.assertCall("GET", "/automations/auto_1/runs")

    def test_stop_update_remove(self):
        senddart.Automations.stop("auto_1")
        self.assertCall("POST", "/automations/auto_1/stop")
        senddart.Automations.update("auto_1", {"status": "enabled"})
        self.assertCall("PATCH", "/automations/auto_1", {"status": "enabled"})
        senddart.Automations.remove("auto_1")
        self.assertCall("DELETE", "/automations/auto_1")
        senddart.Automations.list()
        self.assertCall("GET", "/automations")


class TestEvents(RecordingTestCase):
    def test_send_domain_required_payload(self):
        params = {
            "event": "signup.completed",
            "domain": "acme.com",
            "email": "user@example.com",
            "payload": {"plan": "pro"},
        }
        senddart.Events.send(params)
        self.assertCall("POST", "/events/send", params)

    def test_send_with_idempotency(self):
        params = {"event": "x", "domain": "acme.com", "email": "a@b.com"}
        senddart.Events.send(params, options={"idempotency_key": "evt-1"})
        self.assertCall("POST", "/events/send", params, {"idempotency_key": "evt-1"})

    def test_definitions(self):
        senddart.Events.create({"name": "signup.completed", "schema": {"plan": "string"}})
        self.assertCall(
            "POST", "/events", {"name": "signup.completed", "schema": {"plan": "string"}}
        )
        senddart.Events.list({"limit": 10})
        self.assertCall("GET", "/events?limit=10")
        senddart.Events.remove("evt_1")
        self.assertCall("DELETE", "/events/evt_1")

    def test_update_schema(self):
        senddart.Events.update("evt_1", {"schema": {"plan": "string", "seats": "number"}})
        self.assertCall(
            "PATCH", "/events/evt_1", {"schema": {"plan": "string", "seats": "number"}}
        )


class TestApiKeys(RecordingTestCase):
    def test_list(self):
        senddart.ApiKeys.list()
        self.assertCall("GET", "/api-keys")

    def test_list_paginated(self):
        senddart.ApiKeys.list({"limit": 10, "after": "41"})
        self.assertCall("GET", "/api-keys?limit=10&after=41")

    def test_lifecycle_is_dashboard_only(self):
        """Keys are created, re-scoped and revoked in the dashboard only, so
        the SDK exposes no method that would reach those endpoints."""
        for name in ("create", "update", "remove", "delete", "revoke"):
            self.assertFalse(
                hasattr(senddart.ApiKeys, name),
                f"ApiKeys.{name} must not exist",
            )


class TestLogs(RecordingTestCase):
    def test_list_with_filters(self):
        senddart.Logs.list({"limit": 100, "method": "POST", "status": 429})
        self.assertCall("GET", "/logs?limit=100&method=POST&status=429")

    def test_list_plain(self):
        senddart.Logs.list()
        self.assertCall("GET", "/logs")

    def test_get(self):
        senddart.Logs.get("log_1")
        self.assertCall("GET", "/logs/log_1")


class TestPolls(RecordingTestCase):
    def test_list_and_get(self):
        senddart.Polls.list({"limit": 10})
        self.assertCall("GET", "/polls?limit=10")
        senddart.Polls.get("em_1")
        self.assertCall("GET", "/polls/em_1")
