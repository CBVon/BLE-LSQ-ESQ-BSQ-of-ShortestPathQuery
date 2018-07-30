package landmark_based_shortest_distance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

/**
 * 随机获取10000组pair， 并利用 Jgrapht 计算出准确最短路径
 * @author cbvon
 */

public class RandomPair {
	
	//Slashdot
	public static final int queryPairNum = 10000;
	public static final int vertexNum = 82168;
	public static final String graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/soc-Slashdot0902.txt";
	public static final String bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/10000_pairs.json";
	public static final String bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/10000_pairs_minimum_dis.json";

	//Epinions1_adjFormat
//	public static final int queryPairNum = 10000;
//	public static final int vertexNum = 75879; 
//	public static final String graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/soc-Epinions1.txt";
//	public static final String bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/10000_pairs.json";
//	public static final String bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/10000_pairs_minimum_dis.json";
	
	//dblp
//	public static final int queryPairNum = 10000;
//	public static final int vertexNum = 425957; 
//	public static final String graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/com-dblp.ungraph.txt";
//	public static final String bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/10000_pairs.json";
//	public static final String bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/10000_pairs_minimum_dis.json";
	
	//facebook
//	public static final int queryPairNum = 10000;
//	public static final int vertexNum = 4039; 
//	public static final String graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/facebook_combined.txt";
//	public static final String bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/10000_pairs_2.json";
//	public static final String bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/10000_pairs_minimum_dis_2.json";
	
	//Douban  http://socialcomputing.asu.edu/datasets/Douban
//	public static final int queryPairNum = 10000;
//	public static final int vertexNum = 154908; 
//	public static final String graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/Douban_ungraph.txt";
//	public static final String bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/10000_pairs.json";
//	public static final String bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/10000_pairs_minimum_dis.json";
	
	//youtube http://snap.stanford.edu/data/com-Youtube.html
//	public static final int queryPairNum = 10000;
//	public static final int vertexNum = 1157827;  //1157826  实际上有1134890
//	public static final String graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/youtube.txt";
//	public static final String bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/10000_pairs.json";
//	public static final String bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/10000_pairs_minimum_dis.json";
	
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public String from, to;
	public double minimumDis;
	
	/*
	 * Slashdot social network
	 * vertex_num: 82168
	 * edg_num: 948464 (包含 节点本身的自环)
	 * 
	 * Epinions
	 * Nodes: 75879 
	 * Edges: 508837
	 * max_vertex : 75888    count_vertex : 75879
	 * 
	 * dblp
	 * Nodes: 317080 
	 * Edges: 1049866
	 * max_vertex : 425957    count_vertex : 317080
	 * 
	 * facebook
	 * Nodes: 4039
	 * edgs:  88234
	 * max_vertex : 4039    count_vertex : 4039
	 * 
	 * Douban
	 * nodes: 154908 
	 * edgs: 327162
	 * max_vertex : 154908    count_vertex : 154908
	 * 
	 * youtube (太大了
	 * nodes：1157826
	 * edgs：2987624
	 */
	
	public static UndirectedGraph<String, DefaultEdge> myGraph = creatGraph();;
	
	/**
	 * 构造函数 只构造一次静态成员变量 myGraph：保存全图原始信息
	 * @param from
	 * @param to
	 */
	public RandomPair(String from, String to) {
		this.from = "3496";
		this.to = "4018";
		//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
		DijkstraShortestPath<String, DefaultEdge> myDijkstraShortestPath = 
				new DijkstraShortestPath<String, DefaultEdge>(myGraph, "156583", "210346");
		this.minimumDis = myDijkstraShortestPath.getPathLength();
	}
	
	
	
