import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Project } from '../types';
import { ProjectCard } from './ProjectCard';
import clsx from 'clsx';

interface BoardColumnProps {
  id: number;
  title: string;
  projects: Project[];
  color?: string;
  onStartPomodoro: (id: number) => void;
  onPausePomodoro: (id: number) => void;
  onStopPomodoro: (id: number) => void;
  onDelete: (id: number) => void;
  onDeleteColumn: (id: number) => void;
  onAddCard: () => void;
  activeTimerId: number | null;
}

const colorClasses: Record<string, { header: string; bg: string }> = {
  red: {
    header: 'border-red-500 text-red-700 dark:text-red-300 bg-red-50 dark:bg-red-900/30',
    bg: 'bg-red-50/30 dark:bg-red-900/10'
  },
  orange: {
    header: 'border-orange-500 text-orange-700 dark:text-orange-300 bg-orange-50 dark:bg-orange-900/30',
    bg: 'bg-orange-50/30 dark:bg-orange-900/10'
  },
  green: {
    header: 'border-green-500 text-green-700 dark:text-green-300 bg-green-50 dark:bg-green-900/30',
    bg: 'bg-green-50/30 dark:bg-green-900/10'
  },
  blue: {
    header: 'border-blue-500 text-blue-700 dark:text-blue-300 bg-blue-50 dark:bg-blue-900/30',
    bg: 'bg-blue-50/30 dark:bg-blue-900/10'
  },
  yellow: {
    header: 'border-yellow-500 text-yellow-700 dark:text-yellow-300 bg-yellow-50 dark:bg-yellow-900/30',
    bg: 'bg-yellow-50/30 dark:bg-yellow-900/10'
  },
  purple: {
    header: 'border-purple-500 text-purple-700 dark:text-purple-300 bg-purple-50 dark:bg-purple-900/30',
    bg: 'bg-purple-50/30 dark:bg-purple-900/10'
  },
  indigo: {
    header: 'border-indigo-500 text-indigo-700 dark:text-indigo-300 bg-indigo-50 dark:bg-indigo-900/30',
    bg: 'bg-indigo-50/30 dark:bg-indigo-900/10'
  },
};

export function BoardColumn({
  id,
  title,
  projects,
  color = 'blue',
  onStartPomodoro,
  onPausePomodoro,
  onStopPomodoro,
  onDelete,
  onDeleteColumn,
  onAddCard,
  activeTimerId,
}: BoardColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id });

  return (
    <div className="flex flex-col h-full min-w-[280px] max-w-[320px]">
      {/* Column Header */}
      <div className={clsx(
        'flex items-center justify-between px-3 py-2 mb-2 rounded-t-lg border-t-4 group',
        colorClasses[color]?.header || colorClasses.blue.header
      )}>
        <h2 className="font-semibold text-sm uppercase tracking-wide truncate">
          {title}
        </h2>
        <div className="flex items-center gap-1">
          <span className="px-2 py-0.5 bg-white dark:bg-gray-700 rounded-full text-xs font-bold flex-shrink-0">
            {projects.length}
          </span>
          <button
            onClick={() => onDeleteColumn(id)}
            className="opacity-0 group-hover:opacity-100 px-2 py-0.5 bg-red-500 text-white rounded text-xs font-bold hover:bg-red-600 transition"
            title="Delete this list"
          >
            ✕
          </button>
        </div>
      </div>

      {/* Droppable Area */}
      <div
        ref={setNodeRef}
        className={clsx(
          'flex-1 px-2 py-2 rounded-b-lg overflow-y-auto',
          colorClasses[color]?.bg || colorClasses.blue.bg,
          isOver && 'ring-2 ring-offset-2 ring-blue-400',
          'min-h-[200px]'
        )}
      >
        <SortableContext items={projects.map(p => p.id)} strategy={verticalListSortingStrategy}>
          {projects.length === 0 ? (
            <div className="text-center py-8 text-gray-400 dark:text-gray-600 text-sm">
              No tasks yet
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
      <button 
        onClick={onAddCard}
        className="mt-2 px-3 py-2 text-center text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 dark:bg-blue-700 dark:hover:bg-blue-600 rounded transition-colors w-full"
        title="Create a new card in this list"
      >
        + Add Card
      </button>
    </div>
  );
}
