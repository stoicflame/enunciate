/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.gwt_json_overlay;

import com.webcohesion.enunciate.Enunciate;
import junit.framework.TestCase;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class TestGeneratedJsonTypeSerialization extends TestCase {

  private File sourceDir;
  private File outDir;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    String srcDir = System.getProperty("source.dir");
    this.sourceDir = new File(srcDir);
    String outDir = System.getProperty("target.dir");
    this.outDir = new File(outDir);
  }

  @Test
  public void testCompile() throws Exception {
    assertTrue(this.sourceDir.exists());
    assertTrue(this.outDir.exists() || this.outDir.mkdirs());

    Enunciate enunciate = new Enunciate();
    final ArrayList<File> javaFiles = new ArrayList<File>();

    enunciate.visitFiles(sourceDir, Enunciate.JAVA_FILTER, new Enunciate.FileVisitor() {
      @Override
      public void visit(File file) {
        javaFiles.add(file);
      }
    });

    String classpath = System.getProperty("java.class.path");
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    List<String> options = Arrays.asList("-source", "1.6", "-target", "1.6", "-encoding", "UTF-8", "-cp", classpath, "-d", this.outDir.getAbsolutePath());
    JavaCompiler.CompilationTask task = compiler.getTask(null, null, null, options, null, compiler.getStandardFileManager(null, null, null).getJavaFileObjectsFromFiles(javaFiles));
    assertTrue(task.call());
  }

}
