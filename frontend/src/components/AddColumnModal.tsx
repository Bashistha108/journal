import { useState } from 'react';
import { tableApi, type CreateColumnRequest } from '../api/tables';
import { Loader2, AlertCircle } from 'lucide-react';

interface AddColumnModalProps {
  tableId: number;
  onClose: () => void;
  onSuccess: () => void;
}

const DATA_TYPES = ['TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME', 'SELECT', 'LINK', 'IMAGE'];

export default function AddColumnModal({ tableId, onClose, onSuccess }: AddColumnModalProps) {
  const [column, setColumn] = useState<CreateColumnRequest>({ name: '', dataType: 'TEXT', selectOptions: [] });
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  const updateColumn = (field: keyof CreateColumnRequest, value: any) => {
    const newCol = { ...column, [field]: value };
    if (field === 'dataType' && value !== 'SELECT') {
      newCol.selectOptions = [];
    }
    setColumn(newCol);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});
    setSubmitting(true);

    try {
      const { _rawSelectOptions, ...cleanedColumn } = column as any;
      await tableApi.addColumn(tableId, cleanedColumn);
      onSuccess();
    } catch (err: any) {
      if (err.fieldErrors && err.fieldErrors.length > 0) {
        const fErrors: Record<string, string> = {};
        err.fieldErrors.forEach((f: any) => {
          // Map "column.name" to "name" and "column.selectOptions" to "selectOptions"
          const fieldName = f.field.startsWith('column.') ? f.field.substring(7) : f.field;
          fErrors[fieldName] = f.message;
        });
        setFieldErrors(fErrors);
      } else {
        setError(err.message || 'Failed to add column');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#121212]/80 backdrop-blur-sm overflow-y-auto flex items-center justify-center">
      <div className="w-full max-w-lg bg-[#1c1c1c] rounded-xl border border-[#2e2e2e] shadow-2xl p-6">
        <form onSubmit={handleSubmit}>
          {/* Header */}
          <div className="flex justify-between items-center mb-6">
            <h1 className="text-lg font-bold text-white tracking-tight">Add Column</h1>
            <button type="button" onClick={onClose} className="p-2 hover:bg-[#2a2a2a] rounded-lg transition-colors text-neutral-400">
              <span className="sr-only">Close</span>
              &times;
            </button>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-950/50 border border-red-900/50 text-red-200 rounded-lg text-sm flex items-center gap-3">
              <AlertCircle className="w-4 h-4" />
              {error}
            </div>
          )}

          {/* Form Fields */}
          <div className="space-y-4">
            <div>
              <label className="block mb-2 text-sm font-medium text-neutral-300">Name</label>
              <input 
                type="text" 
                value={column.name} 
                onChange={e => updateColumn('name', e.target.value)} 
                placeholder="e.g. status" 
                autoFocus
                className="w-full bg-[#121212] border border-[#2e2e2e] rounded-md px-3 py-2 text-sm text-neutral-200 placeholder:text-neutral-500 focus:outline-none focus:border-[#4e4e4e]"
              />
              {fieldErrors['name'] && <div className="text-red-400 text-xs mt-1 font-medium">{fieldErrors['name']}</div>}
            </div>

            <div>
              <label className="block mb-2 text-sm font-medium text-neutral-300">Type</label>
              <select 
                value={column.dataType} 
                onChange={e => updateColumn('dataType', e.target.value)}
                className="w-full bg-[#121212] border border-[#2e2e2e] rounded-md px-3 py-2 text-sm text-neutral-200 focus:outline-none focus:border-[#4e4e4e] cursor-pointer"
              >
                {DATA_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
              </select>
            </div>

            {column.dataType === 'SELECT' && (
              <div>
                <label className="block mb-2 text-sm font-medium text-neutral-300">Options</label>
                <input 
                  type="text" 
                  value={(column as any)['_rawSelectOptions'] ?? (column.selectOptions?.join(', ') || '')} 
                  onChange={e => {
                    const rawVal = e.target.value;
                    const opts = rawVal.split(',').map(s => s.trim()).filter(s => s);
                    const newCol = { ...column, selectOptions: opts, _rawSelectOptions: rawVal } as any;
                    setColumn(newCol);
                  }} 
                  placeholder="e.g. Active, Inactive, Pending" 
                  className="w-full bg-[#121212] border border-[#2e2e2e] rounded-md px-3 py-2 text-sm text-neutral-200 placeholder:text-neutral-500 focus:outline-none focus:border-[#4e4e4e]"
                />
                {fieldErrors['selectOptions'] && <div className="text-red-400 text-xs mt-1">{fieldErrors['selectOptions']}</div>}
              </div>
            )}
          </div>

          <div className="mt-8 flex justify-end gap-3">
            <button type="button" onClick={onClose} className="px-4 py-2 text-sm font-medium text-neutral-300 hover:text-white hover:bg-[#2a2a2a] rounded-lg transition-colors">
              Cancel
            </button>
            <button type="submit" className="btn-primary min-w-[100px]" disabled={submitting}>
              {submitting ? <Loader2 className="w-4 h-4 animate-spin mx-auto" /> : 'Add Column'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
