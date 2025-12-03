export interface Project {
  id: number;
  name: string;
  description?: string;
  status: 'TODO' | 'DOING' | 'DONE';
  estimatedPomodoros?: number;
  completedPomodoros?: number;
  totalSecondsSpent: number;
  pomodoroStart?: string;
  pomodoroDuration: number; // minutes
  breakDuration: number; // minutes
  isBreak: boolean;
  currentCycle: number;
  createdAt: string;
  completedAt?: string;
  tasks: Task[];
}

export interface Task {
  id: number;
  name: string;
  projectId: number;
  subtasks: Subtask[];
}

export interface Subtask {
  id: number;
  name: string;
  taskId: number;
  estimatedPomodoros?: number;
  completedPomodoros?: number;
  totalSecondsSpent: number;
  pomodoroStart?: string;
  currentCycle: number;
  completionPercentage: number;
}

export interface PomodoroSettings {
  workDuration: number; // minutes
  shortBreak: number;
  longBreak: number;
  roundsBeforeLongBreak: number;
  soundEnabled: boolean;
  autoStartBreaks: boolean;
}

export interface WebSocketMessage {
  action: 'MOVED' | 'TIMER_STARTED' | 'TIMER_STOPPED' | 'CREATED' | 'DELETED' | 'SUBTASK_ADDED';
  projectId: number;
  projectName: string;
  userName: string;
  userColor: string;
  status?: string;
}

export interface CursorPosition {
  userId: string;
  userName: string;
  userColor: string;
  x: number;
  y: number;
  projectId?: number;
}
