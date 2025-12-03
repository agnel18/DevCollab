import { Project } from '../types';

const API_BASE = '/api';

export const api = {
  // Projects
  getAllProjects: async (): Promise<Project[]> => {
    const res = await fetch(`${API_BASE}/projects`);
    if (!res.ok) throw new Error('Failed to fetch projects');
    return res.json();
  },

  getProject: async (id: number): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects/${id}`);
    if (!res.ok) throw new Error('Failed to fetch project');
    return res.json();
  },

  createProject: async (project: Partial<Project>): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(project),
    });
    if (!res.ok) throw new Error('Failed to create project');
    return res.json();
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

  stopPomodoro: async (id: number): Promise<Project> => {
    const res = await fetch(`${API_BASE}/projects/${id}/pomodoro/stop`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to stop pomodoro');
    return res.json();
  },
};
