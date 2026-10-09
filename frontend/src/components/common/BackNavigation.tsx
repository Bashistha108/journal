import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

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
            className="text-indigo-400 hover:text-indigo-300 text-sm mb-4 inline-block font-medium transition-colors"
        >
            &larr; {label}
        </button>
    );
};

export default BackNavigation;
