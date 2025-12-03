import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Project } from '../types';
import { PomodoroWidget } from './PomodoroWidget';
import clsx from 'clsx';
import { formatTimeHuman } from '../utils/helpers';

interface ProjectCardProps {
  project: Project;
  onStartPomodoro: (id: number) => void;
  onPausePomodoro: (id: number) => void;
  onStopPomodoro: (id: number) => void;
  onDelete: (id: number) => void;
  activeTimerId: number | null;
}

export function ProjectCard({ 
  project, 
  onStartPomodoro, 
  onPausePomodoro,
  onStopPomodoro,
  onDelete,
  activeTimerId 
}: ProjectCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: project.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  const isRunning = !!project.pomodoroStart;
  const isOnlyActiveTimer = activeTimerId === null || activeTimerId === project.id;

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      className={clsx(
        'bg-white dark:bg-gray-800 rounded-lg p-3 mb-2 cursor-grab active:cursor-grabbing',
        'card-shadow hover:card-shadow-hover transition-shadow',
        isDragging && 'opacity-50 rotate-3',
        project.status === 'DOING' && 'ring-2 ring-yellow-400',
        project.status === 'DONE' && 'ring-2 ring-green-400'
      )}
    >
      {/* Header */}
      <div className="flex items-start justify-between mb-2">
        <h3 className="font-medium text-sm text-gray-900 dark:text-gray-100 flex-1">
          {project.name}
        </h3>
        {isRunning && (
          <span className="ml-2 px-2 py-0.5 bg-green-100 dark:bg-green-900 text-green-700 dark:text-green-300 text-[10px] font-semibold rounded-full">
            {project.isBreak ? '☕ Break' : '🍅 Focus'}
          </span>
        )}
      </div>

      {/* Description */}
      {project.description && (
        <p className="text-xs text-gray-600 dark:text-gray-400 mb-3 line-clamp-2">
          {project.description}
        </p>
      )}

      {/* Subtasks preview */}
      {project.tasks && project.tasks.length > 0 && (
        <div className="mb-3 space-y-1">
          {project.tasks.flatMap(task => task.subtasks).slice(0, 2).map(subtask => (
            <div key={subtask.id} className="flex items-center gap-2 text-xs">
              <input
                type="checkbox"
                checked={subtask.completionPercentage === 100}
                readOnly
                className="rounded"
              />
              <span className={clsx(
                'flex-1',
                subtask.completionPercentage === 100 && 'line-through text-gray-400'
              )}>
                {subtask.name}
              </span>
              <span className="text-[10px] text-gray-500">
                {subtask.completedPomodoros}/{subtask.estimatedPomodoros}🍅
              </span>
            </div>
          ))}
          {project.tasks.flatMap(t => t.subtasks).length > 2 && (
            <div className="text-[10px] text-gray-500">
              +{project.tasks.flatMap(t => t.subtasks).length - 2} more
            </div>
          )}
        </div>
      )}

      {/* Labels/Tags */}
      <div className="flex items-center gap-1 mb-3 flex-wrap">
        {project.completedPomodoros !== undefined && project.estimatedPomodoros && (
          <span className="px-2 py-0.5 bg-purple-100 dark:bg-purple-900 text-purple-700 dark:text-purple-300 text-[10px] rounded-full">
            🍅 {project.completedPomodoros}/{project.estimatedPomodoros}
          </span>
        )}
        {project.currentCycle > 1 && (
          <span className="px-2 py-0.5 bg-blue-100 dark:bg-blue-900 text-blue-700 dark:text-blue-300 text-[10px] rounded-full">
            Cycle {project.currentCycle}/4
          </span>
        )}
      </div>

      {/* Pomodoro Widget - always at bottom */}
      <div className="border-t border-gray-200 dark:border-gray-700 pt-2">
        <PomodoroWidget
          project={project}
          onStart={() => onStartPomodoro(project.id)}
          onPause={() => onPausePomodoro(project.id)}
          onStop={() => onStopPomodoro(project.id)}
          isRunning={isRunning}
          isOnlyActiveTimer={isOnlyActiveTimer}
        />
      </div>

      {/* Action buttons for DONE status */}
      {project.status === 'DONE' && (
        <div className="mt-2 pt-2 border-t border-gray-200 dark:border-gray-700 flex items-center justify-between">
          <span className="text-xs text-green-600 dark:text-green-400">
            ✓ Completed in {formatTimeHuman(project.totalSecondsSpent)}
          </span>
          <button
            onClick={(e) => {
              e.stopPropagation();
              if (confirm('Delete this project?')) {
                onDelete(project.id);
              }
            }}
            className="text-xs text-red-600 hover:text-red-700 dark:text-red-400"
          >
            🗑
          </button>
        </div>
      )}
    </div>
  );
}
