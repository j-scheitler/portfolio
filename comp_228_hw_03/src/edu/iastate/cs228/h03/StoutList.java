package edu.iastate.cs228.h03;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
  /**
   * Default number of elements that may be stored in each node.
   */
  private static final int DEFAULT_NODESIZE = 4;
  
  /**
   * Number of elements that can be stored in each node.
   */
  private final int nodeSize;
  
  /**
   * Dummy node for head.  It should be private but set to public here only  
   * for grading purpose.  In practice, you should always make the head of a 
   * linked list a private instance variable.  
   */
  public Node head;
  
  /**
   * Dummy node for tail.
   */
  private Node tail;
  
  /**
   * Number of elements in the list.
   */
  private int size;
  
  /**
   * Constructs an empty list with the default node size.
   */
  public StoutList()
  {
    this(DEFAULT_NODESIZE);
  }

  /**
   * Constructs an empty list with the given node size.
   * @param nodeSize number of elements that may be stored in each node, must be 
   *   an even number
   */
  public StoutList(int nodeSize)
  {
    if (nodeSize <= 0 || nodeSize % 2 != 0) 
    {
    	throw new IllegalArgumentException();
    }
    
    // dummy nodes
    head = new Node();
    tail = new Node();
    head.next = tail;
    tail.previous = head;
    this.nodeSize = nodeSize;
  }
  
  /**
   * Constructor for grading only.  Fully implemented. 
   * @param head
   * @param tail
   * @param nodeSize
   * @param size
   */
  public StoutList(Node head, Node tail, int nodeSize, int size)
  {
	  this.head = head; 
	  this.tail = tail; 
	  this.nodeSize = nodeSize; 
	  this.size = size; 
  }

  @Override
  public int size()
  {
    return this.size;
  }
  
  @Override
  public boolean add(E item)
  {
	if(item == null)
	{
		throw new NullPointerException();
	}
	//if the list empty
	if(size == 0) 
	{
		Node newNode = new Node();
		newNode.addItem(item);
		
		head.next = newNode;
		tail.previous = newNode;
		
		newNode.previous = head;
		newNode.next = tail;
	}
	//the list is not empty
	//check if there is still space in the node 
	else if(tail.previous.count == nodeSize)
	{
		Node newNode = new Node();
		newNode.addItem(item);
		
		tail.previous.next = newNode;
		newNode.previous = tail.previous;
		
		tail.previous = newNode;
		newNode.next = tail;
	}
	//there is no space in the current node
	//make a new node
	else 
	{
		Node curNode = tail.previous;
		tail.previous.addItem(item);
	}
	size++;
    return true;
  }

  @Override
  public void add(int pos, E item)
  {
    //if pos is out of bound
	 if (pos < 0 || pos > size)
	 {
		 throw new IndexOutOfBoundsException(); 
	 }
	 
	 if(head.next == tail)
	 {
		 Node curNode = new Node();
		 curNode.addItem(0,item);
		
		 head.next = curNode;
		 tail.previous = curNode;
		 
		 curNode.next = tail;
		 curNode.previous = head;
	 }
	 else if(find(pos).offset == 0 && (find(pos).node.next == tail || find(pos).node.previous != head))
	 {
		 NodeInfo nodeInfo = find(pos);
		 
		 
		 if(nodeInfo.node.next == tail && nodeInfo.node.count == nodeSize) 
		 {
			 Node newAdd = new Node();
			 newAdd.addItem(0, item);
			
			 newAdd.previous = tail.previous;
			 tail.previous.next = newAdd;
			 
			 newAdd.next = tail;
			 tail.previous = newAdd;
		 }
		 else if (nodeInfo.node.previous != head && nodeInfo.node.previous.count < nodeSize) 
		 {
			 nodeInfo.node.previous.addItem(item);
		 }
	 }
	 else if(find(pos).node.count < nodeSize) 
	 {
		 NodeInfo nodeInfo = find(pos);
		 
		 nodeInfo.node.addItem(nodeInfo.offset, item);
	 }
	 else 
	 {
		 
		 NodeInfo nodeInfo = find(pos);
		 Node next = new Node();
		 
		 nodeInfo.node.next.previous = next;
		 next.next = nodeInfo.node.next;
		 next.previous = nodeInfo.node;
		 nodeInfo.node.next = next;
		 
		 E[] array = (E[]) new Comparable[nodeSize / 2];
		 
		 int index = 0;
		 
		 while(nodeInfo.node.data[nodeSize / 2] != null)
		 {
			 array[index] = nodeInfo.node.data[nodeSize / 2];
			 nodeInfo.node.removeItem(nodeSize / 2);
			 index++;
		 }
		 
		 for(int j = 0; j < array.length; j++) 
		 {
			 next.addItem(array[j]);
		 }
		 
		 if(nodeInfo.offset <= nodeSize / 2) 
		 {
			 nodeInfo.node.addItem(nodeInfo.offset, item);
		 }
		 else 
		 {
			 next.addItem((nodeInfo.offset - nodeSize / 2), item);
		 }
		 
	 }
	 size++;	 
  }

  @Override
  public E remove(int pos)
  {
    if(pos < 0 || pos > size) 
    {
    	throw new IndexOutOfBoundsException();
    }
    
    NodeInfo nodeInfo = find(pos);
    E remove_from = null;
    
    
    if(nodeInfo.node == tail.previous && nodeInfo.node.count == 1) 
    {
    	tail.previous = nodeInfo.node.previous;
    	nodeInfo.node.previous.next = tail;
    	remove_from = nodeInfo.node.data[0];
    	nodeInfo.node.removeItem(0);
    }
    
    else if(nodeInfo.node.next == tail || nodeInfo.node.count > (nodeSize / 2)) 
    {
    	remove_from = nodeInfo.node.data[nodeInfo.offset];
    	nodeInfo.node.removeItem(nodeInfo.offset);
    }
    else 
    {
    	Node next = nodeInfo.node.next;
    	
    	if(next.count > nodeSize / 2) 
    	{
    		remove_from = nodeInfo.node.data[nodeInfo.offset];
    		
    		nodeInfo.node.removeItem(nodeInfo.offset);
    		
    		nodeInfo.node.addItem(next.data[0]);
    		
    		next.removeItem(0);
    	}
    	
    	else 
    	{
    		E[] list = (E[]) new Comparable[nodeSize];
    		int index = 0;
    		
    		for(int i = 0; i < nodeSize; i++) 
    		{
    			if(next.data[i] != null) 
    			{
    				list[index] = next.data[i];
    				index++;
    			}
    		}
    		
    		remove_from = nodeInfo.node.data[nodeInfo.offset];
    		nodeInfo.node.removeItem(nodeInfo.offset);
    		index = 0;
    		int location = nodeInfo.offset +1;
    		
    		while(list[index] != null) 
    		{
    			nodeInfo.node.data[location] = list[index];
    			location++;
    			index++;
    		}
    		
    		nodeInfo.node.next = next.next;
    		next.next.previous = nodeInfo.node;
    	}
    	
    }
    
    size--;
    return remove_from;
  }

  /**
   * Sort all elements in the stout list in the NON-DECREASING order. You may do the following. 
   * Traverse the list and copy its elements into an array, deleting every visited node along 
   * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting 
   * efficiency is not a concern for this project.)  Finally, copy all elements from the array 
   * back to the stout list, creating new nodes for storage. After sorting, all nodes but 
   * (possibly) the last one must be full of elements.  
   *  
   * Comparator<E> must have been implemented for calling insertionSort().    
   */
  public void sort()
  {
	  E[] sortList = (E[]) new Comparable[size];
	  
	  int index = 0;
	  Node curNode = head.next;
	 
	  while(curNode != tail)
	  {
		  for(int k = 0; k < curNode.count; k++) 
		  {
			  sortList[index] = curNode.data[k];
			  index++;
		  }
		  curNode = curNode.next;
	  }
	  
	  head.next = tail;
	  tail.previous = head;
	  
	  insertionSort(sortList, new ElementComparator());
  }
  
  /**
   * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
   * method.  After sorting, all but (possibly) the last nodes must be filled with elements.  
   *  
   * Comparable<? super E> must be implemented for calling bubbleSort(). 
   */
  public void sortReverse() 
  {
	  E[] reverseList = (E[]) new Comparable[size];
	  
	  int index = 0;
	  Node curNode = head.next;
	 
	  while(curNode != tail) 
	  {
		  for(int i = 0; i < curNode.count; i++) 
		  {
			  reverseList[index] = curNode.data[index];
			  index++;
		  }
		  curNode = curNode.next;
	  }
	 
	  head.next = tail;
	  tail.previous = head;
	  
	  bubbleSort(reverseList);
	  size = 0;
	
	  for(int i = 0; i < reverseList.length; i++) 
	  {
		  add(reverseList[i]);
	  }
  }
  
  @Override
  public Iterator<E> iterator()
  {
    return new stoutIterator();
  }

  @Override
  public ListIterator<E> listIterator()
  {
    return new StoutListIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index)
  {
    return new StoutListIterator(index);
  }
  
  /**
   * Returns a string representation of this list showing
   * the internal structure of the nodes.
   */
  public String toStringInternal()
  {
    return toStringInternal(null);
  }

  /**
   * Returns a string representation of this list showing the internal
   * structure of the nodes and the position of the iterator.
   *
   * @param iter
   *            an iterator for this list
   */
  public String toStringInternal(ListIterator<E> iter) 
  {
      int count = 0;
      int position = -1;
     
      if (iter != null) {
          position = iter.nextIndex();
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Node current = head.next;
      while (current != tail) {
          sb.append('(');
          E data = current.data[0];
          if (data == null) {
              sb.append("-");
          } else {
              if (position == count) {
                  sb.append("| ");
                  position = -1;
              }
              sb.append(data.toString());
              ++count;
          }

          for (int i = 1; i < nodeSize; ++i) {
             sb.append(", ");
              data = current.data[i];
              if (data == null) {
                  sb.append("-");
              } else {
                  if (position == count) {
                      sb.append("| ");
                      position = -1;
                  }
                  sb.append(data.toString());
                  ++count;

                  // iterator at end
                  if (position == size && count == size) {
                      sb.append(" |");
                      position = -1;
                  }
             }
          }
          sb.append(')');
          current = current.next;
          if (current != tail)
              sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
  }


  /**
   * Node type for this list.  Each node holds a maximum
   * of nodeSize elements in an array.  Empty slots
   * are null.
   */
  private class Node
  {
    /**
     * Array of actual data elements.
     */
    // Unchecked warning unavoidable.
    public E[] data = (E[]) new Comparable[nodeSize];
    
    /**
     * Link to next node.
     */
    public Node next;
    
    /**
     * Link to previous node;
     */
    public Node previous;
    
    /**
     * Index of the next available offset in this node, also 
     * equal to the number of elements in this node.
     */
    public int count;

    /**
     * Adds an item to this node at the first available offset.
     * Precondition: count < nodeSize
     * @param item element to be added
     */
    void addItem(E item)
    {
      if (count >= nodeSize)
      {
        return;
      }
      data[count++] = item;
      //useful for debugging
      //System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
    }
  
    /**
     * Adds an item to this node at the indicated offset, shifting
     * elements to the right as necessary.
     * 
     * Precondition: count < nodeSize
     * @param offset array index at which to put the new element
     * @param item element to be added
     */
    void addItem(int offset, E item)
    {
      if (count >= nodeSize)
      {
    	  return;
      }
      for (int i = count - 1; i >= offset; --i)
      {
        data[i + 1] = data[i];
      }
      ++count;
      data[offset] = item;
      //useful for debugging 
//      System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
    }

    /**
     * Deletes an element from this node at the indicated offset, 
     * shifting elements left as necessary.
     * Precondition: 0 <= offset < count
     * @param offset
     */
    void removeItem(int offset)
    {
      E item = data[offset];
      for (int i = offset + 1; i < nodeSize; ++i)
      {
        data[i - 1] = data[i];
      }
      data[count - 1] = null;
      --count;
    }    
  }
  
  	/**
	 * Helper class to represent a specific point of the list
	 */
	private class NodeInfo {
		public Node node;
		public int offset;

		public NodeInfo(Node node, int offset) {
			this.node = node;
			this.offset = offset;
		}
	}

	/**
	 * Helper method to locate an specific item
	 * 
	 * @param pos position of item that needs a info
	 * @return NodeInfo of specific point of the list
	 */
	private NodeInfo find(int pos) {
		Node newNode = head.next;
		int curPos = 0;
	
		while (newNode != tail) 
		{
			if(curPos + newNode.count <= pos) 
			{
				curPos += newNode.count;
				newNode = newNode.next;
				continue;
			}
			
			//else {
			NodeInfo node = new NodeInfo(newNode, pos - curPos);
			return node;
			//}
		}
		//pos is outside the range
		return null;
	}
 
  private class StoutListIterator implements ListIterator<E>
  {
	  private static final int BEHIND = -1;
	  private static final int AHEAD = 1;
	  private static final int NONE = 0;
	    
	  private int curPos;
	  private int direction;
	  private Node curNode;
	 // private Node curNode;
	  	
	  
	  private E[] elementList;
	  // constants you possibly use ...   
	  
	// instance variables ... 
	  
    /**
     * Default constructor 
     */
    public StoutListIterator()
    {
    	curNode = head.next;
    	curPos = 0;
    	direction = NONE;
		makeList();
    	
    }

    /**
     * Constructor finds node at a given position.
     * @param pos
     */
    public StoutListIterator(int pos)
    {
    	if (pos < 0 || pos > size) throw new IndexOutOfBoundsException("" + pos);
    	if(pos == size)
    	{
    		curNode = tail.previous;
    	}
    	else 
    	{
    		curNode = find(pos).node;
    	}
    	curPos = pos;
    	direction = NONE;
    	makeList();
    	//curNode = curNode.data[pos];
    }

    private void makeList() {
    	elementList = (E[]) new Comparable[size];
    	
    	int pos = 0;
    	Node curNode = head.next;
    	while(curNode != tail) 
    	{
    		for(int i = 0; i < curNode.count; i++) 
    		{
    			elementList[pos] = curNode.data[i];
    			pos++;
    		}
    		curNode = curNode.next;
    	}
    }
    
    @Override
    public boolean hasNext()
    {
    	return curPos < size;
    }

    @Override
    public E next()
    {
    	if (!hasNext()) 
    	{
    		throw new NoSuchElementException();
    	}
    	
    	int curOffset = find(curPos).offset;
    	E ret = curNode.data[curOffset];
    	
    	if(curOffset == curNode.count - 1 && curNode.next != tail)
    	{
    		curNode = curNode.next;
    	}
        direction = AHEAD;
        curPos++;
        return ret;
    }

    @Override
    public void remove()
    {
    	if(direction == NONE) 
    	{
    		throw new IllegalStateException();
    	}
    	else if(direction == BEHIND) 
    	{
    		StoutList.this.remove(curPos - 1);
    		makeList();
    		direction = NONE;
    		curPos--;
    		if(curPos < 0) {
    			curPos = 0;
    		}
    	}
    	else 
    	{
    		StoutList.this.remove(curPos);
    		makeList();
    		direction = NONE;
    	}
    }

	@Override
	public boolean hasPrevious() {
		return curPos > 0;
	}

	@Override
	public E previous() {
		if (!hasPrevious()) 
		{
			throw new NoSuchElementException();
		}
		//curNode = curNode.previous;
		curPos--;
	    direction = AHEAD;
	    return curNode.data[curPos];
	}

	@Override
	public int nextIndex() {
		return curPos;
	}

	@Override
	public int previousIndex() {
		
		return curPos - 1;
	}

	@Override
	public void set(E e) {
		if (direction == NONE)
	      {
	        throw new IllegalStateException();
	      }
	      
	      if (direction == BEHIND)
	      {
	    	 int curOffset = find(curPos - 1).offset;
	    	 if(previousIndex() < 0) 
	    	 {
	    		 Node pred = curNode.previous;
	    		 pred.data[pred.count -1] = e;
	    	 }
	    	 else 
	    	 {
	    		 curNode.data[curOffset] = e;
	    	 }
	      }
	      else
	      {
	    	int curOffset = find(curPos).offset;
	    	curNode.data[curOffset] = e;
	      }
	}
	
	public void add(E item) {
		if(item == null) 
		{
			throw new NullPointerException();
		}
		
		StoutList.this.add(curPos, item);
		curPos++;
		makeList();
		direction = NONE;
	}
    
    // Other methods you may want to add or override that could possibly facilitate 
    // other operations, for instance, addition, access to the previous element, etc.
    // 
    // ...
    // 
  }
  
  private class stoutIterator implements Iterator<E>{
	private E pending;
	private E cursor;
	private Node currentNode;
	private int curPos;

	public stoutIterator(){
		currentNode = head.next;
		if(size!= 0) 
		{
			cursor = currentNode.data[0];
		}
		pending = null;
		curPos = 0;
	}
	@Override
	public boolean hasNext() {
		return cursor != null;
	}

	@Override
	public E next() {
		if(!hasNext()) 
		{
			throw new NoSuchElementException();
		}
		
		pending = cursor;
		int curOffset = find(curPos).offset;
		
		if(curOffset < currentNode.count - 1)
		{
			cursor = currentNode.data[curOffset + 1];
		}
		else
		{
			currentNode = currentNode.next;
			if(currentNode != tail) {
			cursor = currentNode.data[0];
			}
			else {
			cursor = null;
			}
		}
		curPos++;
		return pending;
	}
	  
  }

  /**
   * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order. 
   * @param arr   array storing elements from the list 
   * @param comp  comparator used in sorting 
   */
  private void insertionSort(E[] arr, Comparator<? super E> comp)
  {
	  for(int i = 1; i < arr.length; i++)
		{
			E key = arr[i];
			int j = i -1;
			
			while((j >= 0) && (comp.compare(arr[j], key) > 0))
			{
				arr[j+ 1] = arr[j];
				j --;
			}
			
			arr[j + 1] = key;
		}
  }
  
  /**
   * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a 
   * description of bubble sort please refer to Section 6.1 in the project description. 
   * You must use the compareTo() method from an implementation of the Comparable 
   * interface by the class E or ? super E. 
   * @param arr  array holding elements from the list
   */
  private <T extends Comparable<? super E>> void bubbleSort(E[] arr)
  {
	  int n = arr.length;
	  for(int i = 0; i < n - 1; i++) 
	  {
		  for(int j = 0; j < n - i - 1; j++) 
		  {
			  if(arr[j].compareTo(arr[j+1]) < 0) 
			  {
				  E temp = arr[j];
				  arr[j]  = arr[j + 1];
				  arr[j + 1] = temp;
			  }
		  }
	  }
  }
  
  
  class ElementComparator<E extends Comparable<E>> implements Comparator<E>{
	  @Override
	  public int compare(E item, E item2) {
		  if(item == null || item2 == null) 
		  {
			  throw new IllegalArgumentException();
		  }
		  return item.compareTo(item2);
		  
	  }
  }
 

}