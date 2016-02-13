import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class queryParser {
	
	public static BufferedReader indexLevel3File = null;	
	public static RandomAccessFile indexLevel2File  = null;
	public static RandomAccessFile indexLevel1File  = null;
	public static RandomAccessFile invertedIndexFile = null;
	public static BufferedReader pidToTitleFile = null;
	
 	public static HashSet<String> stopWordsList = new HashSet<String>(Arrays.asList("a","again","about","above","across","after","against","all","almost","alone",
 						"along","already","also","although","always","am","among","an","and","another","any","anybody","anyone","anything",
							"anywhere","are","area","areas","aren't","around","as","ask","asked","asking","asks","at","away","b","back","backed",
						"backing","backs","be","became","because","become","becomes","been","before","began","behind","being","beings","below",
						"best","better","between","big","both","but","by","c","came","can","can't","cannot","case","cases","certain","certainly",
						"clear","clearly","com","come","coord","could","couldn't","d","did","didn't","differ","different","differently","do",
						"does","doesn't","doing","don't","done","down","downed","downing","downs","during","e","each","early","either","end",
						"ended","ending","ends","enough","even","evenly","ever","every","everybody","everyone","everything","everywhere","f",
						"face","faces","fact","facts","far","felt","few","find","finds","first","for","four","from","full","fully","further",
						"furthered","furthering","furthers","g","gave","general","generally","get","gets","give","given","gives","go","going",
						"good","goods","got","gr","great","greater","greatest","group","grouped","grouping","groups","h","had","hadn't","has",
						"hasn't","have","haven't","having","he","he'd","he'll","he's","her","here","here's","hers","herself","high","higher",
						"highest","him","himself","his","how","how's","however","http","https","i","i'd","i'll","i'm","i've","if","important",
						"in","interest","interested","interesting","interests","into","is","isn't","it","it's","its","itself","j","just","k",
						"keep","keeps","kind","knew","know","known","knows","l","large","largely","last","later","latest","least","less","let",
						"let's","lets","like","likely","long","longer","longest","m","made","make","making","man","many","may","me","member",
						"members","men","might","more","most","mostly","mr","mrs","much","must","mustn't","my","myself","n","nbsp","necessary",
						"need","needed","needing","needs","never","new","newer","newest","next","no","nobody","non","noone","nor","not",
						"nothing","now","nowhere","number","numbers","o","of","off","often","old","older","oldest","on","once","one","only",
						"open","opened","opening","opens","or","order","ordered","ordering","orders","other","others","ought","our","ours",
						"ourselves","out","over","own","p","part","parted","parting","parts","per","perhaps","place","places","point","pointed",
						"pointing","points","possible","present","presented","presenting","presents","problem","problems","put","puts","q",
						"quite","r","rather","really","right","room","rooms","s","said","same","saw","say","says","second","seconds","see",
						"seem","seemed","seeming","seems","sees","several","shall","shan't","she","she'd","she'll","she's","should","shouldn't",
						"show","showed","showing","shows","side","sides","since","small","smaller","smallest","so","some","somebody","someone",
						"something","somewhere","state","states","still","such","sure","t","take","taken","td","than","that","that's","the",
						"their","theirs","them","themselves","then","there","there's","therefore","these","they","they'd","they'll","they're",
						"they've","thing","things","think","thinks","this","those","though","thought","thoughts","three","through","thus","to",
						"today","together","too","took","toward","tr","turn","turned","turning","turns","two","u","under","until","up","upon",
						"us","use","used","uses","v","very","w","want","wanted","wanting","wants","was","wasn't","way","ways","we","we'd",
						"we'll","we're","we've","well","wells","went","were","weren't","what","what's","when","when's","where","where's",
						"whether","which","while","who","who's","whole","whom","whose","why","why's","will","with","within","without","won't",
						"work","worked","working","works","would","wouldn't","www","x","y","year","years","yet","you","you'd","you'll","you're",
						"you've","young","younger","youngest","your","yours","yourself","yourselves","z",
						
						//This are the words which are not used in the text, these words are meta-data
						
						"refbegin","reflist","isbn",";",".","'","|","jpg","png","[","]","br","gt","&","lt","&gt","&lt","htm","en","php","isbn","svg",
						"yes","wikitext","wiki","faq","edu","html","net","org","<",">","ref","refs","cite","pdf","url","web","link","abbreviation",
						"id","caption","page","index","aspx","id","file","thumb","alt","thumbnail","defaultsort","abbr","redirect"
 			));
 	
 	static long startTime = 0;
	static long endTime = 0;
 	
	
	public static void main(String[] args) {
		
		//	preload the index file
		//	get the random iterator for that file
		// 	get the handle to the pid to title map file
		try {
			preload();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Get the query from the user
		System.out.print("Enter the query : ");
		Scanner in = new Scanner(System.in);
		String query = in.nextLine();
		
		queryParser.startTime = System.currentTimeMillis();
		
		parseQuery(query.toLowerCase());
		
		
	}

	private static void parseQuery(String query) {
		
		//	break the query in words
		String queryWords[] = query.trim().split(" ");
		int queryLengthInWords = queryWords.length;
		
		//	remove the stop words
		List <String> refinedQueryWords = new ArrayList <String>() ;
		stemmer s = new stemmer();
		
		for(int i = 0; i < queryLengthInWords; i++) {
			
			
			if(!isStopWord(queryWords[i])) {
				
				//Get stemmer in action
				s.add(queryWords[i].toCharArray(), queryWords[i].length());
				try {
					queryWords[i] = s.stem().toString();
				} catch (Exception e) {
					e.printStackTrace();
				}
				refinedQueryWords.add(queryWords[i]);
				//System.out.println(queryWords[i]);
			}
		}
		//	get the posting list of the query
		if(queryWords[0].length() > 2)
			try {
				getList(queryWords[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	private static void getList(String queryWord) throws IOException {

		//Access the third level
		String lineInThirdLevel = null;
		long startOffset = 0, endOffset = 0, lastOffset = 0;
		
		while((lineInThirdLevel = indexLevel3File.readLine()) != null) {
			
			if(lineInThirdLevel.split(":")[0].compareTo(queryWord) == 0) {
				
				startOffset = Long.parseLong(lineInThirdLevel.split(":")[1]);
				
			} else if(lineInThirdLevel.split(":")[0].compareTo(queryWord) == 1) {
				
				//System.out.println(lineInThirdLevel + " " + queryWord);
				
				if(startOffset == 0){
					
					startOffset = lastOffset;
				}
				endOffset = Long.parseLong(lineInThirdLevel.split(":")[1]);
				break;
				
			} else {
				
				lastOffset = Long.parseLong(lineInThirdLevel.split(":")[1]); 
			}
		}
		
		System.out.println(startOffset + " " + endOffset);
		endTime = System.currentTimeMillis();
		System.out.println("Execution Time : " + (float)(endTime - startTime) / 1000 + " s");
		
		//Access the second level
		
		//Access the first level		
		
	}

	private static boolean isStopWord(String word) {
		
		return stopWordsList.contains(word);
	}

	private static void preload() throws NumberFormatException, IOException {
		
		//Get the handle to the index file
		try {
			 indexLevel3File = new BufferedReader(new FileReader(new File("indexLevel3")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Get the handle to the main data file
		try {
			invertedIndexFile = new RandomAccessFile(new File("Index"), "r");
			indexLevel1File = new RandomAccessFile(new File("indexLevel1"), "r");
			indexLevel2File = new RandomAccessFile(new File("indexLevel2"), "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		//Get the handle to the pid to tile map file
		try {
			pidToTitleFile = new BufferedReader(new FileReader(new File("pIDToTitleMap")));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}