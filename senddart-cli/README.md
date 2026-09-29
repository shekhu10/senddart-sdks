# senddart-cli

Official command-line interface for the [SendDart](https://www.senddart.com) email API. Wraps the [`senddart`](https://www.npmjs.com/package/senddart) Node.js SDK.

## Install

```bash
npm i -g senddart-cli
```

Requires Node.js 18+.

## Authentication

Set your API key once:

```bash
export SENDDART_API_KEY=mb_xxxxxxxxx
```

Or pass `--api-key mb_xxxxxxxxx` to any command. Use `SENDDART_BASE_URL` (or `--base-url`) to target a different API host.

## Output

Every command prints the API response as pretty-printed JSON. Pass `--json` for raw compact JSON (handy for piping to `jq`). On failure a JSON error object is printed to **stderr** and the command exits `1`; stdout stays JSON-only either way, so `senddart … | jq` never has to strip a diagnostic.

The error object is always `{ statusCode, name, message }` — branch on `name`, never on `message`:

| Where it came from | `statusCode` | `name` |
|---|---|---|
| The API rejected the request | the HTTP status | the API's reason, e.g. `validation_error`, `daily_quota_exceeded` |
| The request never reached the API | `0` | `network_error` |
| The CLI rejected your flags before sending | `null` | `cli_error` |

Some API errors are a superset of that envelope and the extra fields are printed too: `limit` on plan/quota rejections, `reputation` on a reputation gate, and `sent`/`sent_count` on a partially applied `emails batch`.

One endpoint reports failure inside a `200` body rather than as an error: `webhooks test` returns `{ ok: false, error }` when the delivery did not land. The CLI prints that body to stdout and still exits `1`, so `senddart webhooks test wh_123 && deploy` behaves as you would expect.

`--help` works at every level: `senddart --help`, `senddart emails --help`, `senddart emails send --help`.

### Pagination

List commands take `--limit` (integer `1`–`100`, default `20`) plus one of `--after` / `--before` — cursors are item ids, and passing both is rejected. Responses are `{ "object": "list", "has_more": bool, "data": [...] }`; page forward by feeding the last `data[].id` back as `--after`.

Two default page sizes exist. `domains list`, `api-keys list`, `topics list`, `contacts list`, `contacts segments`, `contacts topics`, `segments list`, `segments contacts`, `campaigns list`, `contact-properties list`, `polls list`, `emails receiving list` and `emails receiving attachments` skip the default `20` when you pass no pagination flag — but they do **not** return the whole collection: the response is capped at **1,000** rows, and `has_more` is `true` when that cap truncated it, so keep paging with `--after`. Everything else caps at 20 unless you raise `--limit`: `emails list`, `templates list`, `webhooks list`, `audiences list`, `automations list`, `automations runs`, `events list` and `logs list`.

A few endpoints are deliberately unpaginated and take no cursor flags — `emails sources`, `emails attachments`, `emails receiving addresses` and `campaigns engagement` (whose three lists are each capped at 500 rows server-side).

## Usage

### Emails

```bash
senddart emails send --from 'Acme <hi@yourdomain.com>' --to 'delivered@test.senddart.com' --subject 'hello' --html '<p>hi</p>'
senddart emails send --to 'ada@yourdomain.com,rae@yourdomain.com' \
  --template-id tmpl_welcome --variables '{"first_name":"Ada"}' --scheduled-at 2026-08-01T09:00:00Z
senddart emails list --limit 20
senddart emails list --status delivered --search 'invoice'
senddart emails list --folder scheduled      # mailbox folder: outbox | sent | scheduled | failed (anything else is a 422)
senddart emails sources                      # per-source metrics: one row per campaign/automation + api and individual one-off roll-ups
senddart emails get em_123
senddart emails update em_123 --scheduled-at 2026-08-02T09:00:00Z
senddart emails cancel em_123
senddart emails attachments em_123
senddart emails attachment em_123 att_456
```

`--to`, `--cc`, `--bcc` and `--reply-to` are repeatable and accept comma-separated values.

`--from` and `--subject` are required for an ordinary send but **optional** with `--template-id` / `--template-alias`, as the two template examples in this section show: omit them and the template's own stored from and subject are used. Supply them and your values win for good — the flags are sent verbatim, so editing and republishing the template stops changing the from and subject that command sends. The body still re-renders from the template on every send; only those two fields stop tracking it. An empty value is a choice rather than an omission: `--subject ''` ships a blank subject line, and `--from ''` is rejected with `422 missing_required_field`.

`delivered@test.senddart.com` above is the mailbox simulator: it is intercepted before the provider is contacted and synthesizes the documented outcome (`bounced@`, `complained@` and `suppressed@` produce the other three; `delivered@`, `bounced@` and `complained@` also accept a `+label` suffix, `suppressed@` does not). Two things to know when you script against it — it only fires on an **immediate** send, so pairing it with a future `--scheduled-at` skips the simulator and the address is treated as suppressed (`422`); and quota is debited per recipient exactly like a real send. Documentation domains (`example.com`, `example.net`, `example.org`, anything under `.test` / `.invalid` / `.localhost` / `.example`) are blocked outright and never reach a mailbox, so do not use them as stand-in recipients.

Attach files with `--attachment <path>` (read and base64-encoded locally) or `--attachment-url <url>` (fetched server-side). Both are repeatable; the API caps an attachment at 25 MB and a message at 40 MB decoded, and the CLI checks local files against those limits before sending.

```bash
senddart emails send --from hi@yourdomain.com --to delivered@test.senddart.com --subject 'Your invoice' \
  --text 'Attached.' --attachment ./invoice.pdf
senddart emails send --to delivered@test.senddart.com \
  --template-alias welcome --variables '{"first_name":"Ada"}'
```

`--idempotency-key` accepts **1–255 characters**, measured after the server trims the value — 255, not 256 — and is honoured by `emails send`, `emails batch`, `emails receiving reply`, and `emails receiving forward`. The CLI sends the key verbatim and lets the server be the authority: an out-of-range key comes back as `400 invalid_idempotency_key`. Reusing a key with a different body is rejected (`409 invalid_idempotent_request`); replaying it with the same body returns the original response instead of sending twice. Every other command ignores the header, so a retry there creates a second resource.

Batch-send up to 100 emails in one request from a JSON file (an array of send payloads) or inline JSON:

```bash
senddart emails batch --file ./batch.json
senddart emails batch --data '[{"from":"hi@yourdomain.com","to":["delivered@test.senddart.com"],"subject":"hi","text":"hello"}]'
```

### Received (inbound) email

```bash
senddart emails receiving list --limit 20
senddart emails receiving addresses                                         # per-address inbound stats
senddart emails receiving get rem_123
senddart emails receiving attachments rem_123
senddart emails receiving attachment rem_123 att_456 --output invoice.pdf   # default filename: the attachment id
senddart emails receiving raw rem_123 --output message.eml                  # default filename: <id>.eml
senddart emails receiving forward rem_123 --from you@yourdomain.com --to delivered@test.senddart.com
senddart emails receiving reply rem_123 --from you@yourdomain.com --html '<p>thanks!</p>'
senddart emails receiving delete rem_123
```

`attachment` and `raw` download binary content: the file is written to `--output` (or the default filename) and the CLI prints where it was saved. `reply` requires at least one of `--html` / `--text`.

### Domains

```bash
senddart domains add yourdomain.com
senddart domains list
senddart domains get dom_123
senddart domains verify dom_123
senddart domains update dom_123 --click-tracking --tls enforced
senddart domains update dom_123 --custom-return-path mail --receiving
senddart domains mx-check yourdomain.com
senddart domains records-csv dom_123 --output dns.csv   # default filename: <id>-dns-records.csv
senddart domains delete dom_123
```

`records-csv` answers `text/csv`, not JSON, so the CLI writes it to `--output` (or the default filename) and prints where it landed — the same shape as the `emails receiving` binary downloads.

One-click DNS — detect the provider, then apply the records via its API (auto-verifies after):

```bash
senddart domains dns detect dom_123
senddart domains dns cloudflare dom_123 --token cf_api_token
senddart domains dns godaddy dom_123 --key gd_key --secret gd_secret
senddart domains dns namecheap dom_123 --api-user ncuser --key nc_api_key
```

Claim a domain already verified by another account (start the claim, add the TXT record it returns, then verify):

```bash
senddart domains claim start yourdomain.com
senddart domains claim get dom_123
senddart domains claim verify dom_123
```

### Contacts (domain-first)

Each sending domain has its own contact pool, so `contacts create` and `contacts list` need to be told which container to use — exactly one of `--domain <domain>` or `--audience-id <id>`; passing neither, or both, is a usage error:

```bash
senddart contacts create --domain yourdomain.com --email ada@yourdomain.com --first-name Ada
senddart contacts list --domain yourdomain.com
senddart contacts list --domain yourdomain.com --segment-id seg_123
senddart contacts list --audience-id aud_123          # plain audiences instead of a domain pool
senddart contacts get ada@yourdomain.com --domain yourdomain.com   # or by contact id, no --domain needed
senddart contacts update con_123 --unsubscribed
senddart contacts add-to-segment con_123 seg_123
senddart contacts remove-from-segment con_123 seg_123
senddart contacts topics con_123
senddart contacts set-topics con_123 --topics '[{"id":"top_123","subscription":"opt_out"}]'
senddart contacts delete con_123
```

Bulk-import a CSV. Files up to 5 MB / 10,000 rows go inline with `--csv`; anything larger is uploaded directly to storage first — mint a presigned URL, `PUT` the file to it, then finish the import with the `storage_key` it returned:

```bash
senddart contacts import aud_123 --csv ./contacts.csv --segment-id seg_123
senddart contacts import-upload aud_123 --csv ./big-list.csv     # → { storage_key, upload_url, max_bytes, ... }
curl -X PUT --upload-file ./big-list.csv "$UPLOAD_URL"
senddart contacts import aud_123 --storage-key "$STORAGE_KEY"
```

`import` takes exactly one of `--csv` / `--storage-key`. The `upload_url` is a short-lived bearer credential — don't log it or paste it into a shared shell history.

When the source is already structured, `contacts batch` imports a JSON array instead of a CSV (upsert by email, max 10,000 per call, exactly one of `--file` / `--data`):

```bash
senddart contacts batch aud_123 --file ./contacts.json               # audience as the positional…
senddart contacts batch --audience-id aud_123 --file ./contacts.json # …or as the flag, like every other contacts command
senddart contacts batch aud_123 --data '[{"email":"ada@yourdomain.com","first_name":"Ada"}]' --on-conflict skip
# Domain-first: import straight into a domain's pool, no audience id needed.
senddart contacts batch --domain yourdomain.com --file ./contacts.json
```

### Contact properties & audiences

```bash
senddart contact-properties create --key company --type string --fallback-value 'your company'
senddart contact-properties list
senddart contact-properties update prop_123 --fallback-value 'n/a'
senddart contact-properties update prop_123 --clear-fallback
senddart contact-properties delete prop_123

senddart audiences create --name Newsletter
senddart audiences list
senddart audiences update aud_123 --name 'Newsletter EU'
senddart audiences import-sheet aud_123 --url 'https://docs.google.com/spreadsheets/d/...' --segment-name 'July leads'
senddart audiences delete aud_123
```

### Segments & topics

```bash
senddart segments create --domain yourdomain.com --name VIP
senddart segments create --domain yourdomain.com --name Actives --filter '{"status":"subscribed"}'
senddart segments list --domain yourdomain.com
senddart segments contacts seg_123

senddart topics create --domain yourdomain.com --name 'Product updates' --default-subscription opt_in
senddart topics list --domain yourdomain.com
```

### Campaigns

```bash
senddart campaigns create --domain yourdomain.com --from 'Acme <hi@yourdomain.com>' \
  --subject 'Summer sale' --html '<p>50% off</p>' --segment-id seg_123
senddart campaigns create --domain yourdomain.com --from 'Acme <hi@yourdomain.com>' \
  --subject 'Weekly digest' --html '<p>...</p>' --recurrence weekly --unsubscribe-policy domain \
  --followups '[{"condition":"not_opened","delay":"2 days","html":"<p>Did you see this?</p>"}]'
senddart campaigns send camp_123
senddart campaigns send camp_123 --scheduled-at 2026-08-01T09:00:00Z
senddart campaigns send camp_123 --scheduled-at 'in 1 min'
senddart campaigns stats camp_123
senddart campaigns engagement camp_123    # who opened, clicked and replied (each list capped at 500 rows)
senddart campaigns ab camp_123
senddart campaigns cancel camp_123
```

### Templates

```bash
senddart templates create --name Welcome --subject 'Hi {{first_name}}' --html '<p>Welcome!</p>'
senddart templates list
senddart templates duplicate tmpl_123 --name 'Welcome v2'
senddart templates publish tmpl_123

# Declare the variables the template uses, with their types and fallbacks
senddart templates update tmpl_123 \
  --variables '[{"key":"first_name","type":"string","fallback_value":"there"},{"key":"seats","type":"number","fallback_value":1}]'
```

`--variables` is the template's declared-variable registry, not the per-send values: a JSON array of `{"key","type"?,"fallback_value"?}` (max 50 entries; `type` is `string` or `number`, and a `number` fallback must parse as a number). A send prefers a declared fallback when the caller omits that variable, so a template with no registry has no fallbacks. Pass `[]` to clear it. Edits land on the draft — `templates publish` makes them live.

### Automations & events

```bash
senddart automations create --name 'Welcome series' --domain yourdomain.com --trigger contact.created
senddart automations add-step auto_123 --type send_email --config '{"template_id":"tmpl_welcome"}'
senddart automations update-step auto_123 step_456 --type wait_for_event --config '{"event":"email.opened","timeout":"12 hours"}'
senddart automations delete-step auto_123 step_456
senddart automations update auto_123 --status enabled

# update-step replaces the step outright, so --type is required and --config is
# the complete new config. A step's graph key is immutable — delete and re-add
# with `add-step --key` to re-key it.
senddart automations runs auto_123
senddart automations runs auto_123 --status failed,skipped   # filtered before paging; repeatable
senddart automations run auto_123 run_456
senddart automations stop auto_123

# Author (or extend) the step graph from a prompt — the automation must be stopped, and this spends AI credits
senddart automations ai auto_123 --prompt 'Welcome new signups, then nudge anyone who has not opened after 2 days'
senddart automations ai auto_123 --prompt 'Send the upgrade nudge' --attach-from step_3 --attach-type condition_met

# Fire a custom event — only yourdomain.com's automations are triggered
senddart events send --domain yourdomain.com --name signup.completed --email ada@yourdomain.com --data '{"plan":"pro"}'
senddart events list
senddart events create --name signup.completed --schema '{"plan":"string"}'
senddart events update evt_123 --schema '{"plan":"string","seats":"number"}'   # the name is immutable
```

### Webhooks, API keys, logs, polls

```bash
senddart webhooks create --endpoint https://yourapp.com/hooks --events email.delivered,email.bounced
senddart webhooks rotate wh_123
senddart webhooks test wh_123
senddart webhooks verify --secret whsec_xxx --payload-file ./delivery.json \
  --svix-id msg_123 --svix-timestamp 1754000000 --svix-signature 'v1,base64sig'

senddart api-keys list

senddart logs list --limit 100 --method POST --status 429
senddart logs get log_123

senddart polls list
senddart polls get em_123
```

`webhooks verify` is the only command that makes no HTTP request: it recomputes the delivery signature locally and prints `{ valid, reason }`. It is also the only command that needs **no API key** — it never contacts the API, so a webhook receiver can run it without holding a send-capable key. Pass the **exact raw body bytes** your endpoint received via `--payload` or `--payload-file` — re-serialized JSON will not match. `--tolerance <seconds>` caps timestamp skew (default `300`; `0` skips the freshness check). Note the exit code is `0` whenever the check ran, whatever the verdict — branch on `.valid`, not on `$?` (unlike `webhooks test`, which exits `1` on a failed delivery).

#### API keys are read-only from the CLI

`api-keys list` is the whole group. There is no `create`, `update` or `delete`, and that is deliberate: **keys are created, re-scoped and revoked in the [SendDart dashboard](https://www.senddart.com/app/api-keys)**, behind a signed-in session.

Every CLI invocation authenticates with an API key, so keeping key lifecycle out of the CLI means a key that leaks — from a shell history, a CI log, a `.env` someone pasted — cannot mint itself a replacement, widen its own permission or domain scope, or revoke the keys around it. Its blast radius stays fixed at what it could already do. The API enforces the same rule: `POST /api-keys`, `PATCH /api-keys/:id` and `DELETE /api-keys/:id` answer `403 dashboard_only` to any API-key caller.

`api-keys list` still shows everything you need to audit: each key's non-secret prefix, permission, domain scoping and `last_used_at`.

## Command names vs. SDK method names

The CLI's verbs are deliberately shorter than the corresponding SDK methods. A
subcommand is already scoped by its resource path, so the CLI names every list
after its plural noun and every download after the thing downloaded:

| CLI command | SDK method (`senddart` npm naming) |
|---|---|
| `emails attachments <id>` | `emails.listAttachments` |
| `emails receiving addresses` | `emails.receiving.listAddresses` |
| `emails receiving attachments <id>` | `emails.receiving.listAttachments` |
| `emails receiving raw <id>` | `emails.receiving.getRaw` |
| `automations ai <id>` | `automations.createWithAi` |
| `contacts import-upload <audienceId>` | `contacts.createImportUpload` |

This is a shell-surface convention, not drift: the CLI applies the same
shortening rule to every endpoint rather than to some.

The right-hand column is the Node.js SDK's spelling, because that is the library
this CLI calls. Seven of the eight client libraries put the same method on the
same nested resource and differ only in each language's casing and accessor
syntax — `listAttachments` in npm, PHP (`$senddart->emails->receiving`) and
Java (`emails().receiving()`), `list_attachments` in Python, Ruby and Rust,
`ListAttachments` in Go (`client.Emails.Receiving`). The .NET SDK is the
exception: it is a flat client, so read `EmailListAttachmentsAsync`,
`ReceivedEmailListAddressesAsync`, `ReceivedEmailListAttachmentsAsync`,
`ReceivedEmailGetRawAsync`, `AutomationCreateWithAiAsync` and
`ContactImportCreateUploadAsync` instead of the dotted names above.

## Documentation

Full API docs: <https://www.senddart.com/docs>

## License

MIT

## Recovery and tracking contracts

Use a stable, unique operation key for each intended send, batch, reply, or
forward. Keep the same key and payload when recovering that operation. These
are the supported idempotent send endpoints; events do not implement this
header. Existing calls without options still work.

```sh
senddart emails receiving reply rem_123 --from me@yourdomain.com --text "Thanks" --idempotency-key reply-operation-1
senddart emails receiving forward rem_123 --from me@yourdomain.com --to team@yourdomain.com --idempotency-key forward-operation-1
senddart domains tracking-health dom_123
```

Automatic retries consider only 429/503. They stop on an original email `id`,
positive `sent_count`, nonempty `sent` or `reserved`, or `batch_incomplete`.
An ordinary rate limit can retry; a generic 503 can retry a read or a send with
the same supported key. Other writes retry only documented pre-processing
rejections (`service_unavailable`, `sending_service_unavailable`,
`sending_configuration_unavailable`, `contacts_busy`, `contacts_timeout`).
No network/body-read failure, 409, 422, or other 5xx is retried automatically.
The default transport refuses redirects; a custom transport/client must enforce
its own policy.

On a failed or unconfirmed send, inspect `id` with the email retrieval method
before creating another send. A 422 with an ID can identify an uncertain
provider handoff; 422 does not always mean nothing happened. For interrupted
batches, `sent` contains confirmed sends, `reserved` contains the original
attempted prefix (including uncertain handoffs), and `unsent_count` counts the
never-attempted tail. Do not resend the full batch or the reserved prefix under
a new key. Reconcile original IDs first, then submit only known unattempted
items as a new operation. Recovery fields remain available in the full error
body as well as language-specific fields/accessors.

Tracking health returns `custom_host`, `status` (`shared`, `ready`, or
`unavailable`), and `checked_at`. Configure custom tracking through the domain
API and check health before relying on it. A healthy endpoint cannot guarantee
an open event: recipients may block images, and coupon redemption alone is not
proof that the tracking pixel loaded. SDKs preserve supplied HTML/text and do
not infer opens or rewrite editor spacing.

Campaign cancellation also stops pending follow-ups for an already-sent
campaign while retaining its sent history. Permanent received-email deletion
acknowledges a durable cleanup request; attachment/object cleanup can finish
asynchronously. Retrying that deletion is safe; it cannot be undone after the
purge request is accepted.
