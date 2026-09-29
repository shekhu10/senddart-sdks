# frozen_string_literal: true

require "test_helper"

class CampaignsTest < Minitest::Test
  include ClientStubHelper

  def test_create_requires_domain
    assert_raises(ArgumentError) do
      SendDart::Campaigns.create({ from: "a@b.com", subject: "s", html: "<p>x</p>" })
    end
    assert_empty @requests
  end

  def test_create_posts_domain_first_body
    SendDart::Campaigns.create({
                                   domain: "yourdomain.com",
                                   from: "Acme <hello@yourdomain.com>",
                                   subject: "Big news",
                                   html: "<p>Hello</p>",
                                   segment_id: "seg_1"
                                 })
    assert_request :post, "/campaigns"
    assert_equal "yourdomain.com", last_body["domain"]
    assert_equal "seg_1", last_body["segment_id"]
  end

  def test_send_now_and_scheduled
    SendDart::Campaigns.send("camp_1")
    assert_request :post, "/campaigns/camp_1/send"
    assert_equal({}, last_body)

    SendDart::Campaigns.send("camp_1", { scheduled_at: "2026-08-01T09:00:00Z" })
    assert_request :post, "/campaigns/camp_1/send"
    assert_equal "2026-08-01T09:00:00Z", last_body["scheduled_at"]
  end

  def test_lifecycle_and_analytics_routes
    SendDart::Campaigns.get("camp_1")
    assert_request :get, "/campaigns/camp_1"

    SendDart::Campaigns.list({ limit: 25, after: "camp_0" })
    assert_request :get, "/campaigns?limit=25&after=camp_0"

    SendDart::Campaigns.update("camp_1", { subject: "Bigger news" })
    assert_request :patch, "/campaigns/camp_1"

    SendDart::Campaigns.cancel("camp_1")
    assert_request :post, "/campaigns/camp_1/cancel"

    SendDart::Campaigns.stats("camp_1")
    assert_request :get, "/campaigns/camp_1/stats"

    SendDart::Campaigns.engagement("camp_1")
    assert_request :get, "/campaigns/camp_1/engagement"

    SendDart::Campaigns.ab("camp_1")
    assert_request :get, "/campaigns/camp_1/ab"

    SendDart::Campaigns.delete("camp_1")
    assert_request :delete, "/campaigns/camp_1"
  end
end
