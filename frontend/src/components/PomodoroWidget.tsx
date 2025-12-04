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
  const [settings, setSettings] = useState<PomodoroSettings>(DEFAULT_SETTINGS);
  const [showSettings, setShowSettings] = useState(false);
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [elapsedSeconds, setElapsedSeconds] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const intervalRef = useRef<number>();
  const settingsRef = useRef<HTMLDivElement>(null);

  // Initialize remaining seconds from project duration
  useEffect(() => {
    const target = (project.isBreak ? project.breakDuration : project.pomodoroDuration) * 60;
    setRemainingSeconds(target);
  }, [project.pomodoroDuration, project.breakDuration, project.isBreak]);

  // Track paused state
  useEffect(() => {
    setIsPaused(!isRunning && remainingSeconds > 0 && remainingSeconds < ((project.isBreak ? project.breakDuration : project.pomodoroDuration) * 60));
  }, [isRunning, remainingSeconds, project]);

  // Timer countdown logic - PAUSED/RESUMED timer stays at current position
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
      const elapsedSecs = Math.floor((now - start) / 1000);
      const targetSeconds = (project.isBreak ? project.breakDuration : project.pomodoroDuration) * 60;
      const remaining = Math.max(0, targetSeconds - elapsedSecs);
      
      setElapsedSeconds(elapsedSecs);
      setRemainingSeconds(remaining);

      // Notify when complete
      if (remaining === 0 && settings.soundEnabled) {
        playNotificationSound();
      }
    };

    updateTimer();
    intervalRef.current = window.setInterval(updateTimer, 100);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [isRunning, project.pomodoroStart, project.pomodoroDuration, project.breakDuration, project.isBreak, settings.soundEnabled]);

  // Close settings when clicking outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (settingsRef.current && !settingsRef.current.contains(e.target as Node)) {
        setShowSettings(false);
      }
    };

    if (showSettings) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [showSettings]);

  const playNotificationSound = () => {
    try {
      const audio = new Audio('data:audio/wav;base64,UklGRnoGAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQoGAACBhYqFbF1fdJivrJBhNjVgodDbq2EcBj+a2/LDciUFLIHO8tiJNwgZaLvt559NEAxQp+PwtmMcBjiR1/LMeSwFJHfH8N2QQAoUXrTp66hVFApGn+DyvmwhBCh+z/LZhzYIHGSt6+OJOggQT6fg7r58IAMPZr7s56xWFQlXsObxs2kbBDBwzPLTgisFJHfJ8N2MOw==');
      audio.play().catch(() => {});
    } catch(e) {
      // Silence errors
    }
  };

  const targetSeconds = (project.isBreak ? project.breakDuration : project.pomodoroDuration) * 60;
  const progress = isRunning ? ((elapsedSeconds / targetSeconds) * 100) : 0;

  return (
    <div className="flex flex-col gap-2 w-full">
      {/* Single row: Timer + Progress + Buttons */}
      <div className="flex items-center gap-2 bg-gradient-to-r from-green-50 to-blue-50 dark:from-green-900/20 dark:to-blue-900/20 rounded-lg px-3 py-2 border border-green-200 dark:border-green-700">
        {/* Timer display */}
        <div className="text-lg font-bold text-gray-800 dark:text-gray-100 whitespace-nowrap">
          {formatPomodoroTime(remainingSeconds)}
        </div>

        {/* Horizontal progress bar */}
        {isRunning && (
          <div className="flex-1 h-1.5 bg-gray-200 dark:bg-gray-600 rounded-full overflow-hidden">
            <div
              className={clsx(
                'h-full transition-all duration-100',
                project.isBreak ? 'bg-blue-500' : 'bg-green-500'
              )}
              style={{ width: `${progress}%` }}
            />
          </div>
        )}

        {/* Control buttons */}
        <div className="flex items-center gap-1">
          {!isRunning && !isPaused ? (
            <button
              onClick={onStart}
              disabled={!isOnlyActiveTimer}
              className={clsx(
                "w-6 h-6 rounded flex items-center justify-center text-xs font-medium transition-colors",
                isOnlyActiveTimer
                  ? "bg-green-500 text-white hover:bg-green-600"
                  : "bg-gray-300 text-gray-500 cursor-not-allowed"
              )}
              title={!isOnlyActiveTimer ? "Another timer is running" : "Start timer"}
            >
              ▶
            </button>
          ) : isPaused ? (
            <button
              onClick={onStart}
              className="w-6 h-6 bg-green-500 text-white rounded flex items-center justify-center text-xs font-medium hover:bg-green-600 transition-colors"
              title="Resume timer"
            >
              ▶
            </button>
          ) : (
            <button
              onClick={onPause}
              className="w-6 h-6 bg-yellow-500 text-white rounded flex items-center justify-center text-xs font-medium hover:bg-yellow-600 transition-colors"
              title="Pause timer"
            >
              ❚❚
            </button>
          )}

          {isRunning && (
            <button
              onClick={onStop}
              className="w-6 h-6 bg-red-500 text-white rounded flex items-center justify-center text-xs font-medium hover:bg-red-600 transition-colors"
              title="Stop timer and save time"
            >
              ⏹
            </button>
          )}

          {/* Settings button with modal */}
          <div className="relative" ref={settingsRef}>
            <button
              onClick={() => setShowSettings(!showSettings)}
              className="w-6 h-6 bg-gray-200 dark:bg-gray-700 rounded flex items-center justify-center text-xs hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
              title="Pomodoro settings"
            >
              ⚙
            </button>
            
            {showSettings && (
              <div className="fixed inset-0 z-[9998]" onClick={() => setShowSettings(false)} />
            )}

            {showSettings && (
              <div className="fixed left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 bg-white dark:bg-gray-800 rounded-lg shadow-2xl border border-gray-200 dark:border-gray-700 p-4 z-[9999] w-72 text-xs">
                <div className="font-semibold mb-3 text-gray-700 dark:text-gray-300">⏱ Timer Settings</div>
                
                <label className="block mb-3">
                  <span className="text-gray-600 dark:text-gray-400 block mb-1">Work Duration (1-99 min)</span>
                  <input
                    type="number"
                    value={settings.workDuration}
                    onChange={(e) => {
                      const val = Math.max(1, Math.min(99, Number(e.target.value) || 25));
                      setSettings({...settings, workDuration: val});
                    }}
                    min={1}
                    max={99}
                    className="w-full px-2 py-1.5 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </label>
                
                <label className="block mb-3">
                  <span className="text-gray-600 dark:text-gray-400 block mb-1">Short Break (1-99 min)</span>
                  <input
                    type="number"
                    value={settings.shortBreak}
                    onChange={(e) => {
                      const val = Math.max(1, Math.min(99, Number(e.target.value) || 5));
                      setSettings({...settings, shortBreak: val});
                    }}
                    min={1}
                    max={99}
                    className="w-full px-2 py-1.5 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </label>
                
                <label className="block mb-3">
                  <span className="text-gray-600 dark:text-gray-400 block mb-1">Long Break (1-99 min)</span>
                  <input
                    type="number"
                    value={settings.longBreak}
                    onChange={(e) => {
                      const val = Math.max(1, Math.min(99, Number(e.target.value) || 15));
                      setSettings({...settings, longBreak: val});
                    }}
                    min={1}
                    max={99}
                    className="w-full px-2 py-1.5 border rounded dark:bg-gray-700 dark:border-gray-600 dark:text-white"
                  />
                </label>
                
                <label className="flex items-center gap-2 mb-2.5">
                  <input
                    type="checkbox"
                    checked={settings.soundEnabled}
                    onChange={(e) => setSettings({...settings, soundEnabled: e.target.checked})}
                    className="rounded"
                  />
                  <span className="text-gray-600 dark:text-gray-400">🔔 Sound on complete</span>
                </label>
                
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={settings.autoStartBreaks}
                    onChange={(e) => setSettings({...settings, autoStartBreaks: e.target.checked})}
                    className="rounded"
                  />
                  <span className="text-gray-600 dark:text-gray-400">▶ Auto-start breaks</span>
                </label>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Total time spent */}
      <div className="text-center text-xs text-gray-500 dark:text-gray-400">
        Total: {formatTimeHuman(project.totalSecondsSpent)}
      </div>
    </div>
  );
}
