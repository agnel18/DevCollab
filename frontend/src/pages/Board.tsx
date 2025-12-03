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
import { Project } from '../types';
import { BoardColumn } from '../components/BoardColumn';
import { ProjectCard } from '../components/ProjectCard';
import { api } from '../services/api';

export function Board() {
  const [projects, setProjects] = useState<Project[]>([]);
  const [activeId, setActiveId] = useState<number | null>(null);
  const [activeTimerId, setActiveTimerId] = useState<number | null>(null);
  const [isDarkMode, setIsDarkMode] = useState(false);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  useEffect(() => {
    loadProjects();
    
    // Check for active timer
    const timer = projects.find(p => p.pomodoroStart);
    if (timer) {
      setActiveTimerId(timer.id);
    }
  }, []);

  const loadProjects = async () => {
    try {
      const data = await api.getAllProjects();
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
    const newStatus = (over.id as string).toUpperCase() as Project['status'];
    
    const project = projects.find(p => p.id === projectId);
    if (project && project.status !== newStatus) {
      // Optimistic update
      setProjects(prev =>
        prev.map(p => p.id === projectId ? { ...p, status: newStatus } : p)
      );

      try {
        await api.updateProject(projectId, { status: newStatus });
      } catch (error) {
        console.error('Failed to update project status:', error);
        // Revert on error
        loadProjects();
      }
    }
  };

  const handleStartPomodoro = async (id: number) => {
    // Stop any other running timer first
    const runningTimer = projects.find(p => p.pomodoroStart);
    if (runningTimer && runningTimer.id !== id) {
      await handlePausePomodoro(runningTimer.id);
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
      const updated = await api.stopPomodoro(id);
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

  const todoProjects = projects.filter(p => p.status === 'TODO');
  const doingProjects = projects.filter(p => p.status === 'DOING');
  const doneProjects = projects.filter(p => p.status === 'DONE');

  const activeProject = activeId ? projects.find(p => p.id === activeId) : null;

  return (
    <div className={isDarkMode ? 'dark' : ''}>
      <div className="min-h-screen bg-sunset">
        {/* Top Bar - Trello style */}
        <header className="bg-black/20 backdrop-blur-sm border-b border-white/10 px-4 py-2">
          <div className="flex items-center justify-between">
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
              />
              
              {/* Dark mode toggle */}
              <button
                onClick={() => setIsDarkMode(!isDarkMode)}
                className="p-2 rounded hover:bg-white/10 text-white"
                title="Toggle dark mode"
              >
                {isDarkMode ? '☀️' : '🌙'}
              </button>
              
              {/* Share button */}
              <button className="px-3 py-1.5 bg-white/10 hover:bg-white/20 text-white rounded text-sm font-medium border border-white/20">
                Share
              </button>
            </div>
          </div>
        </header>

        {/* Board */}
        <main className="p-4 overflow-x-auto">
          <DndContext
            sensors={sensors}
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
          >
            <div className="flex gap-4 h-[calc(100vh-120px)]">
              <BoardColumn
                id="todo"
                title="To Do"
                projects={todoProjects}
                color="blue"
                onStartPomodoro={handleStartPomodoro}
                onPausePomodoro={handlePausePomodoro}
                onStopPomodoro={handleStopPomodoro}
                onDelete={handleDelete}
                activeTimerId={activeTimerId}
              />
              
              <BoardColumn
                id="doing"
                title="Doing"
                projects={doingProjects}
                color="yellow"
                onStartPomodoro={handleStartPomodoro}
                onPausePomodoro={handlePausePomodoro}
                onStopPomodoro={handleStopPomodoro}
                onDelete={handleDelete}
                activeTimerId={activeTimerId}
              />
              
              <BoardColumn
                id="done"
                title="Done"
                projects={doneProjects}
                color="green"
                onStartPomodoro={handleStartPomodoro}
                onPausePomodoro={handlePausePomodoro}
                onStopPomodoro={handleStopPomodoro}
                onDelete={handleDelete}
                activeTimerId={activeTimerId}
              />
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
      </div>
    </div>
  );
}
