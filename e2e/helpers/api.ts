import { APIRequestContext, Page, expect } from '@playwright/test';

/** Tests share one database and run in parallel, so every test uses its own names. */
export function uniqueName(prefix: string): string {
  return `${prefix} ${Date.now()}-${Math.random().toString(36).slice(2, 6)}`;
}

export async function createProject(request: APIRequestContext, name: string) {
  const response = await request.post('/api/projects', { data: { name } });
  expect(response.status()).toBe(201);
  return response.json();
}

export async function createTask(
  request: APIRequestContext,
  projectId: string,
  task: { title: string; status?: string; priority?: string; dueDate?: string },
) {
  const response = await request.post(`/api/projects/${projectId}/tasks`, { data: task });
  expect(response.status()).toBe(201);
  return response.json();
}

export function projectRow(page: Page, name: string) {
  return page.getByTestId('project-row').filter({ hasText: name });
}

export function taskRow(page: Page, title: string) {
  return page.getByTestId('task-row').filter({ hasText: title });
}
