import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { tableApi, type TableDetail } from '../api/tables';
import CreateTableModal from '../components/CreateTableModal';
import { Plus, Table2, LayoutTemplate, Eye, Edit2, Trash2, ChevronRight } from 'lucide-react';

export default function TableManagementPage() {
  const [tables, setTables] = useState<TableDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const navigate = useNavigate();

  const fetchTables = async () => {
    try {
      setLoading(true);
      const data = await tableApi.listTables();
      setTables(data);
      setError('');
    } catch (err: any) {
      setError(err.message || 'Failed to fetch tables');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTables();
  }, []);

  return (
    <div className="min-h-full bg-[#121212] text-neutral-200">
      <div className="p-8">
        <div className="flex justify-between items-start mb-8">
          <div className="flex items-start gap-4">
            <div className="bg-[#2a2a2a] p-2.5 rounded-lg border border-[#3e3e3e] mt-1">
              <LayoutTemplate className="w-6 h-6 text-yellow-400" />
            </div>
            <div className="flex flex-col">
              <h1 className="text-[22px] font-bold text-white tracking-tight">Tables</h1>
              <p className="text-[13px] text-neutral-400 mt-1">View and manage your database tables. Create new tables, modify existing ones, and explore their structure, columns and relationships.</p>
            </div>
          </div>
          <button className="btn-primary flex items-center gap-2" onClick={() => setIsModalOpen(true)}>
            <Plus className="w-4 h-4" />
            Create Table
          </button>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-950/50 border border-red-900/50 text-red-200 rounded-lg text-sm flex items-center gap-3">
            {error}
          </div>
        )}

        <div className="bg-[#1c1c1c] rounded-xl border border-[#2e2e2e] flex flex-col">
          {/* Table */}
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead>
                <tr className="border-b border-[#2e2e2e]">
                  <th className="px-6 py-4 font-medium text-neutral-400">Table Name</th>
                  <th className="px-6 py-4 font-medium text-neutral-400 w-32">Columns</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#2e2e2e]">
                {loading ? (
                  <tr>
                    <td colSpan={3} className="px-6 py-8 text-center text-neutral-500">Loading...</td>
                  </tr>
                ) : tables.length === 0 ? (
                  <tr>
                    <td colSpan={3} className="px-6 py-8 text-center text-neutral-500">No tables found</td>
                  </tr>
                ) : (
                  tables.map(table => (
                    <tr key={table.id} className="hover:bg-[#2a2a2a] transition-colors group cursor-pointer" onClick={() => navigate(`/tables/${table.id}`)}>
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <ChevronRight className="w-4 h-4 text-neutral-500" />
                          <Table2 className="w-4 h-4 text-neutral-400" />
                          <span className="text-neutral-200 font-medium">{table.displayName}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-neutral-300">{table.columns.length}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {isModalOpen && (
          <CreateTableModal 
            onClose={() => setIsModalOpen(false)} 
            onSuccess={() => {
              setIsModalOpen(false);
              fetchTables();
            }} 
          />
        )}
      </div>
    </div>
  );
}
