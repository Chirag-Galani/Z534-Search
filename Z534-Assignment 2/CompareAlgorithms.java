import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.QueryParserUtil;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

public class CompareAlgorithms {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			extractQuery (new ClassicSimilarity(), "DEFAULT_VECTOR");
			extractQuery(new BM25Similarity(), "BM25");
			extractQuery(new LMDirichletSimilarity(), "LMDirichlet");
			extractQuery(new LMJelinekMercerSimilarity((float) 0.7), "LMJelinek");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	public static void extractQuery(Similarity simi, String algorithmName) throws IOException, ParseException {
		String TOPIC_URL="C:\\Users\\Chirag\\Desktop\\Assignment\\Search\\Assignment 2\\topics.51-100";
		String INDEX_URL="C:\\Users\\Chirag\\Desktop\\Assignment\\Search\\Assignment 2\\index";

		TrecTopicsReader trecTopicReader = new TrecTopicsReader();
		BufferedReader bufferedReader = new BufferedReader(new FileReader(TOPIC_URL));
		QualityQuery[] qualityQueries = trecTopicReader.readQueries(bufferedReader);
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(INDEX_URL)));
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		StandardAnalyzer analyzer = new StandardAnalyzer();
		indexSearcher.setSimilarity(simi);


		QueryParser queryParser = new QueryParser("TEXT", analyzer);

		for (int i = 0; i < qualityQueries.length; i++) {
			QualityQuery qualityQuery = qualityQueries[i];
			String queryID = qualityQuery.getQueryID();
			{
				String titleQuery = qualityQuery.getValue("title");
				String cleanedTitleQuery = getTitle(titleQuery);
				Query titleQ = queryParser.parse(QueryParserUtil.escape(cleanedTitleQuery));
				String op_file =  algorithmName + "ShortQuery" + ".txt";
				appendToFile(titleQ, indexSearcher, queryID, op_file);

			}
			{
				String descQuery = qualityQuery.getValue("description");
				String cleanedDescQuery = getDescription(descQuery);
				Query descQ = queryParser.parse(QueryParserUtil.escape(cleanedDescQuery));
				String op_file= algorithmName + "LongQuery" + ".txt";
					appendToFile(descQ, indexSearcher, queryID, op_file);

			}
		}
		System.out.println("All the queries from TREC 51-100 executed successfully for "+algorithmName);
	}
	public static void appendToFile(Query query, IndexSearcher indexSearcher, String queryID, String fileName) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
		TopDocs results= indexSearcher.search(query, 1000);		

		
		ScoreDoc[] hits = results.scoreDocs;
		for(int i=0;i<hits.length;i++){	
			Document doc=indexSearcher.doc(hits[i].doc);	

			try {
				writer.append(queryID);
				writer.append("\t" + "Q0");
				writer.append("\t"+ doc.get("DOCNO"));
				writer.append("\t"+ (i+1));
				writer.append("\t"+ hits[i].score);
				writer.append("\t"+ "run-1 \n");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		writer.flush();
		writer.close();
	}
	



	public static String getTitle(String query) {
		String title = "";
		int titlePos= query.indexOf(":");
		if (titlePos!= -1) {
			title  = query.substring(titlePos + 1, query.length());
		}
		return title;
	}

	public static String getDescription(String query) {
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

}
