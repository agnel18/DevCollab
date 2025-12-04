import { useState, useEffect } from 'react';
import { Board } from '../types';
import { api } from '../services/api';
import { CreateBoardModal } from '../components/CreateBoardModal';

export function Boards() {
  const [boards, setBoards] = useState<Board[]>([]);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadBoards();
  }, []);

  const loadBoards = async () => {
    try {
      setLoading(true);
      const data = await api.getBoards();
      setBoards(data);
      setError(null);
    } catch (err) {
      setError('Failed to load boards');
      console.error('Failed to load boards:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateBoard = async (boardData: Partial<Board>) => {
    try {
      const newBoard = await api.createBoard(boardData);
      setBoards(prev => [...prev, newBoard]);
      setIsCreateModalOpen(false);
    } catch (error) {
      console.error('Failed to create board:', error);
      setError('Failed to create board');
    }
  };

  const handleDeleteBoard = async (boardId: number) => {
    if (!confirm('Are you sure you want to delete this board? This action cannot be undone.')) {
      return;
    }

    try {
      // Note: You'll need to implement deleteBoard in your API
      // await api.deleteBoard(boardId);
      setBoards(prev => prev.filter(b => b.id !== boardId));
    } catch (error) {
      console.error('Failed to delete board:', error);
      setError('Failed to delete board');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      {/* Header */}
      <header className="bg-black/20 backdrop-blur-sm border-b border-white/10 px-4 py-6 sticky top-0 z-50">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-white font-bold text-3xl mb-1">All Boards</h1>
            <p className="text-white/70">View and manage all your project boards</p>
          </div>
          <button
            onClick={() => setIsCreateModalOpen(true)}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition"
          >
            + Create Board
          </button>
        </div>
      </header>

      {/* Main Content */}
      <main className="p-6">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-white/70">Loading boards...</div>
          </div>
        ) : error ? (
          <div className="flex items-center justify-center h-64">
            <div className="text-red-400">{error}</div>
          </div>
        ) : boards.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-64">
            <div className="text-white/70 mb-4">No boards yet. Create your first board!</div>
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition"
            >
              + Create Board
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {boards.map(board => (
              <div
                key={board.id}
                className="group relative bg-white/5 backdrop-blur-sm border border-white/10 rounded-lg p-6 hover:bg-white/10 transition cursor-pointer"
              >
                {/* Background decoration */}
                <div className="absolute top-0 right-0 w-20 h-20 bg-blue-500/10 rounded-full blur-2xl group-hover:bg-blue-500/20 transition"></div>

                {/* Content */}
                <div className="relative z-10">
                  {/* Header */}
                  <div className="flex items-start justify-between mb-4">
                    <div>
                      <h2 className="text-xl font-semibold text-white mb-1 line-clamp-2">
                        {board.name}
                      </h2>
                      {board.description && (
                        <p className="text-sm text-white/70 line-clamp-2">
                          {board.description}
                        </p>
                      )}
                    </div>
                    <div className="text-2xl">📋</div>
                  </div>

                  {/* Stats */}
                  <div className="flex items-center gap-4 mb-4 text-sm text-white/60">
                    <span>{board.columns?.length || 0} lists</span>
                    <span>•</span>
                    <span>Created {new Date(board.createdAt).toLocaleDateString()}</span>
                  </div>

                  {/* Column preview */}
                  {board.columns && board.columns.length > 0 && (
                    <div className="mb-4 flex gap-2 overflow-x-auto pb-2">
                      {board.columns.slice(0, 3).map(column => (
                        <div
                          key={column.id}
                          className="flex-shrink-0 px-3 py-2 rounded bg-white/10 border border-white/20"
                        >
                          <span className="text-xs text-white/70">{column.name}</span>
                        </div>
                      ))}
                      {board.columns.length > 3 && (
                        <div className="flex-shrink-0 px-3 py-2 rounded bg-white/10 border border-white/20">
                          <span className="text-xs text-white/70">+{board.columns.length - 3}</span>
                        </div>
                      )}
                    </div>
                  )}

                  {/* Actions */}
                  <div className="flex gap-2">
                    <a
                      href={`/?board=${board.id}`}
                      className="flex-1 px-3 py-2 text-center text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded transition"
                    >
                      Open
                    </a>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteBoard(board.id);
                      }}
                      className="px-3 py-2 text-sm font-medium text-red-400 hover:text-red-300 hover:bg-red-500/10 rounded transition"
                      title="Delete board"
                    >
                      🗑
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Create Board Modal */}
      <CreateBoardModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onCreate={handleCreateBoard}
      />
    </div>
  );
}
