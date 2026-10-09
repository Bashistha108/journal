import React, { useState } from 'react';
import type { RowListItem } from '../../types/row';
import type { ColumnDetail } from '../../types/table';
import { rowApi } from '../../api/rowApi';
import RowFormDialog from './RowFormDialog';
import DeleteRowDialog from './DeleteRowDialog';

interface RowActionsProps {
    tableId: number;
    row: RowListItem;
    columns: ColumnDetail[];
    onRowUpdated: () => void;
}

const RowActions: React.FC<RowActionsProps> = ({ tableId, row, columns, onRowUpdated }) => {
    const [isEditOpen, setIsEditOpen] = useState(false);
    const [isDeleteOpen, setIsDeleteOpen] = useState(false);

    const handleEditSubmit = async (values: Record<number, any>, expectedVersion?: number) => {
        await rowApi.updateRow(tableId, row.id, { values, expectedVersion });
        setIsEditOpen(false);
        onRowUpdated();
    };

    const handleDeleteConfirm = async () => {
        await rowApi.deleteRow(tableId, row.id);
        setIsDeleteOpen(false);
        onRowUpdated();
    };

    return (
        <div className="flex gap-3">
            <button
                onClick={() => setIsEditOpen(true)}
                className="text-sm text-blue-400 hover:text-blue-300 font-medium transition-colors"
            >
                Edit
            </button>
            <button
                onClick={() => setIsDeleteOpen(true)}
                className="text-sm text-red-400 hover:text-red-300 font-medium transition-colors"
            >
                Delete
            </button>
            <button
                className="text-sm text-gray-400 hover:text-gray-300 font-medium transition-colors"
                title="View Details (Phase 5)"
            >
                Details
            </button>

            {isEditOpen && (
                <RowFormDialog
                    columns={columns}
                    initialData={{ id: row.id, version: row.version, values: row.values, attachments: [] }}
                    onSubmit={handleEditSubmit}
                    onClose={() => setIsEditOpen(false)}
                    isOpen={isEditOpen}
                />
            )}

            <DeleteRowDialog
                isOpen={isDeleteOpen}
                onConfirm={handleDeleteConfirm}
                onCancel={() => setIsDeleteOpen(false)}
            />
        </div>
    );
};

export default RowActions;
