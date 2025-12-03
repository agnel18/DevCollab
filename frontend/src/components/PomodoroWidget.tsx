import { useState, useEffect, useRef } from 'react';
import { Project, PomodoroSettings } from '../types';
import { formatPomodoroTime, formatTimeHuman } from '../utils/helpers';
import clsx from 'clsx';

interface PomodoroWidgetProps {
  project: Project;
  onStart: () => void;
  onPause: () => void;
  onStop: () => void;
  isRunning: boolean;
  isOnlyActiveTimer: boolean;
}

const DEFAULT_SETTINGS: PomodoroSettings = {
  workDuration: 25,
  shortBreak: 5,
  longBreak: 15,
  roundsBeforeLongBreak: 4,
  soundEnabled: true,
  autoStartBreaks: false,
};

export function PomodoroWidget({ 
  project, 
  onStart, 
  onPause, 
  onStop, 
  isRunning,
  isOnlyActiveTimer 
}: PomodoroWidgetProps) {
  const [expanded, setExpanded] = useState(false);
  const [settings, setSettings] = useState<PomodoroSettings>(DEFAULT_SETTINGS);
  const [showSettings, setShowSettings] = useState(false);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const intervalRef = useRef<number>();

  // Calculate remaining time
  useEffect(() => {
    if (!isRunning || !project.pomodoroStart) {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
      return;
    }

    const updateTimer = () => {
      const start = new Date(project.pomodoroStart!).getTime();
      const now = Date.now();
      const elapsedSeconds = Math.floor((now - start) / 1000);
      const targetSeconds = (project.isBreak ? project.breakDuration : project.pomodoroDuration) * 60;
      const remaining = Math.max(0, targetSeconds - elapsedSeconds);
      setRemainingSeconds(remaining);

      // Notify when complete
      if (remaining === 0 && settings.soundEnabled) {
        playNotificationSound();
      }
    };

    updateTimer();
    intervalRef.current = window.setInterval(updateTimer, 1000);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isRunning, project.pomodoroStart, project.pomodoroDuration, project.breakDuration, project.isBreak, settings.soundEnabled]);

  const playNotificationSound = () => {
    try {
      const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBCh+z/LZhzYIHGSt6+OJOggQT6fg7r58IAMPZr7s56xWFQlXsObxs2kbBDBwzPLTgisFJHfJ8N2MOw==');
      audio.play().catch(() => {});
    } catch(e) {
      // Silence errors
    }
  };

  const progress = isRunning && project.pomodoroStart ? 
    (1 - (remainingSeconds / ((project.isBreak ? project.breakDuration : project.pomodoroDuration) * 60))) * 100 : 0;

  // Collapsed state - just icon + time
  if (!expanded && !isRunning) {
    return (
      <div
        onClick={() => setExpanded(true)}
        className="flex items-center gap-2 px-2 py-1 text-xs text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700 rounded cursor-pointer transition-colors"
      >
        <span className="text-sm">⏱</span>
        <span>{formatTimeHuman(project.totalSecondsSpent)}</span>
      </div>
    );
  }

  // Expanded state - show full controls
  return (
    <div className="relative bg-gradient-to-r from-green-50 to-blue-50 dark:from-green-900/20 dark:to-blue-900/20 rounded-lg p-2 border border-green-200 dark:border-green-700"
      style={{ maxWidth: '140px', maxHeight: '56px' }}>
      
      {/* Progress circle + timer */}
      <div className="flex items-center gap-2">
        {/* Circular progress */}
        {isRunning && (
          <div className="relative w-10 h-10">
            <svg className="w-full h-full -rotate-90">
              <circle
                cx="20"
                cy="20"
                r="16"
                fill="none"
                stroke="currentColor"
                strokeWidth="3"
                className="text-gray-200 dark:text-gray-600"
              />
              <circle
                cx="20"
                cy="20"
                r="16"
                fill="none"
                stroke="currentColor"
                strokeWidth="3"
                strokeDasharray={`${2 * Math.PI * 16}`}
                strokeDashoffset={`${2 * Math.PI * 16 * (1 - progress / 100)}`}
                strokeLinecap="round"
                className={clsx(
                  'transition-all duration-1000',
                  project.isBreak ? 'text-blue-500' : 'text-green-500'
                )}
              />
            </svg>
            <div className="absolute inset-0 flex items-center justify-center text-[10px] font-bold">
              {formatPomodoroTime(remainingSeconds).split(':')[0]}
            </div>
          </div>
        )}

        {/* Controls */}
        <div className="flex flex-col gap-1 flex-1">
          <div className="flex items-center gap-1">
            {!isRunning ? (
              <button
                onClick={onStart}
                disabled={!isOnlyActiveTimer}
                className={clsx(
                  "px-1.5 py-0.5 rounded text-[10px] font-medium transition-colors",
                  isOnlyActiveTimer
                    ? "bg-green-500 text-white hover:bg-green-600"
                    : "bg-gray-300 text-gray-500 cursor-not-allowed"
                )}
                title={!isOnlyActiveTimer ? "Another timer is running" : "Start timer"}
              >
                ▶
              </button>
            ) : (
              <>
                <button
                  onClick={onPause}
                  className="px-1.5 py-0.5 bg-yellow-500 text-white rounded text-[10px] font-medium hover:bg-yellow-600 transition-colors"
                >
                  ❚❚
                </button>
                <button
                  onClick={onStop}
                  className="px-1.5 py-0.5 bg-red-500 text-white rounded text-[10px] font-medium hover:bg-red-600 transition-colors"
                >
                  ⏹
                </button>
              </>
            )}
            
            {/* Settings */}
            <div className="relative">
              <button
                onClick={() => setShowSettings(!showSettings)}
                className="px-1.5 py-0.5 bg-gray-200 dark:bg-gray-700 rounded text-[10px] hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
              >
                ⚙
              </button>
              
              {showSettings && (
                <div className="absolute left-0 bottom-full mb-1 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 p-2 z-50 w-48 text-xs">
                  <div className="font-semibold mb-2 text-gray-700 dark:text-gray-300">Pomodoro Settings</div>
                  
                  <label className="block mb-1">
                    <span className="text-gray-600 dark:text-gray-400">Work:</span>
                    <select
                      value={settings.workDuration}
                      onChange={(e) => setSettings({...settings, workDuration: Number(e.target.value)})}
                      className="ml-2 px-1 py-0.5 border rounded dark:bg-gray-700 dark:border-gray-600"
                    >
                      <option value={15}>15m</option>
                      <option value={25}>25m</option>
                      <option value={45}>45m</option>
                      <option value={60}>60m</option>
                    </select>
                  </label>
                  
                  <label className="block mb-1">
                    <span className="text-gray-600 dark:text-gray-400">Short Break:</span>
                    <select
                      value={settings.shortBreak}
                      onChange={(e) => setSettings({...settings, shortBreak: Number(e.target.value)})}
                      className="ml-2 px-1 py-0.5 border rounded dark:bg-gray-700 dark:border-gray-600"
                    >
                      <option value={5}>5m</option>
                      <option value={10}>10m</option>
                      <option value={15}>15m</option>
                    </select>
                  </label>
                  
                  <label className="block mb-1">
                    <span className="text-gray-600 dark:text-gray-400">Long Break:</span>
                    <select
                      value={settings.longBreak}
                      onChange={(e) => setSettings({...settings, longBreak: Number(e.target.value)})}
                      className="ml-2 px-1 py-0.5 border rounded dark:bg-gray-700 dark:border-gray-600"
                    >
                      <option value={15}>15m</option>
                      <option value={20}>20m</option>
                      <option value={30}>30m</option>
                    </select>
                  </label>
                  
                  <label className="flex items-center gap-2 mb-1">
                    <input
                      type="checkbox"
                      checked={settings.soundEnabled}
                      onChange={(e) => setSettings({...settings, soundEnabled: e.target.checked})}
                      className="rounded"
                    />
                    <span className="text-gray-600 dark:text-gray-400">Sound</span>
                  </label>
                  
                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={settings.autoStartBreaks}
                      onChange={(e) => setSettings({...settings, autoStartBreaks: e.target.checked})}
                      className="rounded"
                    />
                    <span className="text-gray-600 dark:text-gray-400">Auto-start breaks</span>
                  </label>
                </div>
              )}
            </div>
          </div>
          
          {/* Time display */}
          <div className="text-[9px] text-gray-500 dark:text-gray-400">
            {formatTimeHuman(project.totalSecondsSpent)} total
          </div>
        </div>
      </div>

      {/* Close button when expanded and not running */}
      {expanded && !isRunning && (
        <button
          onClick={() => setExpanded(false)}
          className="absolute -top-1 -right-1 w-4 h-4 bg-gray-200 dark:bg-gray-700 rounded-full text-[10px] flex items-center justify-center hover:bg-gray-300 dark:hover:bg-gray-600"
        >
          ×
        </button>
      )}
    </div>
  );
}
