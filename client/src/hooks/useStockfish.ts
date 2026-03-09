// src/hooks/useStockfish.ts
import { useEffect, useRef, useState, useCallback } from "react";

type StockfishStatus = "loading" | "ready" | "error";

export function useStockfish() {
  const workerRef = useRef<Worker | null>(null);
  const isSearchingRef = useRef(false);
  const [status, setStatus] = useState<StockfishStatus>("loading");
  const [isSearching, setIsSearching] = useState(false);
  const [bestMove, setBestMove] = useState<string | null>(null);

  useEffect(() => {
    // Create worker pointing to the file in /public
    const worker = new Worker("/stockfish-18-single.js", { type: "module" });
    workerRef.current = worker;

    worker.onmessage = (e: MessageEvent) => {
      const line = e.data as string;

      if (line.startsWith("Stockfish")) {
        console.log("Engine info:", line);
      } else if (line.includes("readyok")) {
        setStatus("ready");
      } else if (line.startsWith("bestmove")) {
        const move = line.split(" ")[1];
        setBestMove(move);
        isSearchingRef.current = false;
        setIsSearching(false);
      }
    };

    worker.onerror = (err) => {
      console.error("Stockfish worker error:", err);
      setStatus("error");
    };

    // Initialize UCI protocol
    worker.postMessage("uci");
    worker.postMessage("isready");

    return () => {
      worker.terminate();
    };
  }, []);

  const getBestMove = useCallback((fen: string, depth = 12) => {
    const w = workerRef.current;
    if (!w || status !== "ready" || isSearchingRef.current) return;

    isSearchingRef.current = true;
    setIsSearching(true);
    setBestMove(null);

    w.postMessage("ucinewgame");
    w.postMessage(`position fen ${fen}`);
    w.postMessage(`go depth ${depth}`);
  }, [status]);

  const setSkillLevel = useCallback((level: number) => {
    workerRef.current?.postMessage(`setoption name Skill Level value ${level}`);
  }, []);

  return {
    status,
    isSearching,
    bestMove,
    getBestMove,
    setSkillLevel,
  };
}