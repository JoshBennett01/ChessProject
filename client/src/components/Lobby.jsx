import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const API_URL = "/api/game";

export default function Lobby() {
	const navigate = useNavigate();
	const [joinCode, setJoinCode] = useState("");
	const [loading, setLoading] = useState(false);
	const [error, setError] = useState("");

	const createGame = async () => {
		setLoading(true);
		setError("");
		try {
			const res = await axios.post(`${API_URL}/rooms`);
			navigate(`/game/${res.data.roomId}`);
		} catch (err) {
			setError("Failed to create game");
		}
		setLoading(false);
	};

	const joinGame = async () => {
		if (!joinCode.trim()) {
			setError("Please enter a room code");
			return;
		}
		setLoading(true);
		setError("");
		try {
			const res = await axios.get(
				`${API_URL}/rooms/${joinCode.toUpperCase()}`,
			);
			if (res.data) {
				navigate(`/game/${joinCode.toUpperCase()}`);
			}
		} catch (err) {
			setError("Room not found");
		}
		setLoading(false);
	};

	return (
		<div
			style={{
				display: "flex",
				flexDirection: "column",
				alignItems: "center",
				justifyContent: "center",
				minHeight: "100vh",
				backgroundColor: "#1a1a1a",
				color: "#fff",
				fontFamily: "Arial, sans-serif",
			}}
		>
			<h1 style={{ fontSize: "3rem", marginBottom: "40px" }}>
				Chess On Demand
			</h1>

			<div
				style={{
					backgroundColor: "#2d2d2d",
					padding: "40px",
					borderRadius: "12px",
					boxShadow: "0 4px 20px rgba(0,0,0,0.5)",
					textAlign: "center",
					minWidth: "320px",
				}}
			>
				<h2 style={{ marginBottom: "30px" }}>Multiplayer Lobby</h2>

				<button
					onClick={createGame}
					disabled={loading}
					style={{
						width: "100%",
						padding: "16px 24px",
						fontSize: "1.2rem",
						backgroundColor: "#28a745",
						color: "white",
						border: "none",
						borderRadius: "8px",
						cursor: loading ? "not-allowed" : "pointer",
						marginBottom: "20px",
						opacity: loading ? 0.7 : 1,
					}}
				>
					{loading ? "Creating..." : "Create New Game"}
				</button>

				<div style={{ margin: "20px 0", color: "#888" }}>OR</div>

				<div style={{ display: "flex", gap: "10px" }}>
					<input
						type="text"
						value={joinCode}
						onChange={(e) =>
							setJoinCode(e.target.value.toUpperCase())
						}
						placeholder="Enter room code"
						maxLength={8}
						style={{
							flex: 1,
							padding: "12px 16px",
							fontSize: "1rem",
							border: "1px solid #444",
							borderRadius: "8px",
							backgroundColor: "#1a1a1a",
							color: "#fff",
							textTransform: "uppercase",
							textAlign: "center",
						}}
						onKeyDown={(e) => e.key === "Enter" && joinGame()}
					/>
					<button
						onClick={joinGame}
						disabled={loading}
						style={{
							padding: "12px 24px",
							fontSize: "1rem",
							backgroundColor: "#007bff",
							color: "white",
							border: "none",
							borderRadius: "8px",
							cursor: loading ? "not-allowed" : "pointer",
							opacity: loading ? 0.7 : 1,
						}}
					>
						{loading ? "..." : "Join"}
					</button>
				</div>

				{error && (
					<p style={{ color: "#ff6b6b", marginTop: "15px" }}>
						{error}
					</p>
				)}

				<div
					style={{
						marginTop: "30px",
						paddingTop: "20px",
						borderTop: "1px solid #444",
					}}
				>
					<p style={{ color: "#888", fontSize: "0.9rem" }}>
						Want to play solo?{" "}
						<button
							onClick={() => navigate("/solo")}
							style={{
								background: "none",
								border: "none",
								color: "#007bff",
								cursor: "pointer",
								textDecoration: "underline",
							}}
						>
							Play vs AI
						</button>
					</p>
				</div>
			</div>
		</div>
	);
}
