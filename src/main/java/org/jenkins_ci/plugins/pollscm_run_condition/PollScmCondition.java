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
package org.jenkins_ci.plugins.pollscm_run_condition;

import hudson.Extension;
import hudson.Util;
import hudson.console.HyperlinkNote;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.Item;
import hudson.model.Job;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import hudson.util.FormValidation;
import hudson.util.LogTaskListener;
import java.io.IOException;
import java.util.List;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.jenkins_ci.plugins.run_condition.common.AlwaysPrebuildRunCondition;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

/**
 * Run condition to use the reason that the build was started, the actual
 * conditions are subclasses that can be extended using the BuildCauseCondition
 *
 * @author Chris Johnson
 */
@Extension
public class PollScmCondition extends AlwaysPrebuildRunCondition {

  private String projectName;

  /**
   * Data bound constructor taking a condition and exclusive cause
   *
   * @param project name of project to check for changes.
   */
  @DataBoundConstructor
  public PollScmCondition(String project) {
    this.projectName = project;
  }

  public PollScmCondition() {
    this.projectName = "";
  }

  /**
   * Returns the condition for the UI to display
   *
   * @return Condition that is current.
   */
  public String getProject() {
    return projectName;
  }

  /**
   * Performs the check of the condition and exclusiveCause.
   *
   * @return false if more than single cause for the build Otherwise the result
   * of the condition runPerform
   * @see BuildCauseCondition
   */
  @Override
  public boolean runPerform(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException {
    boolean result = false;
    String pn = null;
    //expand project name if possible
    try {
      pn = build.getEnvironment(listener).expand(projectName);
    } catch (IOException e) {
      listener.getLogger().println("Project variable expansion failed for " + projectName + "(false)");
      e.printStackTrace(listener.getLogger());
    }

    if (pn != null) {
      AbstractProject project = Jenkins.getInstance().getItemByFullName(pn, AbstractProject.class);
      if (project != null) {
        if (!project.isBuildable()) {
          //project disabled don't poll
          listener.getLogger().println("Project " + HyperlinkNote.encodeTo('/' + project.getUrl(), project.getFullDisplayName()) + " is disabled. (false)");
        } else {
          //project enabled look for SCM
          SCM scm = project.getScm();

          if (scm != null) {
            // Check that we don't have change in SCM
            PollingResult res = project.poll(LogTaskListener.NULL);

            if (res != PollingResult.NO_CHANGES) {
              listener.getLogger().println("Project " + HyperlinkNote.encodeTo('/' + project.getUrl(), project.getFullDisplayName()) + " has changes. (true)");
              result = true;
            } else {
              // check project has built
              if (project.getLastBuild() == null) {
                listener.getLogger().println("Project " + HyperlinkNote.encodeTo('/' + project.getUrl(), project.getFullDisplayName()) + " has no current builds. (true)");
                result = true;
              }
            }
          } else {
            listener.getLogger().println("Project " + HyperlinkNote.encodeTo('/' + project.getUrl(), project.getFullDisplayName()) + " does not have a SCM configured. (false)");
          }
        }
      } else {
        listener.getLogger().println("No project with name " + pn + " (false)");
      }
    } else {
      listener.getLogger().println("Project name is invalid (false)");
    }
    return result;
  }

  @Extension
  public static class PollScmConditionDescriptor extends RunConditionDescriptor {

    @Override
    public String getDisplayName() {
      return Messages.PollScmCondition_DisplayName();
    }

    /**
     * Form validation method.
     */
    public FormValidation doCheckProject(@AncestorInPath Item project, @QueryParameter String value) {
      // Require CONFIGURE permission on this project
      if (!project.hasPermission(Item.CONFIGURE)) {
        return FormValidation.ok();
      }
      String projectName = Util.fixNull(value);
      if (StringUtils.isNotBlank(projectName)) {
        Item item = Jenkins.getInstance().getItem(projectName, project, Item.class); // only works after version 1.410
        if (item == null) {
          return FormValidation.error(hudson.tasks.Messages.BuildTrigger_NoSuchProject(projectName, AbstractProject.findNearest(projectName).getName()));
        }
        if (!(item instanceof AbstractProject)) {
          return FormValidation.error(hudson.tasks.Messages.BuildTrigger_NotBuildable(projectName));
        }
      } else {
        return FormValidation.error("No project specified");
      }
      return FormValidation.ok();
    }

    /**
     * Autocompletion method
     *
     * @param value
     * @return
     */
    public AutoCompletionCandidates doAutoCompleteProject(@QueryParameter String value) {
      AutoCompletionCandidates candidates = new AutoCompletionCandidates();
      List<Job> jobs = Hudson.getInstance().getItems(Job.class);
      for (Job job : jobs) {
        if (job.getFullName().startsWith(value)) {
          if (job.hasPermission(Item.READ)) {
            candidates.add(job.getFullName());
          }
        }
      }
      return candidates;
    }
  }
}
