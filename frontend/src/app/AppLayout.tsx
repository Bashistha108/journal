import React, { useEffect, useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { tableApi, type TableDetail } from '../api/tables';
import { routes } from './navigation';

export const AppLayout: React.FC = () => {
    const [tables, setTables] = useState<TableDetail[]>([]);

    useEffect(() => {
        tableApi.listTables().then(setTables).catch(console.error);
    }, []);

    return (
        <div className="flex min-h-screen bg-slate-950 text-slate-200">
            {/* Sidebar */}
            <aside className="w-64 border-r border-slate-800 bg-slate-900 flex flex-col hidden md:flex">
                <div className="p-4 border-b border-slate-800">
                    <NavLink to={routes.tables()} className="text-xl font-bold text-indigo-400 hover:text-indigo-300">
                        Journal
                    </NavLink>
                </div>
                <nav className="flex-1 overflow-y-auto p-4 space-y-2">
                    <div className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-3">
                        Tables
                    </div>
                    {tables.map(table => (
                        <NavLink
                            key={table.id}
                            to={routes.tableData(table.id)}
                            className={({ isActive }) =>
                                `block px-3 py-2 rounded-md transition-colors ${
                                    isActive 
                                        ? 'bg-indigo-600 text-white' 
                                        : 'text-slate-300 hover:bg-slate-800 hover:text-white'
                                }`
                            }
                        >
                            {table.displayName}
                        </NavLink>
                    ))}
                </nav>
            </aside>

            {/* Main Content */}
            <main className="flex-1 overflow-y-auto relative">
                <Outlet />
            </main>
        </div>
    );
};
