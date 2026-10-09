import React from 'react';
import { Routes, Route } from 'react-router-dom';

function Home() {
  return (
    <div>
      <h1>Trading Journal</h1>
      <p>Frontend skeleton is running independently.</p>
    </div>
  );
}

function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
    </Routes>
  );
}

export default App;
