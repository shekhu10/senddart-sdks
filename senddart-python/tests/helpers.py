"""Shared test scaffolding: monkeypatch the HTTP layer and record calls."""

import unittest
from unittest import mock

import senddart
from senddart import http_client


class RecordingTestCase(unittest.TestCase):
    """Patches ``senddart.http_client.request`` / ``request_raw`` so every
    resource call is recorded ({method, path, body, options}) instead of
    hitting the network."""

    def setUp(self):
        senddart.api_key = "mb_test_key"
        senddart.base_url = http_client.DEFAULT_BASE_URL
        self.calls = []
        self.response = {"ok": True}
        self.raw_response = b"\x00binary"

        def fake_request(method, path, body=None, options=None):
            self.calls.append(
                {"method": method, "path": path, "body": body, "options": options}
            )
            return self.response

        def fake_request_raw(method, path, options=None):
            self.calls.append(
                {"method": method, "path": path, "body": None, "options": options, "raw": True}
            )
            return self.raw_response

        patcher = mock.patch.object(http_client, "request", side_effect=fake_request)
        patcher_raw = mock.patch.object(http_client, "request_raw", side_effect=fake_request_raw)
        patcher.start()
        patcher_raw.start()
        self.addCleanup(patcher.stop)
        self.addCleanup(patcher_raw.stop)
        self.addCleanup(setattr, senddart, "api_key", None)

    @property
    def last(self):
        return self.calls[-1]

    def assertCall(self, method, path, body=None, options=None):
        self.assertEqual(self.last["method"], method)
        self.assertEqual(self.last["path"], path)
        self.assertEqual(self.last["body"], body)
        self.assertEqual(self.last["options"], options)
