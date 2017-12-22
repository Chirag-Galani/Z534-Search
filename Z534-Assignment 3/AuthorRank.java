import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections15.FactoryUtils;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.io.PajekNetReader;


public class AuthorRank {


	public static void main(String[] args) {
		String GRAPH_PATH = "C:/Users/Chirag/Desktop/Search_assn_3/author.net";
		UndirectedSparseGraph<Integer, String> undirectedSparseGraph = new UndirectedSparseGraph<>();
		Double dampingFactor=0.85;
		
		PajekNetReader pajekNetReader = new PajekNetReader<>(FactoryUtils.instantiateFactory(Object.class));
		try {
			pajekNetReader.load(GRAPH_PATH, undirectedSparseGraph);
			
			PageRank<Integer, String> pageRank = new PageRank<>(undirectedSparseGraph, dampingFactor);
			pageRank.evaluate();
			Map<Object, Double> result = new HashMap<Object, Double>();
			for (Integer v : undirectedSparseGraph.getVertices()) {
				result.put(pajekNetReader.getVertexLabeller().transform(v), pageRank.getVertexScore(v));
			}	
		System.out.println("Top Ranked Authors:");
		result.entrySet()
			.stream()
			.sorted(Map.Entry.<Object, Double> comparingByValue().reversed())
			.limit(10)
			.forEachOrdered(e -> {
					System.out.println("Author ID:"+e.getKey()+"\tScore:"+e.getValue());
			});
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
		
}
		
