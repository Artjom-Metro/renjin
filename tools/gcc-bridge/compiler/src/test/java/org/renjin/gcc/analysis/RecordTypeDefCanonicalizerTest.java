package org.renjin.gcc.analysis;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.renjin.gcc.NullTreeLogger;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.type.*;
import org.renjin.repackaged.guava.collect.Iterables;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class RecordTypeDefCanonicalizerTest {

  public static final GimpleRealType DOUBLE_TYPE = new GimpleRealType(64);


  @Test
  public void simple() {

    // Each compilation unit will have their own copy of the 
    // GimpleRecordTypeDef with its own id
    GimpleRecordTypeDef point1 = new GimpleRecordTypeDef();
    point1.setId("p1");
    point1.getFields().add(new GimpleField("x", DOUBLE_TYPE));
    point1.getFields().add(new GimpleField("y", DOUBLE_TYPE));

    GimpleCompilationUnit unit1 = new GimpleCompilationUnit();
    unit1.getRecordTypes().add(point1);

    GimpleRecordTypeDef point2 = new GimpleRecordTypeDef();
    point2.setId("p2");
    point2.getFields().add(new GimpleField("x", DOUBLE_TYPE));
    point2.getFields().add(new GimpleField("y", DOUBLE_TYPE));

    GimpleCompilationUnit unit2 = new GimpleCompilationUnit();
    unit2.getRecordTypes().add(point2);

    Collection<GimpleRecordTypeDef> canonicalDefs =
        RecordTypeDefCanonicalizer.canonicalize(new NullTreeLogger(), Arrays.asList(unit1, unit2));


    assertThat(canonicalDefs, Matchers.hasSize(1));

    assertTrue(unit1.getRecordTypes().get(0) ==
        unit2.getRecordTypes().get(0));


  }

  @Test
  public void linkedList() {

    // Each compilation unit will have their own copy of the 
    // GimpleRecordTypeDef with its own id

    List<GimpleCompilationUnit> units = new ArrayList<>();

    for(int unitNum = 0; unitNum < 5; ++unitNum) {

      GimpleRecordTypeDef node = new GimpleRecordTypeDef();
      node.setId("N" + unitNum);
      node.getFields().add(new GimpleField("value", DOUBLE_TYPE));
      node.getFields().add(new GimpleField("next", new GimplePointerType(new GimpleRecordType("N" + unitNum))));

      GimpleRecordTypeDef linkedList = new GimpleRecordTypeDef();
      linkedList.setId("LL" + unitNum);
      linkedList.getFields().add(new GimpleField("head", new GimplePointerType(new GimpleRecordType("N" + unitNum))));

      GimpleCompilationUnit unit = new GimpleCompilationUnit();
      unit.getRecordTypes().add(linkedList);
      unit.getRecordTypes().add(node);

      units.add(unit);
    }


    Collection<GimpleRecordTypeDef> canonicalDefs =
        RecordTypeDefCanonicalizer.canonicalize(new NullTreeLogger(), units);

    assertThat(canonicalDefs, Matchers.hasSize(2));
    
    System.out.println(Iterables.get(canonicalDefs, 0));
    System.out.println(Iterables.get(canonicalDefs, 1));

  }



  @Test
  public void graph() {

    // Each compilation unit will have their own copy of the 
    // GimpleRecordTypeDef with its own id

    List<GimpleCompilationUnit> units = new ArrayList<>();

    for(int unitNum = 0; unitNum < 5; ++unitNum) {

      GimpleRecordTypeDef node = new GimpleRecordTypeDef();
      node.setId("N" + unitNum);
      node.getFields().add(new GimpleField("value", DOUBLE_TYPE));
      node.getFields().add(new GimpleField("parentEdge", new GimplePointerType(new GimpleRecordType("E" + unitNum))));
      node.getFields().add(new GimpleField("leftEdge", new GimplePointerType(new GimpleRecordType("E" + unitNum))));


      GimpleRecordTypeDef edge = new GimpleRecordTypeDef();
      edge.setId("E" + unitNum);
      edge.getFields().add(new GimpleField("from", new GimplePointerType(new GimpleRecordType("N" + unitNum))));
      edge.getFields().add(new GimpleField("to", new GimplePointerType(new GimpleRecordType("N" + unitNum))));

      GimpleCompilationUnit unit = new GimpleCompilationUnit();
      unit.getRecordTypes().add(edge);
      unit.getRecordTypes().add(node);

      units.add(unit);
    }


    Collection<GimpleRecordTypeDef> canonicalDefs =
        RecordTypeDefCanonicalizer.canonicalize(new NullTreeLogger(), units);

    assertThat(canonicalDefs, Matchers.hasSize(2));

    System.out.println(Iterables.get(canonicalDefs, 0));
    System.out.println(Iterables.get(canonicalDefs, 1));

  }

}