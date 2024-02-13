package hudson;

import static groovy.util.GroovyTestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FunctionalModelTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void testCreatedState() throws Exception {
        // Creating a job doesn't directly correlate to a Run's state since Runs represent builds.
        // However, creating a project and not triggering a build leaves it without any Runs.
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        assertNull("Project should not have any builds yet", project.getFirstBuild());
    }

    @Test
    public void testQueuedState() throws Exception {
        // Direct testing of the "Queued" state is challenging since it transitions quickly to "Building".
        // This test won't directly capture a "Queued" state but demonstrates setup for potential queued scenarios.
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.scheduleBuild2(0);
        // Use JenkinsRule to wait for the build to start to ensure it was indeed queued.
        jenkinsRule.waitUntilNoActivity();
        assertNotNull("Project should have a build after being queued", project.getFirstBuild());
    }

    @Test
    public void testRunningState() throws Exception {
        // To capture a build in a "Running" state, we might use a build step that pauses the build.
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(new hudson.tasks.Shell("#!sleep 30"));

        FreeStyleBuild build = project.scheduleBuild2(0).waitForStart();
        assertTrue("Build should be in progress", build.isBuilding());
    }

    @Test
    public void testSuccessState() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        FreeStyleBuild build = jenkinsRule.buildAndAssertSuccess(project);
        assertEquals("Build should be successful", Result.SUCCESS, build.getResult());
    }

    @Test
    public void testFailedState() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getBuildersList().add(new hudson.tasks.Shell("exit 1"));
        FreeStyleBuild build = jenkinsRule.assertBuildStatus(Result.FAILURE, project.scheduleBuild2(0).get());
        assertEquals("Build should fail", Result.FAILURE, build.getResult());
    }

    @Test
    public void testAbortedState() throws Exception {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        // Adjust the build step to ensure it does not fail on its own
        project.getBuildersList().add(new hudson.tasks.Shell("sleep 60"));

        FreeStyleBuild build = project.scheduleBuild2(0).waitForStart();

        // Wait a bit to ensure the build has properly started
        Thread.sleep(10000); // 10 seconds

        // Check if the build is in progress and not already completed or failed
        if (build.isBuilding()) {
            // Attempt to abort the build
            build.getExecutor().interrupt(Result.ABORTED);
        } else {
            // If the build is not building, log or handle this condition
            System.out.println("Build was not in progress at the time of abort attempt");
        }

        // Wait for all activities to complete
        jenkinsRule.waitUntilNoActivity();

        // Assert the expected build result
        assertEquals("Expected the build to be aborted", Result.ABORTED, build.getResult());
    }
}