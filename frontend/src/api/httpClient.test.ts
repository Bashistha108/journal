import { fetchClient } from './httpClient';
import { ApiError } from '../types/api';

describe('fetchClient', () => {
    const originalFetch = global.fetch;

    afterEach(() => {
        global.fetch = originalFetch;
    });

    it('adds X-TJ-Client header to non-GET requests', async () => {
        const mockFetch = jest.fn().mockResolvedValue(new Response(JSON.stringify({}), { status: 200 }));
        global.fetch = mockFetch;

        await fetchClient('/test', { method: 'POST' });
        
        const callArgs = mockFetch.mock.calls[0][1] as RequestInit;
        const headers = new Headers(callArgs.headers);
        expect(headers.get('X-TJ-Client')).toBe('web');
    });

    it('throws retryable error on network failure', async () => {
        const mockFetch = jest.fn().mockRejectedValue(new TypeError('Failed to fetch'));
        global.fetch = mockFetch;

        try {
            await fetchClient('/test');
            fail('Expected error');
        } catch (e: any) {
            expect(e.status).toBe(503);
            expect(e.code).toBe('NETWORK_FAILURE');
        }
    });

    it('parses ApiError on non-200 responses', async () => {
        const mockError: ApiError = { status: 400, code: 'BAD_REQUEST', message: 'Bad', requestId: '1' };
        const mockFetch = jest.fn().mockResolvedValue(new Response(JSON.stringify(mockError), { status: 400 }));
        global.fetch = mockFetch;

        try {
            await fetchClient('/test');
            fail('Expected error');
        } catch (e: any) {
            expect(e.code).toBe('BAD_REQUEST');
            expect(e.message).toBe('Bad');
        }
    });

    it('turns 503 into RETRYABLE_ERROR', async () => {
        const mockError: ApiError = { status: 503, code: 'DATABASE_UNAVAILABLE', message: 'Unavailable', requestId: '1' };
        const mockFetch = jest.fn().mockResolvedValue(new Response(JSON.stringify(mockError), { status: 503 }));
        global.fetch = mockFetch;

        try {
            await fetchClient('/test');
            fail('Expected error');
        } catch (e: any) {
            expect(e.status).toBe(503);
            expect(e.code).toBe('RETRYABLE_ERROR');
        }
    });
});
