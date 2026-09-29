# frozen_string_literal: true

require "senddart/version"
require "senddart/error"
require "senddart/client"
require "senddart/emails"
require "senddart/domains"
require "senddart/audiences"
require "senddart/contacts"
require "senddart/contact_properties"
require "senddart/segments"
require "senddart/topics"
require "senddart/campaigns"
require "senddart/templates"
require "senddart/automations"
require "senddart/webhooks"
require "senddart/events"
require "senddart/api_keys"
require "senddart/logs"
require "senddart/polls"

# The SendDart API client.
#
#   SendDart.api_key = "mb_xxxxxxxxx"
#
#   sent = SendDart::Emails.send({
#     from: "Acme <hello@yourdomain.com>",
#     to: ["delivered@test.senddart.com"],
#     subject: "Hello from SendDart",
#     html: "<p>Your first email</p>"
#   })
#   sent["id"] # => "..."
#
# `delivered@test.senddart.com` is the mailbox simulator: the send is accepted and
# produces a real email object without reaching a provider. Swap in a real
# recipient when you go live — an address on a reserved documentation domain
# (example.com, .test, .invalid) is refused with 422 `reserved_recipient`.
module SendDart
  DEFAULT_BASE_URL = "https://www.senddart.com/api"

  # Per-request network timeout, in seconds, applied to both the connect
  # (open) and read phases. 0 or nil means "no timeout".
  DEFAULT_TIMEOUT = 30

  # How many times a retryable response (HTTP 429/503) is retried before
  # giving up. 0 disables retries (a single attempt).
  DEFAULT_MAX_RETRIES = 2

  class << self
    # Your API key, e.g. "mb_xxxxxxxxx". Required before any call.
    attr_accessor :api_key

    # Override the API host (defaults to https://www.senddart.com/api).
    attr_writer :base_url

    # Per-request timeout in seconds (default 30). Set 0 (or nil) for none.
    attr_writer :timeout

    # Max automatic retries on HTTP 429/503 (default 2, i.e. up to 3 tries).
    attr_writer :max_retries

    def base_url
      @base_url || DEFAULT_BASE_URL
    end

    def timeout
      @timeout.nil? ? DEFAULT_TIMEOUT : @timeout
    end

    def max_retries
      @max_retries.nil? ? DEFAULT_MAX_RETRIES : @max_retries
    end

    #   SendDart.configure do |config|
    #     config.api_key = ENV["SENDDART_API_KEY"]
    #   end
    def configure
      yield self
    end
  end
end
