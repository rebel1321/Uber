import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";

const SocketContext = createContext(null);

const SOCKET_RECONNECT_DELAY_MS = 2000;

export const SocketProvider = ({ children }) => {
  const [socket, setSocket] = useState(null);
  const [isConnected, setIsConnected] = useState(false);
  const handlersRef = useRef(new Map());
  const reconnectTimerRef = useRef(null);

  const connect = useCallback(() => {
    if (!import.meta.env.VITE_WS_URL) {
      return;
    }

    const ws = new WebSocket(import.meta.env.VITE_WS_URL);

    ws.onopen = () => {
      setIsConnected(true);
    };

    ws.onclose = () => {
      setIsConnected(false);
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current);
      }
      reconnectTimerRef.current = setTimeout(() => {
        connect();
      }, SOCKET_RECONNECT_DELAY_MS);
    };

    ws.onerror = () => {
      ws.close();
    };

    ws.onmessage = (event) => {
      let message;
      try {
        message = JSON.parse(event.data);
      } catch (error) {
        return;
      }

      if (!message || !message.type) {
        return;
      }

      const handlers = handlersRef.current.get(message.type);
      if (!handlers) {
        return;
      }

      handlers.forEach((handler) => {
        handler(message.payload);
      });
    };

    setSocket(ws);
  }, []);

  useEffect(() => {
    connect();

    return () => {
      if (reconnectTimerRef.current) {
        clearTimeout(reconnectTimerRef.current);
      }
      if (socket) {
        socket.close();
      }
    };
  }, [connect]);

  const send = useCallback(
    (type, payload) => {
      if (!socket || socket.readyState !== WebSocket.OPEN) {
        return;
      }
      socket.send(JSON.stringify({ type, payload }));
    },
    [socket]
  );

  const on = useCallback((type, handler) => {
    if (!handlersRef.current.has(type)) {
      handlersRef.current.set(type, new Set());
    }

    const handlers = handlersRef.current.get(type);
    handlers.add(handler);

    return () => {
      handlers.delete(handler);
      if (handlers.size === 0) {
        handlersRef.current.delete(type);
      }
    };
  }, []);

  return (
    <SocketContext.Provider value={{ socket, isConnected, send, on }}>
      {children}
    </SocketContext.Provider>
  );
};

export const useSocket = () => {
  const context = useContext(SocketContext);
  if (!context) {
    throw new Error("useSocket must be used within SocketProvider");
  }
  return context;
};
