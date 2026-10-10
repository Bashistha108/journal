import { useState } from 'react';
import { tableApi, type CreateColumnRequest } from '../api/tables';
import { Plus, Trash2, Loader2, AlertCircle, ChevronLeft, GripVertical } from 'lucide-react';

interface CreateTableModalProps {
  onClose: () => void;
  onSuccess: () => void;
}

const DATA_TYPES = ['TEXT', 'INTEGER', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME', 'SELECT', 'LINK', 'IMAGE'];

export default function CreateTableModal({ onClose, onSuccess }: CreateTableModalProps) {
  const [name, setName] = useState('');
  const [columns, setColumns] = useState<CreateColumnRequest[]>([{ name: '', dataType: 'TEXT', selectOptions: [] }]);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  const addColumn = () => {
    setColumns([...columns, { name: '', dataType: 'TEXT', selectOptions: [] }]);
  };

  const removeColumn = (index: number) => {
    setColumns(columns.filter((_, i) => i !== index));
  };

  const updateColumn = (index: number, field: keyof CreateColumnRequest, value: any) => {
    const newCols = [...columns];
    newCols[index] = { ...newCols[index], [field]: value };
    if (field === 'dataType' && value !== 'SELECT') {
      newCols[index].selectOptions = [];
    }
    setColumns(newCols);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setFieldErrors({});
    setSubmitting(true);

    try {
      const cleanedColumns = columns.map(c => {
        const { _rawSelectOptions, ...rest } = c as any;
        return rest;
      });
      await tableApi.createTable({ name, columns: cleanedColumns });
      onSuccess();
    } catch (err: any) {
      if (err.fields && err.fields.length > 0) {
        const fErrors: Record<string, string> = {};
        err.fields.forEach((f: any) => {
          fErrors[f.field] = f.message;
        });
        setFieldErrors(fErrors);
      } else {
        setError(err.message || 'Failed to create table');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-[#121212] overflow-y-auto">
      <div className="max-w-6xl mx-auto p-8">
        <form onSubmit={handleSubmit}>
          {/* Header */}
          <div className="flex justify-between items-start mb-8">
            <div className="flex items-center gap-4">
              <button type="button" onClick={onClose} className="p-2 hover:bg-[#2a2a2a] rounded-lg transition-colors text-neutral-400">
                <ChevronLeft className="w-5 h-5" />
              </button>
              <div className="flex flex-col">
                <h1 className="text-[22px] font-bold text-white tracking-tight">Create Table</h1>
                <p className="text-[13px] text-neutral-400 mt-1">Define the table name and initial columns.</p>
              </div>
            </div>
            <button type="submit" className="btn-primary min-w-[120px]" disabled={submitting}>
              {submitting ? <Loader2 className="w-4 h-4 animate-spin mx-auto" /> : 'Create Table'}
            </button>
          </div>

          {error && (
            <div className="mb-6 p-4 bg-red-950/50 border border-red-900/50 text-red-200 rounded-lg text-sm flex items-center gap-3">
              <AlertCircle className="w-4 h-4" />
              {error}
            </div>
          )}

          {/* Table Name Section */}
          <div className="bg-[#1c1c1c] rounded-xl border border-[#2e2e2e] p-6 mb-6">
            <label className="block mb-2 text-[13px] font-medium text-neutral-300">Table Name</label>
            <input 
              type="text" 
              value={name} 
              onChange={e => setName(e.target.value)} 
              placeholder="e.g. users" 
              autoFocus
              className="w-full max-w-md bg-[#121212] border border-[#2e2e2e] rounded-md px-3 py-2 text-sm text-neutral-200 placeholder:text-neutral-500 focus:outline-none focus:border-[#4e4e4e]"
            />
            {fieldErrors['name'] && <div className="text-red-400 text-xs mt-2 font-medium">{fieldErrors['name']}</div>}
          </div>

          {/* Columns Section */}
          <div className="bg-[#1c1c1c] rounded-xl border border-[#2e2e2e] p-6">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-[15px] font-medium text-white">Columns</h2>
              <button type="button" onClick={addColumn} className="btn-secondary text-sm h-8 px-3 gap-1.5 border border-yellow-500/30 text-yellow-500 hover:bg-yellow-500/10 hover:border-yellow-500/50">
                <Plus className="w-3.5 h-3.5" />
                Add Column
              </button>
            </div>
            {fieldErrors['columns'] && <div className="text-red-400 text-xs mb-4 font-medium">{fieldErrors['columns']}</div>}
            
            <div className="overflow-x-auto border border-[#2e2e2e] rounded-lg">
              <table className="w-full text-sm text-left">
                <thead>
                  <tr className="border-b border-[#2e2e2e] bg-[#222]">
                    <th className="px-4 py-3 font-medium text-neutral-400 w-10"></th>
                    <th className="px-4 py-3 font-medium text-neutral-400">Name</th>
                    <th className="px-4 py-3 font-medium text-neutral-400 w-48">Type</th>
                    <th className="px-4 py-3 font-medium text-neutral-400 w-16 text-center"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#2e2e2e]">
                  {/* Dynamic Columns */}
                  {columns.map((col, idx) => (
                    <tr key={idx} className="bg-[#121212] group">
                      <td className="px-4 py-3 text-neutral-600"><GripVertical className="w-4 h-4 cursor-grab" /></td>
                      <td className="px-4 py-3">
                        <input 
                          type="text" 
                          value={col.name} 
                          onChange={e => updateColumn(idx, 'name', e.target.value)} 
                          placeholder="column_name" 
                          className="w-full bg-transparent border border-transparent group-hover:border-[#2e2e2e] focus:border-[#4e4e4e] rounded-md px-3 py-1.5 text-sm text-neutral-200 placeholder:text-neutral-600 focus:outline-none focus:bg-[#1c1c1c] transition-all"
                        />
                        {fieldErrors[`columns[${idx}].name`] && <div className="text-red-400 text-xs mt-1">{fieldErrors[`columns[${idx}].name`]}</div>}
                      </td>
                      <td className="px-4 py-3">
                        <select 
                          value={col.dataType} 
                          onChange={e => updateColumn(idx, 'dataType', e.target.value)}
                          className="w-full bg-[#1c1c1c] border border-[#2e2e2e] rounded-md px-3 py-1.5 text-sm text-neutral-300 focus:outline-none focus:border-[#4e4e4e] cursor-pointer"
                        >
                          {DATA_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                        </select>
                        
                        {col.dataType === 'SELECT' && (
                          <div className="mt-2">
                            <input 
                              type="text" 
                              value={(col as any)['_rawSelectOptions'] ?? (col.selectOptions?.join(', ') || '')} 
                              onChange={e => {
                                const rawVal = e.target.value;
                                const opts = rawVal.split(',').map(s => s.trim()).filter(s => s);
                                const newCols = [...columns];
                                newCols[idx] = { ...newCols[idx], selectOptions: opts, _rawSelectOptions: rawVal } as any;
                                setColumns(newCols);
                              }} 
                              placeholder="Options (comma separated)" 
                              className="w-full bg-[#1c1c1c] border border-[#2e2e2e] rounded-md px-3 py-1.5 text-xs text-neutral-300 placeholder:text-neutral-600 focus:outline-none focus:border-[#4e4e4e]"
                            />
                            {fieldErrors[`columns[${idx}].selectOptions`] && <div className="text-red-400 text-xs mt-1">{fieldErrors[`columns[${idx}].selectOptions`]}</div>}
                          </div>
                        )}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <button type="button" onClick={() => removeColumn(idx)} className="text-neutral-500 hover:text-red-400 transition-colors p-1" title="Remove Column">
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
