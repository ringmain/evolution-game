import React, { useState, useEffect } from 'react';
import LiveLog from '../components/LiveLog';

const GameDashboard = () => {
  const [tribe, setTribe] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isTicking, setIsTicking] = useState(false);

  const API_BASE_URL = 'http://localhost:8080/api/tribe';

  // Fetch initial tribe state on mount
  useEffect(() => {
    const fetchTribe = async () => {
      try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) throw new Error('Failed to fetch tribe data.');
        const data = await response.json();
        setTribe(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchTribe();
  }, []);

  // Handle TICK request
  const handleTick = async () => {
    if (isTicking) return;
    setIsTicking(true);
    try {
      const response = await fetch(`${API_BASE_URL}/tick`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (!response.ok) throw new Error('Failed to execute tick.');
      const updatedTribe = await response.json();
      setTribe(updatedTribe);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsTicking(false);
    }
  };

  // --- STYLES ---
  
  // Using CSS Grid to strictly enforce the 3-column layout
  const gridContainerStyle = {
    display: 'grid',
    gridTemplateColumns: '1fr 2fr 1fr', // Left: 1 part, Middle: 2 parts, Right: 1 part
    gap: '24px',
    height: '100vh',
    width: '100vw',
    padding: '24px',
    boxSizing: 'border-box',
    backgroundColor: '#0f172a', // slate-900
    overflow: 'hidden', // Strict full-screen, no page scrolling
  };

  const panelStyle = {
    backgroundColor: '#1e293b', // slate-800
    borderRadius: '12px',
    padding: '20px',
    border: '1px solid #334155', // slate-700
    display: 'flex',
    flexDirection: 'column',
    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.5)',
    overflow: 'hidden',
  };

  const headerStyle = {
    color: '#38bdf8', // sky-400
    textTransform: 'uppercase',
    letterSpacing: '1px',
    borderBottom: '2px solid #334155',
    paddingBottom: '10px',
    marginBottom: '15px',
    marginTop: '0',
  };

  // --- RENDER STATES ---

  if (loading) {
    return <div style={{ color: '#4ade80', padding: '20px', textAlign: 'center', marginTop: '20vh', fontSize: '1.5rem', backgroundColor: '#0f172a', height: '100vh' }}>Booting Simulation Engine...</div>;
  }

  if (error) {
    return <div style={{ color: '#ef4444', padding: '20px', textAlign: 'center', marginTop: '20vh', backgroundColor: '#0f172a', height: '100vh' }}>[ERROR]: {error}</div>;
  }

  return (
    <div style={gridContainerStyle}>
      
      {/* COLUMN 1 (LEFT): Tribe Overview & Priorities */}
      <div style={panelStyle}>
        <h2 style={headerStyle}>Tribe Overview</h2>
        
        <div style={{ marginBottom: '24px' }}>
          <p style={{ margin: '8px 0' }}><strong>Species:</strong> <span style={{ color: '#94a3b8' }}>{tribe?.members?.[0]?.species || 'UNKNOWN'}</span></p>
          <p style={{ margin: '8px 0' }}><strong>Population:</strong> <span style={{ color: '#4ade80', fontSize: '1.2rem' }}>{tribe?.members?.length || 0}</span></p>
          <p style={{ margin: '8px 0' }}><strong>Current Season:</strong> <span style={{ color: tribe?.currentSeason === 'DRY' ? '#fbbf24' : '#38bdf8' }}>{tribe?.currentSeason || 'UNKNOWN'}</span></p>
        </div>

        <h3 style={{ ...headerStyle, fontSize: '1rem', marginTop: '10px' }}>Active Priorities</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {tribe?.priorities && Object.entries(tribe.priorities).map(([key, value]) => (
            <div key={key} style={{ display: 'flex', justifyContent: 'space-between', background: '#0f172a', padding: '12px', borderRadius: '6px', border: '1px solid #1e293b' }}>
              <span style={{ fontWeight: 'bold' }}>{key}</span>
              <span style={{ color: '#4ade80' }}>{(value * 100).toFixed(0)}%</span>
            </div>
          ))}
        </div>
      </div>

      {/* COLUMN 2 (MIDDLE): Hominid Roster & LiveLog */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '24px', overflow: 'hidden' }}>
        
        {/* Roster Table Container */}
        <div style={{ ...panelStyle, flex: 1, padding: 0 }}>
          <div style={{ padding: '20px 20px 0 20px' }}>
            <h2 style={headerStyle}>Hominid Roster</h2>
          </div>
          
          <div style={{ overflowY: 'auto', padding: '0 20px 20px 20px', flex: 1 }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.9rem' }}>
              <thead style={{ position: 'sticky', top: '0', backgroundColor: '#1e293b', zIndex: 1 }}>
                <tr style={{ borderBottom: '1px solid #334155', color: '#94a3b8' }}>
                  <th style={{ padding: '12px 10px' }}>ID</th>
                  <th style={{ padding: '12px 10px' }}>Gender</th>
                  <th style={{ padding: '12px 10px' }}>Age (Mo)</th>
                  <th style={{ padding: '12px 10px' }}>Health</th>
                  <th style={{ padding: '12px 10px' }}>Satiety</th>
                  <th style={{ padding: '12px 10px' }}>Status</th>
                </tr>
              </thead>
              <tbody>
                {tribe?.members?.map((hominid) => (
                  <tr key={hominid.id} style={{ borderBottom: '1px solid #334155', backgroundColor: hominid.isAlpha ? 'rgba(234, 179, 8, 0.1)' : 'transparent' }}>
                    <td style={{ padding: '10px', fontFamily: 'monospace' }}>{hominid.id.substring(0, 8)}</td>
                    <td style={{ padding: '10px', color: hominid.gender === 'MALE' ? '#60a5fa' : '#f472b6', fontWeight: 'bold' }}>{hominid.gender}</td>
                    <td style={{ padding: '10px' }}>{hominid.ageInMonths}</td>
                    <td style={{ padding: '10px', color: hominid.health < 50 ? '#ef4444' : '#4ade80' }}>{hominid.health.toFixed(1)}</td>
                    <td style={{ padding: '10px', color: hominid.satiety < 50 ? '#ef4444' : '#4ade80' }}>{hominid.satiety.toFixed(1)}</td>
                    <td style={{ padding: '10px' }}>
                      {hominid.isAlpha && <span style={{ backgroundColor: '#eab308', color: '#000', padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold' }}>ALPHA</span>}
                      {hominid.isBlockedMother && <span style={{ backgroundColor: '#c084fc', color: '#000', padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold', marginLeft: hominid.isAlpha ? '6px' : '0' }}>MOTHER</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        
        {/* Live Event Log */}
        <LiveLog />
      </div>

      {/* COLUMN 3 (RIGHT): Controls */}
      <div style={{ ...panelStyle, justifyContent: 'center', alignItems: 'center', backgroundColor: '#0f172a' }}>
        <button 
          onClick={handleTick}
          disabled={isTicking}
          style={{
            backgroundColor: 'transparent',
            border: '2px solid #38bdf8',
            color: '#38bdf8',
            padding: '24px 40px',
            fontSize: '1.25rem',
            fontWeight: 'bold',
            borderRadius: '8px',
            cursor: isTicking ? 'not-allowed' : 'pointer',
            textTransform: 'uppercase',
            letterSpacing: '2px',
            boxShadow: '0 0 15px rgba(56, 189, 248, 0.4), inset 0 0 10px rgba(56, 189, 248, 0.2)',
            transition: 'all 0.2s ease',
            opacity: isTicking ? 0.5 : 1,
            width: '100%',
            maxWidth: '300px'
          }}
          onMouseOver={(e) => {
            if(!isTicking) {
              e.currentTarget.style.backgroundColor = 'rgba(56, 189, 248, 0.1)';
              e.currentTarget.style.boxShadow = '0 0 25px rgba(56, 189, 248, 0.6), inset 0 0 15px rgba(56, 189, 248, 0.4)';
            }
          }}
          onMouseOut={(e) => {
            if(!isTicking) {
              e.currentTarget.style.backgroundColor = 'transparent';
              e.currentTarget.style.boxShadow = '0 0 15px rgba(56, 189, 248, 0.4), inset 0 0 10px rgba(56, 189, 248, 0.2)';
            }
          }}
        >
          {isTicking ? 'Processing...' : 'Next Tick (1 Mo)'}
        </button>
      </div>

    </div>
  );
};

export default GameDashboard;