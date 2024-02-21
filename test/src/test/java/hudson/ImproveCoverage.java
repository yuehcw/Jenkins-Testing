package hudson;


import static groovy.util.GroovyTestCase.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hudson.model.DownloadService;
import hudson.model.UpdateCenter;
import hudson.model.User;
import hudson.security.ACLContext;
import hudson.util.FormValidation;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import jenkins.ClassLoaderReflectionToolkit;
import jenkins.RestartRequiredException;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;
import org.jvnet.hudson.test.FlagRule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.recipes.WithPlugin;

public class ExtraTestCases {

    @Rule public JenkinsRule r = PluginManagerUtil.newJenkinsRule();
    @Rule public TemporaryFolder tmp = new TemporaryFolder();
    @Rule public FlagRule<Boolean> signatureCheck = new FlagRule<>(() -> DownloadService.signatureCheck, x -> DownloadService.signatureCheck = x);

    @Test
    @WithPlugin("legacy.hpi")
    public void noExistentPlugins() throws Exception {
        final UpdateCenter uc = Jenkins.get().getUpdateCenter();
        Assert.assertNull("This test requires the plugin with ID 'legacy' to not exist in update sites", uc.getPlugin("legacy"));

        Assert.assertSame(FormValidation.Kind.OK, uc.getSite("default").updateDirectlyNow().kind);
    }

    @WithPlugin("plugin-first.hpi")
    @Test
    public void findResourceAccessibility() {
        PluginWrapper w = r.jenkins.getPluginManager().getPlugin("plugin-first");
        assertNotNull(w);

        URL fromPlugin = w.classLoader.getResource("org/jenkinsci/plugins/pluginfirst/HelloWorldBuilder/config.jelly");
        assertNotNull(fromPlugin);

        URL fromToolkit = ClassLoaderReflectionToolkit._findResource(w.classLoader, "org/jenkinsci/plugins/pluginfirst/HelloWorldBuilder/config.jelly");

        assertEquals(fromPlugin, fromToolkit);
    }


    @Test
    public void  systemInInitializerRequirement() throws Exception {
        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());
        r.jenkins.setAuthorizationStrategy(new MockAuthorizationStrategy());
        String pluginShortName = "require-system-in-initializer";
        dynamicLoad(pluginShortName + ".jpi");
        try (ACLContext context = hudson.security.ACL.as2(User.getById("underprivileged", true).impersonate2())) {
            r.jenkins.pluginManager.start(List.of(r.jenkins.pluginManager.getPlugin(pluginShortName)));
        }
    }

    private void dynamicLoad(String plugin) throws IOException, InterruptedException, RestartRequiredException {
        PluginManagerUtil.dynamicLoad(plugin, r.jenkins);
    }

    private void dynamicLoadAndDisable(String plugin) throws IOException, InterruptedException, RestartRequiredException {
        PluginManagerUtil.dynamicLoad(plugin, r.jenkins, true);
    }
    
    @WithPlugin("htmlpublisher.jpi")
    @Test
    public void pluginListViaJSONApi() throws IOException {
        net.sf.json.JSONObject response = r.getJSON("pluginManager/plugins").getJSONObject();

        Assert.assertEquals("ok", response.getString("status"));
        JSONArray data = response.getJSONArray("data");
        assertThat(data, Matchers.not(Matchers.empty()));

        JSONObject pluginInfo = data.getJSONObject(0);
        Assert.assertNotNull(pluginInfo.getString("name"));
        Assert.assertNotNull(pluginInfo.getString("title"));
        Assert.assertNotNull(pluginInfo.getString("dependencies"));
    }

    @Test public void installFailPluginDisabledDependency() throws Exception {
        {
            dynamicLoadAndDisable("dependee-0.0.2.hpi");
        }

        {
            dynamicLoad("depender-0.0.2.hpi");
        }
        assertThrows(ClassNotFoundException.class, () -> r.jenkins.getExtensionList("org.jenkinsci.plugins.dependencytest.dependee.DependeeExtensionPoint"));
    }

    @Test public void pluginInstallDisabledOptionalDependency() throws Exception {
        {
            dynamicLoadAndDisable("dependee-0.0.2.hpi");
        }
        assertThrows(IOException.class, () -> dynamicLoad("mandatory-depender-0.0.2.hpi"));
    }
}