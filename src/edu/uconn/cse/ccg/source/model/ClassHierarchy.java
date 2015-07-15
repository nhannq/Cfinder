package edu.uconn.cse.ccg.source.model;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import edu.uconn.cse.ccg.core.CCGraphBuilderAbstract;

public class ClassHierarchy {
  public static final int UNDEFINED = -1;
  public static final int IMPLEMENTATION_CLASS = 1;
  public static final int ABSTRACT_CLASS = 2;
  public static final int INTERFACE = 3;

  boolean isInner;
  int type; // IMPLEMENTATION, ABSTRACT OR INTERFACE
  int countBaseClasses = 0;
  int countSubClasses = 0;
  HashSet<String> baseClasses = new HashSet<String>(); // classname
  HashSet<String> subClasses = new HashSet<String>(); // classname
  // methodname : set<signature of class> of method which this class inherited from its base classes
  HashMap<String, Set<String>> baseMethodInformation = new HashMap<String, Set<String>>();
  // methodname : set<signature of class> of method which is inherited by its subclasses
  HashMap<String, Set<String>> subMethodInformation = new HashMap<String, Set<String>>();
  Set<String> methodSet = new HashSet<String>();
  MethodDeclaration[] methodDecSet = null;


  public ClassHierarchy(int type, boolean isInner) {
    this.type = type;
    this.isInner = isInner;
  }

  public boolean isImplementationClass() {
    return this.type == IMPLEMENTATION_CLASS;
  }

  public boolean isAbstractClass() {
    return this.type == ABSTRACT_CLASS;
  }

  public boolean isInterface() {
    return this.type == INTERFACE;
  }

  public boolean isUndefined() {
    return this.type == UNDEFINED;
  }

  public void setType(int type) {
    this.type = type;
  }

  public int getType() {
    return this.type;
  }

  public void setIsInner(boolean isInner) {
    this.isInner = isInner;
  }

  public boolean getIsInner() {
    return this.isInner;
  }

  public void addBaseClass(String baseClassName) {
    baseClasses.add(baseClassName);
  }

  public void addSubClass(String subClassName) {
    subClasses.add(subClassName);
  }

  public HashSet<String> getBaseClasses() {
    return baseClasses;
  }

  public HashSet<String> getSubClasses() {
    return subClasses;
  }

  public boolean hasBaseClass() {
    return baseClasses.size() != 0;
  }

  public boolean hasSubClass() {
    return subClasses.size() != 0;
  }

  public MethodDeclaration[] getMethodDecSet() {
    return this.methodDecSet;
  }

  public Set<String> getMethodSet() {
    return this.methodSet;
  }

  public void addMethod(String methodSignature) {
    this.methodSet.add(methodSignature);
  }

  public void setMethodDecs(MethodDeclaration[] methodDecs) {
    this.methodDecSet = methodDecs;
  }

  // compare imethodBinding of a method from the base class with all methods
  // of the implemented class to see there is any overried method
  public MethodDeclaration isOverride(String baseMethodName, IMethodBinding imethodBinding,
      List<SingleVariableDeclaration> baseParameters, Set<String> classToInterface) {
    MethodDeclaration methodDec2 = null;
    try {

      for (MethodDeclaration methodDec : this.methodDecSet) {
        methodDec2 = methodDec;
        // System.out.println("Comparing " + methodDec.getName().getFullyQualifiedName() + " : "
        // + baseMethodName);
        if (methodDec.getName().getFullyQualifiedName().equals(baseMethodName)
            && methodDec.parameters().size() == baseParameters.size()) {
          // System.out.println(methodDec.resolveBinding().getDeclaringClass().getQualifiedName());
          // System.out.println("Checking " + methodDec.getName().getFullyQualifiedName() + " And "
          // + imethodBinding.getName());
          // System.out.println(CCGraphBuilderAbstract.getMethodSignatureFromParameters(methodDec.parameters()));

          if (methodDec.resolveBinding().isSubsignature(imethodBinding)) {
            // System.out.println("Override");
            return methodDec;
          } else if (CCGraphBuilderAbstract
              .getMethodSignatureFromParameters(methodDec.parameters()).equals(
                  CCGraphBuilderAbstract.getMethodSignatureFromParameters(baseParameters))) {
            // System.out.println("Parameter identical " +
            // CCGraphBuilderAbstract.getMethodSignatureFromParameters(baseParameters));
            return methodDec;
          } else {
            boolean isOverried = true;
            for (int i = 0; i < baseParameters.size(); i++) {
              SingleVariableDeclaration param =
                  (SingleVariableDeclaration) methodDec.parameters().get(i);
              SingleVariableDeclaration baseParam = baseParameters.get(i);
              // System.err.println("CHECKING " +
              // param.resolveBinding().getType().getQualifiedName()
              // + " AND " + baseParam.resolveBinding().getType().getQualifiedName());
              if (!param.resolveBinding().getType().getQualifiedName()
                  .equals(baseParam.resolveBinding().getType().getQualifiedName())
                  && !classToInterface.contains(param.resolveBinding().getType().getQualifiedName()
                      + ":" + baseParam.resolveBinding().getType().getQualifiedName())) {
                if (baseParam.resolveBinding().getType().isParameterizedType()) {
                  if (!baseParam.resolveBinding().getType().getQualifiedName()
                      .contains(param.resolveBinding().getType().getQualifiedName())) {
                    isOverried = false;
                  }
                } else {
                  isOverried = false;
                  break;
                }
              }
            }
            if (isOverried) {
              // System.out.println("OVERRIED " + methodDec.getName().getFullyQualifiedName() +
              // " : "
              // + baseMethodName);
              return methodDec;
            } else {
              // print to see why two methods have the same name but one is not overrided the other.
              // if (methodDec.parameters().size() == baseParameters.size()) {
              // for (int i = 0; i < baseParameters.size(); i++) {
              //
              // }
              // System.out.println("Checking " + methodDec.getName().getFullyQualifiedName() + ":"
              // + CCGraphBuilderAbstract.getMethodSignatureFromParameters(methodDec.parameters())
              // + " of " + methodDec.resolveBinding().getDeclaringClass().getQualifiedName()
              // + " And " + imethodBinding.getName() + ":"
              // + CCGraphBuilderAbstract.getMethodSignatureFromParameters(baseParameters) + " : "
              // + imethodBinding.getDeclaringClass().getQualifiedName());
              // }
            }
          }

        }
      }
    } catch (Exception e) {
      if (methodDec2 != null)
        System.out.println("Checking " + methodDec2.getName().getFullyQualifiedName() + " And "
            + imethodBinding.getName());
      e.printStackTrace();
    }
    return null;
  }

