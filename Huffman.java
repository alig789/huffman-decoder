//************************************************
//************************************************

//No additional imports, make do with these

//streams are for reading/writing bytes
import java.io.FileInputStream;
import java.io.FileOutputStream;
//for bits used provided BitInputStream and BitOutputStream

//readers/writers are for reading/writing characters
import java.io.FileReader;
import java.io.FileWriter;

//parent of all checked I/O exceptions
import java.io.IOException;

//pretty much all the data structures
import java.util.*;

//************************************************
//************************************************




//the class with the main method
class Huffman {
	
	public static boolean isLeaf(byte[] bytes, int index){
		/*if((index*2)+2 >= bytes.length){//It's in the last row, so it's a leaf for sure
			return true;
		}
		if ((bytes[(index*2)+1]) == 0 && (bytes[(index*2)+2] == 0)){ //The two children are both null, so it's a leaf
			return true;
		}
		return false;
	*/
	
	//The only nodes that aren't leaves are header nodes. We can easily check if a node has the header node value.
		if (bytes[index] == 1){
			return false;
		}
		else{
			return true;
		}
}
	
	
	
	public static void main(String[] args) {
		if(args.length != 1) System.out.println("Usage: java Huffman <inputFile>");
		BitInputStream bis;
		try{ //open the file	
			bis = new BitInputStream(new FileInputStream(args[0]));
		}
		catch (Exception e){
			System.out.println("Couldn't find the file");
			return;
		}
		

		
		byte[] multipleBytes = new byte[4]; //magic number is always 4 bytes, can be hardcoded
		int count = bis.read(multipleBytes);
		if (count == 4){
			for (int i = 0; i < multipleBytes.length; i++){//check if magic number is correct
				if (multipleBytes[i] != 0){
					System.out.println("Wrong magic number");
					return;
				}
			}
		}
		byte height = (byte) bis.readNext(); //get height of tree
		
		int totalNodes = (int)Math.pow(2,height+1)-1;//total number of nodes in tree is 2^(n+1)-1
		
		byte[] treeNodes = new byte[totalNodes]; 
		count = bis.read(treeNodes);
		if (count != totalNodes){
			System.out.println("Didn't read the right number of nodes");
			return;
		}
		
		bis.startBitMode(); //start reading individual bits
		int bit; //current bit
		int index = 0; //Pointer to current node in the tree (array), start at root
		//String s = ""; //decoded message. If we work with larger messages, might be better to write output directly to file line by line
		int current; //current decoded node
		
		
		try{//Write output to file
			FileWriter myWriter =new FileWriter(args[0] + ".txt");
			    
			
		
			while (bis.hasNext()){
				bit = bis.readNext();
				

				if (bit == 0){ //go to left child
					index = (index*2)+1;

					if (isLeaf(treeNodes, index)){ //Check if its a leaf node, else w
						current = treeNodes[index];

						if (current == 3){//end of file
							//System.out.println("end of file");
							break;
						}
						myWriter.write((char)current);
						index = 0;//reset to head
					}

				}
				if (bit == 1){//go to right child
					index = (index*2)+2;
					if (isLeaf(treeNodes, index)){
						current = treeNodes[index]; //check for end of file
						if (current == 3){//end of file
							break;
						}
						myWriter.write((char)current);
						index = 0;//reset to head
					}
				}
			}
			myWriter.close();
		}
		
		catch (Exception e){
			System.out.println("Couldn't open file to write.");
			return;
		}
		
		
		bis.close();// remember to close bitstream	
		return;
	}
}

