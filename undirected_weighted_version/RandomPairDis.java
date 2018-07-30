package undirected_weighted_version;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * 随机获取10000组pair， 并利用 Jgrapht 计算出准确最短路径
 * @author cbvon
 */

@SuppressWarnings("deprecation")
public class RandomPairDis {
	
	public static int queryPairNum = 0;
	public static int vertexNum = 0;
	public static String graphFilePath = null;
	public static String bWriterPairFilePath = null;
	public static String bWriterminimumDisFilePath = null;
	
	//landmarkEmbedding
	public static final int landmarkNum = 100;
	public static final int partGroupSize = 1000;
	
	public static int disconnectJudge = 0; //不连通判定
	public static int upperBoundDis = 0; //连不通的惩罚上界
	public static String adjPart100Graph = null;//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
	public static String bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = null;
	
	/**
	 * NYRN
	 * FLARN
	 */
	public static final String dataSet = "NYRN";
	
	static {
		if (dataSet.equals("NYRN")) {
			/**
			 * NYRN		http://www.dis.uniroma1.it/challenge9/download.shtml
			 * vertexNum: 264,346
			 * maxId: 264345
			 * edgeNum: 733,846
			 * Diameter: 1550723
			 */
			queryPairNum = 1000;
			vertexNum = 264346;
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/NYRN.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/1000_pairs.json";
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E10; //10000作为不连通判定
			upperBoundDis = 1550723; //600在社交网络中已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/cutGraph/NYRN_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}
	}
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public String from, to;
	public int minimumDis;
	
	public RandomPairDis() {
		
	}
	
	public static SimpleWeightedGraph<String, DefaultWeightedEdge> myGraph  = creatWeightedGraph();
	
	/**
	 * 构造函数,只构造一次静态成员变量myGraph：保存全图原始信息
	 * @param from 出点
	 * @param to 入点
	 * @param Graph 工作图
	 */
	public RandomPairDis(String from, String to, SimpleWeightedGraph<String, DefaultWeightedEdge> Graph) {
		this.from = from;
		this.to = to;
		//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
		DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
				new DijkstraShortestPath<String, DefaultWeightedEdge>(Graph, from, to);
		this.minimumDis = (int) myDijkstraShortestPath.getPathLength();
	}
	
	/**
	 * creatWeightedGraph
	 * @return 图-SimpleWeightedGraph
	 */
	public static SimpleWeightedGraph<String, DefaultWeightedEdge> creatWeightedGraph(){
		
		System.out.println("func creatWeightedGraph is running!");
		SimpleWeightedGraph<String, DefaultWeightedEdge> myGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		BufferedReader bReader;
		try {
			bReader = new BufferedReader(new FileReader(graphFilePath));
			String tempString = null;  
	        int line = 0;  
	        while ((tempString = bReader.readLine()) != null) {
	        	if(++line <= 4)
	        		continue;
	        	if(line % 10000 == 0)
	        		System.out.println("line : " + line);
	        	String fromVertex = tempString.split("\t")[0];
	        	String toVertex = tempString.split("\t")[1];
	        	int thisDis = Integer.valueOf(tempString.split("\t")[2]);
	        	if(fromVertex.equals(toVertex))
	        		continue;
	        	
	        	myGraph.addVertex(fromVertex);
	        	myGraph.addVertex(toVertex);
	        	myGraph.addEdge(fromVertex, toVertex);
	        	myGraph.setEdgeWeight(myGraph.getEdge(fromVertex, toVertex), thisDis);
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("func creatWeightedGraph is over!");
		return myGraph;
	}
	
	/**
	 * 随机生成pairNum对pair，并利用RandomPair的DijkstraShortestPath计算pair之间最短距离
	 * @param pairNum pair组数
	 * @return 长度为pairNum 的RandomPair数组
	 */
	public static RandomPairDis[] getRandomPairArray(int queryPairNum, int vertexNum) {
		startTime = System.currentTimeMillis();
		Random random = new Random();
		RandomPairDis randomPairArray[] = new RandomPairDis[queryPairNum];
		for(int i = 0; i < queryPairNum; ++i) {
			int fromInt = -1, toInt = -1;
			while(fromInt == toInt) {
				fromInt = random.nextInt(vertexNum);
				toInt = random.nextInt(vertexNum);
			}
			if (! (myGraph.containsVertex(String.valueOf(fromInt)) && myGraph.containsVertex(String.valueOf(toInt))) ) {
				--i;
				continue;
			}
			randomPairArray[i] = new RandomPairDis(Integer.toString(fromInt), Integer.toString(toInt), myGraph);
			if(randomPairArray[i].minimumDis > (int) 1E10) { //不连通.这里确保10000pair都是可以连通的
				--i;
				continue;
			}
			System.out.println("getRandomPairArray has proccessed " + i + " pairs . fromVertex : " + Integer.toString(fromInt) +
					" || toVertex : " + Integer.toString(toInt) + " || minimumDis : " + randomPairArray[i].minimumDis);
		}
		endTime = System.currentTimeMillis();
		System.out.println(dataSet + " : compute " + queryPairNum + " pairs using " + (endTime - startTime) + " ms!");
		return randomPairArray;
	}
	
	/**
	 * 主函数
	 * @param args
	 * @throws IOException
	 */
	public static void main(String []args) throws IOException {
		RandomPairDis randomPairArray[] = getRandomPairArray(queryPairNum, vertexNum);
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

//NYRN : compute 1000 pairs using 118740 ms!

