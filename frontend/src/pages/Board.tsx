import { useState, useEffect } from 'react';
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  useSensor,
  useSensors,
} from '@dnd-kit/core';
import { Project, Board as BoardType, BoardColumn as BoardColumnType } from '../types';
import { BoardColumn } from '../components/BoardColumn';
import { ProjectCard } from '../components/ProjectCard';
import { CreateProjectModal } from '../components/CreateProjectModal';
import { CreateBoardModal } from '../components/CreateBoardModal';
import { CreateColumnModal } from '../components/CreateColumnModal';
import { api } from '../services/api';

// Convert hex colors to color names
const hexToColorName = (hex: string): string => {
  const hexMap: Record<string, string> = {
    '#EF4444': 'red',
    '#F97316': 'orange',
    '#EAB308': 'yellow',
    '#22C55E': 'green',
    '#3B82F6': 'blue',
    '#A855F7': 'purple',
    '#6366F1': 'indigo',
  };
  return hexMap[hex?.toUpperCase()] || 'blue';
};

export function Board() {
  const [boards, setBoards] = useState<BoardType[]>([]);
  const [currentBoardId, setCurrentBoardId] = useState<number | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [activeTimerId, setActiveTimerId] = useState<number | null>(null);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [createModalColumnId, setCreateModalColumnId] = useState<number | null>(null);
  const [isCreateBoardModalOpen, setIsCreateBoardModalOpen] = useState(false);
  const [isCreateColumnModalOpen, setIsCreateColumnModalOpen] = useState(false);
  const [selectedColumnId, setSelectedColumnId] = useState<number | null>(null);
  const [showColorPicker, setShowColorPicker] = useState(false);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  useEffect(() => {
    loadBoards();
  }, []);

  useEffect(() => {
    if (currentBoardId) {
      loadProjectsByBoard(currentBoardId);
    }
  }, [currentBoardId]);

  const loadBoards = async () => {
    try {
      const data = await api.getBoards();
      setBoards(data);
      // Select the first board by default
      if (data.length > 0 && !currentBoardId) {
        setCurrentBoardId(data[0].id);
      }
    } catch (error) {
      console.error('Failed to load boards:', error);
    }
  };

  const loadProjectsByBoard = async (boardId: number) => {
    try {
      const data = await api.getProjectsByBoard(boardId);
      setProjects(data);
    } catch (error) {
      console.error('Failed to load projects:', error);
    }
  };

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as number);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveId(null);

    if (!over) return;

    const projectId = active.id as number;
    const newColumnId = over.id as number;

    const project = projects.find(p => p.id === projectId);
    if (!project || project.boardColumnId === newColumnId) return;

    // Optimistic update
    setProjects(prev => prev.map(p => 
      p.id === projectId ? { ...p, boardColumnId: newColumnId } : p
    ));

    try {
      await api.updateProject(projectId, { boardColumnId: newColumnId });
    } catch (error) {
      console.error('Failed to update project column:', error);
      // Revert on error
      if (currentBoardId) {
        loadProjectsByBoard(currentBoardId);
      }
    }
  };

  const handleStartPomodoro = async (id: number) => {
    // Stop any other running timer
    if (activeTimerId && activeTimerId !== id) {
      await handleStopPomodoro(activeTimerId);
    }

    try {
      const updated = await api.startPomodoro(id);
      setProjects(prev => prev.map(p => p.id === id ? updated : p));
      setActiveTimerId(id);

      // Auto-move to DOING if not already there
      if (updated.status !== 'DOING') {
        const moved = await api.updateProject(id, { status: 'DOING' });
        setProjects(prev => prev.map(p => p.id === id ? moved : p));
      }
    } catch (error) {
      console.error('Failed to start pomodoro:', error);
    }
  };

  const handlePausePomodoro = async (id: number) => {
    try {
      const updated = await api.pausePomodoro(id);
      setProjects(prev => prev.map(p => p.id === id ? updated : p));
      setActiveTimerId(null);
    } catch (error) {
      console.error('Failed to pause pomodoro:', error);
    }
  };

  const handleStopPomodoro = async (id: number) => {
    try {
      const updated = await api.stopPomodoro(id);
      setProjects(prev => prev.map(p => p.id === id ? updated : p));
      setActiveTimerId(null);
    } catch (error) {
      console.error('Failed to stop pomodoro:', error);
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await api.deleteProject(id);
      setProjects(prev => prev.filter(p => p.id !== id));
    } catch (error) {
      console.error('Failed to delete project:', error);
    }
  };

  const handleCreateProject = async (projectData: Partial<Project>) => {
    if (!currentBoardId || !createModalColumnId) {
      console.log('Missing boardId or columnId:', { currentBoardId, createModalColumnId });
      return;
    }

    try {
      const payload = {
        ...projectData,
        boardId: currentBoardId,
        boardColumnId: createModalColumnId,
      };
      console.log('Creating project with payload:', payload);
      
      const newProject = await api.createProject(payload);
      console.log('Project created:', newProject);
      
      setProjects(prev => [...prev, newProject]);
      setIsCreateModalOpen(false);
    } catch (error) {
      console.error('Failed to create project:', error);
    }
  };

  const openCreateModal = (columnId: number) => {
    setCreateModalColumnId(columnId);
    setIsCreateModalOpen(true);
  };

  const handleCreateBoard = async (boardData: Partial<BoardType>) => {
    try {
      const newBoard = await api.createBoard(boardData);
      
      // Reload the board with columns (already created by backend)
      const updatedBoard = await api.getBoard(newBoard.id);
      setBoards(prev => [...prev, updatedBoard]);
      setCurrentBoardId(updatedBoard.id);
      setIsCreateBoardModalOpen(false);
    } catch (error) {
      console.error('Failed to create board:', error);
    }
  };

  const handleCreateColumn = async (columnData: Partial<BoardColumnType>) => {
    if (!currentBoardId) return;

    try {
      const newColumn = await api.createColumn(currentBoardId, columnData);
      // Update current board with new column
      setBoards(prev => prev.map(b => 
        b.id === currentBoardId
          ? { ...b, columns: [...(b.columns || []), newColumn] }
          : b
      ));
      setIsCreateColumnModalOpen(false);
    } catch (error) {
      console.error('Failed to create column:', error);
    }
  };

  const handleDeleteBoard = async (boardId: number) => {
    if (!confirm('Are you sure you want to delete this board? All cards will be lost.')) {
      return;
    }

    try {
      await api.deleteBoard(boardId);
      setBoards(prev => prev.filter(b => b.id !== boardId));
      if (currentBoardId === boardId) {
        setCurrentBoardId(boards.find(b => b.id !== boardId)?.id || null);
      }
    } catch (error) {
      console.error('Failed to delete board:', error);
    }
  };

  const handleDeleteColumn = async (columnId: number) => {
    if (!confirm('Are you sure you want to delete this list? All cards in it will be moved.')) {
      return;
    }

    try {
      if (currentBoardId) {
        await api.deleteColumn(currentBoardId, columnId);
        setBoards(prev => prev.map(b => 
          b.id === currentBoardId
            ? { ...b, columns: (b.columns || []).filter(c => c.id !== columnId) }
            : b
        ));
        // Also remove projects from this column from state
        setProjects(prev => prev.filter(p => p.boardColumnId !== columnId));
      }
    } catch (error) {
      console.error('Failed to delete column:', error);
    }
  };

  const handleChangeColumnColor = async (columnId: number, hexColor: string) => {
    try {
      if (currentBoardId) {
        await api.updateColumn(currentBoardId, columnId, { bgColor: hexColor });
        setBoards(prev => prev.map(b =>
          b.id === currentBoardId
            ? {
                ...b,
                columns: (b.columns || []).map(c =>
                  c.id === columnId ? { ...c, bgColor: hexColor } : c
                ),
              }
            : b
        ));
        setShowColorPicker(false);
        setSelectedColumnId(null);
      }
    } catch (error) {
      console.error('Failed to update column color:', error);
      alert('Error updating column color');
    }
  };

  const currentBoard = currentBoardId ? boards.find(b => b.id === currentBoardId) : null;
  const activeProject = activeId ? projects.find(p => p.id === activeId) : null;

  const getProjectsByColumn = (columnId: number): Project[] => {
    return projects.filter(p => p.boardColumnId === columnId);
  };

  return (
    <div className={isDarkMode ? 'dark' : ''}>
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 dark:from-slate-950 dark:via-slate-900 dark:to-slate-950">
        {/* Top Bar - Trello style */}
        <header className="bg-black/20 backdrop-blur-sm border-b border-white/10 px-4 py-3 sticky top-0 z-50">
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-4">
              <h1 className="text-white font-bold text-xl">DevCollab</h1>
              <span className="text-white/70 text-sm">Kanban + Pomodoro</span>
            </div>
            
            <div className="flex items-center gap-3">
              {/* Search */}
              <input
                type="search"
                placeholder="Search cards..."
                className="px-3 py-1.5 rounded bg-white/10 border border-white/20 text-white placeholder-white/50 text-sm focus:outline-none focus:bg-white/20"
                title="Search for cards by name or description"
              />
              
              {/* Dark mode toggle */}
              <button
                onClick={() => setIsDarkMode(!isDarkMode)}
                className="p-2 rounded hover:bg-white/10 text-white transition"
                title="Toggle dark mode"
              >
                {isDarkMode ? '☀️' : '🌙'}
              </button>
              
              {/* Share button */}
              <button 
                className="px-3 py-1.5 bg-white/10 hover:bg-white/20 text-white rounded text-sm font-medium border border-white/20 transition"
                title="Share this board with team members"
              >
                Share
              </button>
            </div>
          </div>

          {/* Board Selector */}
          {boards.length > 0 && (
            <div className="flex gap-2 overflow-x-auto pb-2 items-center">
              {boards.map(board => (
                <div key={board.id} className="relative group">
                  <button
                    onClick={() => setCurrentBoardId(board.id)}
                    className={`px-3 py-1.5 rounded text-sm font-medium whitespace-nowrap transition max-w-xs truncate ${
                      currentBoardId === board.id
                        ? 'bg-white/30 text-white border border-white/50'
                        : 'bg-white/10 text-white/70 border border-white/20 hover:bg-white/20'
                    }`}
                    title={board.name}
                  >
                    {board.name}
                  </button>
                  <button
                    onClick={() => handleDeleteBoard(board.id)}
                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-5 h-5 flex items-center justify-center text-xs font-bold opacity-0 group-hover:opacity-100 transition"
                    title="Delete board"
                  >
                    ✕
                  </button>
                </div>
              ))}
              <button
                onClick={() => setIsCreateBoardModalOpen(true)}
                className="px-3 py-1.5 rounded text-sm font-medium text-white/70 hover:text-white bg-white/5 hover:bg-white/10 border border-white/20 transition whitespace-nowrap"
                title="Create a new board"
              >
                + New
              </button>
              <a
                href="/boards"
                className="px-3 py-1.5 rounded text-sm font-medium text-white/70 hover:text-white bg-white/5 hover:bg-white/10 border border-white/20 transition whitespace-nowrap ml-auto"
                title="View all boards"
              >
                📋 All Boards
              </a>
            </div>
          )}
        </header>

        {/* Main Board */}
        {currentBoard && (
          <main className="p-4">
            <DndContext
              sensors={sensors}
              onDragStart={handleDragStart}
              onDragEnd={handleDragEnd}
            >
              <div className="flex gap-4 overflow-x-auto pb-4">
                {currentBoard.columns && currentBoard.columns.map(column => {
                  const colorName = hexToColorName(column.bgColor || '#3B82F6');
                  return (
                    <div key={column.id} className="relative group">
                      <BoardColumn
                        id={column.id}
                        title={column.name}
                        projects={getProjectsByColumn(column.id)}
                        color={colorName}
                        onStartPomodoro={handleStartPomodoro}
                        onPausePomodoro={handlePausePomodoro}
                        onStopPomodoro={handleStopPomodoro}
                        onDelete={handleDelete}
                        onDeleteColumn={handleDeleteColumn}
                        onAddCard={() => openCreateModal(column.id)}
                        activeTimerId={activeTimerId}
                      />
                      {/* Color Picker Button */}
                      <button
                        onClick={() => {
                          setSelectedColumnId(selectedColumnId === column.id ? null : column.id);
                          setShowColorPicker(selectedColumnId === column.id ? false : true);
                        }}
                        className="absolute top-2 right-12 opacity-0 group-hover:opacity-100 px-2 py-1 bg-gray-600 hover:bg-gray-700 text-white rounded text-xs font-bold transition"
                        title="Change color"
                      >
                        🎨
                      </button>
                      {/* Color Picker */}
                      {selectedColumnId === column.id && showColorPicker && (
                        <div className="absolute top-10 right-12 bg-white dark:bg-gray-800 rounded-lg shadow-lg p-3 z-50 flex flex-wrap gap-2 w-48">
                          {['#EF4444', '#F97316', '#EAB308', '#22C55E', '#3B82F6', '#A855F7', '#6366F1'].map(hex => (
                            <button
                              key={hex}
                              onClick={() => handleChangeColumnColor(column.id, hex)}
                              className="w-8 h-8 rounded border-2 border-gray-300 hover:border-gray-600 dark:border-gray-600 dark:hover:border-gray-400 transition font-bold text-xs flex items-center justify-center"
                              style={{ backgroundColor: hex, color: hex === column.bgColor ? 'white' : 'transparent' }}
                              title={`Change to ${hexToColorName(hex)}`}
                            >
                              {hex === column.bgColor ? '✓' : ''}
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
                {/* Add Column Button */}
                <button
                  onClick={() => setIsCreateColumnModalOpen(true)}
                  className="flex-shrink-0 px-4 py-2 rounded-lg bg-white/10 hover:bg-white/20 border border-white/20 text-white text-sm font-medium transition-colors"
                >
                  + Add List
                </button>
              </div>

              <DragOverlay>
                {activeProject && (
                  <ProjectCard
                    project={activeProject}
                    onStartPomodoro={handleStartPomodoro}
                    onPausePomodoro={handlePausePomodoro}
                    onStopPomodoro={handleStopPomodoro}
                    onDelete={handleDelete}
                    activeTimerId={activeTimerId}
                  />
                )}
              </DragOverlay>
            </DndContext>
          </main>
        )}

        {/* Create Project Modal */}
        <CreateProjectModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onCreate={handleCreateProject}
        />

        {/* Create Board Modal */}
        <CreateBoardModal
          isOpen={isCreateBoardModalOpen}
          onClose={() => setIsCreateBoardModalOpen(false)}
          onCreate={handleCreateBoard}
        />

        {/* Create Column Modal */}
        <CreateColumnModal
          isOpen={isCreateColumnModalOpen}
          onClose={() => setIsCreateColumnModalOpen(false)}
          onCreate={handleCreateColumn}
        />
      </div>
    </div>
  );
}
