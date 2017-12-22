import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class generateIndex {
	private static final Pattern DOC_REGEX = Pattern.compile("<DOC>(.+?)</DOC>");
	public static void main(String[] args) throws IOException {

		
		Directory dir = FSDirectory.open(Paths.get("indexDestination"));
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);
		File folder = new File("C:\\\\Users\\\\Chirag\\\\Desktop\\\\Assignment\\\\Search\\\\corpus\\\\corpus");
		File[] listOfFiles = folder.listFiles();
		int counter = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String str = readFileAsString(listOfFiles[i].getAbsolutePath()).replaceAll("\n", "");
				str = str.replaceAll("\n", "").replaceAll("\r", "");


					final List<String> DOCValues = new ArrayList<String>();
					final Matcher matcher = DOC_REGEX.matcher(str);
					//    int o=0;
					//    while (matcher.find()) 
					//    	DOCValues.add(matcher.group(0));
					
//					DOCValues.add(matcher.group(1));
//					luceneDoc = new Document();
					//    	System.out.println("New Doc found")
					while (matcher.find()) {
						Document luceneDoc = new Document();                                                                     

						String docText=matcher.group(1);
						Pattern DOC_REGEX2 = Pattern.compile("<DOCNO>(.+?)</DOCNO>");
						Matcher matcher2 = DOC_REGEX2.matcher(docText);	
						String tempText="";
						while (matcher2.find()) {
							tempText+=" "+matcher2.group(1);
						}
						if(tempText!="") {
							luceneDoc.add(new StringField("DOCNO", tempText,Field.Store.YES));
						}
						tempText="";
						DOC_REGEX2 = Pattern.compile("<HEAD>(.+?)</HEAD>");
						matcher2 = DOC_REGEX2.matcher(docText);	
						while (matcher2.find()) {
							tempText+=" "+matcher2.group(1);
						}
						if(tempText!="")
							luceneDoc.add(new TextField("HEAD", tempText,Field.Store.YES));
						DOC_REGEX2 = Pattern.compile("<BYLINE>(.+?)</BYLINE>");
						matcher2 = DOC_REGEX2.matcher(docText);	
						tempText="";
						while (matcher2.find()) {
							tempText+=" "+matcher2.group(1);
						}
						if(tempText!="")
							luceneDoc.add(new TextField("BYLINE", tempText,Field.Store.YES));
						DOC_REGEX2 = Pattern.compile("<DATELINE>(.+?)</DATELINE>");
						matcher2 = DOC_REGEX2.matcher(docText);	
						tempText="";
						while (matcher2.find()) {
							tempText+=" "+matcher2.group(1);
						}
						if(tempText!="")
							luceneDoc.add(new TextField("DATELINE", tempText,Field.Store.YES));
						DOC_REGEX2 = Pattern.compile("<TEXT>(.+?)</TEXT>");
						matcher2 = DOC_REGEX2.matcher(docText);	
						tempText="";
						while (matcher2.find()) {
							tempText+=" "+matcher2.group(1);
						}
						if(tempText!="")
							luceneDoc.add(new TextField("TEXT", tempText,Field.Store.YES));

						writer.addDocument(luceneDoc);
					}
				}

				
			} 
		
		writer.forceMerge(1);
		writer.commit();
		writer.close();
		System.out.println("Indexing complete"); // Prints [apple, orange, pear]

		
		
	}

	private static String readFileAsString(String filePath) throws IOException {
		StringBuffer fileData = new StringBuffer();
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}
}