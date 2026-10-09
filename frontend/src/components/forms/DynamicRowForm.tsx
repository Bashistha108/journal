import React, { useState, useEffect } from 'react';
import type { ColumnDetail } from '../../types/table';
import type { RowDetail } from '../../types/row';
import FieldInput from './FieldInput';
import { validateField } from '../../validation/fieldValidation';
import type { ApiError } from '../../types/api';
import type { AttachmentUpload } from '../../types/attachment';
import AttachmentPicker from '../attachments/AttachmentPicker';

interface DynamicRowFormProps {
    columns: ColumnDetail[];
    initialData?: RowDetail | null;
    onSubmit: (values: Record<number, any>, expectedVersion?: number, newAttachments?: AttachmentUpload[], removeAttachmentIds?: number[]) => Promise<void>;
    onCancel: () => void;
    apiError: ApiError | null;
    tableId: number;
}

const DynamicRowForm: React.FC<DynamicRowFormProps> = ({ columns, initialData, onSubmit, onCancel, apiError, tableId }) => {
    const [values, setValues] = useState<Record<number, any>>({});
    const [errors, setErrors] = useState<Record<number, string>>({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [newAttachments, setNewAttachments] = useState<AttachmentUpload[]>([]);
    const [removeAttachmentIds, setRemoveAttachmentIds] = useState<number[]>([]);

    useEffect(() => {
        if (initialData) {
            setValues(initialData.values || {});
        } else {
            setValues({});
        }
        setNewAttachments([]);
        setRemoveAttachmentIds([]);
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

    const handleAddAttachments = (uploads: AttachmentUpload[]) => {
        setNewAttachments(prev => [...prev, ...uploads]);
    };

    const handleRemoveNewAttachment = (clientRef: string) => {
        setNewAttachments(prev => prev.filter(a => a.clientRef !== clientRef));
    };

    const handleStageRemoval = (attachmentId: number) => {
        setRemoveAttachmentIds(prev => [...prev, attachmentId]);
    };

    const handleUnstageRemoval = (attachmentId: number) => {
        setRemoveAttachmentIds(prev => prev.filter(id => id !== attachmentId));
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
                await onSubmit(values, initialData?.version, newAttachments, removeAttachmentIds);
            } finally {
                setIsSubmitting(false);
            }
        }
    };

    return (
        <form onSubmit={handleSubmit}>
            <div className="max-h-[60vh] overflow-y-auto pr-2">
                {columns.map(col => {
                    if (col.dataType === 'IMAGE') {
                        return (
                            <div key={col.id} className="mb-4">
                                <label className="block text-sm font-medium mb-1 text-gray-300">
                                    {col.displayName}
                                </label>
                                <AttachmentPicker
                                    tableId={tableId}
                                    rowId={initialData?.id}
                                    columnId={col.id}
                                    existingAttachments={initialData?.attachments?.filter(a => a.columnId === col.id) || []}
                                    newAttachments={newAttachments.filter(a => a.columnId === col.id)}
                                    removeAttachmentIds={removeAttachmentIds}
                                    onAddAttachments={handleAddAttachments}
                                    onRemoveNewAttachment={handleRemoveNewAttachment}
                                    onStageRemoval={handleStageRemoval}
                                    onUnstageRemoval={handleUnstageRemoval}
                                />
                            </div>
                        );
                    }

                    return (
                        <FieldInput
                            key={col.id}
                            column={col}
                            value={values[col.id]}
                            onChange={(val) => handleChange(col.id, val)}
                            error={errors[col.id]}
                        />
                    );
                })}
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
