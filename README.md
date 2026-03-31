# Chess On Demand

A real-time multiplayer chess application built with Spring Boot and React.

## Features

- Real-time multiplayer chess via room-based matchmaking using WebSockets
- Full chess rule enforcement (all piece movements, special moves)
- Check, checkmate, and stalemate detection
- Draw detection (threefold repetition, 50-move rule, insufficient material)
- Castling (kingside and queenside)
- En passant captures
- Pawn promotion
- Spectator mode for full rooms
- Persistent player identification via localStorage

## Architecture

```
┌─────────────────┐     ┌─────────────────┐
│   React Client  │────▶│  Spring Boot    │
│   (Vite dev)   │     │   Backend       │
│   Port 5173    │     │   Port 8080     │
└─────────────────┘     └─────────────────┘
        │                       │
        │      WebSocket/STOMP  │
        └───────────────────────┘
```

### Tech Stack

- **Frontend:** React 19, Vite, react-chessboard, axios
- **Backend:** Spring Boot 3.2, Java 17
- **Real-time:** WebSocket (STOMP protocol)

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.8+

## Setup

### Unit Test

```bash
# Navigate to project root
cd ChessApp

# Run test
mvn test
```

### Backend

```bash
# Navigate to project root
cd ChessApp

# Build and run
mvn spring-boot:run
```

The backend server starts at `http://localhost:8080`.

### Frontend

```bash
# Navigate to client directory
cd client

# Install dependencies
npm install

# Run development server
npm run dev
```

The frontend dev server starts at `http://localhost:5173`.

## How to Play

1. Open the frontend in your browser
2. Click "Create Game" to generate a room
3. Share the room link with your opponent or
   a new private window of you're browser
4. Second player joins via the shared link
5. White plays first; make moves by dragging pieces
6. Game ends on checkmate, stalemate, or draw conditions

## API Reference

### Create Room

```http
POST /api/game/rooms
```

Creates a new game room.

**Response:**

```json
{
  "roomId": "B941B993",
  "shareUrl": "/game/B941B993"
}
```

### Get Room Info

```http
GET /api/game/rooms/{roomId}
```

Retrieves current game state.

**Response:**

```json
{
  "roomId": "B941B993",
  "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
  "message": "Loading...",
  "isWhiteTurn": true,
  "whitePlayerId": "ABC123",
  "blackPlayerId": null,
  "gameOver": false
}
```

### Join Room

```http
POST /api/game/rooms/{roomId}/join?playerId={playerId}&color={color}
```

Joins a room as white or black.

**Parameters:**

- `playerId` (required): Unique player identifier
- `color` (optional): `white` or `black` (default: `white`)

**Response:**

```json
{
  "tokenId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error Responses:**

- `404 Not Found`: Room does not exist or is full

### Make Move

```http
POST /api/game/rooms/{roomId}/move
Content-Type: application/json

{
  "tokenId": "550e8400-e29b-41d4-a716-446655440000",
  "fromRow": 1,
  "fromCol": 4,
  "toRow": 3,
  "toCol": 4
}
```

Makes a move on the board.

**Response:**

```json
{
  "success": true,
  "message": "e2 → e4",
  "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 2",
  "moveCount": 1
}
```

**Error Responses:**

- `400 Bad Request`: Invalid token, invalid coordinates, or illegal move

### Promote Pawn

```http
POST /api/game/rooms/{roomId}/promote
Content-Type: application/json

