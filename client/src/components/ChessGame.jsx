import { useState, useEffect, useCallback } from "react";
import { Chessboard } from "react-chessboard";
import { useNavigate } from "react-router-dom";
import PromotionModal from "./PromotionModal";
import { Chess } from "chess.js";
import { useStockfish } from "../hooks/useStockfish";

const startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

export default function ChessGame() {
	const navigate = useNavigate();

	const [fen, setFen] = useState(startFen);
	const [game, setGame] = useState(new Chess());
	const [currentTurn, setCurrentTurn] = useState("white");
	const [status, setStatus] = useState("White to move");
	const [enableAI, setEnableAI] = useState(false);
	const [gameOver, setGameOver] = useState(false);
	const [playAsBlack, setPlayAsBlack] = useState(false);
	const [boardOrientation, setBoardOrientation] = useState("white");
	const [sliderValue, setSliderValue] = useState(10);

	const [promotionOpen, setPromotionOpen] = useState(false);
	const [promotionSquare, setPromotionSquare] = useState(null);
	const [pendingMove, setPendingMove] = useState(null);

	const { isSearching, bestMove, getBestMove, setSkillLevel } =
		useStockfish();

	const getGameStatus = useCallback((chess) => {
		if (chess.isCheckmate()) {
			setGameOver(true);
			return chess.turn() === "w"
				? "Black wins by checkmate!"
				: "White wins by checkmate!";
		}
		if (chess.isStalemate()) {
			setGameOver(true);
			return "Stalemate - Draw!";
		}
		if (chess.isThreefoldRepetition()) {
			setGameOver(true);
			return "Threefold repetition - Draw!";
		}
		if (chess.isInsufficientMaterial()) {
			setGameOver(true);
			return "Insufficient material - Draw!";
		}
		if (chess.isDraw()) {
			setGameOver(true);
			return "Draw!";
		}
		if (chess.isCheck()) {
			return `${chess.turn() === "w" ? "White" : "Black"} is in check!`;
		}
		return `${chess.turn() === "w" ? "White" : "Black"} to move`;
	}, []);

	useEffect(() => {
		setSkillLevel(sliderValue);
	}, [setSkillLevel, sliderValue]);

	useEffect(() => {
		if (!enableAI || gameOver) return;
		if (currentTurn === (playAsBlack ? "black" : "white")) return;
		if (isSearching) return;

		getBestMove(fen, sliderValue);
	}, [enableAI, currentTurn, fen, getBestMove, isSearching, playAsBlack]);

	useEffect(() => {
		if (!enableAI || gameOver) return;
		if (currentTurn === (playAsBlack ? "black" : "white")) return;
		if (!bestMove) return;

		const uci = bestMove.trim();
		if (uci.length < 4) return;

		const sourceSquare = uci.slice(0, 2);
		const targetSquare = uci.slice(2, 4);
		const promotionPiece = uci.slice(4, 5);

		const chess = new Chess(fen);

		try {
			if (promotionPiece) {
				const move = chess.move({
					from: sourceSquare,
					to: targetSquare,
					promotion: promotionPiece.toLowerCase(),
				});
				if (move) {
					setFen(chess.fen());
					setGame(new Chess(chess.fen()));
					setCurrentTurn(chess.turn() === "w" ? "white" : "black");
					setStatus(getGameStatus(chess));
				}
			} else {
				const move = chess.move({
					from: sourceSquare,
					to: targetSquare,
				});
				if (move) {
					setFen(chess.fen());
					setGame(new Chess(chess.fen()));
					setCurrentTurn(chess.turn() === "w" ? "white" : "black");
					setStatus(getGameStatus(chess));
				}
			}
		} catch (err) {
			console.warn("AI move rejected:", err);
		}
	}, [bestMove, enableAI, fen, getGameStatus, playAsBlack, currentTurn]);

	const onPieceDrop = (info) => {
		const { sourceSquare, targetSquare } = info;

		if (
			typeof sourceSquare !== "string" ||
			typeof targetSquare !== "string"
		) {
			return false;
		}

		if (enableAI && currentTurn === (playAsBlack ? "white" : "black")) {
			return false;
		}

		const chess = new Chess(fen);

		const isPromotion =
			chess.get(sourceSquare)?.type === "p" &&
			((chess.turn() === "w" && targetSquare[1] === "8") ||
				(chess.turn() === "b" && targetSquare[1] === "1"));

		if (isPromotion) {
			setPendingMove({ from: sourceSquare, to: targetSquare });
			setPromotionSquare(targetSquare);
			setPromotionOpen(true);
			return true;
		}

		try {
			const move = chess.move({
				from: sourceSquare,
				to: targetSquare,
			});

			if (move) {
				setGame(new Chess(chess.fen()));
				setFen(chess.fen());
				setCurrentTurn(chess.turn() === "w" ? "white" : "black");
				setStatus(getGameStatus(chess));
				return true;
			}
		} catch (err) {
			console.warn("Invalid move:", err);
			return false;
		}

		return false;
	};

	const handlePromotionChoice = (piece) => {
		setPromotionOpen(false);

		if (!pendingMove) return;

		const chess = new Chess(fen);

		try {
			const move = chess.move({
				from: pendingMove.from,
				to: pendingMove.to,
				promotion: piece.toLowerCase(),
			});

			if (move) {
				setGame(new Chess(chess.fen()));
				setFen(chess.fen());
				setCurrentTurn(chess.turn() === "w" ? "white" : "black");
				setStatus(getGameStatus(chess));
			}
		} catch (err) {
			console.warn("Promotion failed:", err);
		}

		setPendingMove(null);
		setPromotionSquare(null);
	};

	const handleSliderChange = (e) => {
		const value = parseInt(e.target.value);
		setSliderValue(value);
		setSkillLevel(value);
	};

	const resetGame = () => {
		const newGame = new Chess();
		setGameOver(false);
		setGame(newGame);
		setFen(startFen);
		setCurrentTurn("white");
		setStatus("White to move");
		setPendingMove(null);
		setPromotionSquare(null);
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
						width: "220px",
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
					Play as Black
				</label>
			</div>

			<div style={{ width: "600px", margin: "20px auto" }}>
				<Chessboard
					id="LocalBoard"
					options={{
						position: fen,
						onPieceDrop: onPieceDrop,
						animationDuration: 200,
						boardOrientation: boardOrientation,
						arePiecesDraggable: true,
						customBoardStyle: {
							borderRadius: "4px",
							boxShadow: "0 2px 10px rgba(0, 0, 0, 0.5)",
						},
					}}
					animationDuration={200}
					arePiecesTilted={false}
					boardWidth={600}
				/>
				<PromotionModal
					isOpen={promotionOpen}
					color={
						pendingMove
							? game.turn() === "w"
								? "white"
								: "black"
							: "white"
					}
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
	);
}
