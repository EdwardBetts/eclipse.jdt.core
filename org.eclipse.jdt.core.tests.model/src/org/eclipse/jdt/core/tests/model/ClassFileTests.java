/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class ClassFileTests extends ModifyingResourceTests {
	
	IPackageFragmentRoot jarRoot;
	
	public ClassFileTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		return new Suite(ClassFileTests.class);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject javaProject = createJavaProject("P", new String[] {}, new String[] {"/P/lib.jar"}, "");
		IProject project = javaProject.getProject();
		String projectLocation = project.getLocation().toOSString();
		String jarPath = projectLocation + File.separator + "lib.jar";
		String sourceZipPath = projectLocation + File.separator + "libsrc.zip";
		Map options = new HashMap();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
		String[] pathAndContents = new String[] {
			"nongeneric/A.java", 
			"package nongeneric;\n" +
			"public class A {\n" + 
			"}",			
			"generic/X.java", 
			"package generic;\n" +
			"public class X<T> {\n" + 
			"  void foo(X<T> x) {\n" +
			"  }\n" +
			"  <K, V> V foo(K key, V value) {\n" +
			"    return value;\n" +
			"  }\n" +
			"}",
			"generic/Y.java", 
			"package generic;\n" +
			"public class Y<K, V> {\n" + 
			"}",
			"generic/Z.java", 
			"package generic;\n" +
			"public class Z<T extends Object & I<? super T>> {\n" + 
			"}",
			"generic/I.java", 
			"package generic;\n" +
			"public interface I<T> {\n" + 
			"}",
			"generic/W.java", 
			"package generic;\n" +
			"public class W<T extends X<T> , U extends T> {\n" + 
			"}",
		};
		org.eclipse.jdt.core.tests.util.Util.createJar(pathAndContents, options, jarPath);
		org.eclipse.jdt.core.tests.util.Util.createSourceZip(pathAndContents, sourceZipPath);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		this.jarRoot = javaProject.getPackageFragmentRoot(project.getFile("lib.jar"));
			
	}
	
	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
		deleteProject("P");
	}
	
	/*
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures1() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Ljava.lang.Object;\n",
			type.getTypeParameterSignatures());
	}
	
	/*
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures2() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("nongeneric").getClassFile("A.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"",
			type.getTypeParameterSignatures());
	}
	
	/*
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures3() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("Y.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"K:Ljava.lang.Object;\n" + 
			"V:Ljava.lang.Object;\n",
			type.getTypeParameterSignatures());
	}

	/*
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures4() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("Z.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Ljava.lang.Object;\n" + 
			":Lgeneric.I<-TT;>;\n",
			type.getTypeParameterSignatures());
	}
	
	/*
	 * Ensure that the type parameter signatures of a binary type are correct.
	 */
	public void testParameterTypeSignatures5() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("W.class").getType();
		assertStringsEqual(
			"Unexpected type parameters",
			"T:Lgeneric.X<TT;>;\n" + 
			"U:TT;\n",
			type.getTypeParameterSignatures());
	}

	/*
	 * Ensure that the type parameter signatures of a binary method are correct.
	 */
	public void testParameterTypeSignatures6() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getClassFile("X.class").getType();
		IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
		assertStringsEqual(
			"Unexpected type parameters",
			"K:Ljava.lang.Object;\n" + 
			"V:Ljava.lang.Object;\n",
			method.getTypeParameterSignatures());
	}
}
