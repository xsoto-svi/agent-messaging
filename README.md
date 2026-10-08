# Telegram AI Messaging PoC

A Spring Boot bot that polls the official Telegram Bot API and replies through the OpenAI Responses API. It uses no database or inbound webhook, so it runs locally without a public URL.

## Project structure

| Path | Responsibility |
| --- | --- |
| `model` | Platform neutral incoming and outgoing message records. |
| `messaging` | `MessagingService` coordinates reply generation and delivery; `ReplyGenerator` and `MessageSender` are the small interfaces at its boundaries. |
| `telegram` | `TelegramPoller` manages offsets, `TelegramUpdateHandler` converts updates and handles commands, `TelegramConnector` sends replies, and `TelegramBotClient` calls the Bot API. |
| `ai` | `OpenAiService` calls the Responses API; `EchoReplyGenerator` supports the Phase 1 echo check. |

The request path is `getUpdates -> TelegramUpdateHandler -> IncomingMessage -> MessagingService -> ReplyGenerator -> OutgoingMessage -> TelegramConnector -> sendMessage`. Telegram chat IDs stay in the platform neutral `conversationId` field; the core service has no Telegram API types.

## Requirements and dependencies

Use Java 17 or newer. The included Maven wrapper downloads Maven 3.9.6 if needed. The project uses Spring Boot 4.1.1, `spring-boot-starter-webmvc` for `RestClient` and JSON, and `spring-boot-starter-test` for tests. It calls the official Telegram and OpenAI HTTP APIs directly, so no platform SDK is needed.

Create a bot with BotFather and set environment variables in your shell:

```powershell
$env:TELEGRAM_BOT_TOKEN = 'your-bot-token'
$env:OPENAI_API_KEY = 'your-openai-api-key'
$env:OPENAI_MODEL = 'gpt-5.4-mini' # optional default
.\mvnw.cmd spring-boot:run
```

Keep credentials out of source control. The app fails at startup if the Telegram token is absent, or if the OpenAI key is absent in AI mode. If a webhook was previously configured for the bot, remove it through Telegram's `deleteWebhook` method before polling. Run only one poller for a bot token.

For the Phase 1 echo check, set `$env:APP_REPLY_MODE = 'echo'` before running. In echo mode, no OpenAI key is needed. Send `/start`, a text message, and a non-text message to the bot; it should send a greeting, echo the text, and explain that it currently handles text only. Then unset `APP_REPLY_MODE` or set it to `ai` and provide `OPENAI_API_KEY` for Phase 2.

Run tests with `.\mvnw.cmd test` on Windows or `./mvnw test` on macOS/Linux. They cover update parsing, routing to the original chat, `/start`, unsupported messages, offsets, API response parsing, and basic failures. If your shell still uses Java 8, set `JAVA_HOME` to a Java 17 or newer JDK first.

## PoC limits

Offsets live in memory. A crash or restart before Telegram receives the next higher offset can replay an update; a `sendMessage` timeout may also have an unknown delivery outcome. This PoC therefore has at-least-once handling, not durable exactly-once delivery. Model requests are independent one-turn prompts, with no stored chat history. Keep the bot in one local process while testing.
