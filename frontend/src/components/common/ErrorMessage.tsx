import React from 'react';
import type { ApiError } from '../../types/api';

interface ErrorMessageProps {
    error: ApiError | null;
}

export const ErrorMessage: React.FC<ErrorMessageProps> = ({ error }) => {
    if (!error) return null;

    return (
        <div className="error-message" role="alert" style={{ color: 'red', border: '1px solid red', padding: '10px' }}>
            <p><strong>Error:</strong> {error.message}</p>
            {error.code && <p>Code: {error.code}</p>}
            {error.fieldErrors && error.fieldErrors.length > 0 && (
                <ul>
                    {error.fieldErrors.map((fe, idx) => (
                        <li key={idx}>{fe.field}: {fe.message}</li>
                    ))}
                </ul>
            )}
        </div>
    );
};
