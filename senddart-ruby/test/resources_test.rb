# frozen_string_literal: true

require "test_helper"

# Route-level sweep of the remaining resources: audiences, contact properties,
# topics, templates, automations, events, api keys, logs, polls.
class ResourcesTest < Minitest::Test
  include ClientStubHelper

  def test_audiences
    SendDart::Audiences.create({ name: "Newsletter" })
    assert_request :post, "/audiences"

    SendDart::Audiences.get("aud_1")
    assert_request :get, "/audiences/aud_1"

    SendDart::Audiences.list({ limit: 3 })
    assert_request :get, "/audiences?limit=3"

    SendDart::Audiences.update("aud_1", { name: "Weekly" })
    assert_request :patch, "/audiences/aud_1"

    SendDart::Audiences.import_sheet("aud_1", { url: "https://docs.google.com/x", segment_name: "June" })
    assert_request :post, "/audiences/aud_1/contacts/import-sheet"
    assert_equal "June", last_body["segment_name"]

    SendDart::Audiences.delete("aud_1")
    assert_request :delete, "/audiences/aud_1"
  end

  def test_contact_properties
    SendDart::ContactProperties.create({ key: "plan", type: "string", fallback_value: "free" })
    assert_request :post, "/contact-properties"
    assert_equal "plan", last_body["key"]

    SendDart::ContactProperties.get("prop_1")
    assert_request :get, "/contact-properties/prop_1"

    SendDart::ContactProperties.list
    assert_request :get, "/contact-properties"

    SendDart::ContactProperties.update("prop_1", { fallback_value: "pro" })
    assert_request :patch, "/contact-properties/prop_1"

    SendDart::ContactProperties.delete("prop_1")
    assert_request :delete, "/contact-properties/prop_1"
  end

  def test_topics_domain_required
    assert_raises(ArgumentError) { SendDart::Topics.create({ name: "Updates" }) }
    assert_raises(ArgumentError) { SendDart::Topics.list({}) }

    SendDart::Topics.create({ domain: "yourdomain.com", name: "Updates", default_subscription: "opt_in" })
    assert_request :post, "/topics"
    assert_equal "yourdomain.com", last_body["domain"]

    SendDart::Topics.list({ domain: "yourdomain.com", limit: 50 })
    assert_request :get, "/topics?domain=yourdomain.com&limit=50"

    SendDart::Topics.get("top_1")
    assert_request :get, "/topics/top_1"

    SendDart::Topics.update("top_1", { description: "News" })
    assert_request :patch, "/topics/top_1"

    SendDart::Topics.delete("top_1")
    assert_request :delete, "/topics/top_1"
  end

  def test_templates
    SendDart::Templates.create({ name: "Welcome", subject: "Hi", html: "<p>Hi</p>" })
    assert_request :post, "/templates"

    SendDart::Templates.get("tmpl_1")
    assert_request :get, "/templates/tmpl_1"

    SendDart::Templates.list
    assert_request :get, "/templates"

    SendDart::Templates.update("tmpl_1", { subject: "Hello" })
    assert_request :patch, "/templates/tmpl_1"

    SendDart::Templates.duplicate("tmpl_1", { name: "Welcome v2" })
    assert_request :post, "/templates/tmpl_1/duplicate"
    assert_equal "Welcome v2", last_body["name"]

    SendDart::Templates.duplicate("tmpl_1")
    assert_equal({}, last_body)

    SendDart::Templates.publish("tmpl_1")
    assert_request :post, "/templates/tmpl_1/publish"

    SendDart::Templates.delete("tmpl_1")
    assert_request :delete, "/templates/tmpl_1"
  end

  def test_automations_domain_required_and_full_surface
    assert_raises(ArgumentError) { SendDart::Automations.create({ name: "Welcome" }) }

    SendDart::Automations.create({ name: "Welcome", domain: "yourdomain.com", trigger: "contact.created" })
    assert_request :post, "/automations"
    assert_equal "yourdomain.com", last_body["domain"]

    SendDart::Automations.get("auto_1")
    assert_request :get, "/automations/auto_1"

    SendDart::Automations.list({ limit: 10 })
    assert_request :get, "/automations?limit=10"

    SendDart::Automations.update("auto_1", { status: "enabled" })
    assert_request :patch, "/automations/auto_1"

    SendDart::Automations.add_step("auto_1", { type: "send_email", config: { template_id: "tmpl_1" } })
    assert_request :post, "/automations/auto_1/steps"
    assert_equal "send_email", last_body["type"]

    # `type` must ride along: the server re-validates the whole step, so a
    # type-less body is a 422 no matter how valid the config is.
    SendDart::Automations.update_step("auto_1", "step_1",
                                        { type: "send_email", config: { template_id: "tmpl_1", subject: "Hi" } })
    assert_request :patch, "/automations/auto_1/steps/step_1"
    assert_equal "send_email", last_body["type"]
    assert_equal({ "template_id" => "tmpl_1", "subject" => "Hi" }, last_body["config"])

    SendDart::Automations.delete_step("auto_1", "step_1")
    assert_request :delete, "/automations/auto_1/steps/step_1"

    SendDart::Automations.create_with_ai("auto_1", { prompt: "Wait 2 days then send the welcome email" })
    assert_request :post, "/automations/auto_1/ai"
    assert_equal "Wait 2 days then send the welcome email", last_body["prompt"]

    SendDart::Automations.runs("auto_1", { limit: 25 })
    assert_request :get, "/automations/auto_1/runs?limit=25"

    SendDart::Automations.get_run("auto_1", "run_1")
    assert_request :get, "/automations/auto_1/runs/run_1"

    SendDart::Automations.stop("auto_1")
    assert_request :post, "/automations/auto_1/stop"

    SendDart::Automations.delete("auto_1")
    assert_request :delete, "/automations/auto_1"
  end

  def test_events_domain_required_on_send
    assert_raises(ArgumentError) { SendDart::Events.send({ event: "signup.completed", email: "a@b.com" }) }

    SendDart::Events.send(
      { event: "signup.completed", domain: "yourdomain.com", email: "a@b.com", payload: { plan: "pro" } },
      { idempotency_key: "evt-1" }
    )
    assert_request :post, "/events/send"
    assert_equal "yourdomain.com", last_body["domain"]
    assert_equal "evt-1", last_request["Idempotency-Key"]

    SendDart::Events.create({ name: "signup.completed", schema: { plan: "string" } })
    assert_request :post, "/events"

    SendDart::Events.list
    assert_request :get, "/events"

    SendDart::Events.update("evt_1", { schema: { plan: "string", seats: "number" } })
    assert_request :patch, "/events/evt_1"
    assert_equal({ "plan" => "string", "seats" => "number" }, last_body["schema"])

    SendDart::Events.delete("evt_1")
    assert_request :delete, "/events/evt_1"
  end

  def test_api_keys
    SendDart::ApiKeys.list
    assert_request :get, "/api-keys"

    SendDart::ApiKeys.list({ limit: 10 })
    assert_request :get, "/api-keys?limit=10"
  end

  # Key lifecycle is dashboard-only, so the SDK exposes nothing that would
  # reach POST /api-keys, PATCH /api-keys/:id or DELETE /api-keys/:id.
  def test_api_key_lifecycle_methods_are_absent
    %i[create update delete remove revoke].each do |name|
      refute_respond_to SendDart::ApiKeys, name, "SendDart::ApiKeys.#{name} must not exist"
    end
  end

  def test_logs_with_filters
    SendDart::Logs.list({ limit: 100, method: "POST", status: 429 })
    assert_request :get, "/logs?limit=100&method=POST&status=429"

    SendDart::Logs.get("log_1")
    assert_request :get, "/logs/log_1"
  end

  def test_automation_runs_status_filter_accepts_a_string_or_an_array
    SendDart::Automations.runs("auto_1", { status: "failed" })
    assert_request :get, "/automations/auto_1/runs?status=failed"

    # The server takes a comma-separated list; an Array is joined for you.
    SendDart::Automations.runs("auto_1", { limit: 5, status: %w[failed skipped] })
    assert_request :get, "/automations/auto_1/runs?limit=5&status=failed%2Cskipped"
  end

  def test_polls
    SendDart::Polls.list({ limit: 10 })
    assert_request :get, "/polls?limit=10"

    SendDart::Polls.get("email_1")
    assert_request :get, "/polls/email_1"
  end
end
