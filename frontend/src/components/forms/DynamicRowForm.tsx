import React, { useState, useEffect } from 'react';
import type { ColumnDetail } from '../../types/table';
import type { RowDetail } from '../../types/row';
import FieldInput from './FieldInput';
import { validateField } from '../../validation/fieldValidation';
import type { ApiError } from '../../types/api';

interface DynamicRowFormProps {
    columns: ColumnDetail[];
    initialData?: RowDetail | null;
    onSubmit: (values: Record<number, any>, expectedVersion?: number) => Promise<void>;
    onCancel: () => void;
    apiError: ApiError | null;
}

const DynamicRowForm: React.FC<DynamicRowFormProps> = ({ columns, initialData, onSubmit, onCancel, apiError }) => {
    const [values, setValues] = useState<Record<number, any>>({});
    const [errors, setErrors] = useState<Record<number, string>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (initialData) {
            setValues(initialData.values || {});
        } else {
            setValues({});
        }
    }, [initialData]);

    useEffect(() => {
        if (apiError && apiError.fieldErrors) {
            const serverErrors: Record<number, string> = {};
            apiError.fieldErrors.forEach(fe => {
                const colIdStr = fe.field.replace('values[', '').replace(']', '');
                const colId = parseInt(colIdStr, 10);
                if (!isNaN(colId)) {
                    serverErrors[colId] = fe.message;
                }
            });
            setErrors(prev => ({ ...prev, ...serverErrors }));
        }
    }, [apiError]);

    const handleChange = (columnId: number, value: any) => {
        setValues(prev => ({ ...prev, [columnId]: value }));
        
        const column = columns.find(c => c.id === columnId);
        if (column) {
            const error = validateField(value, column);
            setErrors(prev => ({
                ...prev,
                [columnId]: error || ''
            }));
        }
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        
        const newErrors: Record<number, string> = {};
        let hasError = false;
        
        columns.forEach(col => {
            if (col.dataType !== 'IMAGE') {
                const error = validateField(values[col.id], col);
                if (error) {
                    newErrors[col.id] = error;
                    hasError = true;
                }
            }
        });
        
        setErrors(newErrors);
        
        if (!hasError) {
            setIsSubmitting(true);
            try {
                await onSubmit(values, initialData?.version);
            } finally {
                setIsSubmitting(false);
            }
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <div className="max-h-[60vh] overflow-y-auto pr-2">
                {columns.map(col => (
                    <FieldInput
                        key={col.id}
                        column={col}
                        value={values[col.id]}
                        onChange={(val) => handleChange(col.id, val)}
                        error={errors[col.id]}
                    />
                ))}
            </div>
            
            {apiError && !apiError.fieldErrors && (
                <div className="mt-4 p-3 bg-red-900/50 text-red-200 border border-red-700 rounded text-sm">
                    {apiError.message}
                </div>
            )}
            
            <div className="flex justify-end gap-3 mt-6 pt-4 border-t border-gray-800">
                <button
                    type="button"
                    onClick={onCancel}
                    className="px-4 py-2 text-gray-300 hover:text-white transition-colors"
                    disabled={isSubmitting}
                >
                    Cancel
                </button>
                <button
                    type="submit"
                    className="px-4 py-2 bg-white text-black font-medium rounded hover:bg-gray-200 transition-colors disabled:opacity-50"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? 'Saving...' : 'Save'}
                </button>
            </div>
        </form>
    );
};

export default DynamicRowForm;
