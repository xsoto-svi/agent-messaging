# AGENTS.md

## Project Overview

This project is an omnichannel AI messaging application that enables users to interact with an AI assistant through various messaging platforms.

The intended platforms include:

- Telegram
- Facebook Messenger
- Viber
- WhatsApp
- SMS
- Apple Messages (subject to supported business messaging APIs)

The application uses a centralized backend to process incoming messages, communicate with an AI model, and deliver responses through the originating messaging platform.

The architecture should support multiple messaging connectors without requiring separate implementations of the core AI functionality.

## Technology Stack

- **Language:** Java
- **Framework:** Spring Boot
- **Build Tool:** Maven
- **Communication:** HTTP/REST, JSON
- **AI Integration:** OpenAI API initially, with support for alternative providers
- **Messaging Integration:** Official messaging platform APIs

## Architecture Guidelines

Follow a simple, modular architecture with clear separation of responsibilities.

### Messaging Connectors

Each messaging platform should have its own connector responsible for:

- Handling platform-specific API communication.
- Receiving and parsing incoming messages.
- Converting messages into a standardized internal format.
- Sending responses to the appropriate platform.

Platform-specific implementation details must remain within their respective connectors.

### Messaging Service

Responsible for coordinating communication between messaging connectors and the AI service.

The messaging service should remain independent of any particular messaging platform.

### AI Service

Responsible for communicating with the configured AI provider.

The AI service should not depend on Telegram, WhatsApp, or other messaging platform APIs.

### Shared Models

Use standardized internal message models to represent incoming and outgoing messages.

Shared models should contain the information necessary to identify the platform, conversation, sender, and message content.

## Development Guidelines

- Follow established Java and Spring Boot conventions.
- Use dependency injection rather than tightly coupling components.
- Keep classes focused on a single responsibility.
- Prefer interfaces where multiple implementations are expected.
- Avoid unnecessary abstractions and overengineering.
- Use meaningful names for classes, methods, and variables.
- Keep business logic separate from external API communication.
- Use environment variables for API keys, tokens, and secrets.
- Never hardcode or log sensitive credentials.
- Implement appropriate error handling and logging.
- Write tests for important business logic and external API interactions.

## Scope Management

The application will be developed incrementally.

Each messaging platform should be integrated independently while reusing existing services and internal models.

Do not implement additional platforms or features unless explicitly requested.

Prioritize a working, maintainable Proof of Concept before introducing production-level complexity.

## AI Agent Instructions

When working on this project:

1. Inspect the existing project structure before proposing changes.
2. Respect existing architectural decisions unless changes are necessary.
3. Explain significant architectural changes and their purpose.
4. Prefer straightforward implementations over complex design patterns.
5. Reuse existing services instead of duplicating functionality.
6. Reference official API documentation for platform-specific implementation details.
7. Do not introduce new dependencies without justification.
8. Keep changes limited to the requested task.
9. Include relevant tests and instructions for verifying functionality.
10. Clearly distinguish completed functionality from suggested future improvements.
