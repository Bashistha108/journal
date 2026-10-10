import React, { useState, useEffect } from 'react';
import type { ColumnDetail } from '../../types/table';
import type { RowDetail } from '../../types/row';
import FieldInput from './FieldInput';
import { validateField } from '../../validation/fieldValidation';
import type { ApiError } from '../../types/api';
import type { AttachmentUpload } from '../../types/attachment';
import AttachmentPicker from '../attachments/AttachmentPicker';
import { Loader2 } from 'lucide-react';

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
            const initialVals: Record<number, any> = {};
            columns.forEach(col => {
                if (col.dataType === 'BOOLEAN') {
                    initialVals[col.id] = true;
                } else if (col.dataType === 'SELECT' && col.selectOptions && col.selectOptions.length > 0) {
                    initialVals[col.id] = col.selectOptions[0].value;
                }
            });
            setValues(initialVals);
        }
        setNewAttachments([]);
        setRemoveAttachmentIds([]);
    }, [initialData, columns]);

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
            
            <div className="flex justify-end gap-3 mt-8 pt-6 border-t border-[#2e2e2e]">
                <button
                    type="button"
                    onClick={onCancel}
                    className="btn-secondary"
                    disabled={isSubmitting}
                >
                    Cancel
                </button>
                <button
                    type="submit"
                    className="btn-primary min-w-[140px]"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? (
                        <span className="flex items-center justify-center gap-2">
                            <Loader2 className="animate-spin w-4 h-4" />
                            Saving...
                        </span>
                    ) : 'Save Record'}
                </button>
            </div>
        </form>
    );
};

export default DynamicRowForm;
