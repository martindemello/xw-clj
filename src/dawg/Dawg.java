package twinfeats;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeMap;

/*
Directed Acyclic Word Graphs

original algorithm concept and description at http://www.wutka.com/dawg.html
This implementation and revised text by Kent L. Smotherman http://www.twinfeats.com

A Directed Acyclic Word Graph, or DAWG, is a data structure that permits
extremely fast word searches. The entry point into the graph represents 
the starting letter in the search. Each node represents a letter, and you 
can travel from the node to two other nodes, depending on whether you the 
letter matches the one you are searching for.

It's a Directed graph because you can only move in a specific direction 
between two nodes. In other words, you can move from A to B, but you can't 
move from B to A. It's Acyclic because there are no cycles. You cannot have 
a path from A to B to C and then back to A. The link back to A would create 
a cycle, and probably an endless loop in your search program.

The description is a little confusing without an example, so imagine we have 
a DAWG containing the words CAT, CAN, DO, and DOG. The graph woud look like this:

     C --Child--> A --Child--> N (EOW)
     |                         |
     |                       Next
   Next                        |
     |                         v
     |                         T (EOW)
     v
     D--Child--> O (EOW) --Child --> G (EOW)

Now, imagine that we want to see if CAT is in the DAWG. We start at the entry 
point (the C) in this case. Since C is also the letter we are looking for, we 
go to the child node of C. Now we are looking for the next letter in CAT, which 
is A. Again, the node we are on (A) has the letter we are looking for, so we 
again pick the child node which is now N. Since we are looking for T and the 
current node is not T, we take the Next node instead of the child. The Next 
node of N is T. T has the letter we want. Now, since we have processed all 
the letters in the word we are searching for, we need to make sure that the 
current node has an End-of-word flag (EOW) which it does, so CAT is stored in 
the graph.

One of the tricks with making a DAWG is trimming it down so that words with 
common endings all end at the same node. For example, suppose we want to store 
DOG and LOG in a DAWG. The ideal would be something like this:

   D --Child--> O --Child--> G(EOW)
   |            ^
  Next          |
   |            |
   v            |
   L --Child----

In other words, the OG in DOG and LOG is defined by the same pair of nodes.
 
Creating a DAWG

The idea is to first create a tree, where a leaf would represent the end of a 
word and there can be multiple leaves that are identical. For example, DOG and 
LOG would be stored like this:

  D --Child--> O --Child--> G (EOW)
  |
 Next
  |
  v
  L --Child-> O --Child--> G (EOW)

Now, suppose you want to add DOGMA to the tree. You'd proceed as if you were 
doing a search. Once you get to G, you find it has no children, so you add a 
child M, and then add a child A to the M, making the graph look like:

  D --Child--> O --Child--> G (EOW) --Child--> M --Child--> A (EOW)
  |
 Next
  |
  v
  L --Child-> O --Child--> G (EOW)

As you can see, by adding nodes to the tree this way, you share common 
beginnings, but the endings are still separated. To shrink the size of the 
DAWG, you need to find common endings and combine them. To do this, you start 
at the leaf nodes (the nodes that have no children). If two leaf nodes are 
identical, you combine them, moving all the references from one node to the 
other. For two nodes to be identical, they not only must have the same letter, 
but if they have Next nodes, the Next nodes must also be identical (if they 
have child nodes, the child nodes must also be identical).

Take the following tree of CITIES, CITY, PITIES and PITY:

 C --Child--> I --Child--> T --Child--> I --Child--> E --Child--> S (EOW)
 |                                      |
 |                                     Next
Next                                    |
 |                                      v
 |                                      Y (EOW)
 P --Child--> I --Child--> T --Child--> I --Child--> E --Child--> S (EOW)
                                        |
                                       Next
                                        |
                                        v
                                        Y (EOW)

Once I create the tree, I run through it and tag each node with a number of 
children and a child depth (the highest number of nodes you can go through 
from the current node to reach a leaf). Leaf nodes have 0 for the child
depth and 0 for the number of children. The main reason for the tagging is 
that when looking for identical nodes, you can quickly rule out nodes with 
a different number of children or a different child depth. When I tag the 
nodes, I also put them in an array of nodes with a similar child depth 
(again to speed searching).

Now that the nodes have been tagged and sorted by depth, I start with the 
children that have a depth of 0. If node X and node Y are identical, I make 
any node that points to Y as a child now point to X. Originally, I also 
allowed nodes that pointed to Y as a Next node point to X. While this works 
from a data structure standpoint, the implemented algorithm here does not
perform this step.

In the CITY-PITY graph, the algorithm would see that the S's at the end are 
the same. Although the Y nodes are identical, they are not referenced by any 
Child links, only Next links. As I said before, combining common next links 
make it difficult to store the graph the way I needed to. The graph would look 
like this after processing the nodes with a 0 child depth (leaf nodes):

 C --Child--> I --Child--> T --Child--> I --Child--> E --Child--> S (EOW)
 |                                      |                       ^
 |                                     Next                     |
Next                                    |                       |
 |                                      v                       |
 |                                      Y (EOW)                 |
 P --Child--> I --Child--> T --Child--> I --Child--> E --Child--
                                        |
                                       Next
                                        |
                                        v
                                        Y (EOW)

Next, the algorithm looks at nodes with a child depth of 1, which in this case 
would be the two E nodes (although T has 1-deep path to Y, it's child depth is 
3 because the longest path is the 3-deep path to S). In order to test that the 
two E's are identical, you first see that the letters are the same, which they 
are. Now you make sure that the children are identical. Since the only child of 
E is the same node, you KNOW they are identical. So the E's are now combined:

 C --Child--> I --Child--> T --Child--> I --Child--> E --Child--> S (EOW)
 |                                      |          ^
 |                                     Next        |
Next                                    |          |
 |                                      v          |
 |                                      Y (EOW)    |
 P --Child--> I --Child--> T --Child--> I --Child-->
                                        |
                                       Next
                                        |
                                        v
                                        Y (EOW)

Notice that the E and S nodes that come from the PITIES word are no longer 
needed. This technique pares the tree down pretty nicely.

Next come the nodes with a child depth of 2, which would be the I (the I's 
that have ES and Y as children) nodes. Applying the same comparison strategy, 
we can see that both the I nodes are identical, so they become combined. 
This procedure repeats for the T nodes and then the I nodes that are the 
parents of T. The last set of nodes, the C & P nodes, are not identical, 
so they can't be combined. The final tree looks like this:

 C --Child--> I --Child--> T --Child--> I --Child--> E --Child--> S (EOW)
 |            |                         |
 |            |                        Next
Next          |                         |
 |            |                         v
 |            |                         Y (EOW)
 P --Child---- 
 */
