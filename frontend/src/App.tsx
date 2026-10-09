import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import TableManagementPage from './pages/TableManagementPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/tables" replace />} />
        <Route path="/tables" element={<TableManagementPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
