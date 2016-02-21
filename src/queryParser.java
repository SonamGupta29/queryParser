import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Vector;

public class queryParser {
	
	//public static RandomAccessFile invertedIndexFile = null;
	public static RandomAccessFile pidToTitleFile = null;
	public static RandomAccessFile innerIndexPidTitle = null;
	public static BufferedReader outerIndexPidTitle  = null;
	public static HashMap <Character, BufferedReader> outerindexMap = new HashMap<Character, BufferedReader>();
	public static HashMap <Character, RandomAccessFile> innerindexMap = new HashMap<Character, RandomAccessFile>();
	public static HashMap <Character, RandomAccessFile> dataStoreMap = new HashMap<Character, RandomAccessFile>();
	public static HashMap<String, Float> output = new HashMap<String, Float>();
	public static boolean isFieldQuery = false; 
	public static int totalWords = 0;
	public static int ZERORESULTS = 0;
 	static long startTime = 0;
	static long endTime = 0;
	
	public static void main(String[] args) throws Exception {
		
		System.out.print("Enter the query : ");
		Scanner in = new Scanner(System.in);
		String query = in.nextLine();
		preload();
		
		startTime = System.currentTimeMillis();		
		query = query.toLowerCase().replaceAll(".-|of","").trim();
		query = query.replaceAll("( )+"," ");
		parseQuery(query.toLowerCase().replaceAll("\\!\\@#\\$%+\\^\\&;\\*'.>\\-<"," ").trim());			
		endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (float)(endTime - startTime) / 1000 + " s");
	}

	private static void parseQuery(String query) throws Exception {
		
		//	break the query in words
		query =  query.replaceAll(": ", ":");
		int len = query.length();
		StringBuilder queryString = new StringBuilder(query);
		List <String> refinedQueryWords = new ArrayList <String>() ;
		stemmer s = new stemmer();
		String secWords[][] = new String[10][2];
		
		for(int i = 0; i < len; i++){			
			if(queryString.charAt(i) == ':') {
				isFieldQuery = true;
				if(i - 2 >= 0)
					queryString.setCharAt(i - 2, '$');
			}
		}
		
		if(isFieldQuery) {
			
			//tikenoze on "$"
			String queryWords[] = queryString.toString().split("\\$");
			len = queryWords.length;
			
			for(int i = 0; i < len; i++) {
				String secWord[] = queryWords[i].split(":");
				String queryWordsList[] = secWord[1].split(" ");
				int length = queryWordsList.length;
				//System.out.println(secWord[1]);
				for(int j = 0; j < length; j++) {
					s.add(queryWordsList[j].toCharArray(), queryWordsList[j].length());
					secWords[totalWords][0] = s.stem().toString();;
					secWords[totalWords++][1] = secWord[0]; 
				}
			}
		} else {
			//tokeinze on the basis of space " "
			String queryWordsList[] = query.split(" ");
			len = queryWordsList.length;
			for(int i = 0; i < len; i++) {
				secWords[i][0] = queryWordsList[i];
				totalWords++;
			}
		}		
		//	get the posting list of the query
		for(int i = 0; i < totalWords; i++){			
			mergeList(getList(secWords[i][0]), secWords[i][1], i + 1);
			if(ZERORESULTS == 1) {
				System.out.println("No matching document found...");
				System.exit(0);
			}				
		}
			
		//Iterate throught map
		int resultCount = 0;
		List <String> searchResultOutput = new ArrayList<String>();
		
		Iterator it = output.entrySet().iterator();
		while (it.hasNext()) {
			resultCount++;
			Map.Entry pair = (Map.Entry)it.next();
			searchResultOutput.add(pair.getKey() + "=" + pair.getValue());
			it.remove(); 
		}
		
		Collections.sort(searchResultOutput, new Comparator<String>(){
			@Override
			public int compare(String s1, String s2) {
				float value1 = Float.parseFloat(s1.split("=")[1]);
				float value2 = Float.parseFloat(s2.split("=")[1]);
				if(value1 == value2)
					return 0; 
				else if (value1 < value2)
					return -1;
				else
					return 1;
			}
		});
		
		//Load the inner map of pidtotitle map file in random access
		innerIndexPidTitle = new RandomAccessFile("innerIndex", "r");
		
		//Now load the pidtotitlemapfile into the memory
		pidToTitleFile = new RandomAccessFile("pIDToTitleMap", "r");
		
		
		System.out.println("\nSearch returned following results : \n");
		
		//What if I sort on the basis of pid and then fetch the index of the pid to title
		Vector <String> pid = new Vector<String>();
		for(int i = 0; i < resultCount ; i++) {
			pid.add((searchResultOutput.get(i).split("=")[0]));
		}
		
		Collections.sort(pid, new Comparator<String>(){
			@Override
			public int compare(String s1, String s2) {
				Integer value1 = Integer.parseInt(s1);
				Integer value2 = Integer.parseInt(s2);
				if(value1 == value2)
					return 0; 
				else if (value1 < value2)
					return -1;
				else
					return 1;
			}
		});
		
		int count = 0;
		//fetch the title from the pid	
		for(int i = 0; count < 10 && i < resultCount ; i++) {
			//(getTitleOfpId(searchResultOutput.get(i).split("=")[0]));
			String temp = getTitleOfpId(pid.get(i));
			//if(temp.compareTo("Wikipedia") == 0) continue;
			System.out.println(count + 1 + ":" + temp);
			count++;
		}
		
		innerIndexPidTitle.close();
		pidToTitleFile.close();
	}
	
	private static void closeAll() throws IOException {
		
		//Get the handle to the index files
		for(int i = 0; i < 26; i++) {			
			//indexMap.put((char)('a' + i), new BufferedReader(new FileReader(new File((char)('a' + i) + "index"))));
			innerindexMap.get((char)('a' + i)).close();
		}
		
		//Get the handles to the datastore file
		for(int i = 0; i < 26; i++) {			
			dataStoreMap.get((char)('a' + i)).close();
		}
		
		//	Get the handle to the inner title index file
		innerIndexPidTitle = new RandomAccessFile(new File("innerIndex"),"r");
		
		//	Get the handle to the pid to tile map file
		pidToTitleFile = new RandomAccessFile(new File("pIDToTitleMap"), "r");		
	}

	private static void mergeList(String list, String secWords, int callTime) {
		
		if(list == null || ZERORESULTS == 1){
			ZERORESULTS = 1;
			return;
		}		
		
		if(isFieldQuery) {
			
			int score = 0;
			switch(secWords) {
				case "t": 	//	title
					score = 1;				
					break;
				case "i": 	//"infobox":
					score = 2;
					break;
				case "e": 	//	externalLinks"
					score = 4;
					break;	
				case "r":	//"references":
					score = 8;
					break;	
				case "c": 	//	"category":
					score = 16;
					break;
				case "b": 	//optionIndex = 5;
					score = 32;
					break;
			}
			String line[] = list.split(":");
			int len = line.length;
			
			for(int i = 1; i < len && i < 200; i++){				
				String inside[] = line[i].split("=");
				if((score & Integer.parseInt(inside[1].split(",")[0])) >= 1){					
					if(callTime != 1) {
						if(output.containsKey(inside[0])) {
							output.put(inside[0], output.get(inside[0]) + Float.parseFloat(inside[1].split(",")[1]));
						} else {
							output.remove(inside[0]);
						}
					} else {
						output.put(inside[0], Float.parseFloat(inside[1].split(",")[1]));
					}
				}
			}
		} else {
			String line[] = list.split(":");
			int len = line.length;			
			for(int i = 1; i < len && i < 200; i++) {
				String inside[] = line[i].split("=");
				if(callTime != 1) {
					if(output.containsKey(inside[0])) {
						output.put(inside[0], output.get(inside[0]) + Float.parseFloat(inside[1].split(",")[1]));
					} else {
						output.put(inside[0], Float.parseFloat(inside[1].split(",")[1]));
					}
				} else {
					output.put(inside[0], Float.parseFloat(inside[1].split(",")[1]));
				}
			}
		}
	}

	private static String getList(String queryWord) throws IOException {
		
		char c1 = queryWord.charAt(0);
		String line = null, startOffset = "0", endOffset = "0";
		if(outerindexMap.get(c1) == null){
			outerindexMap.put(c1, new BufferedReader(new FileReader(String.valueOf(c1) + "outerIndex"))); 
		}
		
		while((line = outerindexMap.get(c1).readLine()) != null){
			if(line.split(":")[0].compareTo(queryWord) < 0) {
				startOffset = line.split(":")[1];
			} else if(line.split(":")[0].compareTo(queryWord) >= 0) {
				endOffset = line.split(":")[1]; 
				break;
			}
		}
		
		//Get the block into the memory
		//but before that open the file for using random access
		if(innerindexMap.get(c1) == null){
			innerindexMap.put(c1, new RandomAccessFile(new File(String.valueOf(c1) + "innerIndex"),"r")); 
		}
		
		HashMap <String, Long> mymap = new HashMap <String, Long>();
		innerindexMap.get(c1).seek(Long.parseLong(startOffset));
		
		Long offset = Long.parseLong(startOffset);
		line = null;
		while((line = innerindexMap.get(c1).readLine()) != null){
			mymap.put(line.split(":")[0], Long.parseLong(line.split(":")[1]));
			offset = offset + line.length() + 1;
			if(offset > Long.parseLong(endOffset))
				break;
		}
		innerindexMap.get(c1).close();
		
		if(dataStoreMap.get(c1) == null){
			dataStoreMap.put(c1, new RandomAccessFile(new File(String.valueOf(c1) + "DateStore"),"r"));
		}
		if(mymap.get(queryWord) == null) {
			dataStoreMap.get(c1).close();
			mymap.clear();
			ZERORESULTS = 1;
			return null;
		}
			
		dataStoreMap.get(c1).seek(mymap.get(queryWord));
		line =  dataStoreMap.get(c1).readLine();
		
		dataStoreMap.get(c1).close();
		mymap.clear();
		return line;
	}
	
	private static void preload() throws NumberFormatException, IOException {
		
		//date store files
		for(int i = 0; i < 26; i++) {			
			outerindexMap.put((char)('a' + i), new BufferedReader(new FileReader((char)('a' + i) + "outerIndex")));
		}
		
		//Index files
		outerIndexPidTitle = new BufferedReader(new FileReader(new File("outerIndex")));
	}
	
	private static String getTitleOfpId(String pID) throws IOException{
		
		//System.out.println(pID);
		
		String line = null;
		String startOffset = "0", endOffset = "0";
		
		while((line = outerIndexPidTitle.readLine()) != null) {
			if(Long.parseLong(line.split(":")[0]) > Long.parseLong(pID)) {
				endOffset = line.split(":")[1];
				break;
			}
			startOffset = line.split(":")[1];
		}
		
		HashMap <String, Long> mymap = new HashMap <String, Long>();		
		innerIndexPidTitle.seek(Long.parseLong(startOffset));
		
		Long offset = Long.parseLong(startOffset);
		int counter = 0;
		line = null;
		while((line = innerIndexPidTitle.readLine()) != null){
			//System.out.println(line);
			mymap.put(line.split(":")[0], Long.parseLong(line.split(":")[1]));
			if(counter++ == 1000)
				break;
		}
		
		pidToTitleFile.seek(mymap.get(pID));
		
		line = pidToTitleFile.readLine().split(":")[1];
		mymap.clear();
		return line;
	}
}