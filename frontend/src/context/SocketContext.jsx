import React, { createContext, useContext, useEffect } from 'react';
import { io } from 'socket.io-client';

// Create Context
export const SocketContext = createContext();

// Initialize Socket outside the component
const socket = io(`${import.meta.env.VITE_BASE_URL_SOCKET}`);

// Context Provider Component
export const SocketProvider = ({ children }) => {

  useEffect(() => {
    socket.on('connect', () => {
      console.log('🔌 Connected to server');
    });

    socket.on('disconnect', () => {
      console.log('❌ Disconnected from server');
    });

    // Optional cleanup
    // return () => {
    //   socket.disconnect();
    // };
  }, []);

  

  return (
    <SocketContext.Provider value={{ socket }}>
      {children}
    </SocketContext.Provider>
  );
};

// Custom Hook to use the socket context
export const useSocket = () => useContext(SocketContext);
