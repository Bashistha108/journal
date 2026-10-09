import { ApiError } from '../types/api';

const API_BASE = '/api/v1';

export async function fetchClient(endpoint: string, options: RequestInit = {}): Promise<Response> {
    const headers = new Headers(options.headers || {});
    
    if (options.method && options.method.toUpperCase() !== 'GET') {
        headers.set('X-TJ-Client', 'web');
    }
    
    if (options.body && !headers.has('Content-Type')) {
        headers.set('Content-Type', 'application/json');
    }

    const config: RequestInit = {
        ...options,
        headers,
    };

    let response: Response;
    try {
        response = await fetch(`${API_BASE}${endpoint}`, config);
    } catch (error) {
        throw {
            status: 503,
            code: 'NETWORK_FAILURE',
            message: 'A network error occurred. Please try again.',
            requestId: 'local'
        } as ApiError;
    }

    if (!response.ok) {
        let apiError: ApiError;
        try {
            apiError = await response.json();
            if (response.status === 503) {
                apiError.code = 'RETRYABLE_ERROR';
            }
        } catch (e) {
            apiError = {
                status: response.status,
                code: 'INTERNAL_ERROR',
                message: 'An unexpected error occurred.',
                requestId: 'unknown'
            };
        }
        throw apiError;
    }

    return response;
}
