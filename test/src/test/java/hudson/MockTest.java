package hudson;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.TaskListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class MockTest {

    @Mock
    private AbstractBuild build; // Mock the AbstractBuild dependency

    @Test
    public void testGetResult() {
        // Stub the getResult() method to return FAILURE
        when(build.getResult()).thenReturn(Result.FAILURE);

        // Verify that the method returns the expected result
        assertEquals(Result.FAILURE, build.getResult());
    }

    @Test
    public void testGetProject() {
        // Create a mock instance of AbstractProject
        AbstractProject project = Mockito.mock(AbstractProject.class);

        // Stub the getProject() method to return the mock project
        Mockito.when(build.getProject()).thenReturn(project);

        // Verify that the method returns the expected project
        assertEquals(project, build.getProject());
    }

    @Test
    public void testGetEnvironment() throws IOException, InterruptedException {
        // Create a mock instance of AbstractBuild
        AbstractBuild build = Mockito.mock(AbstractBuild.class);

        // Create a mock instance of TaskListener
        TaskListener listener = Mockito.mock(TaskListener.class);

        // Create a sample environment (you can replace this with your actual data)
        Map<String, String> sampleEnvironment = new HashMap<>();
        sampleEnvironment.put("KEY1", "VALUE1");
        sampleEnvironment.put("KEY2", "VALUE2");

        // Stub the getEnvironment() method to return the sample environment
        when(build.getEnvironment(listener)).thenReturn(new hudson.EnvVars(sampleEnvironment));

        // Verify that the method returns the expected environment
        assertEquals("VALUE1", build.getEnvironment(listener).get("KEY1"));
        assertEquals("VALUE2", build.getEnvironment(listener).get("KEY2"));
    }
    @Test
    public void testGetWorkspace() {
        // Create a mock instance of FilePath (workspace)
        FilePath workspace = Mockito.mock(FilePath.class);

        // Stub the getWorkspace() method to return the mock workspace
        Mockito.when(build.getWorkspace()).thenReturn(workspace);

        // Verify that the method returns the expected workspace
        assertEquals(workspace, build.getWorkspace());
    }
}
