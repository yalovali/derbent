# GitHub Workflows

This directory contains GitHub Actions workflows for the Derbent project.

## AI Code Review Workflow

The `ai-code-review.yml` workflow provides automated AI-powered code reviews for pull requests using OpenAI's GPT-4o model.

### Features

- Automatically reviews pull requests when they are opened, reopened, or synchronized
- Provides feedback on Java, Spring Boot, Vaadin, and security best practices
- Posts review comments directly on the pull request
- Gracefully handles errors and missing configuration

### Configuration

The workflow is **optional** and will gracefully skip AI reviews if not configured. To enable AI code reviews:

1. **Obtain an OpenAI API Key**
   - Go to https://platform.openai.com/api-keys
   - Create a new API key
   - Ensure you have credits/quota available

2. **Add the Secret to GitHub**
   - Go to your repository settings
   - Navigate to **Settings > Secrets and variables > Actions**
   - Click **New repository secret**
   - Name: `OPENAI_API_KEY`
   - Value: Your OpenAI API key
   - Click **Add secret**

3. **Verify Configuration**
   - Create or update a pull request
   - Check the Actions tab to see the workflow run
   - If configured correctly, you'll see an AI review comment on the PR
   - If not configured, you'll see a message indicating the API key is not set

### Behavior

The workflow will **never fail** your CI/CD pipeline. It handles all error conditions gracefully:

- ✅ **Missing API Key**: Workflow succeeds with a skip message
- ✅ **API Call Failure**: Workflow succeeds with an error message
- ✅ **Quota Exceeded**: Workflow succeeds with a quota exceeded message
- ✅ **Invalid Response**: Workflow succeeds with a parsing error message

### Disabling the Workflow

If you don't want the workflow to run at all, you can:

1. Delete the workflow file
2. Or, disable it in the GitHub UI:
   - Go to **Actions** tab
   - Click on **AI Code Review (Derbent)**
   - Click the **...** menu
   - Select **Disable workflow**

### Manual Triggering

You can manually trigger the workflow using the **workflow_dispatch** event:

1. Go to the **Actions** tab
2. Select **AI Code Review (Derbent)**
3. Click **Run workflow**
4. Select the branch and click **Run workflow**

### Cost Considerations

- Each API call costs approximately $0.01-0.05 depending on the size of the diff
- The workflow truncates diffs to 60,000 characters to control costs
- Consider your OpenAI quota and billing limits when enabling this feature

### Troubleshooting

**Problem**: Workflow runs but no comment appears on PR

**Solution**: Check the workflow logs in the Actions tab. Look for:
- HTTP error codes (401 = invalid key, 429 = quota exceeded, 500 = server error)
- Permission issues with the GitHub token
- API response parsing errors

**Problem**: Getting rate limited

**Solution**: OpenAI has rate limits. Consider:
- Reducing the number of PRs or commits
- Upgrading your OpenAI plan
- The workflow already handles rate limits gracefully

**Problem**: Comments are too short or incomplete

**Solution**: The `max_tokens` parameter in the workflow controls response length. You can increase it, but this will increase costs.
