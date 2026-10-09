# Telegram and Viber AI Messaging PoC

A Spring Boot messaging bot that receives Telegram or Viber messages through webhooks and replies in echo mode. OpenAI replies remain optional. Viber is disabled by default so it can be developed and tested without a commercial bot account.

## Project structure

| Path | Responsibility |
| --- | --- |
| `config` | Shared HTTP client builder configuration. |
| `messaging` | Platform-neutral messages, orchestration, reply and delivery interfaces, and echo mode. |
| `openai` | OpenAI reply adapter and its API response mapping. |
| `telegram` | Telegram delivery adapter and the Bot API interface used by its inbound and outbound code. |
| `telegram/client` | Outbound Telegram Bot API HTTP client and response mapping. |
| `telegram/webhook` | Webhook endpoint, update mapping, handling, registration, and settings. |
| `viber` | Viber delivery adapter and API interface. |
| `viber/client` | Outbound Viber REST API client and response mapping. |
| `viber/webhook` | Signed webhook endpoint, callback handling, registration, and settings. |

Both webhook paths map into `IncomingMessage -> MessagingService -> ReplyGenerator -> OutgoingMessage -> RoutingMessageSender`. The dispatcher selects `TelegramConnector` or `ViberConnector` from the message's platform. Telegram chat IDs and Viber user IDs stay in the platform neutral `conversationId` field.

## Requirements and dependencies

Use Java 17 or newer. The included Maven wrapper downloads Maven 3.9.6 if needed. The project uses Spring Boot 4.1.1, `spring-boot-starter-webmvc` for `RestClient` and JSON, and `spring-boot-starter-test` for tests. It calls the official Telegram and OpenAI HTTP APIs directly, so no platform SDK is needed.

Before starting, run `.\mvnw.cmd -version` on Windows or `./mvnw -version` on macOS/Linux and confirm Maven reports Java 17 or newer. If it reports an older version, configure `JAVA_HOME` or your IDE's Maven runner to use a compatible JDK. The commands below use PowerShell; in other shells, set the same environment variables and use `./mvnw`.

Create a bot with BotFather. To run the webhook locally, start a tunnel that forwards a public HTTPS address to `http://localhost:8080`. Set `TELEGRAM_WEBHOOK_URL` to that public address plus `/telegram/webhook`. Telegram cannot deliver webhooks directly to `localhost`.

Set a random `TELEGRAM_WEBHOOK_SECRET` of 1-256 letters, digits, `_`, or `-`; use at least 32 random characters for local testing. Telegram sends it in `X-Telegram-Bot-Api-Secret-Token`, and the endpoint rejects requests without the matching value. Set the variables in your shell or a gitignored `.env` file before starting the app:

```powershell
$env:TELEGRAM_BOT_TOKEN = 'your-bot-token'
$env:TELEGRAM_WEBHOOK_URL = 'https://your-public-host/telegram/webhook'
$env:TELEGRAM_WEBHOOK_SECRET = 'your-random-secret'
$env:APP_REPLY_MODE = 'echo'
.\mvnw.cmd spring-boot:run
```

At startup, the app calls Telegram's `setWebhook` with the URL and secret, subscribing to message updates. Wait for the `Telegram webhook registered` log entry, then send `/start`, text, and a photo to the bot. They should produce a greeting, an echo, and a text-only notice. If the tunnel URL changes, update `TELEGRAM_WEBHOOK_URL` and restart the app so Telegram receives the new address.

For optional AI replies, stop the app, then run it with the OpenAI key:

```powershell
$env:OPENAI_API_KEY = 'your-openai-api-key'
$env:APP_REPLY_MODE = 'ai'
.\mvnw.cmd spring-boot:run
```

`OPENAI_MODEL` defaults to `gpt-5.4-mini` and can be overridden. Keep credentials out of source control. The app fails at startup if an enabled connector lacks its token or webhook URL, if Telegram lacks its webhook secret, or if the OpenAI key is absent in AI mode. Run only one active app or tunnel for a bot token.

The app defaults to echo mode. In echo mode, no OpenAI key is needed.

## Viber connector

Viber requires an authorized bot, administrator account, and bot token for a live webhook. The connector can be verified without any of those using mocked Viber HTTP responses and signed callback fixtures:

```powershell
$env:JAVA_HOME = 'C:\path\to\jdk-17-or-newer'
.\mvnw.cmd test '-Dtest=ViberFlowTest,ViberBotClientTest,ViberConnectorTest,ViberApplicationContextTest'
```

These tests exercise the signed `/viber/webhook` callback, message mapping, reply routing, outbound `set_webhook` and `send_message` requests, and Viber's JSON `status` checks. They do not contact Viber. `ViberApplicationContextTest` starts the app with Telegram disabled and a mocked Viber API, so it also checks startup registration without credentials or network calls.

When you have a live bot, give Viber a public HTTPS webhook URL with a trusted certificate and start the app with:

```powershell
$env:TELEGRAM_ENABLED = 'false' # Optional if you are running Viber alone
$env:VIBER_ENABLED = 'true'
$env:VIBER_BOT_TOKEN = 'your-viber-bot-token'
$env:VIBER_WEBHOOK_URL = 'https://your-public-host/viber/webhook'
$env:VIBER_SENDER_NAME = 'Agent Messaging'
.\mvnw.cmd spring-boot:run
```

`VIBER_SENDER_NAME` is limited to 28 characters. Leave `TELEGRAM_ENABLED=true` (the default) to run both connectors, with the existing Telegram settings present. Viber registers its webhook at startup and accepts callbacks only when their `X-Viber-Content-Signature` matches the HMAC SHA-256 of the exact request body using `VIBER_BOT_TOKEN`. Only inbound text messages reach the reply generator; other message types get a text-only notice. Viber limits text messages to 7,000 characters, so the connector sends long replies in smaller segments.

The request and callback formats follow the [Viber REST Bot API](https://developers.viber.com/docs/api/rest-bot-api/).

Run tests with `.\mvnw.cmd test` on Windows or `./mvnw test` on macOS/Linux. They cover webhook authentication, routing to the original chat, `/start`, unsupported messages, registration, API responses, and basic failures.

If an AI reply fails with `status=429`, check the `code` in the application log. A rate-limit code means requests or tokens are arriving too quickly; wait for the limit to reset before trying again. A credit, spend, or usage-limit code requires checking the OpenAI project's billing and limits. The app sends a generic error reply to Telegram and does not automatically retry a 429.

## PoC limits

Telegram and Viber retry webhook callbacks after non-2xx responses. A lost success response or a send timeout can therefore produce duplicate replies; this PoC has no durable deduplication. Model requests are independent one-turn prompts, with no stored chat history. Mocked Viber tests verify the connector contract but cannot confirm delivery against a live Viber bot.
