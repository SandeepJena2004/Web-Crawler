import React, { useState, useRef, useEffect } from 'react';
import './App.css';

function App() {
  const [url, setUrl] = useState('');
  const [maxPages, setMaxPages] = useState(50);
  const [numThreads, setNumThreads] = useState(4);
  const [maxDepth, setMaxDepth] = useState(2);
  const [status, setStatus] = useState('Idle');
  const [results, setResults] = useState([]); // Array to store crawled URLs or data

  const pollingRef = useRef();

  const startCrawl = async () => {
    if (!url) {
      setStatus('Please enter a valid URL.');
      return;
    }

    setStatus('Starting crawl...');
    setResults([]);  // Clear previous results if any

    try {
      await fetch('http://localhost:8080/api/crawl/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ url, maxPages, numThreads, maxDepth }),
      });
      pollStatus();
    } catch (error) {
      setStatus('Error starting crawl: ' + error.message);
    }
  };

  // Poll backend for crawl status and results
  const pollStatus = () => {
    if (pollingRef.current) clearInterval(pollingRef.current);

    pollingRef.current = setInterval(async () => {
      try {
        const response = await fetch('http://localhost:8080/api/crawl/status');
        const data = await response.json();

        setStatus(data.status);
        if (data.results) setResults(data.results);

        // Stop polling when crawl completes or idle
        if (data.status === 'Completed' || data.status === 'Idle') {
          clearInterval(pollingRef.current);
        }
      } catch (error) {
        setStatus('Error fetching status: ' + error.message);
        clearInterval(pollingRef.current);
      }
    }, 2000);
  };

  // Clean up polling on component unmount
  useEffect(() => {
    return () => clearInterval(pollingRef.current);
  }, []);

  return (
    <div className="App" style={{ padding: 20 }}>
      <h2>Web Crawler Control Panel</h2>

      {/* Input fields */}
      <label>Start URL:</label><br />
      <input
        type="text"
        value={url}
        onChange={e => setUrl(e.target.value)}
        placeholder="https://example.com"
        style={{ width: '300px' }}
      /><br /><br />

      <label>Max Pages:</label><br />
      <input
        type="number"
        value={maxPages}
        onChange={e => setMaxPages(Number(e.target.value))}
        min="1"
        style={{ width: '100px' }}
      /><br /><br />

      <label>Number of Threads:</label><br />
      <input
        type="number"
        value={numThreads}
        onChange={e => setNumThreads(Number(e.target.value))}
        min="1"
        style={{ width: '100px' }}
      /><br /><br />

      <label>Max Depth:</label><br />
      <input
        type="number"
        value={maxDepth}
        onChange={e => setMaxDepth(Number(e.target.value))}
        min="0"
        style={{ width: '100px' }}
      /><br /><br />

      <button onClick={startCrawl} style={{ padding: '10px 20px', cursor: 'pointer' }}>
        Start Crawl
      </button>

      {/* Status display */}
      <div style={{ marginTop: '20px', whiteSpace: 'pre-wrap' }}>
        <strong>Status:</strong><br />{status}
      </div>

      {/* Results display */}
      {results.length > 0 && (
        <div style={{ marginTop: 20 }}>
          <strong>Crawled URLs:</strong>
          <ul>
            {results.map((url, idx) => (
              <li key={idx}><a href={url} target="_blank" rel="noreferrer">{url}</a></li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}

export default App;
