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

  // Handle single TICK request
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

  // Handle MULTIPLE TICKS request
  const handleMultipleTicks = async (count) => {
    if (isTicking) return;
    setIsTicking(true);
    try {
      const response = await fetch(`${API_BASE_URL}/tick/${count}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (!response.ok) throw new Error(`Failed to execute ${count} ticks.`);
      const updatedTribe = await response.json();
      setTribe(updatedTribe);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsTicking(false);
    }
  };

  // Handle RESET request
  const handleReset = async () => {
    if (isTicking) return;
    // Optional: Add a simple confirmation dialog so users don't misclick
    if (!window.confirm("Are you sure you want to completely reset the simulation?")) return;
    
    setIsTicking(true);
    try {
      const response = await fetch(`${API_BASE_URL}/reset`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
      });
      if (!response.ok) throw new Error('Failed to reset simulation.');
      const freshlyResetTribe = await response.json();
      setTribe(freshlyResetTribe);
    } catch (err) {
      setError(err.message);
    } finally {
      setIsTicking(false);
    }
  };

  // --- STYLES ---
  
  const gridContainerStyle = {
    display: 'grid',
    gridTemplateColumns: '1fr 2fr 1fr',
    gap: '24px',
    height: '100vh',
    width: '100vw',
    padding: '24px',
    boxSizing: 'border-box',
    backgroundColor: '#0f172a',
    overflow: 'hidden',
  };

  const panelStyle = {
    backgroundColor: '#1e293b',
    borderRadius: '12px',
    padding: '20px',
    border: '1px solid #334155',
    display: 'flex',
    flexDirection: 'column',
    boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.5)',
    overflow: 'hidden',
  };

  const headerStyle = {
    color: '#38bdf8',
    textTransform: 'uppercase',
    letterSpacing: '1px',
    borderBottom: '2px solid #334155',
    paddingBottom: '10px',
    marginBottom: '15px',
    marginTop: '0',
  };

  // Helper for consistent button styling
  const getButtonStyle = (colorHex, isSubdued = false) => ({
    backgroundColor: 'transparent',
    border: `2px solid ${colorHex}`,
    color: colorHex,
    padding: '16px 20px',
    fontSize: isSubdued ? '1rem' : '1.1rem',
    fontWeight: 'bold',
    borderRadius: '8px',
    cursor: isTicking ? 'not-allowed' : 'pointer',
    textTransform: 'uppercase',
    letterSpacing: '1px',
    boxShadow: `0 0 15px ${colorHex}66, inset 0 0 10px ${colorHex}33`,
    transition: 'all 0.2s ease',
    opacity: isTicking ? 0.5 : 1,
    width: '100%',
    maxWidth: '300px'
  });

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
                  <tr key={hominid.id} style={{ borderBottom: '1px solid #334155', backgroundColor: hominid.alpha ? 'rgba(234, 179, 8, 0.1)' : 'transparent' }}>
                    <td style={{ padding: '10px', fontFamily: 'monospace' }}>{hominid.id.substring(0, 8)}</td>
                    <td style={{ padding: '10px', color: hominid.gender === 'MALE' ? '#60a5fa' : '#f472b6', fontWeight: 'bold' }}>{hominid.gender}</td>
                    <td style={{ padding: '10px' }}>{hominid.ageInMonths}</td>
                    <td style={{ padding: '10px', color: hominid.health < 50 ? '#ef4444' : '#4ade80' }}>{hominid.health.toFixed(1)}</td>
                    <td style={{ padding: '10px', color: hominid.satiety < 50 ? '#ef4444' : '#4ade80' }}>{hominid.satiety.toFixed(1)}</td>
                    <td style={{ padding: '10px' }}>
                      {/* Fixed Jackson Serialization mappings: alpha, pregnant, blockedMother */}
                      {hominid.alpha && <span style={{ backgroundColor: '#eab308', color: '#000', padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold', marginRight: '6px' }}>ALPHA</span>}
                      {hominid.pregnant && <span style={{ backgroundColor: '#f472b6', color: '#000', padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold', marginRight: '6px' }}>PREGNANT</span>}
                      {hominid.blockedMother && <span style={{ backgroundColor: '#c084fc', color: '#000', padding: '4px 8px', borderRadius: '4px', fontSize: '0.75rem', fontWeight: 'bold' }}>MOTHER</span>}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
        
        <LiveLog />
      </div>

      {/* COLUMN 3 (RIGHT): Controls */}
      <div style={{ ...panelStyle, justifyContent: 'center', alignItems: 'center', backgroundColor: '#0f172a', gap: '20px' }}>
        
        {/* Button 1: Single Tick (Blue) */}
        <button 
          onClick={handleTick}
          disabled={isTicking}
          style={getButtonStyle('#38bdf8')}
          onMouseOver={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'rgba(56, 189, 248, 0.1)')}
          onMouseOut={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'transparent')}
        >
          {isTicking ? 'Processing...' : 'Next Tick (1 Mo)'}
        </button>

        {/* Button 2: 10 Ticks (Emerald) */}
        <button 
          onClick={() => handleMultipleTicks(10)}
          disabled={isTicking}
          style={getButtonStyle('#10b981')}
          onMouseOver={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'rgba(16, 185, 129, 0.1)')}
          onMouseOut={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'transparent')}
        >
          {isTicking ? 'Processing...' : 'Advance 10 Ticks'}
        </button>

        {/* Button 3: 100 Ticks (Green) */}
        <button 
          onClick={() => handleMultipleTicks(100)}
          disabled={isTicking}
          style={getButtonStyle('#4ade80')}
          onMouseOver={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'rgba(74, 222, 128, 0.1)')}
          onMouseOut={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'transparent')}
        >
          {isTicking ? 'Processing...' : 'Super Fast 100 Ticks'}
        </button>

        {/* Spacer to push Reset button slightly apart from the progression buttons */}
        <div style={{ margin: '10px 0', borderBottom: '1px solid #334155', width: '80%' }}></div>

        {/* Button 4: Reset Simulation (Red) */}
        <button 
          onClick={handleReset}
          disabled={isTicking}
          style={getButtonStyle('#ef4444', true)}
          onMouseOver={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'rgba(239, 68, 68, 0.1)')}
          onMouseOut={(e) => !isTicking && (e.currentTarget.style.backgroundColor = 'transparent')}
        >
          {isTicking ? 'Processing...' : 'Reset Simulation'}
        </button>

      </div>
    </div>
  );
};

export default GameDashboard;