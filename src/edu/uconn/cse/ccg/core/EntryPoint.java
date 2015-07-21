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
package edu.uconn.cse.ccg.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author nhannguyen
 *
 */
public class EntryPoint implements IApplication {

  IJavaProject targetProject;
  IPackageFragment[] packages;

  void parseCassandraOptions10(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseCassandraOptions");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    IPath projectDotProjectFile =
        new Path("/home/nhannguyen/workspace/apache-cassandra-1.0.0-src" + "/.project");
    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();

          // ParseConfigurationInfo parseConfigurationInfo = new ParseCassandraConfigInfo();
          // parseConfigurationInfo.getConfigurationInfo();

          String source_path = "org/apache/cassandra";

          String fileName = "cassandra10";

          CassandraLikeCCG cassandraCCG =
              new CassandraLikeCCG("DatabaseDescriptor.java", fileName, eclipseDirectoryPath);
          String startClassName = "org.apache.cassandra.config.DatabaseDescriptor";
          cassandraCCG.constructCallGraph(packages, source_path, "cassandra.yaml", startClassName);

          // GetDataType getDataType = new CassandraDataType("DatabaseDescriptor.java");
          // getDataType.getDataType(packages, source_path);

          // javaFileObjectsMap = parserImpl.lightParse(packages, "DatabaseDescriptor.java",
          // source_path);
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          //
          // MethodLevelCCGGenerator genMethodLevelCCG = new MethodLevelCCGGenerator();

          // genMethodLevelCCG.generate(packages, "DatabaseDescriptor.",
          // "/apache-cassandra-2.0.7-src/src/java", source_path);

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }
  }

  /**
	 * 
	 */
  void parseCassandraOptions(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseCassandraOptions");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    IPath projectDotProjectFile =
        new Path("/home/nhannguyen/workspace/apache-cassandra-2.0.7-src" + "/.project");
    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();

          // ParseConfigurationInfo parseConfigurationInfo = new ParseCassandraConfigInfo();
          // parseConfigurationInfo.getConfigurationInfo();

          String source_path = "org/apache/cassandra";

          String fileName = "cassandra";

          CassandraLikeCCG cassandraCCG =
              new CassandraLikeCCG("DatabaseDescriptor.java", fileName, eclipseDirectoryPath);
          String startClassName = "org.apache.cassandra.config.DatabaseDescriptor";
          cassandraCCG.constructCallGraph(packages, source_path, "cassandra.yaml", startClassName);

          // GetDataType getDataType = new CassandraDataType("DatabaseDescriptor.java");
          // getDataType.getDataType(packages, source_path);

          // javaFileObjectsMap = parserImpl.lightParse(packages, "DatabaseDescriptor.java",
          // source_path);
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          //
          // MethodLevelCCGGenerator genMethodLevelCCG = new MethodLevelCCGGenerator();

          // genMethodLevelCCG.generate(packages, "DatabaseDescriptor.",
          // "/apache-cassandra-2.0.7-src/src/java", source_path);

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }
  }

  void parseHadoopOptions12(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseHadooCommonOptions");

    String projectSourcePath = "/home/nhannguyen/workspace/hadoop-1.2.1/";
    String fileName;
    String path;


    path = (new StringBuilder(projectSourcePath).append("/.project")).toString();

    IPath projectDotProjectFile = new Path(path);

    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();

          String source_path = "org/apache/hadoop";
          String oracleFolder = "/home/nhannguyen/Setup/eclipse/oracle/hadoop12";

          HadoopLikeCCG hadoopCCG =
              new HadoopLikeCCG("Configuration.java", oracleFolder, "hadoop12",
                  eclipseDirectoryPath);
          // hadoopCCG.createOracle(packages, source_path, fileName);
          // hadoopCCG.constructCallGraph(packages, source_path, "hadoop",
          // "org.apache.hadoop.conf.Configuration");

          // getDataType.getDataType(packages, source_path);

          // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          // System.out.println(Factory.getInstance().getSourcePath());
          // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
          // "org/apache/hadoop/conf/Configuration","");
          // convertToNewFormat();
          // parse("Configuration.", "/hadoop-common/src/main/java");

        }
      } catch (Exception e) {
        System.err.println("Stop here 22");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }
  }

  void parseHadoopOptions22(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseHadooCommonOptions Hadoop 2.2.0");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    String projectSourcePath = "/home/nhannguyen/workspace/hadoop-2.2.0-src/";
    String fileName;
    String path;

    HashMap<String, String> moduleInformation = new HashMap<String, String>();

    try {
      BufferedReader br = new BufferedReader(new FileReader("hadoopmoduleinformation22.txt"));
      try {
        String line = br.readLine();

        while (line != null) {
          String[] option = line.split(":");
          if (option.length == 2) {
            moduleInformation.put(option[1].trim(), option[0].trim());
            System.out.println(option[1].trim());
          }
          line = br.readLine();
        }
      } finally {
        br.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println(moduleInformation.size());
    for (String key : moduleInformation.keySet()) {

      fileName = key;

      System.out.println(moduleInformation.get(key) + " : " + fileName);

      path =
          (new StringBuilder(projectSourcePath).append(moduleInformation.get(key)).append(fileName)
              .append("/.project")).toString();

      IPath projectDotProjectFile = new Path(path);

      IProjectDescription projectDescription;

      try {
        projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
        IProject project = workspace.getRoot().getProject(projectDescription.getName());
        try {
          if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
            targetProject = JavaCore.create(project);
            packages = targetProject.getPackageFragments();

            String source_path = "org/apache/hadoop";
            String oracleFolder = "/home/nhannguyen/Setup/eclipse/oracle/hadoop22";

            HadoopLikeCCG hadoopCCG =
                new HadoopLikeCCG("Configuration.java", oracleFolder, fileName + "22",
                    eclipseDirectoryPath);
            // hadoopCCG.createOracle(packages, source_path, fileName);

            // hadoopCCG.constructCallGraph(packages, source_path, "hadoop",
            // "org.apache.hadoop.conf.Configuration");

            // getDataType.getDataType(packages, source_path);

            // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
            // parseApplication = new ParserApplication(javaFileObjectsMap);
            // System.out.println(Factory.getInstance().getSourcePath());
            // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
            // "org/apache/hadoop/conf/Configuration","");
            // convertToNewFormat();
            // parse("Configuration.", "/hadoop-common/src/main/java");

          }
        } catch (Exception e) {
          System.err.println("Stop here 2");
          e.printStackTrace();
        }
        // }

        // System.out.println(project.getName());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        System.err.println("Stop here");
        e.printStackTrace();
      }
    }
  }

  void parseHadoopOptions26(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseHadooCommonOptions 26");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    String projectSourcePath = "/home/nhannguyen/workspace/hadoop-2.6.0-src/";
    String fileName;
    String path;

    int moduleId = 0;

    // fileName = "hadoop-annotations"; //0
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-common-project/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-auth"; //0
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-common-project/").append(fileName).append("/.project")).toString();
    // //
    // fileName = "hadoop-common"; //189
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-common-project/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-kms"; //11
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-common-project/").append(fileName).append("/.project")).toString();
    // //
    // fileName = "hadoop-minikdc";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-common-project/").append(fileName).append("/.project")).toString();
    // //
    // fileName = "hadoop-nfs";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-common-project/").append(fileName).append("/.project")).toString();


    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-assemblies/").append("/.project")).toString();

    // fileName = "/hadoop-hdfs";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-hdfs-project/").append(fileName).append("/.project")).toString();

    // fileName = "/hadoop-hdfs-httpfs";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-hdfs-project/").append(fileName).append("/.project")).toString();

    // fileName = "/hadoop-hdfs-nfs";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-hdfs-project/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-mapreduce-client-app";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-mapreduce-project/hadoop-mapreduce-client/").append(fileName).append("/.project")).toString();
    //
    //
    // fileName = "hadoop-mapreduce-client-common";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-mapreduce-project/hadoop-mapreduce-client/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-mapreduce-client-core";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-mapreduce-project/hadoop-mapreduce-client/").append(fileName).append("/.project")).toString();
    // ////
    // fileName = "hadoop-mapreduce-client-hs";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-mapreduce-project/hadoop-mapreduce-client/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-mapreduce-client-hs-plugins";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-mapreduce-project/hadoop-mapreduce-client/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-mapreduce-client-jobclient";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-mapreduce-project/hadoop-mapreduce-client/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-mapreduce-client-shuffle";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-mapreduce-project/hadoop-mapreduce-client/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-ant";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-archives";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-aws";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-datajoin";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-distcp";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-extras";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-gridmix";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-openstack";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-pipes"; //no .project
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-rumen";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-sls";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-streaming";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-tools-dist";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-tools/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-yarn-api";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/").append(fileName).append("/.project")).toString();
    //

    // fileName = "hadoop-yarn-applications-distributedshell";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-yarn-applications-unmanaged-am-launcher";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/hadoop-yarn-applications/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-yarn-client";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-yarn-common";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/").append(fileName).append("/.project")).toString();

    // fileName = "hadoop-yarn-registry";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-yarn-server-applicationhistoryservice";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-yarn-server-common";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-yarn-server-nodemanager";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-yarn-server-resourcemanager";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/").append(fileName).append("/.project")).toString();
    //
    // fileName = "hadoop-yarn-server-web-proxy";
    // path = (new
    // StringBuilder(projectSourcePath).append("hadoop-yarn-project/hadoop-yarn/hadoop-yarn-server/").append(fileName).append("/.project")).toString();

    HashMap<String, String> moduleInformation = new HashMap<String, String>();

    try {
      BufferedReader br = new BufferedReader(new FileReader("hadoopmoduleinformation26.txt"));
      System.err.println("Reading data from " + "hadoopmoduleinformation26.txt");
      try {
        String line = br.readLine();

        while (line != null) {
          String[] option = line.split(":");
          if (option.length == 2) {
            moduleInformation.put(option[1].trim(), option[0].trim());
            System.out.println(option[1].trim());
          }
          line = br.readLine();
        }
      } finally {
        br.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.out.println(moduleInformation.size());
    for (String key : moduleInformation.keySet()) {

      fileName = key;

      System.out.println(moduleInformation.get(key) + " : " + fileName);

      path =
          (new StringBuilder(projectSourcePath).append(moduleInformation.get(key)).append(fileName)
              .append("/.project")).toString();

      IPath projectDotProjectFile = new Path(path);

      IProjectDescription projectDescription;

      try {
        projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
        IProject project = workspace.getRoot().getProject(projectDescription.getName());
        try {
          if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
            targetProject = JavaCore.create(project);
            packages = targetProject.getPackageFragments();

            String source_path = "org/apache/hadoop";
            String oracleFolder = eclipseDirectoryPath + "ccgdata/oracle/hadoop2.6.0";

            HadoopLikeCCG hadoopCCG =
                new HadoopLikeCCG("Configuration.java", oracleFolder, fileName + "26",
                    eclipseDirectoryPath);
            // hadoopCCG.createOracle(packages, source_path, fileName);

            // hadoopCCG.constructCallGraph(packages, source_path, "hadoop",
            // "org.apache.hadoop.conf.Configuration");

            // getDataType.getDataType(packages, source_path);

            // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
            // parseApplication = new ParserApplication(javaFileObjectsMap);
            // System.out.println(Factory.getInstance().getSourcePath());
            // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
            // "org/apache/hadoop/conf/Configuration","");
            // convertToNewFormat();
            // parse("Configuration.", "/hadoop-common/src/main/java");

          }
        } catch (Exception e) {
          System.err.println("Stop here 2");
          e.printStackTrace();
        }
        // }

        // System.out.println(project.getName());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        System.err.println("Stop here");
        e.printStackTrace();
      }
    }
  }

  void parseHBaseOptions090(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println("parseHBaseOptions");

    // IPath projectDotProjectFile = new Path(
    // // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-common" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-server" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-it" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-protocol" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-thrift" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-prefix-tree" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-hadoop-compat" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-examples" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-client" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-annotations" + "/.project");

    String fileName, path;
    String projectSourcePath = "/home/nhannguyen/workspace/hbase-0.90.0/";


    path = (new StringBuilder(projectSourcePath).append("/.project")).toString();

    IPath projectDotProjectFile = new Path(path);

    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();
          // System.out.println("Here");


          String source_path = "org/apache/hadoop";
          String oracleFolder = "/home/nhannguyen/Setup/eclipse/oracle/hbase";

          HadoopLikeCCG hadoopCCG =
              new HadoopLikeCCG("", oracleFolder, "hbase090", eclipseDirectoryPath);
          // hadoopCCG.createOracle(packages, source_path, fileName);

          // hadoopCCG.constructCallGraph(packages, source_path, "hbase",
          // "org.apache.hadoop.conf.Configuration");

          // GetDataType getDataType = new HBaseDataType("");
          // getDataType.getDataType(packages, source_path);

          // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          // System.out.println(Factory.getInstance().getSourcePath());
          // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
          // "org/apache/hadoop/conf/Configuration","");
          // convertToNewFormat();
          // parse("Configuration.", "/src/main/java");

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }

  }

  void parseHBaseOptions094(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println("parseHBaseOptions");

    // IPath projectDotProjectFile = new Path(
    // // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-common" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-server" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-it" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-protocol" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-thrift" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-prefix-tree" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-hadoop-compat" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-examples" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-client" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-annotations" + "/.project");

    String fileName, path;
    String projectSourcePath = "/home/nhannguyen/workspace/hbase-0.94.27/";


    path = (new StringBuilder(projectSourcePath).append("/.project")).toString();

    IPath projectDotProjectFile = new Path(path);

    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();
          // System.out.println("Here");


          String source_path = "org/apache/hadoop";
          String oracleFolder = "/home/nhannguyen/Setup/eclipse/oracle/hbase";

          HadoopLikeCCG hadoopCCG =
              new HadoopLikeCCG("", oracleFolder, "hbase094", eclipseDirectoryPath);
          // hadoopCCG.createOracle(packages, source_path, fileName);

          // hadoopCCG.constructCallGraph(packages, source_path, "hbase",
          // "org.apache.hadoop.conf.Configuration");

          // GetDataType getDataType = new HBaseDataType("");
          // getDataType.getDataType(packages, source_path);

          // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          // System.out.println(Factory.getInstance().getSourcePath());
          // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
          // "org/apache/hadoop/conf/Configuration","");
          // convertToNewFormat();
          // parse("Configuration.", "/src/main/java");

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }

  }

  void parseHBaseOptions(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println("parseHBaseOptions");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    // IPath projectDotProjectFile = new Path(
    // // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-common" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-server" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-it" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-protocol" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-thrift" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-prefix-tree" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-hadoop-compat" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-examples" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-client" + "/.project");
    // "/home/nhannguyen/workspace/hbase-1.0.0/hbase-annotations" + "/.project");

    HashMap<String, String> moduleInformation = new HashMap<String, String>();

    try {
      BufferedReader br = new BufferedReader(new FileReader("hbasemoduleinformation.txt"));
      try {
        String line = br.readLine();

        while (line != null) {
          // String[] option = line.split(":");
          // if (option.length == 2) {
          moduleInformation.put(line.trim(), "");
          System.out.println(line.trim());
          // }
          line = br.readLine();
        }
      } finally {
        br.close();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    String fileName, path;
    String projectSourcePath = "/home/nhannguyen/workspace/hbase-1.0.0/";

    System.out.println(moduleInformation.size());
    for (String key : moduleInformation.keySet()) {

      fileName = key;

      System.out.println(fileName);

      path = (new StringBuilder(projectSourcePath).append(fileName).append("/.project")).toString();

      IPath projectDotProjectFile = new Path(path);

      IProjectDescription projectDescription;

      try {
        projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
        IProject project = workspace.getRoot().getProject(projectDescription.getName());
        try {
          if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
            targetProject = JavaCore.create(project);
            packages = targetProject.getPackageFragments();
            // System.out.println("Here");


            String source_path = "org/apache/hadoop";
            String oracleFolder = "/home/nhannguyen/Setup/eclipse/oracle/hbase";

            HadoopLikeCCG hadoopCCG =
                new HadoopLikeCCG("", oracleFolder, fileName, eclipseDirectoryPath);
            // hadoopCCG.createOracle(packages, source_path, fileName);

            // hadoopCCG.constructCallGraph(packages, source_path, "hbase",
            // "org.apache.hadoop.conf.Configuration");

            // GetDataType getDataType = new HBaseDataType("");
            // getDataType.getDataType(packages, source_path);

            // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
            // parseApplication = new ParserApplication(javaFileObjectsMap);
            // System.out.println(Factory.getInstance().getSourcePath());
            // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
            // "org/apache/hadoop/conf/Configuration","");
            // convertToNewFormat();
            // parse("Configuration.", "/src/main/java");

          }
        } catch (Exception e) {
          System.err.println("Stop here 2");
          e.printStackTrace();
        }
        // }

        // System.out.println(project.getName());
      } catch (Exception e) {
        // TODO Auto-generated catch block
        System.err.println("Stop here");
        e.printStackTrace();
      }
    }
  }

  void parseElasticSearchOptions(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseElasticSearchOptions");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    IPath projectDotProjectFile =
        new Path("/home/nhannguyen/workspace/elasticsearch" + "/.project");
    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();
          // System.out.println("Here");
          String fileName = "elasticSearch";
          String source_path = "org/elasticsearch";
          String oracleFolder = "/home/nhannguyen/Setup/eclipse/oracle/elasticsearch";

          HadoopLikeCCG hadoopCCG =
              new HadoopLikeCCG("Settings.java", oracleFolder, fileName, eclipseDirectoryPath);

          // hadoopCCG.constructCallGraph(packages, source_path, "elasticsearch",
          // "org.elasticsearch.common.settings.Settings");

          // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          // System.out.println(Factory.getInstance().getSourcePath());
          // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
          // "org/apache/hadoop/conf/Configuration","");
          // convertToNewFormat();
          // parse("Configuration.", "/hadoop-common/src/main/java");

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }
  }

  void parseAntOptions(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseAntOptions");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    IPath projectDotProjectFile =
        new Path("/home/nhannguyen/workspace/apache-ant-1.9.4" + "/.project");
    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();
          // System.out.println("Here");

          String fileName = "";

          String source_path = "org/apache/tools/ant";

          String oracleFolder = "";
          // GetDataType getDataType = new AntDataType("");
          // getDataType.getDataType(packages, source_path);

          HadoopLikeCCG hadoopCCG =
              new HadoopLikeCCG("", oracleFolder, fileName, eclipseDirectoryPath);

          // hadoopCCG.constructCallGraph(packages, source_path, "ant.txt", "");

          // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          // System.out.println(Factory.getInstance().getSourcePath());
          // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
          // "org/apache/hadoop/conf/Configuration","");
          // convertToNewFormat();
          // parse("Configuration.", "/hadoop-common/src/main/java");

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }
  }

  void parseMahoutOptions(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseMahoutOptions");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    IPath projectDotProjectFile =
        new Path("/home/nhannguyen/workspace/mahout-distribution-0.9-src/core" + "/.project");
    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();

          String source_path = "org/apache/mahout";
          String oracleFolder = "";
          String fileName = "";

          HadoopLikeCCG hadoopCCG =
              new HadoopLikeCCG("AbstractJob.java", oracleFolder, fileName, eclipseDirectoryPath);

          // hadoopCCG.constructCallGraph(packages, source_path, "mahout.txt",
          // "org.apache.mahout.common.AbstractJob");

          // javaFileObjectsMap = parserImpl.lightParse(packages, "Configuration.java");
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          // System.out.println(Factory.getInstance().getSourcePath());
          // getConfiguration("Configuration.java", "org.apache.hadoop.conf.Configuration",
          // "org/apache/hadoop/conf/Configuration","");
          // convertToNewFormat();
          // parse("Configuration.", "/hadoop-common/src/main/java");

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }
  }

  void parseVoldemortOptions(String eclipseDirectoryPath) {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    System.err.println(getClass() + " parseVoldemortOptions");
    // IProject[] projects = workspace.getRoot().getProjects();
    // System.err.println("Length " + projects.length);
    // for (IProject project : projects) {
    // System.err.println(project.getName());
    // }

    IPath projectDotProjectFile = new Path("/home/nhannguyen/workspace/voldemort" + "/.project");
    IProjectDescription projectDescription;

    try {
      projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
      IProject project = workspace.getRoot().getProject(projectDescription.getName());
      try {
        if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
          targetProject = JavaCore.create(project);
          packages = targetProject.getPackageFragments();

          // ParseConfigurationInfo parseConfigurationInfo = new ParseCassandraConfigInfo();
          // parseConfigurationInfo.getConfigurationInfo();

          String source_path = "voldemort";

          String fileName = "voldermor";

          CassandraLikeCCG cassandraCCG =
              new CassandraLikeCCG("VoldemortConfig.java", fileName, eclipseDirectoryPath);
          // cassandraCCG.createReadFunctionFile(packages, source_path);
          String startClassName = "voldemort.server.VoldemortConfig";
          cassandraCCG.constructCallGraph(packages, source_path, "voldemort.txt", startClassName);

          // GetDataType getDataType = new CassandraDataType("DatabaseDescriptor.java");
          // getDataType.getDataType(packages, source_path);

          // javaFileObjectsMap = parserImpl.lightParse(packages, "DatabaseDescriptor.java",
          // source_path);
          // parseApplication = new ParserApplication(javaFileObjectsMap);
          //
          // MethodLevelCCGGenerator genMethodLevelCCG = new MethodLevelCCGGenerator();

          // genMethodLevelCCG.generate(packages, "DatabaseDescriptor.",
          // "/apache-cassandra-2.0.7-src/src/java", source_path);

        }
      } catch (Exception e) {
        System.err.println("Stop here 2");
        e.printStackTrace();
      }
      // }

      // System.out.println(project.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      System.err.println("Stop here");
      e.printStackTrace();
    }
  }


  void loadProject(String projectName) {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    IProject m_project = root.getProject(projectName);
    try {
      // Create the project
      if (!m_project.exists()) {
        m_project.create(null);
      }
      m_project.open(null);

      IJavaProject javaProject = JavaCore.create(m_project);

      IProjectDescription description = m_project.getDescription();
      description.setNatureIds(new String[] {JavaCore.NATURE_ID});

      m_project.setDescription(description, null);

      // need to check to make sure this is right JRE
      // IClasspathEntry[] buildPath = {
      // JavaCore.newSourceEntry(m_project.getFullPath().append("src")),
      // JavaRuntime.getDefaultJREContainerEntry() };
      // // set the classpath
      // javaProject.setRawClasspath(buildPath, m_project.getFullPath()
      // .append("bin"), null);
      // // create the src folder
      IFolder folder = m_project.getFolder("hraven-core/src");
      if (!folder.exists())
        folder.create(true, true, null);
      IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(folder);

      System.out.println(srcFolder.getPath() + " : " + srcFolder.getElementName());

      // Assert.isTrue(srcFolder.exists());
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      // return false;
    }
  }

  // CompilationUnit cu;
  // ASTNode savedASTNode;
  // ASTNode stmtNode;
  // public void testCFG(final String source_path) {
  // final IWorkspace workspace = ResourcesPlugin.getWorkspace();
  // System.err.println("Running testCFG");
  // // IProject[] projects = workspace.getRoot().getProjects();
  // // System.err.println("Length " + projects.length);
  // // for (IProject project : projects) {
  // // System.err.println(project.getName());
  // // }
  //
  // IPath projectDotProjectFile = new Path(
  // "/home/nhannguyen/workspace/apache-cassandra-2.0.7-src"
  // + "/.project");
  // IProjectDescription projectDescription;
  //
  // try {
  // projectDescription = workspace
  // .loadProjectDescription(projectDotProjectFile);
  // IProject project = workspace.getRoot().getProject(
  // projectDescription.getName());
  // try {
  // if (project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
  // targetProject = JavaCore.create(project);
  // packages = targetProject.getPackageFragments();
  //
  // for (IPackageFragment mypackage : packages) {
  // if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
  // // System.err.println("Package " + mypackage.getElementName());
  //
  // for (ICompilationUnit iCompilationUnit : mypackage.getCompilationUnits()) {
  // // System.out.println(iCompilationUnit.getPath().toString());
  // if (iCompilationUnit.getPath().toString().contains(source_path)
  // && iCompilationUnit.getElementName().equals("DatabaseDescriptor.java") ) {
  //
  // System.out.println("Here");
  // ASTParser parser = ASTParser
  // .newParser(AST.JLS4);
  // parser.setSource(iCompilationUnit); //source from compilation unit
  // parser.setResolveBindings(true);
  // parser.setKind(ASTParser.K_COMPILATION_UNIT);
  // cu = (CompilationUnit) parser.createAST(null);
  // if (cu != null && cu.getPackage() != null && cu.getPackage().getName() != null) {
  // cu.accept(new ASTVisitor() {
  // /*
  // * In the first step, we only care about the return type of a method. This helps to detect all
  // methods that
  // * work on the configuration class
  // * (non-Javadoc)
  // * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
  // */
  // public boolean visit(MethodDeclaration node) {
  // if (node != null && node.resolveBinding() != null
  // && node.getName().getFullyQualifiedName().equals("hasLargeAddressSpace")) {
  // savedASTNode = node;
  //
  // }
  // return true;
  // }
  //
  // public boolean visit(VariableDeclarationFragment node) {
  //
  // if (node.getInitializer() != null) {
  // Expression tempExp = node.getInitializer();
  // if (tempExp instanceof StringLiteral) {
  // // System.out.println("Pasring Import " + node.getName().getFullyQualifiedName() + " : "
  // // + node.getInitializer().toString());
  // System.out
  // .println(tempExp);
  // stmtNode = node;
  // }
  // if (tempExp instanceof NumberLiteral) {
  //
  // // System.out.println("Pasring Import " + node.getName().getFullyQualifiedName() + " : "
  // // + node.getInitializer().toString());
  // }
  // // Utils.getExpressionType(tempExp);
  // }
  // else {
  // // System.out.println(node.getName().getFullyQualifiedName());
  // }
  // return true;
  // }
  //
  // });
  // }
  // }
  // }
  // }
  // }
  // if (savedASTNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
  // System.out.println("Its is method decalration");
  // MethodDeclaration node = (MethodDeclaration)savedASTNode;
  // System.out.println(node.getName());
  // System.out.println("Here at applyConfig");
  //
  // IControlFlowGraph cfg = new EclipseNodeFirstCFG(node);
  //
  // ICFGNode startNode = cfg.getStartNode();
  // Set<ICFGEdge> outputs = startNode.getOutputs();
  // for (ICFGEdge icfgNode : outputs) {
  // System.out
  // .println("output " + icfgNode.toString());
  // }
  //
  // File out = null, original, projectRoot;
  // FileOutputStream outStream = null;
  // Graph testGraph;
  // String className, methodName, testName;
  // projectRoot = new File("/home/nhannguyen/workspace/JavaCodeParser/");
  // className = node.resolveBinding().getDeclaringClass().getName();
  // methodName = node.getName().getIdentifier();
  // testName = className + "_" + methodName + ".dot";
  // out = new File(projectRoot, "test/dotFiles/cfg/last/" + testName);
  // System.out
  // .println(out.getAbsolutePath());
  // testGraph = cfg.getDotGraph();
  // try {
  // outStream = new FileOutputStream(out);
  // } catch (FileNotFoundException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  //
  // try {
  // testGraph.printGraph(outStream);
  // } finally {
  // try {
  // outStream.close();
  // } catch (IOException e) {
  // // TODO Auto-generated catch block
  // e.printStackTrace();
  // }
  // }
  // Block block = node.getBody();
  // if (block != null) {
  // List<Statement> statements = block.statements();
  // int count = 0;
  // for (Statement stmt : statements) {
  // System.out.println(count++ + ": " + stmt.toString());
  // }
  // }
  // // savedASTNode.
  // }
  //
  // // if (stmtNode.getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
  // // VariableDeclarationFragment node = (VariableDeclarationFragment)stmtNode;
  // // System.out.println(node);
  // //
  // // System.out.println(node.getParent());
  // // }
  //
  // }
  // } catch (Exception e) {
  // System.err.println("Stop here 2");
  // e.printStackTrace();
  // }
  // // }
  //
  // // System.out.println(project.getName());
  // } catch (Exception e) {
  // // TODO Auto-generated catch block
  // System.err.println("Stop here");
  // e.printStackTrace();
  // }
  //
  // }
  @Override
  public Object start(IApplicationContext context) throws Exception {
    String eclipseDirectoryPath = Platform.getInstallLocation().getURL().getPath();
    Long startTime = System.currentTimeMillis();
    int select = 1;
    switch (select) {
      case 0:
        parseCassandraOptions(eclipseDirectoryPath);
        break;
      case 8:
        parseCassandraOptions10(eclipseDirectoryPath);
        break;
      case 1:
        parseHadoopOptions26(eclipseDirectoryPath);
        break;
      case 2:
        parseHadoopOptions12(eclipseDirectoryPath);
        break;
      case 10:
        parseHadoopOptions22(eclipseDirectoryPath);
        break;
      case 3:
        parseHBaseOptions(eclipseDirectoryPath);
        break;
      case 11:
        parseHBaseOptions094(eclipseDirectoryPath);
        break;
      case 12:
        parseHBaseOptions090(eclipseDirectoryPath);
        break;
      case 4:
        loadProject("hraven");
        break;
      case 5:
        parseElasticSearchOptions(eclipseDirectoryPath);
        break;
      case 9:
        parseVoldemortOptions(eclipseDirectoryPath);
        break;
      default:
        String source_path = "org/apache/cassandra";
        // testCFG(source_path);
        break;

    }
    //
    System.out.println("Run time " + (System.currentTimeMillis() - startTime) / 1000);
    // System.out.println("Number of nodes " + nbNodes);
    return EXIT_OK;
  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

}
