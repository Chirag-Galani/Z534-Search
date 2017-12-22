import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

public class EasySearch {
	public static void main(String args[]) throws IOException, ParseException {
		String INDEX_URL="C:\\Users\\Chirag\\Desktop\\Assignment\\Search\\Assignment 2\\index";
		String queryString = "how are you";
		String algorithmName="MYALGO";

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_URL)));

		TreeMap<String,Double> sorted_map= new TreeMap<String,Double>();
		sorted_map=calculateSortedRelevanceScore(queryString, reader, new ClassicSimilarity(),algorithmName);
	}
	public static TreeMap<String,Double> calculateSortedRelevanceScore(String queryString,IndexReader reader, Similarity dSimi, String algoName){
		try {
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer();
		QueryParser parser = new QueryParser("TEXT", analyzer);
		HashMap<String,Double> hash= new HashMap<String,Double>();
		Query query = parser.parse(QueryParserUtil.escape(queryString));
		Set<Term> queryTerms = new LinkedHashSet<Term>();
		searcher.createNormalizedWeight(query, false).extractTerms(queryTerms);
		searcher.setSimilarity(dSimi);

		for (Term t : queryTerms) {
			int df = reader.docFreq(new Term("TEXT", t.text()));
			List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
			float docLeng=0;
			for (int i = 0; i < leafContexts.size(); i++) {
				// Get document length
				LeafReaderContext leafContext = leafContexts.get(i);
				int startDocNo = leafContext.docBase;
				int numberOfDoc = leafContext.reader().maxDoc();
				for (int docId = 0; docId < numberOfDoc; docId++) {
					// Get normalized length (1/sqrt(numOfTokens)) of the document
					float normDocLeng = ((ClassicSimilarity) dSimi).decodeNormValue(leafContext.reader()
							.getNormValues("TEXT").get(docId));
					docLeng = 1 / (normDocLeng * normDocLeng);
				}
				PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),"TEXT", new BytesRef(t.text()));

				int doc;
				double sum=0;
				if (de != null) {
					while ((doc = de.nextDoc()) != PostingsEnum.NO_MORE_DOCS) {
						int docID = de.docID() + leafContext.docBase;
						String docNO = searcher.doc(docID).get("DOCNO");

//						System.out.println("docNO"+docNO);
//						System.out.println("docID"+docID);
//						System.out.println("de.freq()"+de.freq());
//						System.out.println("docLeng"+docLeng);
//						System.out.println("reader.maxDoc()"+reader.maxDoc());
//						System.out.println("df"+df);
//						System.out.println("Score="+((de.freq()/docLeng)*Math.log((1+reader.maxDoc())/df)));
						double docScore =((de.freq()/docLeng)*Math.log((1+reader.maxDoc())/(float)df));
						if(!hash.containsKey(docNO)) {
							hash.put(docNO,docScore);
						}
						else {
							hash.put(docNO,docScore+hash.get(docNO));
						}
					}
				}
			}
		}
        ValueComparator bvc = new ValueComparator(hash);
        TreeMap<String, Double> sorted_map = new TreeMap<String, Double>(bvc);
        sorted_map.putAll(hash);
        return sorted_map;
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void appendToFile(String str, String queryString) throws IOException {
	BufferedWriter writer = new BufferedWriter(new FileWriter(queryString+".txt", true));
	writer.append(str);
	writer.append("\n");
	writer.close();
	}

}
