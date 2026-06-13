import React from 'react';
import GameDashboard from './views/GameDashboard';

function App() {
  // Global strict dark theme wrapper
  const appStyle = {
    backgroundColor: '#020617', // Very dark blue/black
    color: '#e2e8f0', // Slate text
    margin: 0,
    padding: 0,
    minHeight: '100vh',
    width: '100vw',
    fontFamily: '"Inter", "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
    overflow: 'hidden', // Prevent scrolling on the body level
  };

  return (
    <div style={appStyle}>
      {/* Global CSS reset for the body tag to ensure no native margins ruin the 100vh */}
      <style>
        {`
          body { margin: 0; padding: 0; overflow: hidden; background-color: #020617; }
          * { box-sizing: border-box; }
          
          /* Custom Scrollbar for inner components */
          ::-webkit-scrollbar { width: 8px; }
          ::-webkit-scrollbar-track { background: #0f172a; }
          ::-webkit-scrollbar-thumb { background: #334155; border-radius: 4px; }
          ::-webkit-scrollbar-thumb:hover { background: #475569; }
        `}
      </style>
      
      <GameDashboard />
    </div>
  );
}

export default App;