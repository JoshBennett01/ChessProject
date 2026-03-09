import { useState, useEffect } from "react";
import { Chessboard } from "react-chessboard";
import PromotionModal from "./PromotionModal";
import axios from "axios";
import { Chess } from "chess.js";
import { useStockfish } from "../hooks/useStockfish";

const API_URL = "/api/game";
const startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1";

export default function ChessGame() {
	const [fen, setFen] = useState(startFen);
	const [currentTeam, setCurrentTeam] = useState(true);
	const [status, setStatus] = useState("Waiting for backend...");
	const [enableAI, setEnableAI] = useState(false);
	const [playAsBlack, setPlayAsBlack] = useState(false);
	const [boardOrientation, setBoardOrientation] = useState("white");
	const [sliderValue, setSliderValue] = useState(10);

	const [promotionOpen, setPromotionOpen] = useState(false);
	const [promotionColor, setPromotionColor] = useState("white");
	const [promotionPendingAi, setPromotionPendingAi] = useState(false);

	const {
		status: engineStatus,
		isSearching,
		bestMove,
		getBestMove,
		setSkillLevel,
	} = useStockfish();

	// Load initial FEN from backend
	useEffect(() => {
		const loadFromBackend = async () => {
			try {
				const res = await axios.get(`${API_URL}/fen`);
				setFen(res.data || startFen);
			} catch (err) {
				console.warn("No backend available");
			}
		};
		loadFromBackend();
	}, []);

	// Request Stockfish move when it's AI's turn
	useEffect(() => {
		if (!enableAI) return;
		if (currentTeam !== playAsBlack) return;
		if (isSearching) return;

		getBestMove(fen, sliderValue);
	}, [enableAI, currentTeam, fen, getBestMove, isSearching, playAsBlack]);

	// Set initial skill level
	useEffect(() => {
		setSkillLevel(sliderValue);
	}, [setSkillLevel, sliderValue]);

	// Apply Stockfish's best move
	useEffect(() => {
		if (!enableAI) return;
		if (currentTeam !== playAsBlack) return;
		if (!bestMove) return;

		console.log();
		const uci = bestMove.trim();
		if (uci.length < 4) return;

		const sourceSquare = uci.slice(0, 2);
		const targetSquare = uci.slice(2, 4);
		const PawnPromotion = uci.slice(4, 5);

		console.log(sourceSquare + ", " + targetSquare + ", " + PawnPromotion);

		const fakeInfo = {
			piece: {
				isSparePiece: false,
				position: sourceSquare,
			},
			sourceSquare,
			targetSquare,
		};

		if (promotionPendingAi) {
			handlePromotionChoice(PawnPromotion);
			setPromotionPendingAi(false);
		} else {
			onPieceDrop(fakeInfo).then((accepted) => {
				if (!accepted) {
					console.warn("AI move was rejected by backend", uci);
				} else {
					if (promotionPendingAi) {
						handlePromotionChoice(PawnPromotion);
						setPromotionPendingAi(false);
					}
				}
			});
		}
	}, [bestMove, enableAI]);

	const handleSliderChange = (e) => {
		const value = parseInt(e.target.value);
		setSliderValue(value);
		setSkillLevel(value);
	};

	const onPieceDrop = async (info) => {
		const { sourceSquare, targetSquare } = info;

		if (
			typeof sourceSquare !== "string" ||
			typeof targetSquare !== "string"
		) {
			console.warn("Invalid drop args:", { sourceSquare, targetSquare });
			return false;
		}

		try {
			const response = await axios.post(`${API_URL}/move`, {
				fromRow: parseInt(sourceSquare[1]) - 1,
				fromCol: sourceSquare.charCodeAt(0) - 97,
				toRow: parseInt(targetSquare[1]) - 1,
				toCol: targetSquare.charCodeAt(0) - 97,
			});

			const data = response.data;

			if (data.message === "PROMOTION_REQUIRED") {
				const square = data.promotionSquare;

				// Optional: determine color from square or from backend data
				const rank = parseInt(square[1], 10);
				const promotingColor = rank === 8 ? "white" : "black";

				if (currentTeam === playAsBlack && enableAI) {
					setPromotionPendingAi(true);
				} else {
					setPromotionColor(promotingColor);
					setPromotionOpen(true);
				}

				return true;
			}
			if (data.success && data.fen) {
				setFen(data.fen);
				setCurrentTeam(getTurnFromFen(data.fen));
				setStatus(data.message || "Move completed");
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

		try {
			const res = await axios.post(`${API_URL}/promote`, {
				pieceType: piece.toUpperCase(),
			});

			const data = res.data;

			if (data.success && data.fen) {
				setFen(data.fen);
				setCurrentTeam(getTurnFromFen(data.fen));
				setStatus(data.message || "Promoted to " + piece);
			} else {
				setStatus(data.message || "Promotion failed");
			}
		} catch (err) {
			console.error("Promotion error:", err);
			setStatus("Promotion failed");
		}
	};

	function getTurnFromFen(fen) {
		try {
			const chess = new Chess(fen);
			return chess.turn() === "w";
		} catch (err) {
			console.warn("Bad FEN:", fen);
			return null;
		}
	}

	const resetGame = async () => {
		try {
			const res = await axios.post(`${API_URL}/reset`);
			setFen(res.data || startFen);
			setCurrentTeam(true);
			setStatus("Game Reset");
		} catch (err) {
			console.error("Reset failed:", err);
		}
	};

	const flipBoard = () => {
		setBoardOrientation((prev) => (prev === "white" ? "black" : "white"));
	};

	return (
		<div style={{ textAlign: "center", padding: "20px" }}>
			<h1>Chess On Demand</h1>

			<div
				style={{
					display: "flex",
					gap: "10px",
					justifyContent: "center",
					alignItems: "center",
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
						width: "120px",
						fontSize: "15px",
						backgroundColor: "#120303",
						color: "#ffffff",
						cursor: "default",
						boxShadow: "inset 0 1px 3px rgba(0,0,0,0.1)",
					}}
				/>
				<button
					onClick={flipBoard}
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
					Flip Board
				</button>
				<label
					style={{
						color: "#fff",
						display: "flex",
						alignItems: "center",
						gap: "6px",
					}}
				>
					<input
						type="checkbox"
						checked={playAsBlack}
						onChange={(e) => setPlayAsBlack(e.target.checked)}
					/>
					Switch Teams(Black)
				</label>
			</div>

			<div style={{ width: "600px", margin: "20px auto" }}>
				<Chessboard
					id="CustomBoard"
					options={{
						position: fen,
						onPieceDrop,
						animationDuration: 200,
						boardOrientation: boardOrientation,
						arePiecesDraggable: true,
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

			<div
				style={{
					marginTop: "30px",
					display: "flex",
					gap: "20px",
					justifyContent: "center",
					alignItems: "center",
				}}
			>
				<label
					style={{
						color: "#fff",
						display: "flex",
						alignItems: "center",
						gap: "6px",
					}}
				>
					<input
						type="range"
						min="0"
						max="20"
						value={sliderValue}
						onChange={handleSliderChange}
					/>
					<span>Skill: {sliderValue}</span>
				</label>
				<label
					style={{
						color: "#fff",
						display: "flex",
						alignItems: "center",
						gap: "6px",
					}}
				>
					<input
						type="checkbox"
						checked={enableAI}
						onChange={(e) => setEnableAI(e.target.checked)}
					/>
					Enable AI Stockfish
				</label>

				<button
					onClick={resetGame}
					style={{
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
		</div>
	);
}
