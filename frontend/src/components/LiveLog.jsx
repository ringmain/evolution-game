import React, { useEffect, useRef } from 'react';

const LiveLog = ({ messages = [] }) => {
  const logEndRef = useRef(null);

  // Auto-scroll to the bottom whenever new messages arrive
  useEffect(() => {
    if (logEndRef.current) {
      logEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  const containerStyle = {
    backgroundColor: '#0f172a', // Deep slate background
    border: '1px solid #334155',
    borderRadius: '8px',
    padding: '16px',
    fontFamily: '"Fira Code", monospace',
    color: '#4ade80', // Neon green text
    height: '200px',
    overflowY: 'auto',
    boxShadow: 'inset 0 0 10px rgba(0,0,0,0.5)',
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  };

  const lineStyle = {
    fontSize: '0.9rem',
    borderBottom: '1px dashed #1e293b',
    paddingBottom: '4px',
  };

  const timestampStyle = {
    color: '#38bdf8', // Neon blue timestamp
    marginRight: '8px',
  };

  return (
    <div style={containerStyle}>
      <h3 style={{ margin: '0 0 10px 0', color: '#f8fafc', fontSize: '1rem', textTransform: 'uppercase', flexShrink: 0 }}>
        Live Event Log
      </h3>
      
      {(!messages || messages.length === 0) ? (
        <div style={lineStyle}>
          <span style={timestampStyle}>{`>_`}</span>
          Čekání na první události...
        </div>
      ) : (
        messages.map((log, index) => (
          <div key={index} style={lineStyle}>
            <span style={timestampStyle}>{`>_`}</span>
            {log}
          </div>
        ))
      )}
      
      {/* Invisible element to anchor our auto-scroll */}
      <div ref={logEndRef} />
    </div>
  );
};

export default LiveLog;