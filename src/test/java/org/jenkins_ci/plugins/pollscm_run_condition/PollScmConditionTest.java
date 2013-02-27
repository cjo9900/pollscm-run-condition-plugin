/*
 * The MIT License
 *
 * Copyright (C) 2013 by Chris Johnson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package  org.jenkins_ci.plugins.pollscm_run_condition;


import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.StreamBuildListener;
import hudson.scm.PollingResult;
import java.nio.charset.Charset;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

public class PollScmConditionTest extends HudsonTestCase {
    @Test
    public void testBUILD_NOW() throws Exception {
     FreeStyleProject thisProject = createFreeStyleProject();
     FreeStyleBuild build = thisProject.scheduleBuild2(0).get();

     FreeStyleProject projectA = createFreeStyleProject("projectA");
     projectA.setScm(new PollingResultSCM(PollingResult.BUILD_NOW));
     
     PollScmCondition condition = new PollScmCondition(projectA.getFullName());
     BuildListener bl = new StreamBuildListener(System.out, Charset.defaultCharset());
     
     assertTrue(condition.runPerform(build, bl));
    }
    @Test
    public void testSIGNIFICANT() throws Exception {
     FreeStyleProject thisProject = createFreeStyleProject();
     FreeStyleBuild build = thisProject.scheduleBuild2(0).get();

     FreeStyleProject projectA = createFreeStyleProject("projectA");
     projectA.setScm(new PollingResultSCM(PollingResult.SIGNIFICANT));
     
     PollScmCondition condition = new PollScmCondition(projectA.getFullName());
     BuildListener bl = new StreamBuildListener(System.out, Charset.defaultCharset());
     
     assertTrue(condition.runPerform(build, bl));
    }
    @Test
    public void testNO_CHANGES() throws Exception {
     FreeStyleProject thisProject = createFreeStyleProject();
     FreeStyleBuild build = thisProject.scheduleBuild2(0).get();

     FreeStyleProject projectA = createFreeStyleProject("projectA");
     projectA.setScm(new PollingResultSCM(PollingResult.NO_CHANGES));
     projectA.scheduleBuild2(0).get();
             
     PollScmCondition condition = new PollScmCondition(projectA.getFullName());
     BuildListener bl = new StreamBuildListener(System.out, Charset.defaultCharset());
     
     assertFalse(condition.runPerform(build, bl));
    }
    
    @Test
    public void testDisabled() throws Exception {
     FreeStyleProject thisProject = createFreeStyleProject();
     FreeStyleBuild build = thisProject.scheduleBuild2(0).get();

     FreeStyleProject projectA = createFreeStyleProject("projectA");
     projectA.setScm(new PollingResultSCM(PollingResult.BUILD_NOW));
     projectA.disable();
     
     PollScmCondition condition = new PollScmCondition(projectA.getFullName());
     BuildListener bl = new StreamBuildListener(System.out, Charset.defaultCharset());
     
     assertFalse(condition.runPerform(build, bl));
    }
    
    @Test
    public void testNobuilds() throws Exception {
     FreeStyleProject thisProject = createFreeStyleProject();
     FreeStyleBuild build = thisProject.scheduleBuild2(0).get();

     FreeStyleProject projectA = createFreeStyleProject("projectA");
     projectA.setScm(new PollingResultSCM(PollingResult.NO_CHANGES));
     
     PollScmCondition condition = new PollScmCondition(projectA.getFullName());
     BuildListener bl = new StreamBuildListener(System.out, Charset.defaultCharset());
     
     assertTrue(condition.runPerform(build, bl));
    }
    @Test
    public void testNoproject() throws Exception {
     FreeStyleProject thisProject = createFreeStyleProject();
     FreeStyleBuild build = thisProject.scheduleBuild2(0).get();
   
     PollScmCondition condition = new PollScmCondition("");
     BuildListener bl = new StreamBuildListener(System.out, Charset.defaultCharset());
     
     assertFalse(condition.runPerform(build, bl));
    }
    
    
}

