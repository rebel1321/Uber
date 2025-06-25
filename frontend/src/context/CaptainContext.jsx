import React, { createContext, useContext, useState } from 'react';

// Create the context
export const CaptainDataContext = createContext();

// Provider component
export const CaptainContext = ({ children }) => {
  const [captain, setCaptain] = useState(null);
  const [ isLoading,setIsLoading]=useState(false);
  const [ error,setError]=useState(null);

  // You can add more state or functions as needed
  const updateCaptain = (captainData)=>{
    setCaptain(captainData);
  };

  const value = {
    captain,
    setCaptain,
    isLoading,
    setIsLoading,
    error,
    setError,
    updateCaptain
  };

  return (
    <CaptainDataContext.Provider value={value}>
      {children}
    </CaptainDataContext.Provider>
  );
};
export const useCaptain = () => {
  const context=useContext(CaptainDataContext);
  if(!context){
    throw new Error('useCaptain must be used within a CaptainProvider');
  }
  return context;
}