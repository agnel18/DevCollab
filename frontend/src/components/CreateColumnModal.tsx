import { useState } from 'react';
import { BoardColumn } from '../types';

interface CreateColumnModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreate: (column: Partial<BoardColumn>) => void;
}

export function CreateColumnModal({ isOpen, onClose, onCreate }: CreateColumnModalProps) {
  const [name, setName] = useState('');
  const [bgColor, setBgColor] = useState('blue');

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;

    onCreate({
      name: name.trim(),
      bgColor,
    });

    // Reset form
    setName('');
    setBgColor('blue');
    onClose();
  };

  const colors = ['blue', 'red', 'green', 'purple', 'yellow', 'indigo', 'pink'];

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={onClose}>
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-md w-full mx-4" onClick={(e) => e.stopPropagation()}>
        <div className="p-6">
          <h2 className="text-xl font-semibold mb-4 text-gray-900 dark:text-gray-100">Create New List</h2>
          
          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
                List Name *
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 dark:bg-gray-700 dark:text-white"
                placeholder="Enter list name..."
                autoFocus
                required
              />
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Color
              </label>
              <div className="flex gap-2 flex-wrap">
                {colors.map(color => (
                  <button
                    key={color}
                    type="button"
                    onClick={() => setBgColor(color)}
                    className={`w-8 h-8 rounded-lg border-2 transition ${
                      bgColor === color
                        ? 'border-white ring-2 ring-offset-2'
                        : 'border-gray-300 dark:border-gray-600'
                    }`}
                    style={{
                      backgroundColor: `rgb(var(--color-${color}))`,
                      '--color-blue': '59, 130, 246',
                      '--color-red': '239, 68, 68',
                      '--color-green': '34, 197, 94',
                      '--color-purple': '147, 51, 234',
                      '--color-yellow': '234, 179, 8',
                      '--color-indigo': '99, 102, 241',
                      '--color-pink': '236, 72, 153',
                    } as any}
                    title={color}
                  />
                ))}
              </div>
            </div>

            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-md transition-colors"
              >
                Cancel
              </button>
              <button
                type="submit"
                className="px-4 py-2 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-md transition-colors"
              >
                Create List
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