	/**
	 * creatGraph
	 * @return 图-UndirectedGraph
	 */
	public static UndirectedGraph<String, DefaultEdge> creatGraph(){
		
		System.out.println("func creatGraph is running!");
		UndirectedGraph<String, DefaultEdge> myGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		
		BufferedReader bReader;
		try {
			bReader = new BufferedReader(new FileReader(graphFilePath));
		
			String tempString = null;  
	        int line = 0;  
	        // 一次读入一行，直到读入null为文件结束  
	        while ((tempString = bReader.readLine()) != null) {
	        	
	        	if(++line <= 4)
	        		continue;
	        	
	        	if(line % 10000 == 0)
	        		System.out.println("line : " + line);
	        	
	        	String fromVertex = tempString.split("\t")[0];
	        	String toVertex = tempString.split("\t")[1];
	        	if(fromVertex.equals(toVertex))
	        		continue;
	        	
	        	myGraph.addVertex(fromVertex);
	        	myGraph.addVertex(toVertex);
	        	myGraph.addEdge(fromVertex, toVertex);
	        	
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.out.println("func creatGraph is over!");
		return myGraph;
		
	}
	
	/**
	 * 随机生成pairNum对pair，并利用RandomPair的DijkstraShortestPath计算pair之间最短距离
	 * @param pairNum pair组数
	 * @return 长度为pairNum 的RandomPair数组
	 */
	public static RandomPair[] getRandomPairArray(int pairNum, int vertexNum) {
		
		startTime = System.currentTimeMillis();
		
		Random random = new Random();
		RandomPair randomPairArray[] = new RandomPair[pairNum];
		for(int i = 0; i < pairNum; ++i) {
				
			int fromInt = -1, toInt = -1;
			while(fromInt == toInt) {
				
				fromInt = random.nextInt(vertexNum);
				toInt = random.nextInt(vertexNum);
				
			}
			if (! (myGraph.containsVertex(String.valueOf(fromInt)) && myGraph.containsVertex(String.valueOf(toInt))) ) {
				--i;
				continue;
			}
			randomPairArray[i] = new RandomPair(Integer.toString(fromInt), Integer.toString(toInt));
			if(randomPairArray[i].minimumDis > Double.valueOf(1E10)) { //不连通.这里确保10000pair都是可以连通的
				--i;
				continue;
			}
				
			System.out.println("getRandomPairArray has proccessed " + i + " pairs . fromVertex : " + Integer.toString(fromInt) +
					" toVertex : " + Integer.toString(toInt) + " minimumDis : " + randomPairArray[i].minimumDis);
			
		}
		
		endTime = System.currentTimeMillis();
		System.out.println("compute " + pairNum + " pairs using " + (endTime - startTime) + " ms!");
		return randomPairArray;
		
	}
	
	/**
	 * 主函数
	 * @param args
	 * @throws IOException
	 */
	public static void main(String []args) throws IOException {
		
		RandomPair randomPairArray[] = getRandomPairArray(queryPairNum, vertexNum);
		BufferedWriter bWriterPair = new BufferedWriter(new FileWriter(bWriterPairFilePath));
		BufferedWriter bWriterminimumDis = new BufferedWriter(new FileWriter(bWriterminimumDisFilePath));
		for(int i = 0; i < queryPairNum; ++i) {
			bWriterPair.write(randomPairArray[i].from + " " + randomPairArray[i].to + "\n");
			bWriterminimumDis.write(String.valueOf(randomPairArray[i].minimumDis) + "\n");
		}
		bWriterPair.close();
		bWriterminimumDis.close();
		
	}

}
/*
//Slashdot
compute 10000 pairs using 2881658 ms!

//Google-web
compute 10000 pairs using 8306081 ms!

//Amazon
compute 10000 pairs using 5117710 ms!

//Epinions1_adjFormat
compute 10000 pairs using 2238068 ms!

//dblp117185083
compute 10000 pairs using 5311154 ms!

//facebook
compute 10000 pairs using 165829 ms!
compute 10000 pairs using 171138 ms!

//Douban
compute 10000 pairs using 1893717 ms!



 */