{
  "tokenId": "550e8400-e29b-41d4-a716-446655440000",
  "pieceType": "Q"
}
```

Promotes a pawn to the specified piece type.

**Piece Types:** `Q` (Queen), `R` (Rook), `B` (Bishop), `N` (Knight)

### Reset Game

```http
POST /api/game/rooms/{roomId}/reset
```

Resets the game to initial position (available after game over).

## Input Validation

All endpoints validate input and return `400 Bad Request` with an error response for invalid data:

```json
{
  "error": "VALIDATION_ERROR",
  "message": "fieldName: error description"
}
```

### Join Room Validation

| Field              | Validation Rules                              |
| ------------------ | --------------------------------------------- |
| `roomId` (path)    | Exactly 8 characters                          |
| `playerId` (query) | Required, cannot be blank                     |
| `color` (query)    | Must be `white` or `black` (case-insensitive) |

### Make Move Validation

| Field            | Validation Rules          |
| ---------------- | ------------------------- |
| `roomId` (path)  | Exactly 8 characters      |
| `tokenId` (body) | Required, cannot be blank |
| `fromRow` (body) | Required, must be 0-7     |
| `fromCol` (body) | Required, must be 0-7     |
| `toRow` (body)   | Required, must be 0-7     |
| `toCol` (body)   | Required, must be 0-7     |

### Promote Pawn Validation

| Field              | Validation Rules                                           |
| ------------------ | ---------------------------------------------------------- |
| `roomId` (path)    | Exactly 8 characters                                       |
| `tokenId` (body)   | Required, cannot be blank                                  |
| `pieceType` (body) | Required, must be `Q`, `R`, `B`, or `N` (case-insensitive) |

### Error Codes

| Code               | Description                                    |
| ------------------ | ---------------------------------------------- |
| `VALIDATION_ERROR` | Request body or parameters failed validation   |
| `INVALID_ARGUMENT` | Request contains invalid game logic arguments  |
| `INTERNAL_ERROR`   | Unexpected server error (includes error message)|

## Project Structure

```
ChessApp/
├── src/main/java/dev/JoshBennett/chess/
│   ├── ChessApplication.java          # Spring Boot entry point
│   ├── controller/
│   │   ├── GameController.java        # REST API endpoints
│   │   └── GlobalExceptionHandler.java # Error handling with logging
│   ├── logic/
│   │   ├── GameMaster.java           # Core chess game logic
│   │   ├── Coord.java                # Board coordinate representation
│   │   ├── SquareStatus.java         # Square occupancy states
│   │   └── CastlingSide.java         # Castling type enum
│   ├── model/
│   │   └── GameRoom.java             # Game room state management
│   ├── pieces/
│   │   ├── ChessPiece.java           # Abstract base class
│   │   ├── Pawn.java, Rook.java, Knight.java, Bishop.java, Queen.java, King.java
│   └── service/
│       ├── GameService.java          # Business logic layer
│       └── RoomInfo.java             # Room state DTO
├── client/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Lobby.jsx             # Room creation/joining
│   │   │   ├── RoomGame.jsx          # Multiplayer game board
│   │   │   ├── ChessGame.jsx         # Single-player game
│   │   │   └── PromotionModal.jsx    # Pawn promotion UI
│   │   ├── hooks/
│   │   │   ├── useGameWebSocket.js   # WebSocket connection for multiplayer
│   │   │   └── useStockfish.ts       # AI opponent integration
│   │   ├── App.jsx                  # Main application
│   │   └── main.jsx                 # React entry point
│   └── package.json
└── pom.xml                           # Maven configuration
```

## Security

- **Player Authentication:** Each player is assigned a unique `playerId` stored in localStorage
- **Move Authorization:** Players receive a `tokenId` upon joining; all moves must include a valid token
- **Token Persistence:** Token is stored in localStorage to survive page reloads

## Game Rules

The application implements standard online chess rules including(chess.com):

- **Basic Moves:** All pieces move according to standard chess rules
- **Castling:** King-side (O-O) and queen-side (O-O-O) when conditions are met
- **En Passant:** Pawns can capture en passant under correct conditions
- **Pawn Promotion:** Pawns reaching the back rank must be promoted
- **Check:** Kings cannot remain in check
- **Checkmate:** Game ends when a player has no legal moves while in check
- **Stalemate:** Game ends in draw when a player has no legal moves but is not in check
- **Threefold Repetition:** Draw when the same position occurs three times
- **50-Move Rule:** Draw after 50 consecutive moves without pawn movement or capture
- **Insufficient Material:** Draw when neither player has enough pieces to checkmate

## Known Limitations

- No move history panel
- No game clock/timer
- Rooms are stored in memory and reset on server restart
- No mobile-optimized UI

## Future Improvements

- Move history and game notation
- Game clock with configurable time controls
- User authentication system
- Database persistence for game history
- Mobile-responsive design
- Spectator chat
- Rematch functionality
