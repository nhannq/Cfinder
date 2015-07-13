/**
 * Copyright (c) 2014, 2015 Nhan Nguyen.
 *
 * This file is part of Cfinder.
 *
 * Cfinder is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Cfinder is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with Cfinder. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package edu.uconn.cse.cfinder.handlers;

import java.io.FileWriter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageFragmentRootContainer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.internal.Workbench;

import edu.uconn.cse.ccg.core.CfinderEntryPoint;

/**
 * Our sample action implements workbench action delegate. The action proxy will be created by the
 * workbench and shown in the UI. When the user tries to use the action, this delegate will be
 * created and execution will be delegated to it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class CfinderAction implements IWorkbenchWindowActionDelegate {
  // private IWorkbenchWindow window;
  private Shell shell;
  FileWriter f;
  IJavaProject targetProject;
  IPackageFragment[] packages;
  InputDialog dialog;
  String eclipseDirectoryPath;

  /**
   * The constructor.
   */
  public CfinderAction() {}

  public static IProject getCurrentProject() {
    ISelectionService selectionService =
        Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();

    ISelection selection = selectionService.getSelection();

    IProject project = null;
    if (selection instanceof IStructuredSelection) {
      Object element = ((IStructuredSelection) selection).getFirstElement();

      if (element instanceof IResource) {
        project = ((IResource) element).getProject();
      } else if (element instanceof PackageFragmentRootContainer) {
        IJavaProject jProject = ((PackageFragmentRootContainer) element).getJavaProject();
        project = jProject.getProject();
      } else if (element instanceof IJavaElement) {
        IJavaProject jProject = ((IJavaElement) element).getJavaProject();
        project = jProject.getProject();
      }
    }
    return project;
  }

  /**
   * The action has been activated. The argument of the method represents the 'real' action sitting
   * in the workbench UI.
   * 
   * @see IWorkbenchWindowActionDelegate#run
   */

  public void run(IAction action) {
    try {
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IWorkspaceRoot root = workspace.getRoot();
      eclipseDirectoryPath = Platform.getInstallLocation().getURL().getPath();
      // Get all projects in the workspace
      IProject[] projects = root.getProjects();
      IProject selectedProject = getCurrentProject();
      // printProjectInfo(selectedProject);
      targetProject = JavaCore.create(selectedProject);
      if (targetProject == null) {
        MessageDialog.openInformation(shell, "Error",
            "You need to select the project from the Package Explorer first");
        return;
      }
    } catch (Exception e) {

    }
    dialog =
        new InputDialog(shell, "Cfinder", "Input the version of the software.", "2.0.7",
            new IInputValidator() {

              @Override
              public String isValid(String newText) {
                // try {
                // int n = Integer.parseInt(newText);
                // if (n > 0) {
                // return null;
                // } else {
                // return "Requires positive number";
                // }
                // } catch (NumberFormatException e) {
                // return "Requires a number";
                // }
                return null;
              }
            });
    if (Window.OK == dialog.open()) {
      // if (!MessageDialog.openConfirm(
      // shell,
      // "Cfinder",
      // "You want to build CCG for the project "
      // + targetProject.getElementName() + " version "
      // + dialog.getValue())) {
      // return;
      // }
    } else {
      return;
    }
    Job job = new Job("Building CCG for " + targetProject.getElementName()) {
      @Override
      protected IStatus run(IProgressMonitor monitor) {
        try {
          String version = dialog.getValue();

          try {

            System.out.println(targetProject.getPath().toString() + " version " + version);
            packages = targetProject.getPackageFragments();
            CfinderEntryPoint entryPoint =
                new CfinderEntryPoint(targetProject, packages, version, eclipseDirectoryPath);
            entryPoint.run("");

          } catch (Exception e) {
            e.printStackTrace();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        // System.out.println("Doing something");

        // Use this to open a Shell in the UI thread
        Display.getDefault().asyncExec(new Runnable() {
          public void run() {
            MessageDialog.openInformation(shell, "Cfinder", "Building CCG has finished.");
          }
        });
        return Status.OK_STATUS;
      }

    };
    job.setUser(true);
    job.schedule();
  }

  /**
   * Selection in the workbench has been changed. We can change the state of the 'real' action here
   * if we want, but this can only happen after the delegate has been created.
   * 
   * @see IWorkbenchWindowActionDelegate#selectionChanged
   */
  public void selectionChanged(IAction action, ISelection selection) {}

  /**
   * We can use this method to dispose of any system resources we previously allocated.
   * 
   * @see IWorkbenchWindowActionDelegate#dispose
   */
  public void dispose() {}

  /**
   * We will cache window object in order to be able to provide parent shell for the message dialog.
   * 
   * @see IWorkbenchWindowActionDelegate#init
   */
  public void init(IWorkbenchWindow window) {
    // this.window = window;
  }
}
