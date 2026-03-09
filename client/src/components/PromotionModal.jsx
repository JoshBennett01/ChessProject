import React from "react";
import "../PromotionModal.css";

const PromotionModal = ({ isOpen, color = "white", onSelect, onClose }) => {
	if (!isOpen) return null;

	const pieces = [
		{ type: "Q", symbol: color === "white" ? "♕" : "♛" },
		{ type: "R", symbol: color === "white" ? "♖" : "♜" },
		{ type: "B", symbol: color === "white" ? "♗" : "♝" },
		{ type: "N", symbol: color === "white" ? "♘" : "♞" },
	];

	return (
		<div className="promotion-overlay" onClick={onClose}>
			<div
				className="promotion-dialog"
				onClick={(e) => e.stopPropagation()}
			>
				<h3>Promote to?</h3>

				<div className="promotion-pieces">
					{pieces.map((piece) => (
						<button
							key={piece.type}
							className="promotion-piece-btn"
							onClick={() => onSelect(piece.type)}
							title={
								piece.type.charAt(0).toUpperCase() +
								piece.type.slice(1)
							}
						>
							<span className="piece-symbol">{piece.symbol}</span>
							<span className="piece-label">{piece.type}</span>
						</button>
					))}
				</div>

				{onClose && (
					<button className="cancel-btn" onClick={onClose}>
						Cancel
					</button>
				)}
			</div>
		</div>
	);
};

export default PromotionModal;
