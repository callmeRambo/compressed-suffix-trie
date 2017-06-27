package suffix_trie;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


public class CompactCompressedSuffixTrie {
	mNode root;
	int datalen;
	public mNode getRoot() {
		return root;
	}
	// readData reads file and return the DNA sequence(A OR C OR G OR T)
	public static String readData(String file) throws IOException{
		String str="";
		FileReader in = new FileReader(file);
		int c;
        while ((c=in.read())!=-1){
        	if (c==65||c==67||c==71||c==84)
        		str=str+(char)(c);
        };
      return str;
	}
	public void setRoot(mNode root) {
		this.root = root;
	}
	//There's where I construct the trie.
	//Firstly I recursively read the string and build the trie, which takes n^2 time complexity 
	//Then doing compressing, for each branch, if the child has only 1 "grandchild", combine it to root
	//This operation also takes n^2 because I use BFS(a queue) to visit all nodes in trie.
	//There's a optimal way to build trie in O(n) by using UKKONENS algorithm, by using 2 pointers to represent a branch, but Im still working on it.
	//In UKKONEN'S algorithm when insert a char, if this char is not in children of root, modify every pointer with this char
	//etc: [0,#] represents from position 0 of this string to position#
	//when insert a new child, the root has [0,#] and [1,#] 2 children.
	//If this char is in child, match the next char till unmatch or no char left, if unmatch, split into another branch.
	//The next insert move is still the above matched first char's next.
	//Because every step takes O(1) that's why UKKONEN'S algorithm overall time complexity is O(n)
	public CompactCompressedSuffixTrie( String f ) throws IOException // Create a compact compressed suffix trie from file f
	{
		String data =readData(f);
		this.datalen = data.length();
		setRoot(new mNode(null,null)); 
		for (int i = 0; i < data.length(); i++) {
			mNode current_node = root;
			addSubtree(data.substring(i),current_node);
		}
		ArrayList<mNode> L = new ArrayList<mNode>();
		L.addAll(root.children);
	}
	//recursively add subtree there, O(n^2)
	public void addSubtree(String f, mNode m_N){
		if (!f.isEmpty())
		{
			int child_index = search(f.charAt(0)+"",m_N.children);
			if (child_index!=-1){
				for (int j=0; j<m_N.children.size();j++){
					addSubtree(f.substring(1),m_N.children.get(child_index));
				}
			}
			else{
				m_N.addChildren(new mNode(f.charAt(0)+"",m_N));
				m_N.children.get(m_N.children.size()-1).setIndex(f.length());
				addSubtree(f.substring(1),m_N.children.get(m_N.children.size()-1));
			}
		}
	}
	//there I classify the branch, which the new node belongs with.
	public int search (String c,ArrayList<mNode> children){
		for (int i=0; i<children.size();i++){
			if (c.equals(children.get(i).getValue())){
				return i;
			}
		}
		return -1;	
	}
	//Use BFS to visit all nodes and compress.O(n^2)
	public void visitAndCompressTree(ArrayList<mNode> L){
		mNode newRoot = L.remove(0);
		L.addAll(newRoot.children);
		if (L.size()==0)
			return;	
		compressTree(newRoot);
		visitAndCompressTree(L);
	}
	//compress the child to root.
	//I choose to use child to replace root because it reduct complexity
	//in modifying the grandson's pre.
	public void compressTree(mNode root){
		if (root.children.size()==1){
			if (root.pre.equals(this.root)){
				return;
			}
			root.children.get(0).pre = root.pre;
			root.children.get(0).value = root.value+root.children.get(0).value;
			root.pre.children.add(root.children.get(0));
			root.pre.children.remove(root.pre.children.indexOf(root));
			root.children.get(0).setIndex(root.getIndex());
		}
	}
	
	
	/** Method for finding the first occurrence of a pattern s in the DNA sequence */
	//There findString function will call the function visitCompressTrie
	//Which returns the index of the last word in s,then index minus length of s is the first occurence.
	//The node in trie is mNode, which I defined has a index.
	//O(mn) because every match will make the length of m and n decrease.
	public int findString( String s )
	{
		int stringLen = this.datalen;
		ArrayList<mNode> L = new ArrayList<mNode>();
		L.addAll(root.children);
		int index=visitCompressTrie(s,L,-1);
		if (index!=-1){
			//the "right" index should be (stringlen-index) because when
			//set the index for each node, I use a trick by counting the remaining words in DNAsequence.
			//so still, the last word's index - s.length
			return stringLen-index+1-s.length();
		}
		else return -1;
	}
	// recursively visit trie and return the index.
	public int visitCompressTrie(String s,ArrayList<mNode> L,int index){
		if (s.isEmpty()){
			return index;
		}
		if (L.isEmpty()){
			return -1;
		}
		mNode newRoot = L.remove(0);
		String tempS=matchString(newRoot.value,s);
		if (!tempS.equals(s)){
			L.addAll(newRoot.children);
			s = tempS;
		}
		index = newRoot.getIndex();
		index=visitCompressTrie(s,L,index);
		
		return index;
		
	}
	// There if there's some match, return the unmatch parts.
	public String matchString(String s1,String s2){
		if (s2.startsWith(s1)){
			return s2.substring(s1.length());
		}
		if (s1.startsWith(s2)){
			return "";
		}
		return s2;
	}
	/** Method for finding k longest common substrings of two DNA sequences stored
	in the text files f1 and f2 */
	// To find kLongestSubstring, i generate a table with every word in f1 and f2.
	// Then every cell in the table is a object,storing the sequence and the index.
	// So after filling the table, we can find the longest sequence.
	// Next time we remove these words in the string and fill the table again for k times.
	// Every time we generate the table will take O(mn) because the table is m*n size.
	// Then we do k times O(kmn)
	public static void kLongestSubstrings(String f1, String f2, String f3, int k)
	{
		String result = "";
		try {
			f1 = readData(f1);
			f2 = readData(f2);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i < k; i++) {
			GhostInCell ghost_temp = findLongest(f1,f2);
			GhostInCell ghost = new GhostInCell(ghost_temp.getCommon(), 
					ghost_temp.getStartingpoint1(), ghost_temp.getStartingpoint2());
			
			result = result+ghost.getCommon()+"\r\n";
			f1 = f1.substring(0,ghost.getStartingpoint1())+
					f1.substring(1+ghost.getStartingpoint1()+ghost.getlen());
			f2 = f2.substring(0,ghost.getStartingpoint2())+
					f2.substring(1+ghost.getStartingpoint2()+ghost.getlen());
		}
    try {
		FileWriter fileWritter = new FileWriter(f3,false);
		fileWritter.write(result);
		fileWritter.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	//there i generate the table and find the longest.
	public static GhostInCell findLongest(String f1,String f2){
		GhostInCell result = new GhostInCell("",0,0);
		GhostInCell[][] matrix = new GhostInCell[f1.length()][f2.length()];
		for (int i = 0; i < f1.length(); i++) {
			for (int j = 0; j < f2.length(); j++) {
				matrix[i][j] = new GhostInCell("",i,j);
				if (f1.charAt(i)==f2.charAt(j)){
					if (i==0||j==0)
						{
						matrix[i][j].setCommon(f1.charAt(0)+"");
						continue;
						}
					matrix[i][j].setCommon(matrix[i-1][j-1].getCommon()+f1.charAt(i));
					matrix[i][j].setStartingpoint1(matrix[i-1][j-1].getStartingpoint1());
					matrix[i][j].setStartingpoint2(matrix[i-1][j-1].getStartingpoint2());
					if (i==f1.length()-1||j==f2.length()-1)
					{
						if (matrix[i][j].getlen()>result.getlen()){
							result.setCommon(matrix[i][j].getCommon());
							result.setStartingpoint1(matrix[i][j].getStartingpoint1());
							result.setStartingpoint2(matrix[i][j].getStartingpoint2());
						}
					}
				}
				else{
					if (i==0||j==0)
					{
						continue;
					}
					if (matrix[i-1][j-1].getlen()>result.getlen()){
						result.setCommon(matrix[i-1][j-1].getCommon());
						result.setStartingpoint1(matrix[i-1][j-1].getStartingpoint1());
						result.setStartingpoint2(matrix[i-1][j-1].getStartingpoint2());
					}
				}
			}
		}
	return result;
	}
	//mNode is for the trie.
	static class mNode{
		mNode pre;
		String value;
		ArrayList<mNode> children;
		int index;
		public mNode(String value, mNode pre){
			this.value = value;
			this.pre = pre;
			this.children = new ArrayList<mNode>();
			this.index= -1;
		}
		public mNode(String value, mNode pre, int index){
			this.value = value;
			this.pre = pre;
			this.children = new ArrayList<mNode>();
			this.index = index;
		}
		public mNode getPre() {
			return pre;
		}
		public void setPre(mNode pre) {
			this.pre = pre;
		}
		public ArrayList<mNode> getChildren() {
			return children;
		}
		public void setChildren(ArrayList<mNode> children) {
			this.children = children;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}

		public void addChildren(mNode e) {
			children.add(e);
			e.setPre(this);
		}
		public mNode(mNode pre, String value, ArrayList<mNode> children) {
			super();
			this.pre = pre;
			this.value = value;
			this.children = children;
		}
		public int getIndex() {
			return index;
		}
		public void setIndex(int index) {
			this.index = index;
		}
	}
	//GhostInCell class is for the table. 
	static class GhostInCell{
		String common;
		int startingpoint1;
		int startingpoint2;
		public GhostInCell(String common, int startingpoint1, int startingpoint2) {
			super();
			this.common = common;
			this.startingpoint1 = startingpoint1;
			this.startingpoint2 = startingpoint2;
		}
		public String getCommon() {
			return common;
		}
		public void setCommon(String common) {
			this.common = common;
		}
		public int getStartingpoint1() {
			return startingpoint1;
		}
		public void setStartingpoint1(int startingpoint1) {
			this.startingpoint1 = startingpoint1;
		}
		public int getStartingpoint2() {
			return startingpoint2;
		}
		public void setStartingpoint2(int startingpoint2) {
			this.startingpoint2 = startingpoint2;
		}
		public int getlen(){
			return this.common.length();
		}	
	}
	static class mNode2{
		String value;
		int	index;
		mNode2[] next;
		public mNode2(String value, int index, mNode2[] next) {
			super();
			this.value = value;
			this.index = index;
			this.next = next;
		}
	}
	public static void main(String args[]) throws Exception{
	        
		 /** Construct a compact compressed suffix trie named trie1
		  */       
		 CompactCompressedSuffixTrie trie1 = new CompactCompressedSuffixTrie("file1.txt");

		 System.out.println("ACTTCGTAAG is at: " + trie1.findString("ACTTCGTAAG"));
//
		 System.out.println("AAAACAACTTCG is at: " + trie1.findString("AAAACAACTTCG"));
//		         
		 System.out.println("ACTTCGTAAGGTT : " + trie1.findString("ACTTCGTAAGGTT"));
//		         
		 CompactCompressedSuffixTrie.kLongestSubstrings("file2.txt", "file3.txt", "file4.txt", 6);
	}
}
