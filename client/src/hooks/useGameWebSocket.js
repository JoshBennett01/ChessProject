import { useEffect, useRef, useCallback, useState } from 'react';
import { Client } from '@stomp/stompjs';

export function useGameWebSocket(roomId, { onMoveMade, onGameOver, onGameReset, onPlayerJoined, onRoomFull }) {
    const clientRef = useRef(null);
    const subscriptionRef = useRef(null);
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        if (!roomId) return;

        const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.hostname}:8080/ws`;

        const client = new Client({
            brokerURL: wsUrl,
            reconnectDelay: 5000,
            onConnect: () => {
                setIsConnected(true);
                subscriptionRef.current = client.subscribe(`/topic/room/${roomId}`, (message) => {
                    try {
                        const event = JSON.parse(message.body);
                        handleEvent(event);
                    } catch (e) {
                        console.error('Failed to parse message:', message.body, e);
                    }
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
            onWebSocketError: (error) => {
                console.error('WebSocket error:', error);
            },
            onStompError: (error) => {
                console.error('STOMP error:', error);
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            if (subscriptionRef.current) {
                subscriptionRef.current.unsubscribe();
            }
            if (clientRef.current) {
                clientRef.current.deactivate();
            }
        };
    }, [roomId]);

    const handleEvent = useCallback((event) => {
        switch (event.type) {
            case 'MoveMade':
                onMoveMade?.(event);
                break;
            case 'GameOver':
                onGameOver?.(event);
                break;
            case 'GameReset':
                onGameReset?.(event);
                break;
            case 'PlayerJoined':
                onPlayerJoined?.(event);
                break;
            case 'JoinedRoom':
                onRoomFull?.(event);
                break;
            default:
                break;
        }
    }, [onMoveMade, onGameOver, onGameReset, onPlayerJoined, onRoomFull]);

    const sendMove = useCallback((tokenId, fromRow, fromCol, toRow, toCol) => {
        if (clientRef.current?.connected) {
            clientRef.current.publish({
                destination: `/app/room/${roomId}/move`,
                body: JSON.stringify({ tokenId, fromRow, fromCol, toRow, toCol }),
            });
        }
    }, [roomId]);

    const sendPromotion = useCallback((tokenId, pieceType) => {
        if (clientRef.current?.connected) {
            clientRef.current.publish({
                destination: `/app/room/${roomId}/promote`,
                body: JSON.stringify({ tokenId, pieceType }),
            });
        }
    }, [roomId]);

    const sendReset = useCallback(() => {
        if (clientRef.current?.connected) {
            clientRef.current.publish({
                destination: `/app/room/${roomId}/reset`,
            });
        }
    }, [roomId]);

    return {
        isConnected,
        sendMove,
        sendPromotion,
        sendReset,
    };
}
