# Database + API Layer Plan

## Current State
- Game engine is fully functional with CLI interface
- Engine is decoupled from I/O via `ActionProvider`, `BetProvider`, `RoundResultListener`
- Custom exception hierarchy (`GameException` tree) is in place
- JPA entities, repositories, DTOs, and controller stubs are scaffolded

## Phase 1: Database (Postgres)

### Setup
- Add `spring.datasource.*` and `spring.jpa.*` config to `application.properties`
- Use Flyway or Liquibase for schema migrations (recommended over `ddl-auto`)
- Docker Compose for local Postgres

### Entities (already scaffolded)
- `UserAccount` — username, balance, createdAt
- `HandHistory` — ties to user, stores bet, cards, outcome, payout, balanceAfter

### Services to implement
- `UserService` — create account, get balance, credit/debit
- `HandHistoryService` — record each hand result, query history by user

### Key decision: Player balance source of truth
The in-memory `Player.balance` currently drives everything. Once DB is introduced:
- `UserAccount.balance` becomes the source of truth
- `Player` is loaded from DB at game join, written back on round settlement
- Consider optimistic locking (`@Version`) on `UserAccount` for concurrency

## Phase 2: API Layer

### Architecture shift
The CLI runs a synchronous game loop (`Game.start()` blocks). The API must be **request-driven**:
- Each API call advances the game state by one step
- Need a `GameSession` concept that holds the `Table` + `Round` in memory between requests

### Session management
- `GameSessionService` — manages in-memory `Map<String, GameSession>`
- Session ID returned on game creation, used in all subsequent calls
- Consider Redis for session storage if scaling horizontally

### Endpoints (already scaffolded in controllers)

**UserController** (`/api/users`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create user account |
| GET | `/{userId}` | Get user profile + balance |
| GET | `/{userId}/history` | Get hand history |

**GameController** (`/api/games`)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create new table session |
| POST | `/{sessionId}/join` | Seat player at table |
| POST | `/{sessionId}/bet` | Place bet (triggers deal when all bets in) |
| POST | `/{sessionId}/action` | Player action (hit/stand/double/split/surrender) |
| GET | `/{sessionId}/state` | Get current game state |

### Error handling
`GlobalExceptionHandler` already maps exceptions to HTTP status codes:
- `InvalidBetException` → 400
- `InvalidActionException` → 400
- `InsufficientBalanceException` → 402
- `GameException` (catch-all) → 422

## Phase 3: Hand History Recording

### When to record
After `Round.settleOutcomes()` completes, for each seat:
- Serialize player cards and dealer cards as strings
- Record bet, outcome, payout, and post-round balance
- Write to `HandHistory` via `HandHistoryService`

### Integration point
Implement `RoundResultListener` as a service that:
1. Updates `UserAccount.balance` in DB
2. Creates `HandHistory` records
3. The same listener can also push results to the API response

## Implementation Order
1. Wire `UserService` + `UserAccountRepository` with full CRUD
2. Wire `HandHistoryService` + `HandHistoryRepository`
3. Implement `GameSessionService` to manage in-memory game state
4. Implement `GameController` endpoints, using `GameSessionService`
5. Implement the DB-backed `RoundResultListener`
6. Add Flyway migrations, Docker Compose for Postgres
7. Integration tests with `@SpringBootTest` + Testcontainers
