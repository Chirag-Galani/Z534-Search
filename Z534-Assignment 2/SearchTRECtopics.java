import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class SearchTRECtopics {

	public static String fetchTitle(String query) {
		String title = "";
		int titlePos= query.indexOf(":");
		if (titlePos!= -1) {
			title  = query.substring(titlePos + 1, query.length());
		}
		return title;
	}

	public static String fetchSummary(String query) {
		int descStart = query.indexOf("<desc>");
		int descEnd = query.indexOf("<",descStart+1);
		String desc= query.substring(descStart+6, descEnd);
		desc=desc.replace("Description:", " ");
		desc = desc.replace('?' , ' ');
		desc = desc.replace('/' , ' '); 

//		String summary = "";
//		int summaryPos= query.indexOf("<smry>");
//		if (summaryPos!= -1) {
//			summary  = query.substring(0, summaryPos);
//		}
		return desc;
	}
	public static void main(String[] args) throws IOException, ParseException {

		String TOPIC_URL="C:\\Users\\Chirag\\Desktop\\Assignment\\Search\\Assignment 2\\topics.51-100";
		String INDEX_URL="C:\\Users\\Chirag\\Desktop\\Assignment\\Search\\Assignment 2\\index";
		String algorithmName="MYALGO";
		TrecTopicsReader trecTopicReader = new TrecTopicsReader();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(TOPIC_URL));
		QualityQuery[] qualityQueries = trecTopicReader.readQueries(bufferedReader);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_URL)));
		for (int i = 0; i < qualityQueries.length; i++) {
			QualityQuery qualityQuery = qualityQueries[i];
			String queryID = qualityQuery.getQueryID();
			{
				String titleQuery = qualityQuery.getValue("title");
				String fetchTitleQuery = fetchTitle(titleQuery);
				TreeMap<String,Double> sorted_map= new TreeMap<String,Double>();
				sorted_map=EasySearch.calculateSortedRelevanceScore(fetchTitleQuery, reader, new ClassicSimilarity(),algorithmName);
				String op_file =  algorithmName + "ShortQuery" + ".txt";
				if(sorted_map!=null)
					appendToFile(op_file,queryID,sorted_map);

			}
			{
				String descQuery = qualityQuery.getValue("description");
				String fetchSummaryQuery = fetchSummary(descQuery);
				TreeMap<String,Double> sorted_map= new TreeMap<String,Double>();
				sorted_map=EasySearch.calculateSortedRelevanceScore(fetchSummaryQuery , reader, new ClassicSimilarity(),algorithmName);

				String op_file= algorithmName + "LongQuery" + ".txt";
				if(sorted_map!=null)
					appendToFile(op_file,queryID,sorted_map);
			}
		}
		System.out.println("All the queries from TREC 51-100 executed successfully.");
	}
	public static void appendToFile(String fileName,String queryId, TreeMap<String,Double> sorted_map) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
		int c=0;
		for (Map.Entry<String, Double> entry : sorted_map.entrySet()) {
			try {
				writer.append(queryId);
				writer.append("\t" + "Q0");
				writer.append("\t"+ entry.getKey());
				writer.append("\t"+ ++c);
				writer.append("\t"+ entry.getValue());
				writer.append("\t"+ "run-1 \n");
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(c==1000)
				break;
		}
		writer.flush();
		writer.close();
	}
}	



