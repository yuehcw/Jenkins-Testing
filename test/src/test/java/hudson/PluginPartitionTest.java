package hudson;

import static hudson.cli.CLICommandInvoker.Matcher.succeeded;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import hudson.cli.CLICommandInvoker;
import hudson.cli.EnablePluginCommand;
import hudson.cli.EnablePluginCommandTest;
import hudson.cli.InstallPluginCommand;
import java.io.File;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.WithPlugin;

public class PluginPartitionTest {
    
    @Rule public JenkinsRule r = PluginManagerUtil.newJenkinsRule();

    private CLICommandInvoker.Result installTestPlugin(String name) {
        return new CLICommandInvoker(r, new InstallPluginCommand())
                .withStdin(EnablePluginCommandTest.class.getResourceAsStream("/plugins/" + name + ".hpi"))
                .invokeWithArgs("-name", name, "-deploy", "=");
    }

    private CLICommandInvoker.Result enablePlugins(String... names) {
        return new CLICommandInvoker(r, new EnablePluginCommand()).invokeWithArgs(names);
    }

    private void assertPluginEnabled(String name) {
        PluginWrapper plugin = r.getPluginManager().getPlugin(name);
        assertThat(plugin, is(notNullValue()));
        assertTrue(plugin.isEnabled());
    }

    private void disablePlugin(String name) throws IOException {
        PluginWrapper plugin = r.getPluginManager().getPlugin(name);
        assertThat(plugin, is(notNullValue()));
        plugin.disable();
    }

    private void assertPluginDisabled(String name) {
        PluginWrapper plugin = r.getPluginManager().getPlugin(name);
        assertThat(plugin, is(notNullValue()));
        assertFalse(plugin.isEnabled());
    }

    @Test
    public void testEnablelugin() throws IOException {
        String name = "token-macro";
        PluginManager m = r.getPluginManager();
        assertThat(m.getPlugin(name), is(nullValue()));
        assertThat(installTestPlugin(name), succeeded());
        assertPluginEnabled(name);
        disablePlugin(name);
        assertPluginDisabled(name);
        assertThat(enablePlugins(name), succeeded());
        assertPluginEnabled(name);
    }

    @WithPlugin("dependee.hpi")
    @Test public void testDisablePlugin() throws Exception {
        PluginWrapper pw = r.jenkins.pluginManager.getPlugin("dependee");
        assertNotNull(pw);

        pw.doMakeDisabled();

        File disabledHpi = new File(r.jenkins.getRootDir(), "plugins/dependee.hpi.disabled");
        assertFalse(disabledHpi.exists());
    } 
}