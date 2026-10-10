import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { routes } from './navigation';
import { Database, Home, Table2 } from 'lucide-react';

export const AppLayout: React.FC = () => {
    const location = useLocation();

    const NavItem = ({ to, icon: Icon, label, isActive }: { to: string, icon: any, label: string, isActive?: boolean }) => {
        const active = isActive !== undefined ? isActive : location.pathname === to || location.pathname.startsWith(to + '/');
        return (
            <NavLink
                to={to}
                className={`flex items-center gap-3 px-3 py-2 text-[13px] font-medium rounded-md transition-colors ${
                    active 
                        ? 'bg-[#2a2a2a] text-white' 
                        : 'text-neutral-400 hover:bg-[#2a2a2a]/50 hover:text-white'
                }`}
            >
                <Icon className="w-4 h-4" />
                {label}
            </NavLink>
        );
    };

    return (
        <div className="flex h-screen bg-[#121212] text-neutral-200 overflow-hidden font-sans">
            {/* Sidebar */}
            <aside className="w-64 flex-shrink-0 flex flex-col bg-[#1c1c1c] border-r border-[#2e2e2e] relative z-20">
                <div className="p-5 pb-4">
                    <div className="flex items-center gap-3 font-semibold text-yellow-400">
                        <Database className="w-5 h-5" />
                        <span className="text-[15px] tracking-wide">Database</span>
                    </div>
                </div>
                
                <nav className="flex-1 overflow-y-auto px-3 py-2 space-y-6">
                    <div className="space-y-1">
                        <NavItem to="/" icon={Home} label="Home" isActive={location.pathname === '/'} />
                    </div>

                    <div className="space-y-1">
                        <div className="text-[10px] font-bold text-neutral-500 uppercase tracking-wider mb-2 px-3">
                            Schema
                        </div>
                        <NavItem to={routes.tables()} icon={Table2} label="Tables" />
                    </div>
                </nav>
            </aside>

            {/* Main Content */}
            <main className="flex-1 flex flex-col min-w-0 min-h-0 overflow-hidden bg-[#121212]">
                <Outlet />
            </main>
        </div>
    );
};
