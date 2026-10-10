import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';

interface BackNavigationProps {
    to: string;
    label: string;
}

const BackNavigation: React.FC<BackNavigationProps> = ({ to, label }) => {
    const navigate = useNavigate();
    const location = useLocation();
    
    const handleBack = () => {
        // If we came from within the app (location.key !== 'default'), navigate back to restore state
        if (location.key !== 'default' && window.history.length > 2) {
            navigate(-1);
        } else {
            navigate(to);
        }
    };
    
    return (
        <button 
            onClick={handleBack}
            className="group flex items-center gap-2 px-3 py-1.5 bg-[#1c1c1c] hover:bg-[#2a2a2a] border border-[#2e2e2e] hover:border-[#4e4e4e] rounded-lg text-neutral-300 hover:text-white text-sm font-medium transition-all shadow-sm mb-4"
        >
            <ArrowLeft className="w-4 h-4 text-neutral-500 group-hover:text-neutral-300 transition-colors" />
            <span>{label}</span>
        </button>
    );
};

export default BackNavigation;
