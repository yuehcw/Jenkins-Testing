package hudson;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Result;
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

}
