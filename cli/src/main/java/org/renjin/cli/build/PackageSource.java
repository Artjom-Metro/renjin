package org.renjin.cli.build;


import joptsimple.OptionSet;
import org.renjin.packaging.PackageDescription;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PackageSource {

  private final BuildReporter reporter;
  private final File packageDir;
  private final PackageDescription description;
  private final File namespaceFile;
  private final String groupId;
  private final File sourceDir;

  public PackageSource(String packagePath, OptionSet options) {
    this.reporter = new BuildReporter();
    this.packageDir = new File(packagePath);
    check(packageDir.exists(), "Package path '%s' does not exist.", packageDir.getAbsolutePath());
    check(packageDir.isDirectory(), "Package path '%s' is not a directory.", packageDir.getAbsolutePath());
    
    this.description = readDescription();

    this.groupId = "org.renjin.cran";
    
    this.sourceDir = new File(this.packageDir, "R");

    this.namespaceFile = new File(packageDir, "NAMESPACE");
    check(namespaceFile.exists(), "NAMESPACE file is missing at '%s'", namespaceFile);
  }


  private PackageDescription readDescription() {
    File descriptionFile = new File(packageDir, "DESCRIPTION");
    check(descriptionFile.exists(), "DESCRIPTION file does not exist at '%s'", descriptionFile);

    PackageDescription description = null;
    try {
      description = PackageDescription.fromFile(descriptionFile);
    } catch (IOException e) {
      throw new BuildException("Exception reading DESCRIPTION file: " + e.getMessage());
    }
    return description;
  }

  private void check(boolean condition, String message, Object... args) {
    if(!condition) {
      throw new BuildException(String.format(message, args));
    }
  }

  public String getGroupId() {
    return groupId;
  }
  
  public String getName() {
    return description.getPackage();
  }

  public File getNamespaceFile() {
    return namespaceFile;
  }

  public File getSourceDir() {
    return sourceDir;
  }
  
  public File getNativeSourceDir() {
    return new File(packageDir, "src");
  }

  public List<String> getSourceFiles() {
    return null;
  }

  public String getVersion() {
    return description.getVersion();
  }

  public File getPackageDir() {
    return packageDir;
  }

  public String getJavaPackageName() {
    return groupId + "." + getName();
  }

  public boolean needsCompilation() {
    return "yes".equals(description.getFirstProperty("NeedsCompilation"));
  }

  public PackageDescription getDescription() {
    return description;
  }
}