public class Dawg {
	static Node head = new Node();	//dummy head node, only so we have the children TreeMap
	static int count,words;	//just some stats counters
	
	static ArrayList[] depthList = new ArrayList[36];
		
	public Dawg() {
		super();
	}

	public static void main(String[] args) throws Exception {
		head.letter = '0';
		for (int i=0;i<36;i++) depthList[i] = new ArrayList();
		FileReader fr = new FileReader("word.list");
		BufferedReader reader = new BufferedReader(fr);
		while (true) {
			words++;
			String line = reader.readLine();
			if (line == null) break;
			line = line.trim();
			if (line.length() > 36) continue;	//only store words up to 36 letters long
			addToDawg(line,0,head);
		}
		System.out.println("starting nodes="+count+", words="+words);
		reader.close();
		fr.close();
		compress();
		count = 0;
		countNodes(head);
		System.out.println("ending nodes="+count);
		FileOutputStream fw = new FileOutputStream("words.dawg");
		BufferedOutputStream writer = new BufferedOutputStream(fw);
		clearCounts(head);
		outputTree(head,writer);
		writer.close();
		fw.close();
//		RandomAccessFile file = new RandomAccessFile("words.dawg","r");
//		long start = System.currentTimeMillis();
//		dumpWordsDAWG(file);
//		System.out.println((System.currentTimeMillis()-start));
//		dumpWords(head,buf,0);
	}
	
