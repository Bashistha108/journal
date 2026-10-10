import React from 'react';
import type { ColumnDetail } from '../../types/table';
import { InputMask } from '@react-input/mask';

interface FieldInputProps {
    column: ColumnDetail;
    value: any;
    onChange: (value: any) => void;
    error?: string;
}

const FieldInput: React.FC<FieldInputProps> = ({ column, value, onChange, error }) => {
    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
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
                return (
                    <textarea
                        rows={3}
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 bg-[#121212] border ${error ? 'border-red-500/50 focus:border-red-500' : 'border-[#2e2e2e] focus:border-[#4e4e4e]'} rounded-md text-neutral-200 text-sm focus:outline-none transition-colors resize-y`}
                    />
                );
            case 'LINK':
                return (
                    <input
                        type="text"
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 bg-[#121212] border ${error ? 'border-red-500/50 focus:border-red-500' : 'border-[#2e2e2e] focus:border-[#4e4e4e]'} rounded-md text-neutral-200 text-sm focus:outline-none transition-colors`}
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
                        className={`w-full px-3 py-2 bg-[#121212] border ${error ? 'border-red-500/50 focus:border-red-500' : 'border-[#2e2e2e] focus:border-[#4e4e4e]'} rounded-md text-neutral-200 text-sm focus:outline-none transition-colors`}
                    />
                );
            case 'DATE':
                return (
                    <InputMask
                        mask="DD.MM.YYYY"
                        replacement={{ D: /\d/, M: /\d/, Y: /\d/ }}
                        showMask
                        placeholder="DD.MM.YYYY"
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 bg-[#121212] border ${error ? 'border-red-500/50 focus:border-red-500' : 'border-[#2e2e2e] focus:border-[#4e4e4e]'} rounded-md text-neutral-200 text-sm focus:outline-none transition-colors`}
                    />
                );
            case 'DATETIME':
                return (
                    <InputMask
                        mask="DD.MM.YYYY HH:MM:00"
                        replacement={{ D: /\d/, M: /\d/, Y: /\d/, H: /\d/ }}
                        showMask
                        placeholder="DD.MM.YYYY HH:MM:00"
                        value={value ?? ''}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 bg-[#121212] border ${error ? 'border-red-500/50 focus:border-red-500' : 'border-[#2e2e2e] focus:border-[#4e4e4e]'} rounded-md text-neutral-200 text-sm focus:outline-none transition-colors`}
                    />
                );
            case 'BOOLEAN':
                return (
                    <select
                        value={value === true ? 'true' : value === false ? 'false' : 'true'}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 bg-[#121212] border ${error ? 'border-red-500/50 focus:border-red-500' : 'border-[#2e2e2e] focus:border-[#4e4e4e]'} rounded-md text-neutral-200 text-sm focus:outline-none transition-colors`}
                    >
                        <option value="true">Yes</option>
                        <option value="false">No</option>
                    </select>
                );
            case 'SELECT':
                return (
                    <select
                        value={value ?? (column.selectOptions?.[0]?.value ?? '')}
                        onChange={handleChange}
                        className={`w-full px-3 py-2 bg-[#121212] border ${error ? 'border-red-500/50 focus:border-red-500' : 'border-[#2e2e2e] focus:border-[#4e4e4e]'} rounded-md text-neutral-200 text-sm focus:outline-none transition-colors`}
                    >
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
