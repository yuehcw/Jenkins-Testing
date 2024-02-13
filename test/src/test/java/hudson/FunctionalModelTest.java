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

}