	static void addToDawg(String word, int idx, Node parent) {
		char c = word.charAt(idx);
		Character ch = new Character(c);
		Node node = (Node)parent.children.get(ch);
		if (node == null) {	//first time for this letter as child of parent letter
			node = new Node();
			node.parent = parent;
			node.letter = c;
			if (parent.children.size() > 0) {
				Node tempNode = (Node)parent.children.get(parent.children.lastKey());
				tempNode.next = node;
				node.prev = tempNode;
			}
			parent.children.put(ch,node);	//add letter to parent's list of letters
			node.maxDepth = 0;
			Node walk = node.parent;
			int depth = 0;
			while (walk != null) {
				depth++;
				if (walk.maxDepth < depth) {
					walk.maxDepth = depth;
				}
				walk.numChildren++;
				walk = walk.parent;
			}
		}
		if (idx+1 < word.length()) {	//not done with word
			addToDawg(word,idx+1,node);	//next letter
		}
		else {
			node.endOfWord = true;	//we have a complete word
		}
	}
	
	static void compress() {
		//first we need to build lists of nodes by their maxDepth
		buildDepthArrays(head);
		
		//next we start the node processing from depth 0 to depth 35 (depth is distance from leaf node)
		for (int i=0;i<36;i++) {
			processDepth(i);
		}
	}

	static void processDepth(int depth) {
		Collections.sort(depthList[depth]);
		for (int i=0;i<depthList[depth].size();i++) {
			Node n1 = (Node)depthList[depth].get(i);
			for (int j=i+1;j<depthList[depth].size();j++) {
				Node n2 = (Node)depthList[depth].get(j);
				if (compareSubTrees(n1,n2)) {
					if (compareNexts(n1,n2)) {
						n2.parent.children.put(new Character(n1.letter),n1);
						if (n2.prev != null) {
							n2.prev.next = n1;
						}
						depthList[depth].remove(j);
						j--;	//must reprocess due to the remove
					}
				}
				else {	//since the depth arrays are sorted, when we find a difference we can skip all the nodes up to this point since they must have been processed already
					i = j-1;
					break;
				}
			}
		}
	}

	static boolean compareNexts(Node p1, Node p2) {
		while (true) {
			if (p1 == p2) return true;
			if ((p1==null) != (p2==null)) return false;
			p1 = p1.next;
			p2 = p2.next;
			if (p1 != null && p2 != null) {
				if (!compareSubTrees(p1,p2)) {
					return false;
				}
			}
		}
	}
	
	static boolean compareSubTrees(Node n1, Node n2) {
		if (n1 == n2 || (n1.letter == n2.letter && n1.numChildren == n2.numChildren && n1.maxDepth == n2.maxDepth && n1.children.size() == n2.children.size() && n1.endOfWord == n2.endOfWord)) {	//might be same subtrie
			Iterator nodes1 = n1.children.values().iterator();
			Iterator nodes2 = n2.children.values().iterator();
			while (nodes1.hasNext()) {
				Node node1 = (Node)nodes1.next();
				Node node2 = (Node)nodes2.next();
				if (!compareSubTrees(node1,node2)) return false;
			}
			return true;
		}
		return false;
	}
	
	static void buildDepthArrays(Node parent) {
		if (parent != head) {
			depthList[parent.maxDepth].add(parent);
		}
		if (parent.children != null) {
			Iterator it = parent.children.values().iterator();
			while (it.hasNext()) {
				Node node = (Node)it.next();
				buildDepthArrays(node);
			}
		}
	}

	static void dumpWords(Node parent, StringBuffer word, int len) {
		word.setLength(len);
		if (parent != head) {
			word.append(parent.letter);
			len++;
		}
		if (parent.endOfWord) {
			System.out.println(word.toString());
		}
		if (parent.children != null) {
			Iterator it = parent.children.values().iterator();
			while (it.hasNext()) {
				Node node = (Node)it.next();
				dumpWords(node,word,len);
			}
		}
	}
	
	static void dumpWordsDAWG(RandomAccessFile file) throws Exception {
		StringBuffer word = new StringBuffer();
		int nextIdx = 0;
		do {
			file.seek(nextIdx*6);
			byte[] buf = new byte[6];
			file.readFully(buf);
			int v1 = ((buf[1]&0xff)>>4)<<16;
			int v2 = ((buf[4]&0xff)<<8);
			int v3 = buf[5]&0xff;
			nextIdx = v1+v2+v3;
			dumpWordsDAWG(file,0,word,0);
		} while(nextIdx != 0);
	}
	