  public MethodDeclaration isOverride(String baseMethodSignature) {
    String[] splitMethodSig = baseMethodSignature.split(":");
    String mName = splitMethodSig[0];
    int length = 0;
    if (splitMethodSig.length == 2) {
      length = splitMethodSig[1].split("\t").length;
    }
    for (MethodDeclaration methodDec : this.methodDecSet) {
      // System.out.println("Comparing " + methodDec.getName().getFullyQualifiedName() + " : " +
      // baseMethodName);

      if (methodDec.getName().getFullyQualifiedName().equals(baseMethodSignature.split(":")[0])
          && methodDec.parameters().size() == length) {
        // System.out.println(methodDec.resolveBinding().getDeclaringClass().getQualifiedName());
        // System.out.println("Checking " + methodDec.getName().getFullyQualifiedName() + " And " +
        // imethodBinding.getName());
        // System.out.println(CCGraphBuilderAbstract.getMethodSignatureFromParameters(methodDec.parameters()));
        if (length == 0) {
          return methodDec;
        } else if (CCGraphBuilderAbstract.getMethodSignatureFromParameters(methodDec.parameters())
            .equals(splitMethodSig[1])) {
          // System.out.println("Parameter identical " +
          // CCGraphBuilderAbstract.getMethodSignatureFromParameters(methodDec.parameters()));
          return methodDec;
        } else {
          boolean isOverried = true;
        }

      }
    }
    return null;
  }

  public boolean checkExistingMethod(String methodSignature) {
    return this.methodSet.contains(methodSignature);
  }

  public boolean isOverriedMethod(String methodSinature) {
    return this.subMethodInformation.containsKey(methodSinature);
  }

  public String overrideMethod(String methodSignature) {
    String result = null;
    if (this.baseMethodInformation.containsKey(methodSignature)) {
      for (String r : baseMethodInformation.get(methodSignature)) {
        return r;
      }
    }
    return result;
  }

  public boolean inheritClass(String className) {
    return baseClasses.contains(className);
  }

  public void updateMethod(HashMap<String, Set<String>> methodInformation, String methodSignature,
      String methodClassSignature) {
    Set<String> idSetTemp;
    if (methodInformation.containsKey(methodSignature)) {
      idSetTemp = methodInformation.get(methodSignature);
    } else {
      idSetTemp = new HashSet<String>();
    }
    idSetTemp.add(methodClassSignature);
    methodInformation.put(methodSignature, idSetTemp);
  }

  public void updateBaseMethod(String methodSignature, String baseMethodClassSignature) {
    updateMethod(baseMethodInformation, methodSignature, baseMethodClassSignature);
  }

  public void updateSubMethod(String methodSignature, String subMethodClassSignature) {
    updateMethod(subMethodInformation, methodSignature, subMethodClassSignature);
  }

  public int getBaseMethodInformartionSize() {
    return baseMethodInformation.size();
  }

  public HashMap<String, Set<String>> getBaseMethodInformation() {
    return this.baseMethodInformation;
  }

  public String printBaseMethodInformation() {
    StringBuilder string = new StringBuilder();
    for (String k : baseMethodInformation.keySet()) {
      string.append(k).append("\n");
      for (String k2 : baseMethodInformation.get(k)) {
        string.append("INHERITED " + k2).append("\n");
      }
    }
    return string.toString();
  }

  public String toString() {
    // StringBuilder string = new StringBuilder("Type " + type);
    StringBuilder string = new StringBuilder();
    // string.append("\n");
    for (String str : baseClasses) {
      // if (str.contains("org.apache.cassandra")) {
      string.append("BaseClass: \t").append(str).append("\n");
      // }
    }
    for (String str : subClasses) {
      string.append("SubClass: \t").append(str).append("\n");
    }
    return string.toString();
  }

  public String subClassesToString() {
    StringBuilder string = new StringBuilder();
    for (String str : subClasses) {
      string.append("SubClass: \t").append(str).append("\n");
    }
    return string.toString();
  }
}
