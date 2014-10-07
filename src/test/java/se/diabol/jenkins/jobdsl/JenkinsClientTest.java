package se.diabol.jenkins.jobdsl;

import hudson.model.FreeStyleProject;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.WithoutJenkins;

import static org.junit.Assert.*;

public class JenkinsClientTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testJobExists() throws Exception {
        jenkins.createFreeStyleProject("job");
        JenkinsClient client = new JenkinsClient(jenkins.getURL());
        assertTrue(client.itemExists("job", true));
        assertFalse(client.itemExists("job1", true));

        MockFolder folder = jenkins.createFolder("Folder");
        folder.createProject(FreeStyleProject.class, "job2");
        assertTrue(client.itemExists("Folder/job2", true));
        assertFalse(client.itemExists("Folder/job1", true));
    }


    @Test
    @Ignore
    public void testCreateUpdateJob() throws Exception {
        JenkinsClient client = new JenkinsClient(jenkins.getURL());
        String jobXml1 = convertStreamToString(this.getClass().getResourceAsStream("/job1.xml"));
        client.createItem("job1", jobXml1, true);
        FreeStyleProject job1 = (FreeStyleProject) jenkins.getInstance().getItem("job1");
        assertNotNull(job1);
        String jobXml2 = convertStreamToString(this.getClass().getResourceAsStream("/job2.xml"));
        client.updateItem("job1", jobXml2, true);
        FreeStyleProject job2 = (FreeStyleProject) jenkins.getInstance().getItem("job1");
        assertNotNull(job2);
        assertEquals("This is a job", job2.getDescription());
    }


    @Test
    @WithoutJenkins
    public void test() throws Exception {
        JenkinsClient client = new JenkinsClient(null);
        assertEquals("/job/Folder/", client.getFolderPath("Folder/Project"));
        assertEquals("/job/Folder/", client.getFolderPath("Folder/Project"));
        assertEquals("Project", client.getItemName("Folder/Project"));
        assertEquals("Project", client.getItemName("Project"));

    }

    private static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
