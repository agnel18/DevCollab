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
import { api } from '../services/api';

export function Board() {
  const [boards, setBoards] = useState<BoardType[]>([]);
  const [currentBoardId, setCurrentBoardId] = useState<number | null>(null);
  const [projects, setProjects] = useState<Project[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [activeTimerId, setActiveTimerId] = useState<number | null>(null);
  const [isDarkMode, setIsDarkMode] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [createModalColumnId, setCreateModalColumnId] = useState<number | null>(null);

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
    if (!currentBoardId || !createModalColumnId) return;

    try {
      const newProject = await api.createProject({
        ...projectData,
        boardId: currentBoardId,
        boardColumnId: createModalColumnId,
      });
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
            <div className="flex gap-2 overflow-x-auto pb-2">
              {boards.map(board => (
                <button
                  key={board.id}
                  onClick={() => setCurrentBoardId(board.id)}
                  className={`px-3 py-1.5 rounded text-sm font-medium whitespace-nowrap transition ${
                    currentBoardId === board.id
                      ? 'bg-white/30 text-white border border-white/50'
                      : 'bg-white/10 text-white/70 border border-white/20 hover:bg-white/20'
                  }`}
                >
                  {board.name}
                </button>
              ))}
              <button
                onClick={() => console.log('Create board')}
                className="px-3 py-1.5 rounded text-sm font-medium text-white/70 hover:text-white bg-white/5 hover:bg-white/10 border border-white/20 transition whitespace-nowrap"
              >
                + New Board
              </button>
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
                {currentBoard.columns && currentBoard.columns.map(column => (
                  <BoardColumn
                    key={column.id}
                    id={column.id}
                    title={column.name}
                    projects={getProjectsByColumn(column.id)}
                    color={column.bgColor || 'blue'}
                    onStartPomodoro={handleStartPomodoro}
                    onPausePomodoro={handlePausePomodoro}
                    onStopPomodoro={handleStopPomodoro}
                    onDelete={handleDelete}
                    onAddCard={() => openCreateModal(column.id)}
                    activeTimerId={activeTimerId}
                  />
                ))}
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
      </div>
    </div>
  );
}
