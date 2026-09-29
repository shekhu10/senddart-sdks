# frozen_string_literal: true

require_relative "lib/senddart/version"

Gem::Specification.new do |spec|
  spec.name          = "senddart"
  spec.version       = SendDart::VERSION
  spec.authors       = ["SendDart"]
  spec.email         = ["support@senddart.com"]

  spec.summary       = "Official Ruby SDK for the SendDart email API"
  spec.description   = "Send transactional and marketing email from your own verified domain: " \
                       "emails, domains, contacts, segments, campaigns, templates, automations, " \
                       "webhooks, events and more. Zero runtime dependencies (Net::HTTP only)."
  spec.homepage      = "https://www.senddart.com"
  spec.license       = "MIT"

  spec.metadata["homepage_uri"]      = spec.homepage
  spec.metadata["documentation_uri"] = "https://www.senddart.com/docs"
  spec.metadata["rubygems_mfa_required"] = "true"

  spec.files         = Dir["lib/**/*.rb"] + %w[README.md LICENSE]
  spec.require_paths = ["lib"]

  spec.required_ruby_version = ">= 2.7"

  spec.add_development_dependency "minitest", "~> 5.0"
end
