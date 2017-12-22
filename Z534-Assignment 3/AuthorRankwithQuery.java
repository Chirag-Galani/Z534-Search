import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections15.FactoryUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;

public class AuthorRankwithQuery {

	//Discussed with anekkal

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		String INDEX_PATH = "C:/Users/Chirag/Desktop/Search_assn_3/author_index/author_index/";
		String GRAPH_PATH = "C:/Users/Chirag/Desktop/Search_assn_3/author.net";
		Graph<Integer, String> graph = new UndirectedSparseGraph<>();
		Map<String, String> author_detail_map = new HashMap<>();
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(INDEX_PATH)));
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		indexSearcher.setSimilarity(new BM25Similarity());
		StandardAnalyzer standardAnalyzer = new StandardAnalyzer();
		QueryParser parser= new QueryParser("content",standardAnalyzer);
		String[] query_string_arr= {"Information Retrieval","Data Mining"} ;
		PajekNetReader pajekNetReader= new PajekNetReader<>(FactoryUtils.instantiateFactory(Object.class));
		pajekNetReader.load(GRAPH_PATH, graph);
		Map<String, Double> rankHashMap = new HashMap<String, Double>();

		for (String query_string:query_string_arr) {
			Query query = parser.parse(query_string);
	        TopDocs docs= indexSearcher.search(query, null, 300);
			float totalScore = 0.0f;
			rankHashMap.clear();
			for (ScoreDoc scoreDoc : docs.scoreDocs) {
				Document document = indexSearcher.doc(scoreDoc.doc);
//				String id = document.getField("authorid").toString();
//				String name= document.getField("authorName").toString();
				String id = document.getField("authorid").stringValue();
				String name= document.getField("authorName").stringValue();
				totalScore += scoreDoc.score;
				author_detail_map.put(id, name);
				rankHashMap.put(id,rankHashMap.getOrDefault(id, 0d) + scoreDoc.score);
			}
			for (Entry<String, Double> entry : rankHashMap.entrySet()) {
				rankHashMap.put(entry.getKey(), entry.getValue() / totalScore); 
//				rankHashMap.forEach((e->{});
			}

			Transformer<Integer, Double> vertex_prior = new Transformer<Integer, Double>() {
				@Override
				public Double transform(Integer v) {
					return (double)rankHashMap.getOrDefault(pajekNetReader.getVertexLabeller().transform(v), (double) 0.0);
				}
			};
			PageRankWithPriors<Integer, String> pageRankWithPriors = new PageRankWithPriors<>(graph, vertex_prior, 0.7);
			pageRankWithPriors.evaluate();

			Map<Object, Double> result = new HashMap<Object, Double>();
			for (Integer v : graph.getVertices()) {
				result.put(pajekNetReader.getVertexLabeller().transform(v), pageRankWithPriors.getVertexScore(v));
			}
			System.out.println("\n"+query_string);
			System.out.println("Author ID\tAuthor Name");

			result.entrySet()
			.stream()
			.sorted(Map.Entry.<Object, Double> comparingByValue().reversed()).limit(10)
			.forEachOrdered(e -> {
				String id=e.getKey().toString();
				System.out.println(id+ "\t\t" + author_detail_map.get(id));
			});
		}

		reader.close();
	}


}
