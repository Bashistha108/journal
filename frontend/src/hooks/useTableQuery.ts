import { useState, useCallback, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { rowApi } from '../api/rowApi';
import type { QueryRequest, PageResponse, FilterRequest, SortRequest } from '../types/query';
import type { RowListItem } from '../types/row';

export function useTableQuery(tableId: number) {
    const [searchParams, setSearchParams] = useSearchParams();
    
    // Parse query from URL or defaults
    const [query, setQuery] = useState<QueryRequest>(() => {
        const page = parseInt(searchParams.get('page') || '1', 10);
        const sortStr = searchParams.get('sort');
        const filtersStr = searchParams.get('filters');
        
        let sort: SortRequest | undefined;
        if (sortStr) {
            try { sort = JSON.parse(sortStr); } catch (e) {}
        }
        
        let filters: FilterRequest[] | undefined;
        if (filtersStr) {
            try { filters = JSON.parse(filtersStr); } catch (e) {}
        }
        
        return { page, sort, filters };
    });

    const [data, setData] = useState<PageResponse<RowListItem> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // Sync query changes to URL
    useEffect(() => {
        const params = new URLSearchParams();
        if (query.page > 1) params.set('page', query.page.toString());
        if (query.sort) params.set('sort', JSON.stringify(query.sort));
        if (query.filters && query.filters.length > 0) params.set('filters', JSON.stringify(query.filters));
        
        setSearchParams(params, { replace: true });
    }, [query, setSearchParams]);

    const executeQuery = useCallback(async () => {
        if (!tableId) return;
        setLoading(true);
        setError(null);
        try {
            const result = await rowApi.queryRows(tableId, query);
            setData(result);
        } catch (e: any) {
            setError(e.message || 'Failed to fetch rows');
        } finally {
            setLoading(false);
        }
    }, [tableId, query]);

    useEffect(() => {
        executeQuery();
    }, [executeQuery]);

    const setPage = (page: number) => setQuery(prev => ({ ...prev, page }));
    const setSort = (sort?: SortRequest) => setQuery(prev => ({ ...prev, sort, page: 1 }));
    const setFilters = (filters?: FilterRequest[]) => setQuery(prev => ({ ...prev, filters, page: 1 }));

    return {
        query,
        data,
        loading,
        error,
        setPage,
        setSort,
        setFilters,
        refresh: executeQuery
    };
}
