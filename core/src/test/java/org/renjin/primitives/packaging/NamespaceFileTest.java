package org.renjin.primitives.packaging;

import com.google.common.collect.Iterables;
import com.google.common.io.CharSource;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class NamespaceFileTest {

  
  @Test
  public void testJvmImports() throws IOException {
    
    String NAMESPACE = 
        "importClass(org.renjin.stats.dist.Distance)\n";
    
    NamespaceFile file = new NamespaceFile(CharSource.wrap(NAMESPACE));
    
    NamespaceFile.JvmClassImportEntry entry = Iterables.getOnlyElement(file.getJvmImports());
    assertThat(entry.isClassImported(), equalTo(true));
    assertThat(entry.getClassName(), equalTo("org.renjin.stats.dist.Distance"));
  }
  
  @Test
  public void testDynLib() throws IOException {
    String NAMESPACE = 
        "useDynLib(stats, .registration = TRUE, .fixes='C_',\n" +
        "    kmeans_Lloyd,\n" +
        "    kmeans_MacQueen,\n" +
        "    kmns = kmns_)\n";
      
    NamespaceFile file = new NamespaceFile(CharSource.wrap(NAMESPACE));

    NamespaceFile.DynLibEntry dynLib = Iterables.getOnlyElement(file.getDynLibEntries());
    assertThat(dynLib.getPrefix(), equalTo("C_"));
    assertThat(dynLib.isRegistration(), equalTo(true));

    NamespaceFile.DynLibSymbol kmns = dynLib.getSymbols().get(2);
    assertThat(kmns.getSymbolName(), equalTo("kmns_"));
    assertThat(kmns.getAlias(), equalTo("kmns"));
  }
  
  @Test
  public void exportPatterns() throws IOException {
    String NAMESPACE = 
        "exportPattern(cell_effect_mult_or,cell_effect_or,Cloglin,Cloglin_mult,exp_par,exp_par_mult)\n";

    NamespaceFile file = new NamespaceFile(CharSource.wrap(NAMESPACE));

    assertThat(file.getExportedPatterns(), Matchers.hasItems(
        "cell_effect_mult_or",
        "cell_effect_or",
        "Cloglin",
        "Cloglin_mult",
        "exp_par",
        "exp_par_mult"));
    
  }
  
  @Test
  public void malformedImportsFrom() throws IOException {
    
    // The following actually has no effect, but is included in at least one
    // CRAN package. The fact that it has no effect was balanced out in practice by
    // listing survival also in the Depends clause of the DESCRIPTION file.
    String NAMESPACE = 
        "importFrom(survival)\n";

    NamespaceFile file = new NamespaceFile(CharSource.wrap(NAMESPACE));

    NamespaceFile.PackageImportEntry entry = Iterables.getOnlyElement(file.getPackageImports());
    assertThat(entry.getPackageName(), equalTo("survival"));
    assertTrue(entry.getSymbols().isEmpty());
    assertTrue(entry.getClasses().isEmpty());
  }
}