	static void dumpWordsDAWG(RandomAccessFile file, int parentIdx, StringBuffer word, int len) throws Exception {
		word.setLength(len);
		file.seek(parentIdx*6);
		byte[] buf = new byte[6];
		file.readFully(buf);
		char c = (char)((buf[0]&0x1f) + 'a');
		word.append(c);
		len++;
		if ((buf[0]&0x80) != 0) {
//			System.out.println(word.toString());
		}
		int v1 = (buf[1]&0x0f)<<16;
		int v2 = ((buf[2]&0xff)<<8);
		int v3 = buf[3]&0xff;
		int childIdx = v1+v2+v3;
		v1 = ((buf[1]&0xff)>>4)<<16;
		v2 = ((buf[4]&0xff)<<8);
		v3 = buf[5]&0xff;
		int nextIdx = v1+v2+v3;
		if (childIdx != 0) {
			int walkIdx = childIdx;
			while (walkIdx != 0) {
				dumpWordsDAWG(file,walkIdx, word, len);
				file.seek(walkIdx*6);
				byte[] buf2 = new byte[6];
				file.readFully(buf2);
				v1 = ((buf2[1]&0xff)>>4)<<16;
				v2 = ((buf2[4]&0xff)<<8);
				v3 = buf2[5]&0xff;
				walkIdx = v1+v2+v3;
			}
		}
	}
	
	static void clearCounts(Node parent) {
		parent.counted = false;
		if (parent.children != null) {
			Iterator it = parent.children.values().iterator();
			while (it.hasNext()) {
				Node node = (Node)it.next();
				clearCounts(node);
			}
		}
	}

	static void countNodes(Node parent) {
		if (parent.children != null) {
			Iterator it = parent.children.values().iterator();
			Node n = null;
			if (parent.children.size() > 0)
				n = (Node)(parent.children.get(parent.children.firstKey()));
			while (it.hasNext()) {
				Node node = (Node)it.next();
				if (!node.counted) {
					node.counted = true;
					node.nodeNum = count++;
				}
				else {
//					if (n != node) System.out.println("sibling already counted");
				}
			}
			it = parent.children.values().iterator();
			while (it.hasNext()) {
				Node node = (Node)it.next();
				countNodes(node);
			}
		}
	}

	static void outputTree(Node parent, BufferedOutputStream writer) throws Exception {
		byte[] bytes;
		if (parent.children != null) {
			Iterator it = parent.children.values().iterator();
			while (it.hasNext()) {
				Node node = (Node)it.next();
				if (!node.counted) {
					node.counted = true;
					bytes = node.toBytes(!it.hasNext());
					writer.write(bytes);
				}
			}
			it = parent.children.values().iterator();
			while (it.hasNext()) {
				Node node = (Node)it.next();
				outputTree(node,writer);
			}
		}
	}
}

class Node implements Comparable {
	char letter;
	boolean endOfWord;
	TreeMap children = new TreeMap();
	
	Node parent;
	Node prev,next;
	int numChildren;
	int maxDepth;
	boolean counted;
	int nodeNum;
	
	
	public int compareTo(Object arg0) {
		Node n = (Node)arg0;
		if (letter < n.letter)
			return -1;
		if (letter > n.letter)
			return 1;
		if (numChildren < n.numChildren) 
			return -1;
		if (numChildren > n.numChildren)
			return 1;
		if (maxDepth < n.maxDepth)
			return -1;
		if (maxDepth > n.maxDepth)
			return 1;
		return 1;
	}


	Node() {
		Dawg.count++;
	}
	
	public byte[] toBytes(boolean endOfList) {
		byte[] bytes = new byte[6];
		for (int i=0;i<6;i++) bytes[i] = 0;
		if (endOfWord) {
			bytes[0] |= 0x80;
		}
		if (endOfList) {
			bytes[0] |= 0x40;
		}		
		byte c = (byte)(letter-'a');
		bytes[0] |= c;
		int num = 0;
		if (children.size() > 0) {
			Node n = (Node)(children.get(children.firstKey()));
			num = n.nodeNum;
		}
		bytes[1] = (byte)(num>>16);
		bytes[2] = (byte)((num&0xffff)>>8);
		bytes[3] = (byte)(num&0xff);
		if (next != null) {
			bytes[1] |= (byte)((next.nodeNum>>16)<<4);
			bytes[4] = (byte)((next.nodeNum&0xffff)>>8);
			bytes[5] = (byte)(next.nodeNum&0xff);
		}

		if ((next == null) != endOfList) {
			System.out.println("Inconsistent endOfList");
		}
		return bytes;
	}
}
