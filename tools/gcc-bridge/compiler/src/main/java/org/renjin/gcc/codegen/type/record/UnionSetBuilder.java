/**
 * Renjin : JVM-based interpreter for the R language for the statistical analysis
 * Copyright © 2010-2016 BeDataDriven Groep B.V. and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, a copy is available at
 * https://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.renjin.gcc.codegen.type.record;

import org.renjin.gcc.gimple.type.GimpleField;
import org.renjin.gcc.gimple.type.GimpleRecordType;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.repackaged.guava.collect.HashMultimap;
import org.renjin.repackaged.guava.collect.Multimap;

import java.util.*;

/**
 * Graph of record types that need to have the same layout.
 */
public class UnionSetBuilder {


  private class Node {
    private GimpleRecordTypeDef typeDef;
    private Set<Node> adjacent = new HashSet<>(0);
    private int subGraph = -1;

    public Node(GimpleRecordTypeDef typeDef) {
      this.typeDef = typeDef;
    }
  }
  
  private Map<String, Node> nodes = new HashMap<>();
  
  private int nextSubGraphId = 0;
  private Multimap<Integer, Node> subGraphs = HashMultimap.create();
  
  public UnionSetBuilder(Collection<GimpleRecordTypeDef> recordTypeDefs) {
    
    // Add nodes
    for (GimpleRecordTypeDef recordTypeDef : recordTypeDefs) {
      node(recordTypeDef);
    }

    // Add edges between unioned structs
    for (GimpleRecordTypeDef recordTypeDef : recordTypeDefs) {
      if(recordTypeDef.isUnion()) {
        addUnionEdges(recordTypeDef);
      }
    }
    
    // Identify all connected subgraphs
    findSubGraphs();
  }

  private void addUnionEdges(GimpleRecordTypeDef unionDef) {

    Node unionNode = node(unionDef);
    List<Node> members = new ArrayList<>();
    
    for (GimpleField field : unionDef.getFields()) {
      if(field.getType() instanceof GimpleRecordType) {
        GimpleRecordType memberType = (GimpleRecordType) field.getType();
        members.add(node(memberType));
      }
    }

    for (int i = 0; i < members.size(); i++) {
      addEdge(unionNode, members.get(i));
      for(int j = i+1; j < members.size(); ++j) {
        addEdge(members.get(i), members.get(j));
      }
    }
  }
  
  private void findSubGraphs() {
    for (Node node : nodes.values()) {
      if(node.subGraph == -1) {
        findSubGraph(node, nextSubGraphId++);
      }
    }
  }

  private void findSubGraph(Node node, int id) {
    node.subGraph = id;
    subGraphs.put(id, node);
    for (Node adjacentNode : node.adjacent) {
      if(adjacentNode.subGraph != id) {
        findSubGraph(adjacentNode, id);
      }
    }
  }


  private Node node(GimpleRecordTypeDef typeDef) {
    Node node = nodes.get(typeDef.getId());
    if(node == null) {
      node = new Node(typeDef);
      nodes.put(typeDef.getId(), node);
    }
    return node;
  }

  private Node node(GimpleRecordType type) {
    Node node = nodes.get(type.getId());
    if(node == null) {
      throw new IllegalStateException("Node for type " + type + " does not exist.");
    }
    return node;
  }
  
  public void addEdge(Node a, Node b) {
    a.adjacent.add(b);
    b.adjacent.add(a);
  }
  
  public List<UnionSet> build() {
    List<UnionSet> unionSets = new ArrayList<>();
    for(int i=0;i<nextSubGraphId;++i) {
      Collection<Node> members = subGraphs.get(i);
      List<GimpleRecordTypeDef> unions = new ArrayList<>();
      List<GimpleRecordTypeDef> records = new ArrayList<>();
      for (Node member : members) {
        if (member.typeDef.isUnion()) {
          unions.add(member.typeDef);
        } else {
          records.add(member.typeDef);
        }
      }
      unionSets.add(new UnionSet(unions, records));
    }
    return unionSets;
  }
  
}
