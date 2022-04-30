/**
 * Thomas Wiemold
 * 4/22/2022
 * CS224
 * Programming Assignment #6
 * Ford-Fulkerson
 * Graph.java implements Ford-Fulkerson
 */

import java.util.ArrayList;
import java.util.Queue;
import java.util.Stack;

public class Graph {
  ArrayList<Node> nodes;

  //==============================================================

  public Graph() {
    this.nodes = new ArrayList<Node>();
  }

  //==============================================================

  public void addNode(Node n) {
    this.nodes.add(n);
  }

  //==============================================================

  public void addEdge(Node n1, Node n2, int capacity) {
    this.addEdge(n1, n2, capacity, 0);
  }

  //==============================================================

  public void addEdge(Node n1, Node n2, int capacity, int flow) {
    Edge e1 = new Edge(n1, n2, capacity, flow);
    assert(flow <= capacity);
    int idx1 = this.nodes.indexOf(n1);
    if (idx1 >= 0) {
      this.nodes.get(idx1).add(e1);
    } else {
      System.out.println("node " + n1.name + " not found in graph");
    }
  }

  //==============================================================

  private void addResidualEdge(Node n1, Node n2, int capacity, boolean backward) {
    Edge e1 = new Edge(n1, n2, capacity, backward);
    int idx1 = this.nodes.indexOf(n1);
    if (idx1 >= 0) {
      this.nodes.get(idx1).addResidualEdge(e1);
    } else {
      System.out.println("node " + n1.name + " not found in graph");
    }
  }

  //==============================================================

  public void print() {
    for (Node n: this.nodes) {
      System.out.print("Node " + n.name + ":");
      for (Edge edge: n.adjlist) {
        System.out.print(" " + edge.n2.name + " (c=" + edge.capacity);
        System.out.print(", f=" + edge.flow + ")");
      }
      System.out.println();
    }
  }

  //==============================================================

  private void printResidual() {
    for (Node n: this.nodes) {
      System.out.print("Node " + n.name + ":");
      for (Edge edge: n.adjlistResid) {
        System.out.print(" " + edge.n2.name + " (c=" + edge.capacity);
        if (edge.backward)
          System.out.print(" <=");
        System.out.print(")");
      }
      System.out.println();
    }
  }

  //=========================================================

  private ArrayList<Edge> findPathInResid(Node s, Node t) {
    int i, k, idx;
    boolean done, found;
    Node n1, n2;

    ArrayList<Edge> path = new ArrayList<Edge>();

    Stack<Node> stack = new Stack<Node>();
    boolean explored[] = new boolean[1 + this.nodes.size()];
    int parent[] = new int[1+this.nodes.size()];

    for (i=0; i<=this.nodes.size(); ++i)
      explored[i] = false;

    done = false;
    stack.push(s);
    while ( ! done && ! stack.empty() ) {
      n1 = stack.pop();
      if ( ! explored[n1.name] ) {
        explored[n1.name] = true;
//P     System.out.println("explore: " + n1.name);
        if (parent[n1.name] != 0)
          System.out.println("tree: " + n1.name + " -> " + parent[n1.name]);
//P     System.out.println("set explored [" + n1.name + "] to true");
        for (Edge edge: n1.adjlistResid) {
          n2 = edge.n2;
          if ( ! explored[n2.name] ) {
//P         System.out.println("add edge from " + n1.name + " to " + n2.name);
//P         System.out.println("discover: " + n2.name);
            stack.push(n2);
            parent[n2.name] = n1.name;
            if (n2.name == t.name)
              done = true;
//P       } else {
//P         System.out.println("have already explored " + n2.name);
          }
        }
      }
    }

    System.out.println("here's the backward path from " + t.name);
    done = false;
    idx = t.name;
    while ( ! done ) {
      if (parent[idx] == 0)
        done = true;
      else {
        System.out.println(parent[idx] + " to " + idx);
        // find the edge from parent[idx] to idx
        found = false;
        k = 0;
        while ( ! found && k < nodes.size()) {
          if (nodes.get(k).name == parent[idx])
            found = true;
          else
            k = k + 1;
        }
        n1 = nodes.get(k);
        found = false;
        for (Edge e: n1.adjlistResid) {
          if (e.n2.name == idx) {
//P         System.out.println("found edge from " + parent[idx] + " to " + idx + " " + e);
            path.add(e);
            found = true;
          }
        }
        idx = parent[idx];
      }
    }

    System.out.println();
    return path;
  } // findPathInResid()

  //==============================================================

  public boolean checkFlow(Node s, Node t) {
    // check that flow out of s == flow into t

    boolean goodFlow = true;
    int sFlow = 0;
    int tFlow = 0;

    for (Edge e: s.adjlist) {
      sFlow += e.flow;
    }

    for (Node n : nodes) {
      for (Edge e: n.adjlist) {
        if (e.n2.name == t.name) {
          tFlow += e.flow;
        }
      }
    }

    if (sFlow != tFlow) {
      System.out.println("Failed conservation condition root nodes");
      goodFlow = false;
      return goodFlow;
    }

    for (Node n : nodes) {
      for (Edge e : n.adjlist) {
      // check conservation condition at each internal node
        if (e.flow > e.capacity) {
          System.out.printf("Failed conservation condition, edge between Node %s and Node %s", e.n1.name, e.n2.name);
          goodFlow = false;
          return goodFlow;
        }
      }
    }
    return goodFlow;
  } // checkFlow()

  //=========================================================

  private void constructResidualGraph() {
    for (Node n : nodes) {
      n.adjlistResid.clear();
    }

    for (Node n : nodes) {
      for (Edge e : n.adjlist) {
        if (e.flow > 0) {
          int backwardCapacity = e.flow;
          this.addResidualEdge(e.n2, e.n1, backwardCapacity, true);
        }
        if (e.capacity > e.flow) {
          int forwardFlow = e.capacity - e.flow;
          this.addResidualEdge(e.n1, e.n2, forwardFlow, false);
        }
      }
    }

  } // constructResidualGraph()

  //=========================================================

  private int findBottleneck(ArrayList<Edge> path) {
    int bottleneck = 500;
    for (Edge e : path) {
      if (e.capacity < bottleneck) {
        bottleneck = e.capacity;
      }
    }
    return bottleneck;
  } // findBottleneck()

  //=========================================================

  private void augment(ArrayList<Edge> path) {
    int bottleneck = findBottleneck(path);
    for (Edge eResid : path) {
      if (!(eResid.backward)) {
        for (Edge eGraph : eResid.n1.adjlist) {
            if (eGraph.n1.name == eResid.n1.name && eGraph.n2.name == eResid.n2.name) {
              eGraph.flow += bottleneck;
            }
          }
      } else {
        for (Edge eGraph : eResid.n1.adjlist) {
            if (eGraph.n1.name == eResid.n2.name && eGraph.n2.name == eResid.n1.name) {
              eGraph.flow -= bottleneck;
            }
          }
        }
    }
  } // augment()

  //=========================================================

  public int maxFlow(Node s, Node t) {
    boolean goodFlow;
    this.constructResidualGraph();
    ArrayList<Edge> path = findPathInResid(s, t);
    while (!(path.isEmpty())) {
      augment(path);
      goodFlow = checkFlow(s, t);
      if (!(goodFlow)) {return -1;}
      this.constructResidualGraph();
      path = findPathInResid(s, t);
    }

    int flow = 0;
    for (Edge e : s.adjlist) {
      flow += e.flow;
    }

    return flow;
  } // maxFlow()
}
