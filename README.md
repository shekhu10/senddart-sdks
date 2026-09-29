# SendDart SDKs

Official client libraries and tools for the [SendDart](https://www.senddart.com) email API — one package per ecosystem, all mirroring the same resource surface (emails, receiving, domains, contacts, segments, topics, campaigns, templates, automations, webhooks, events, API keys, logs, polls) with the platform's domain-first model and Svix-style webhook signature verification.

**SDK release: v1.0.0.** Grab your API key from the [SendDart dashboard](https://www.senddart.com) → API Keys, and you're one snippet away from the inbox.

| Package | Language | Install | Registry |
|---|---|---|---|
| [`senddart-npm`](./senddart-npm) | Node.js ≥ 18 | `npm install senddart` | [npm](https://www.npmjs.com/package/senddart) |
| [`senddart-python`](./senddart-python) | Python ≥ 3.8 | `pip install senddart` | [PyPI](https://pypi.org/project/senddart/) |
| [`senddart-go`](./senddart-go) | Go ≥ 1.22 | `go get github.com/shekhu10/senddart-sdks/senddart-go` | Go modules |
| [`senddart-ruby`](./senddart-ruby) | Ruby ≥ 2.7 | `gem install senddart` | [RubyGems](https://rubygems.org/gems/senddart) |
| [`senddart-php`](./senddart-php) | PHP ≥ 8.1 | `composer require senddart/senddart` | Packagist |
| [`senddart-java`](./senddart-java) | Java ≥ 11 | `com.senddart:senddart:1.0.0` | Maven Central |
| [`senddart-dotnet`](./senddart-dotnet) | .NET 8 | `dotnet add package SendDart` | [NuGet](https://www.nuget.org/packages/SendDart) |
| [`senddart-rust`](./senddart-rust) | Rust ≥ 1.75 | `cargo add senddart` | [crates.io](https://crates.io/crates/senddart) |
| [`senddart-cli`](./senddart-cli) | Node CLI | `npm i -g senddart-cli` | [npm](https://www.npmjs.com/package/senddart-cli) |

Every package: base URL `https://www.senddart.com/api`, Bearer `mb_…` keys, the API's `{statusCode, name, message}` error shape, percent-encoded path ids, and `domain`-scoped contacts/segments/topics/campaigns per the [docs](https://www.senddart.com/docs/api/introduction).

## Quickstart — send your first email

### Node.js

```bash
npm install senddart
```

```ts
import { SendDart } from 'senddart';

const mb = new SendDart('mb_xxxxxxxxx');

const { data, error } = await mb.emails.send({
  from: 'Acme <hello@yourdomain.com>',
  to: ['user@example.com'],
  subject: 'Hello from SendDart',
  html: '<p>Your first email 🎉</p>',
});
if (error) console.error(error.name, error.message);
else console.log('sent', data.id);
```

### Python

```bash
pip install senddart
```

```python
import senddart

senddart.api_key = "mb_xxxxxxxxx"

email = senddart.Emails.send({
    "from": "Acme <hello@yourdomain.com>",
    "to": ["user@example.com"],
    "subject": "Hello from SendDart",
    "html": "<p>Your first email 🎉</p>",
})
print(email["id"])
```

### Go

```bash
go get github.com/shekhu10/senddart-sdks/senddart-go
```

```go
client := senddart.NewClient("mb_xxxxxxxxx")

sent, err := client.Emails.Send(&senddart.SendEmailRequest{
    From:    "Acme <hello@yourdomain.com>",
    To:      []string{"user@example.com"},
    Subject: "Hello from SendDart",
    Html:    "<p>Your first email 🎉</p>",
})
```

### Ruby

```bash
gem install senddart
```

```ruby
require "senddart"

SendDart.api_key = "mb_xxxxxxxxx"

sent = SendDart::Emails.send({
  from: "Acme <hello@yourdomain.com>",
  to: ["user@example.com"],
  subject: "Hello from SendDart",
  html: "<p>Your first email 🎉</p>"
})
puts sent["id"]
```

### PHP

```bash
composer require senddart/senddart
```

```php
use SendDart\SendDart;

$senddart = SendDart::client('mb_xxxxxxxxx');

$sent = $senddart->emails->send([
    'from' => 'Acme <hello@yourdomain.com>',
    'to' => ['user@example.com'],
    'subject' => 'Hello from SendDart',
    'html' => '<p>Your first email 🎉</p>',
]);
echo 'sent ' . $sent['id'];
```

### Java

```xml
<dependency>
  <groupId>com.senddart</groupId>
  <artifactId>senddart</artifactId>
  <version>1.0.0</version>
</dependency>
```

```java
SendDart senddart = new SendDart("mb_xxxxxxxxx");

SendEmailRequest request = SendEmailRequest.builder()
        .from("Acme <hello@yourdomain.com>")
        .to("user@example.com")
        .subject("Hello from SendDart")
        .html("<p>Your first email 🎉</p>")
        .build();

SendDartResponse sent = senddart.emails().send(request);
System.out.println("sent " + sent.getString("id"));
```

### .NET

```bash
dotnet add package SendDart
```

```csharp
using SendDart;

ISendDart senddart = SendDartClient.Create("mb_xxxxxxxxx");

var sent = await senddart.EmailSendAsync(new EmailMessage
{
    From = "Acme <hello@yourdomain.com>",
    To = "user@example.com",
    Subject = "Hello from SendDart",
    HtmlBody = "<p>Your first email 🎉</p>",
});
Console.WriteLine($"sent {sent.Id}");
```

### Rust

```bash
cargo add senddart
cargo add tokio -F macros,rt-multi-thread
```

```rust
use senddart::{SendEmailOptions, SendDart, Result};

#[tokio::main]
async fn main() -> Result<()> {
    let senddart = SendDart::new("mb_xxxxxxxxx");

    let email = SendEmailOptions::new(
        "Acme <hello@yourdomain.com>", ["user@example.com"], "Hello from SendDart",
    ).with_html("<p>Your first email 🎉</p>");

    let sent = senddart.emails.send(email).await?;
    println!("sent {}", sent.id);
    Ok(())
}
```

### CLI

```bash
npm i -g senddart-cli
export SENDDART_API_KEY=mb_xxxxxxxxx

senddart emails send \
  --from 'Acme <hello@yourdomain.com>' --to 'user@example.com' \
  --subject 'Hello from SendDart' --html '<p>Your first email 🎉</p>'
```

The CLI covers the full surface — `senddart <resource> <action>` for emails, batches, receiving, domains, contacts, segments, topics, campaigns, templates, automations, webhooks, events, keys, logs, and polls. `senddart --help` lists everything.

## Beyond sending

Each package README documents its full surface: batch sends, scheduling (`scheduled_at`), template sends with variables, inbound email, domain management with DNS records, audiences (contacts/segments/topics), campaigns, automations, API keys, logs, polls — and **webhook signature verification** (Svix-compatible `svix-id`/`svix-timestamp`/`svix-signature` headers with `whsec_…` secrets) so your webhook handlers can trust what they receive.

## Repository layout & contributing

This is the development monorepo for all SDKs — issues and PRs for every language belong here.

- [`shekhu10/senddart-php`](https://github.com/shekhu10/senddart-php) is a **read-only subtree split** of [`senddart-php/`](./senddart-php), regenerated by CI on every release, because Packagist requires `composer.json` at the repository root. Never edit it directly.
- Go consumes this monorepo directly via the `senddart-go/vX.Y.Z` tags.

Releases: tag `vX.Y.Z` here and CI publishes every package (`.github/workflows/release.yml`).

## License

MIT — see the `LICENSE` file in each package.
