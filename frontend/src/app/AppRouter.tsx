import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AppLayout } from './AppLayout';
import TableManagementPage from '../pages/TableManagementPage';
import TableDataPage from '../pages/TableDataPage';
import RowDetailsPage from '../pages/RowDetailsPage';

export const AppRouter: React.FC = () => {
    return (
        <Routes>
            <Route element={<AppLayout />}>
                <Route path="/" element={<Navigate to="/tables" replace />} />
                <Route path="/tables" element={<TableManagementPage />} />
                <Route path="/tables/:tableId" element={<TableDataPage />} />
                <Route path="/tables/:tableId/rows/:rowId" element={<RowDetailsPage />} />
                <Route path="*" element={<Navigate to="/tables" replace />} />
            </Route>
        </Routes>
    );
};
