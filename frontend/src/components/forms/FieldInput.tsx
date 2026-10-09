import React from 'react';
import type { ColumnDetail } from '../../types/table';

interface FieldInputProps {
    column: ColumnDetail;
    value: any;
    onChange: (value: any) => void;
    error?: string;
}

const FieldInput: React.FC<FieldInputProps> = ({ column, value, onChange, error }) => {
    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const val = e.target.value;
        if (column.dataType === 'BOOLEAN') {
            onChange(val === '' ? null : val === 'true');
        } else {
            onChange(val === '' ? null : val);
        }
    };

    const renderInput = () => {
        switch (column.dataType) {
            case 'TEXT':
            case 'LINK':
                return (
                    <input
                        type="text"
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full p-2 bg-gray-900 border ${error ? 'border-red-500' : 'border-gray-700'} rounded text-white`}
                    />
                );
            case 'INTEGER':
            case 'DECIMAL':
                return (
                    <input
                        type="number"
                        step={column.dataType === 'INTEGER' ? '1' : 'any'}
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full p-2 bg-gray-900 border ${error ? 'border-red-500' : 'border-gray-700'} rounded text-white`}
                    />
                );
            case 'DATE':
                return (
                    <input
                        type="date"
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full p-2 bg-gray-900 border ${error ? 'border-red-500' : 'border-gray-700'} rounded text-white`}
                    />
                );
            case 'DATETIME':
                return (
                    <input
                        type="datetime-local"
                        step="1"
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full p-2 bg-gray-900 border ${error ? 'border-red-500' : 'border-gray-700'} rounded text-white`}
                    />
                );
            case 'BOOLEAN':
                return (
                    <select
                        value={value === true ? 'true' : value === false ? 'false' : ''}
                        onChange={handleChange}
                        className={`w-full p-2 bg-gray-900 border ${error ? 'border-red-500' : 'border-gray-700'} rounded text-white`}
                    >
                        <option value="">—</option>
                        <option value="true">Yes</option>
                        <option value="false">No</option>
                    </select>
                );
            case 'SELECT':
                return (
                    <select
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full p-2 bg-gray-900 border ${error ? 'border-red-500' : 'border-gray-700'} rounded text-white`}
                    >
                        <option value="">—</option>
                        {column.selectOptions?.map(opt => (
                            <option key={opt.id} value={opt.value}>{opt.value}</option>
                        ))}
                    </select>
                );
            case 'IMAGE':
                return <div className="text-gray-400 text-sm">Image attachments will be supported in Phase 6.</div>;
            default:
                return null;
        }
    };

    return (
        <div className="mb-4">
            <label className="block text-sm font-medium mb-1 text-gray-300">
                {column.displayName}
            </label>
            {renderInput()}
            {error && <div className="text-red-500 text-sm mt-1">{error}</div>}
        </div>
    );
};

export default FieldInput;
