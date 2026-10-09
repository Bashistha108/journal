import React, { useState } from 'react';
import { Link } from 'react-router-dom';
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
    const [detail, setDetail] = useState<any>(null);

    const handleEditClick = async () => {
        try {
            const data = await rowApi.getRow(tableId, row.id);
            setDetail(data);
            setIsEditOpen(true);
        } catch (e) {
            console.error('Failed to fetch row detail', e);
        }
    };

    const handleEditSubmit = async (values: Record<number, any>, expectedVersion?: number, newAttachments?: import('../../types/attachment').AttachmentUpload[], removeAttachmentIds?: number[]) => {
        await rowApi.updateRow(tableId, row.id, { values, expectedVersion, removeAttachmentIds }, newAttachments);
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
                onClick={handleEditClick}
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
            <Link
                to={`/tables/${tableId}/rows/${row.id}`}
                className="text-sm text-gray-400 hover:text-gray-300 font-medium transition-colors"
                title="View Details"
            >
                Details
            </Link>

            {isEditOpen && detail && (
                <RowFormDialog
                    tableId={tableId}
                    columns={columns}
                    initialData={detail}
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
