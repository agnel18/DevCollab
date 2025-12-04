import { Project, Board, BoardColumn } from '../types';

const API_BASE = '/api';

export const api = {
  // Boards
  getBoards: async (): Promise<Board[]> => {
    const res = await fetch(`${API_BASE}/boards`);
    if (!res.ok) throw new Error('Failed to fetch boards');
    return res.json();
  },

  getBoard: async (id: number): Promise<Board> => {
    const res = await fetch(`${API_BASE}/boards/${id}`);
    if (!res.ok) throw new Error('Failed to fetch board');
    return res.json();
  },

  createBoard: async (board: Partial<Board>): Promise<Board> => {
    const res = await fetch(`${API_BASE}/boards`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(board),
    });
    if (!res.ok) throw new Error('Failed to create board');
    return res.json();
  },

  updateBoard: async (id: number, updates: Partial<Board>): Promise<Board> => {
    const res = await fetch(`${API_BASE}/boards/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updates),
    });
    if (!res.ok) throw new Error('Failed to update board');
    return res.json();
  },

  deleteBoard: async (id: number): Promise<void> => {
    const res = await fetch(`${API_BASE}/boards/${id}`, {
      method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete board');
  },

  // Board Columns
  createColumn: async (boardId: number, column: Partial<BoardColumn>): Promise<BoardColumn> => {
    const res = await fetch(`${API_BASE}/boards/${boardId}/columns`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(column),
    });
    if (!res.ok) throw new Error('Failed to create column');
    return res.json();
  },

  updateColumn: async (boardId: number, columnId: number, updates: Partial<BoardColumn>): Promise<BoardColumn> => {
    const res = await fetch(`${API_BASE}/boards/${boardId}/columns/${columnId}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updates),
    });
    if (!res.ok) throw new Error('Failed to update column');
    return res.json();
  },

  deleteColumn: async (boardId: number, columnId: number): Promise<void> => {
    const res = await fetch(`${API_BASE}/boards/${boardId}/columns/${columnId}`, {
      method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete column');
  },

  // Projects
  getProjectsByBoard: async (boardId: number): Promise<Project[]> => {
    const res = await fetch(`${API_BASE}/projects/board/${boardId}`);
    if (!res.ok) throw new Error('Failed to fetch projects');
    return res.json();
  },

  getProject: async (id: number): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects/${id}`);
    if (!res.ok) throw new Error('Failed to fetch project');
    return res.json();
  },

  createProject: async (project: Partial<Project>): Promise<Project> => {
    console.log('API: Creating project with:', project);
    const res = await fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(project),
    });
    console.log('API: Response status:', res.status);
    if (!res.ok) {
      const errorText = await res.text();
      console.error('API: Error response:', errorText);
      throw new Error('Failed to create project');
    }
    const data = await res.json();
    console.log('API: Created project:', data);
    return data;
  },

  updateProject: async (id: number, updates: Partial<Project>): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects/${id}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(updates),
    });
    if (!res.ok) throw new Error('Failed to update project');
    return res.json();
  },

  deleteProject: async (id: number): Promise<void> => {
    const res = await fetch(`${API_BASE}/projects/${id}`, {
      method: 'DELETE',
    });
    if (!res.ok) throw new Error('Failed to delete project');
  },

  // Pomodoro
  startPomodoro: async (id: number): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects/${id}/pomodoro/start`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to start pomodoro');
    return res.json();
  },

  pausePomodoro: async (id: number): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects/${id}/pomodoro/pause`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to pause pomodoro');
    return res.json();
  },

  stopPomodoro: async (id: number): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects/${id}/pomodoro/stop`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to stop pomodoro');
    return res.json();
  },
};
