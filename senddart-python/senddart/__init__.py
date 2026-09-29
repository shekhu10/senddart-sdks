"""Official SendDart Python SDK.

Usage::

    import senddart
    senddart.api_key = "mb_xxxxxxxxx"

    email = senddart.Emails.send({
        "from": "Acme <hello@yourdomain.com>",
        "to": ["user@example.com"],
        "subject": "Hello from SendDart",
        "html": "<p>Your first email</p>",
    })

Every method returns the parsed JSON response and raises
:class:`senddart.SendDartError` on any non-2xx status.
"""

from typing import Optional

from .http_client import (
    DEFAULT_BASE_URL,
    IDEMPOTENCY_KEY_MAX_LENGTH,
    USER_AGENT,
    VERSION,
)
from .exceptions import SendDartError
from .emails import Emails
from .batch import Batch
from .domains import Domains
from .audiences import Audiences
from .contacts import Contacts
from .contact_properties import ContactProperties
from .campaigns import Campaigns
from .segments import Segments
from .topics import Topics
from .templates import Templates
from .automations import Automations
from .webhooks import Webhooks, verify_webhook_signature
from .events import Events
from .api_keys import ApiKeys
from .logs import Logs
from .polls import Polls

# ---- Module-level configuration ----

# Your SendDart API key, e.g. senddart.api_key = "mb_xxxxxxxxx"
api_key: Optional[str] = None

# Override to point at a different API host.
base_url: str = DEFAULT_BASE_URL

__version__ = VERSION

__all__ = [
    "api_key",
    "base_url",
    "SendDartError",
    "verify_webhook_signature",
    "Emails",
    "Batch",
    "Domains",
    "Audiences",
    "Contacts",
    "ContactProperties",
    "Campaigns",
    "Segments",
    "Topics",
    "Templates",
    "Automations",
    "Webhooks",
    "Events",
    "ApiKeys",
    "Logs",
    "Polls",
    "DEFAULT_BASE_URL",
    "IDEMPOTENCY_KEY_MAX_LENGTH",
    "USER_AGENT",
    "VERSION",
]
