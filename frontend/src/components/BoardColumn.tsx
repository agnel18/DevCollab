import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Project } from '../types';
import { ProjectCard } from './ProjectCard';
import clsx from 'clsx';

interface BoardColumnProps {
  id: string;
  title: string;
  projects: Project[];
  color: 'blue' | 'yellow' | 'green';
  onStartPomodoro: (id: number) => void;
  onPausePomodoro: (id: number) => void;
  onStopPomodoro: (id: number) => void;
  onDelete: (id: number) => void;
  activeTimerId: number | null;
}

const colorClasses = {
  blue: 'border-blue-500 text-blue-700 dark:text-blue-300',
  yellow: 'border-yellow-500 text-yellow-700 dark:text-yellow-300',
  green: 'border-green-500 text-green-700 dark:text-green-300',
};

export function BoardColumn({
  id,
  title,
  projects,
  color,
  onStartPomodoro,
  onPausePomodoro,
  onStopPomodoro,
  onDelete,
  activeTimerId,
}: BoardColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id });

  return (
    <div className="flex flex-col h-full min-w-[280px] max-w-[320px]">
      {/* Column Header */}
      <div className={clsx(
        'flex items-center justify-between px-3 py-2 mb-2 rounded-t-lg border-t-4',
        colorClasses[color],
        'bg-gray-100 dark:bg-gray-800'
      )}>
        <h2 className="font-semibold text-sm uppercase tracking-wide">
          {title}
        </h2>
        <span className="px-2 py-0.5 bg-white dark:bg-gray-700 rounded-full text-xs font-bold">
          {projects.length}
        </span>
      </div>

      {/* Droppable Area */}
      <div
        ref={setNodeRef}
        className={clsx(
          'flex-1 px-2 py-2 rounded-b-lg overflow-y-auto',
          'bg-gray-50 dark:bg-gray-900/50',
          isOver && 'bg-blue-50 dark:bg-blue-900/20 ring-2 ring-blue-400',
          'min-h-[200px]'
        )}
      >
        <SortableContext items={projects.map(p => p.id)} strategy={verticalListSortingStrategy}>
          {projects.length === 0 ? (
            <div className="text-center py-8 text-gray-400 dark:text-gray-600 text-sm">
              {id === 'todo' && 'No tasks yet'}
              {id === 'doing' && 'Start working on a task'}
              {id === 'done' && 'Complete tasks to see them here'}
            </div>
          ) : (
            projects.map((project) => (
              <ProjectCard
                key={project.id}
                project={project}
                onStartPomodoro={onStartPomodoro}
                onPausePomodoro={onPausePomodoro}
                onStopPomodoro={onStopPomodoro}
                onDelete={onDelete}
                activeTimerId={activeTimerId}
              />
            ))
          )}
        </SortableContext>
      </div>

      {/* Add card button */}
      <button className="mt-2 px-3 py-2 text-left text-sm text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 rounded transition-colors">
        + Add a card
      </button>
    </div>
  );
}
