import { BrowserRouter, Routes, Route } from "react-router-dom";
import ChessGame from "./components/ChessGame";
import Lobby from "./components/Lobby";
import RoomGame from "./components/RoomGame";
import "./App.css";

function App() {
	return (
		<BrowserRouter>
			<div className="app">
				<Routes>
					<Route path="/" element={<Lobby />} />
					<Route path="/solo" element={<ChessGame />} />
					<Route path="/game/:roomId" element={<RoomGame />} />
				</Routes>
			</div>
		</BrowserRouter>
	);
}

export default App;
