import { useState, useEffect, useRef, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { Chessboard } from "react-chessboard";
import PromotionModal from "./PromotionModal";
import axios from "axios";

const API_URL = "/api/game";
const POLL_INTERVAL = 1000;
const startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

function generatePlayerId() {
	let id = localStorage.getItem("chess-player-id");
	if (!id) {
		id = Math.random().toString(36).substring(2, 10).toUpperCase();
		localStorage.setItem("chess-player-id", id);
	}
	return id;
}

function getStoredTokenId(roomId) {
	return localStorage.getItem(`chess-token-${roomId}`);
}

function setStoredTokenId(roomId, tokenId) {
	localStorage.setItem(`chess-token-${roomId}`, tokenId);
}

export default function RoomGame() {
	const { roomId } = useParams();
	const navigate = useNavigate();
	const playerId = useRef(generatePlayerId()).current;

	const [fen, setFen] = useState(startFen);
	const [tokenId, setTokenId] = useState();
	const [status, setStatus] = useState("Waiting for opponent...");
	const [playerColor, setPlayerColor] = useState(null);
	const [isPlayerTeam, setPlayerTeam] = useState(null);
	const [isWhiteTurn, setWhiteTurn] = useState(true);
	const [boardOrientation, setBoardOrientation] = useState("white");
	const [gameOver, setGameOver] = useState(false);
	const [shareUrl, setShareUrl] = useState("");
	const [copied, setCopied] = useState(false);
	const [roomFull, setRoomFull] = useState(false);
	const [gameStart, setGameStart] = useState(false);

	const [promotionOpen, setPromotionOpen] = useState(false);
	const [promotionColor, setPromotionColor] = useState("white");
	const [pendingMove, setPendingMove] = useState(null);
	const hasInitialized = useRef(false);

	useEffect(() => {
		if (hasInitialized.current) return;
		hasInitialized.current = true;

		const initGame = async () => {
			try {
				const res = await axios.get(`${API_URL}/rooms/${roomId}`);
				const data = res.data;

				setFen(data.fen || startFen);
				setGameOver(data.gameOver);
				setWhiteTurn(data.isWhiteTurn);

				const fullUrl = window.location.origin + `/game/${roomId}`;
				setShareUrl(fullUrl);

				if (data.whitePlayerId === playerId) {
					setPlayerColor("white");
					setPlayerTeam(true);
					setBoardOrientation("white");
					const storedToken = getStoredTokenId(roomId);
					if (storedToken) {
						setTokenId(storedToken);
					}
				} else if (data.blackPlayerId === playerId) {
					setPlayerColor("black");
					setPlayerTeam(false);
					setBoardOrientation("black");
					const storedToken = getStoredTokenId(roomId);
					if (storedToken) {
						setTokenId(storedToken);
					}
				} else if (!data.whitePlayerId) {
					const storedToken = getStoredTokenId(roomId);
					if (storedToken) {
						setTokenId(storedToken);
						setPlayerColor("white");
						setPlayerTeam(true);
						setBoardOrientation("white");
					} else {
						const joinRes = await axios.post(
							`${API_URL}/rooms/${roomId}/join?playerId=${playerId}&color=white`,
						);
						const joinData = joinRes.data;
						setTokenId(joinData.tokenId);
						setStoredTokenId(roomId, joinData.tokenId);

						setPlayerColor("white");
						setPlayerTeam(true);
						setBoardOrientation("white");
					}
				} else if (!data.blackPlayerId) {
					const storedToken = getStoredTokenId(roomId);
					if (storedToken) {
						setTokenId(storedToken);
						setPlayerColor("black");
						setPlayerTeam(false);
						setBoardOrientation("black");
					} else {
						const joinRes = await axios.post(
							`${API_URL}/rooms/${roomId}/join?playerId=${playerId}&color=black`,
						);
						const joinData = joinRes.data;
						setTokenId(joinData.tokenId);
						setStoredTokenId(roomId, joinData.tokenId);

						setPlayerColor("black");
						setPlayerTeam(false);
						setBoardOrientation("black");
					}
				} else {
					// Room full
					setRoomFull(true);
					setStatus("Room is full. Spectating...");
				}
			} catch (err) {
				setStatus("Failed to load game");
			}
		};
		initGame();
	}, []);

	useEffect(() => {
		if (!roomId || roomFull) return;

		const poll = async () => {
			try {
				const res = await axios.get(`${API_URL}/rooms/${roomId}`);
				const data = res.data;

				setFen(data.fen);
				setGameOver(data.gameOver);
				setWhiteTurn(data.isWhiteTurn);
				if (data.gameOver) {
					setStatus(data.message);
				}
				if (data.isWhiteTurn === isPlayerTeam) {
					setStatus(data.message);
				}
			} catch (err) {
				console.error("Poll error:", err);
			}
		};

		poll();
		const interval = setInterval(poll, POLL_INTERVAL);
		return () => clearInterval(interval);
	}, [roomId, roomFull, gameOver, isPlayerTeam]);

	const onPieceDrop = async (info) => {
		if (isPlayerTeam !== isWhiteTurn || gameOver) {
			return false;
		}

		const { sourceSquare, targetSquare } = info;

		if (
			typeof sourceSquare !== "string" ||
			typeof targetSquare !== "string"
		) {
			return false;
		}

		try {
			const response = await axios.post(
				`${API_URL}/rooms/${roomId}/move`,
				{
					tokenId: tokenId,
					fromRow: parseInt(sourceSquare[1]) - 1,
					fromCol: sourceSquare.charCodeAt(0) - 97,
					toRow: parseInt(targetSquare[1]) - 1,
					toCol: targetSquare.charCodeAt(0) - 97,
				},
			);

			const data = response.data;

			if (data.message === "PROMOTION_REQUIRED") {
				const square = data.promotionSquare;
				const rank = parseInt(square[1], 10);
				const promotingColor = rank === 8 ? "white" : "black";

				setPromotionColor(promotingColor);
				setPendingMove({ sourceSquare, targetSquare });
				setPromotionOpen(true);
				return true;
			}

			if (data.success && data.fen) {
				setGameStart(true);
				setFen(data.fen);
				setStatus("Opponent's turn...");
				console.log(data.message);
				return true;
			}

			setStatus(data.message || "Invalid move");
			return false;
		} catch (err) {
			console.error("Move error:", err);
			return false;
		}
	};

	const handlePromotionChoice = async (piece) => {
		setPromotionOpen(false);

		if (!pendingMove) return;

		try {
			const res = await axios.post(`${API_URL}/rooms/${roomId}/promote`, {
				tokenId: tokenId,
				pieceType: piece.toUpperCase(),
			});

			const data = res.data;
			if (data.success && data.fen) {
				setFen(data.fen);
				setStatus("Opponent's turn...");
			} else {
				setStatus(data.message || "Promotion failed");
			}
		} catch (err) {
			console.error("Promotion error:", err);
		}

		setPendingMove(null);
	};

	const copyLink = () => {
		navigator.clipboard.writeText(shareUrl);
		setCopied(true);
		setTimeout(() => setCopied(false), 2000);
	};

	const resetGame = async () => {
		try {
			if (gameOver) {
				const res = await axios.post(
					`${API_URL}/rooms/${roomId}/reset`,
				);

				const data = res.data;
				if (data.fen && data.gameOver && data.isWhiteTurn) {
					setFen(data.fen);
					setGameOver(data.gameOver);
					setWhiteTurn(data.isWhiteTurn);
					setStatus("Game has been reset");
				}
			} else {
				setStatus("Game cannot be reset");
			}
		} catch (err) {
			console.error("Reset error:", err);
		}
	};

	if (roomFull) {
		return (
			<div
				style={{ textAlign: "center", padding: "50px", color: "#fff" }}
			>
				<h1>Room is Full</h1>
				<p>This game already has two players.</p>
				<button
					onClick={() => navigate("/ ")}
					style={{
						padding: "12px 24px",
						fontSize: "1rem",
						cursor: "pointer",
					}}
				>
					Back to Lobby
				</button>
			</div>
		);
	}

	return (
		<div style={{ textAlign: "center", padding: "20px" }}>
			<h1>Chess On Demand</h1>

			<div
				style={{
					display: "flex",
					gap: "10px",
					justifyContent: "center",
					alignItems: "center",
					marginBottom: "15px",
				}}
			>
				<input
					type="text"
					value={status}
					readOnly
					style={{
						padding: "12px 16px",
						border: "1px solid #8c8c8c",
						borderRadius: "8px",
						outline: "none",
						width: "180px",
						fontSize: "15px",
						backgroundColor: "#120303",
						color: "#ffffff",
						cursor: "default",
					}}
				/>
				<button
					onClick={copyLink}
					style={{
						padding: "12px 20px",
						fontSize: "15px",
						backgroundColor: copied ? "#28a745" : "#6c757d",
						color: "white",
						border: "none",
						borderRadius: "8px",
						cursor: "pointer",
					}}
				>
					{copied ? "Copied!" : "Share Link"}
				</button>
				<button
					onClick={() => navigate("/ ")}
					style={{
						padding: "12px 20px",
						fontSize: "15px",
						backgroundColor: "#6c757d",
						color: "white",
						border: "none",
						borderRadius: "8px",
						cursor: "pointer",
					}}
				>
					Exit
				</button>
			</div>

			{playerColor && (
				<p style={{ color: "#888", marginBottom: "10px" }}>
					You are playing as{" "}
					<strong>
						{playerColor === "white" ? "White" : "Black"}
					</strong>
				</p>
			)}

			<div style={{ width: "600px", margin: "20px auto" }}>
				<Chessboard
					id="RoomBoard"
					options={{
						position: fen,
						onPieceDrop: onPieceDrop,
						animationDuration: 200,
						boardOrientation: boardOrientation,
						arePiecesDraggable: isPlayerTeam && !gameOver,
						customBoardStyle: {
							borderRadius: "4px",
							boxShadow: "0 2px 10px rgba(0, 0, 0, 0.5)",
						},
					}}
					arePiecesTilted={false}
					boardWidth={600}
				/>
				<PromotionModal
					isOpen={promotionOpen}
					color={promotionColor}
					onSelect={handlePromotionChoice}
				/>
			</div>

			<button
				onClick={resetGame}
				style={{
					marginTop: "20px",
					padding: "12px 28px",
					fontSize: "1.1em",
					backgroundColor: "#007bff",
					color: "white",
					border: "none",
					borderRadius: "6px",
					cursor: "pointer",
				}}
			>
				Reset Game
			</button>
		</div>
	);
}
