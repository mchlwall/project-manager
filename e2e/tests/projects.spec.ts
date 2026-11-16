import { test, expect } from '@playwright/test';
import { createProject, projectRow, uniqueName } from '../helpers/api';

test.describe('projects', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await expect(page).toHaveTitle('Project Manager');
  });

  test('duplicate name shows an error', async ({ page, request }) => {
    const name = uniqueName('Duplicate');
    await createProject(request, name);

    await page.getByTestId('project-name-input').fill(name);
    await page.getByTestId('create-project-button').click();

    await expect(page.getByTestId('message')).toHaveText('project name already exists');
  });

  test('blank name shows an error', async ({ page }) => {
    await page.getByTestId('project-name-input').fill('   ');
    await page.getByTestId('create-project-button').click();

    await expect(page.getByTestId('message')).toHaveText('name must not be blank');
  });

  test('rename a project', async ({ page, request }) => {
    const name = uniqueName('Before');
    const newName = uniqueName('After');
    await createProject(request, name);
    await page.reload();

    // Rename uses the browser's prompt box; answer it with the new name
    page.once('dialog', (dialog) => dialog.accept(newName));
    await projectRow(page, name).getByTestId('rename-project-button').click();

    await expect(projectRow(page, newName)).toBeVisible();
    await expect(projectRow(page, name)).toHaveCount(0);
  });

  test('delete a project', async ({ page, request }) => {
    const name = uniqueName('Delete me');
    await createProject(request, name);
    await page.reload();

    // Delete asks for confirmation; Playwright dismisses dialogs unless told to accept
    page.once('dialog', (dialog) => dialog.accept());
    await projectRow(page, name).getByTestId('delete-project-button').click();

    await expect(projectRow(page, name)).toHaveCount(0);
  });
});
