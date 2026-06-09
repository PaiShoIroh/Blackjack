# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

- **Build:** `./gradlew build`
- **Run all tests:** `./gradlew test`
- **Run a single test class:** `./gradlew test --tests "com.aditya.blackjack.domain.hand.HandTest"`
- **Run a single test method:** `./gradlew test --tests "com.aditya.blackjack.domain.hand.HandTest.testMethod"`
- **Run the CLI app:** `./gradlew run` (main class: `com.blackjack.cli.CLI`)
- **Clean build:** `./gradlew clean build`

## Tech Stack

- Java 21, Spring Boot 4.0, Gradle 9.4 (Kotlin DSL)
- Lombok for boilerplate reduction (`@Getter`, `@Setter`, `@Slf4j`)
- Spring Data JPA + Postgres (production), H2 (tests)
- JUnit 5 + Mockito + AssertJ for testing
- JaCoCo for code coverage (`./gradlew jacocoTestReport`, report at `build/reports/jacoco/test/html/`)

## Architecture

The codebase follows a domain-driven structure under `com.aditya.blackjack`:

### Domain Layer (`domain/`)
Pure game entities with no framework dependencies:
- **card/** — `Card` (record-like), `Rank` (with point values; ACE=11), `Suit`
- **hand/** — `Hand` (card collection, value calculation with soft-ace logic, status tracking), `HandStatus` enum (ACTIVE, STOOD, BUST, BLACKJACK, SURRENDERED)
- **shoe/** — `Shoe` (multi-deck card source with cut-card shuffle trigger at 75%)
- **dealer/** — `Dealer` (hits on soft 17 configurable)
- **player/** — `Player` (username + balance with credit/debit), `PlayerAction` enum
- **seat/** — `Seat` (links a Player to a Hand with bet tracking), `SeatStatus`
- **table/** — `Table` (composes Shoe + Dealer + Seats), `TableConfig` (decks, seats, min/max bet, hitOnSoft17)

### Engine Layer (`engine/`)
Game orchestration, decoupled from I/O via three functional interfaces:
- **`ActionProvider`** — prompts for player action (hit/stand/double/split/surrender)
- **`BetProvider`** — collects bets from occupied seats; returning empty map stops the game
- **`RoundResultListener`** — receives round outcomes for display

**`Game`** runs the game loop: checks player eligibility, collects bets, plays rounds, resets table.
**`Round`** implements round phases in strict order: BETTING → DEALING → PLAYING → DEALER → SETTLING → COMPLETE. Supports split hands. Dealer blackjack short-circuits to COMPLETE.

### CLI Layer (`cli/`)
`CLI` implements all three engine interfaces for terminal-based play. Not wired as a Spring bean — has its own `main()`.

### Exception Layer (`exception/`)
`GameException` hierarchy replaces raw `IllegalStateException`/`IllegalArgumentException`:
- `InvalidBetException` — bet validation (limits, balance, even amounts)
- `InvalidActionException` — action not allowed for current hand state
- `InsufficientBalanceException` — not enough balance for bet/double/split
- `InvalidPhaseException` — round method called in wrong phase

`Game.start()` catches `GameException` per-round so one bad round doesn't kill the game loop. CLI validates user input before passing to engine and re-prompts on errors.

### API + Persistence Layer (scaffolded, not yet wired)
- `persistence/entity/` — `UserAccount`, `HandHistory` JPA entities
- `persistence/repository/` — Spring Data JPA repositories
- `api/controller/` — REST controllers (`UserController`, `GameController`) with stub endpoints
- `api/dto/` — Request/response records
- `api/GlobalExceptionHandler` — maps `GameException` hierarchy to HTTP status codes
- See `PLAN.md` for the implementation roadmap

### Key Design Decisions
- Split blackjacks pay 1:1 (not 3:2)
- Blackjack pays 5:2 (via `(bet * 5) / 2`)
- `Shoe.loadCards()` exists as a test hook for deterministic card sequences
- Seats are 0-indexed internally but the CLI prompts 1-7
- Bets must be even (for clean split payouts)