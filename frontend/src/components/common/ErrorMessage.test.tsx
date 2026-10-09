import React from 'react';
import { render, screen } from '@testing-library/react';
import { ErrorMessage } from './ErrorMessage';
import { ApiError } from '../../types/api';

describe('ErrorMessage', () => {
    it('renders nothing when error is null', () => {
        const { container } = render(<ErrorMessage error={null} />);
        expect(container.firstChild).toBeNull();
    });

    it('renders error message and code', () => {
        const error: ApiError = {
            status: 400,
            code: 'BAD_REQUEST',
            message: 'Invalid input',
            requestId: '123'
        };
        render(<ErrorMessage error={error} />);
        expect(screen.getByText(/Invalid input/i)).toBeInTheDocument();
        expect(screen.getByText(/BAD_REQUEST/i)).toBeInTheDocument();
    });

    it('renders field errors', () => {
        const error: ApiError = {
            status: 422,
            code: 'VALIDATION_FAILED',
            message: 'Validation failed',
            requestId: '123',
            fieldErrors: [
                { field: 'name', code: 'REQUIRED', message: 'Name is required' }
            ]
        };
        render(<ErrorMessage error={error} />);
        expect(screen.getByText(/Name is required/i)).toBeInTheDocument();
        expect(screen.getByText(/name:/i)).toBeInTheDocument();
    });
});
