import React from 'react';

const LiveLog = () => {
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

  // Placeholder logs as requested
  const logs = [
    "Rok 10,000,000 př. n. l.: Tlupa šimpanzů se usadila v tropickém lese.",
    "Sezóna MONSOON začala. Ovoce je dostatek.",
    "[SYSTEM]: Kmen byl úspěšně inicializován.",
    "[SIMULACE]: Čekání na další cyklus (TICK)..."
  ];

  return (
    <div style={containerStyle}>
      <h3 style={{ margin: '0 0 10px 0', color: '#f8fafc', fontSize: '1rem', textTransform: 'uppercase' }}>
        Live Event Log
      </h3>
      {logs.map((log, index) => (
        <div key={index} style={lineStyle}>
          <span style={timestampStyle}>{`>_`}</span>
          {log}
        </div>
      ))}
    </div>
  );
};

export default LiveLog;