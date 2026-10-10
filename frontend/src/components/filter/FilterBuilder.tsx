import React from 'react';
import type { FilterRequest, FilterOperator } from '../../types/query';
import type { ColumnDetail } from '../../types/table';

interface Props {
    columns: ColumnDetail[];
    filters: FilterRequest[];
    onChange: (filters: FilterRequest[]) => void;
}

export const FilterBuilder: React.FC<Props> = ({ columns, filters, onChange }) => {
    const addFilter = () => {
        if (columns.length === 0) return;
        if (filters.length >= 20) return; // limit
        const col = columns[0];
        onChange([...filters, { columnId: col.id, operator: getDefaultOperator(col.dataType) }]);
    };

    const updateFilter = (index: number, update: Partial<FilterRequest>) => {
        const newFilters = [...filters];
        newFilters[index] = { ...newFilters[index], ...update };
        onChange(newFilters);
    };

    const removeFilter = (index: number) => {
        const newFilters = [...filters];
        newFilters.splice(index, 1);
        onChange(newFilters);
    };

    return (
        <div className="flex flex-col gap-3 p-6 bg-transparent">
            <div className="flex justify-between items-center">
                <h3 className="text-sm font-semibold text-neutral-300">Filters</h3>
                <button
                    className="text-xs bg-white/10 hover:bg-white/20 text-white font-medium px-3 py-1.5 rounded-lg transition-colors disabled:opacity-50 border border-white/5"
                    disabled={filters.length >= 20 || columns.length === 0}
                    onClick={addFilter}
                >
                    + Add Filter
                </button>
            </div>
            {filters.length === 0 ? (
                <p className="text-sm text-neutral-500 italic mt-1">No filters applied.</p>
            ) : (
                <div className="flex flex-col gap-3 mt-3">
                    {filters.map((f, i) => (
                        <FilterRow
                            key={i}
                            filter={f}
                            columns={columns}
                            onChange={(update) => updateFilter(i, update)}
                            onRemove={() => removeFilter(i)}
                        />
                    ))}
                </div>
            )}
        </div>
    );
};

interface RowProps {
    filter: FilterRequest;
    columns: ColumnDetail[];
    onChange: (update: Partial<FilterRequest>) => void;
    onRemove: () => void;
}

const FilterRow: React.FC<RowProps> = ({ filter, columns, onChange, onRemove }) => {
    const column = columns.find(c => c.id === filter.columnId);
    if (!column) return null;

    const ops = getOperatorsForType(column.dataType);

    return (
        <div className="flex items-center gap-3 text-sm flex-wrap sm:flex-nowrap">
            <select
                className="input-field py-2 text-sm !w-auto bg-black"
                value={filter.columnId}
                onChange={e => {
                    const newColId = Number(e.target.value);
                    const newCol = columns.find(c => c.id === newColId);
                    if (newCol) {
                        onChange({ columnId: newColId, operator: getDefaultOperator(newCol.dataType), value: '', from: '', to: '', values: [] });
                    }
                }}
            >
                {columns.map(c => <option key={c.id} value={c.id}>{c.displayName}</option>)}
            </select>

            <select
                className="input-field py-2 text-sm !w-auto bg-black"
                value={filter.operator}
                onChange={e => onChange({ operator: e.target.value as FilterOperator })}
            >
                {ops.map(op => <option key={op} value={op}>{op}</option>)}
            </select>

            {needsValue(filter.operator) && (
                <input
                    type="text"
                    className="input-field py-2 text-sm flex-1 min-w-[150px]"
                    value={filter.value || ''}
                    onChange={e => onChange({ value: e.target.value })}
                    placeholder="Value..."
                />
            )}

            {filter.operator === 'BETWEEN' && (
                <>
                    <input
                        type="text"
                        className="input-field py-2 text-sm flex-1 min-w-0 w-24"
                        value={filter.from || ''}
                        onChange={e => onChange({ from: e.target.value })}
                        placeholder="From..."
                    />
                    <span className="text-neutral-500 font-medium">and</span>
                    <input
                        type="text"
                        className="input-field py-2 text-sm flex-1 min-w-0 w-24"
                        value={filter.to || ''}
                        onChange={e => onChange({ to: e.target.value })}
                        placeholder="To..."
                    />
                </>
            )}

            <button
                className="w-9 h-9 flex items-center justify-center text-neutral-500 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors ml-auto sm:ml-0"
                onClick={onRemove}
                title="Remove filter"
            >
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18"/><path d="m6 6 12 12"/></svg>
            </button>
        </div>
    );
};

function getOperatorsForType(type: string): FilterOperator[] {
    switch (type) {
        case 'TEXT':
        case 'LINK': return ['CONTAINS', 'EQUALS', 'IS_EMPTY', 'IS_NOT_EMPTY'];
        case 'INTEGER':
        case 'DECIMAL': return ['EQUALS', 'GREATER_THAN', 'LESS_THAN', 'BETWEEN', 'IS_EMPTY', 'IS_NOT_EMPTY'];
        case 'DATE':
        case 'DATETIME': return ['ON', 'BEFORE', 'AFTER', 'BETWEEN', 'IS_EMPTY', 'IS_NOT_EMPTY'];
        case 'BOOLEAN': return ['IS_TRUE', 'IS_FALSE', 'IS_EMPTY', 'IS_NOT_EMPTY'];
        case 'SELECT': return ['EQUALS', 'MATCHES_ANY', 'IS_EMPTY', 'IS_NOT_EMPTY'];
        case 'IMAGE': return ['HAS_ATTACHMENTS', 'HAS_NO_ATTACHMENTS'];
        default: return ['EQUALS'];
    }
}

function getDefaultOperator(type: string): FilterOperator {
    return getOperatorsForType(type)[0];
}

function needsValue(op: FilterOperator): boolean {
    return !['IS_EMPTY', 'IS_NOT_EMPTY', 'BETWEEN', 'IS_TRUE', 'IS_FALSE', 'MATCHES_ANY', 'HAS_ATTACHMENTS', 'HAS_NO_ATTACHMENTS'].includes(op);
}
