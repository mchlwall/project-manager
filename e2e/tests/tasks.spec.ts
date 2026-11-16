import { test, expect, Page } from '@playwright/test';
import { createProject, createTask, projectRow, taskRow, uniqueName } from '../helpers/api';

// Clicking Open again reloads the task list, e.g. after creating tasks through the API
async function openProject(page: Page, name: string) {
  await projectRow(page, name).getByTestId('open-project-button').click();
  await expect(page.getByTestId('selected-project-name')).toHaveText(name);
}

test.describe('tasks', () => {
  let projectId: string;
  let projectName: string;

  // Each test gets its own fresh project, opened in the UI
  test.beforeEach(async ({ page, request }) => {
    projectName = uniqueName('Tasks');
    projectId = (await createProject(request, projectName)).id;

    await page.goto('/');
    await openProject(page, projectName);
  });

  test('new task defaults to TODO and MEDIUM', async ({ page }) => {
    await page.getByTestId('task-title-input').fill('Write tests');
    await page.getByTestId('add-task-button').click();

    const row = taskRow(page, 'Write tests');
    await expect(row.getByTestId('task-status-select')).toHaveValue('TODO');
    await expect(row.getByTestId('task-priority')).toHaveText('MEDIUM');
  });

  test('blank title shows an error', async ({ page }) => {
    await page.getByTestId('add-task-button').click();

    await expect(page.getByTestId('message')).toHaveText('title must not be blank');
  });

  test('status change is saved', async ({ page, request }) => {
    await createTask(request, projectId, { title: 'Ship it' });
    await openProject(page, projectName);

    const saved = page.waitForResponse((response) => response.request().method() === 'PATCH');
    await taskRow(page, 'Ship it').getByTestId('task-status-select').selectOption('DONE');
    await saved;

    // Reload and reopen to prove it was saved, not just changed on screen
    await page.reload();
    await openProject(page, projectName);
    await expect(taskRow(page, 'Ship it').getByTestId('task-status-select')).toHaveValue('DONE');
  });

  test('status filter shows only matching tasks', async ({ page, request }) => {
    await createTask(request, projectId, { title: 'Finished task', status: 'DONE' });
    await createTask(request, projectId, { title: 'Open task' });

    await page.getByTestId('status-filter').selectOption('DONE');

    await expect(taskRow(page, 'Finished task')).toBeVisible();
    await expect(taskRow(page, 'Open task')).toHaveCount(0);
  });

  test('delete a task', async ({ page, request }) => {
    await createTask(request, projectId, { title: 'Remove me' });
    await openProject(page, projectName);

    await taskRow(page, 'Remove me').getByTestId('delete-task-button').click();

    await expect(taskRow(page, 'Remove me')).toHaveCount(0);
    await expect(page.getByTestId('no-tasks')).toBeVisible();
  });
});
