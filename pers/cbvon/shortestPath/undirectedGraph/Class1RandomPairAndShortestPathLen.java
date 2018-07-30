package pers.cbvon.shortestPath.undirectedGraph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

/**
 * 随机获取queryPairNum组pair, 并利用bfs/dijk计算出准确最短路径
 * @author cbvon
 */
@SuppressWarnings("deprecation")
public class Class1RandomPairAndShortestPathLen {
	
	public static int queryPairNum = 0;
	public static int vertexNum = 0;
	public static String graphFilePath = null;
	public static String bWriterPairFilePath = null;
	public static String bWriterShortestPathLenFilePath = null;
	
	//landmarkEmbedding配置信息
	public static final int landmarkNum = 100;
	public static final int partGroupSize = 1000;
	
	public static int disconnectJudge = 0; //一个超大值,不连通判定
	public static int upperBoundDis = 0; //社交网络中一个最大值,作为连不通的惩罚
	public static String adjHundredPartGraph = null;//邻接图的分图（100分）文件路径; 文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
	public static String bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = null;
	
	/**
	 * unweightedGraph:----- -----
	 * Slashdot
	 * Epinions
	 * dblp
	 * facebook
	 * Douban
	 * youtube
	 * youtube_4945382
	 * unweightedNYRN
	 * 
	 * weightedGraph:----- -----
	 * NYRN
	 * FLARN
	 */
	public static final String dataSet = "youtube";
	public static boolean isWightedGraph;
	static {
		if (dataSet.equals("NYRN")) {
			isWightedGraph = true;
		}else {
			isWightedGraph = false;
		}
	}
	
	static {
		if (dataSet.equals("Slashdot")) {
			/**
			 * Slashdot		social network   http://snap.stanford.edu/data/soc-Slashdot0902.html
			 * vertexNum: 82168 (0~82167 没有空点)
			 * maxId: 82167
			 * edgNum: 948464 (包含 节点本身的自环)
			 * Diameter (longest shortest path): 	11
			 */
			queryPairNum = 1000;
			vertexNum = 82168;
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/soc-Slashdot0902.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 11; //社交网络中一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/cutGraph/Slashdot_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("Epinions")){
			/**
			 * Epinions   http://snap.stanford.edu/data/soc-Epinions1.html
			 * vertexNum: 75879
			 * maxId: 75887 (有空点)
			 * edgNum： 508837
			 * Diameter (longest shortest path) 	14
			 */
			queryPairNum = 1000;
			vertexNum = 75888; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/soc-Epinions1.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 14; //一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/cutGraph/soc-Epinions1_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("dblp")) {
			/**
			 * dblp    http://snap.stanford.edu/data/com-DBLP.html
			 * vertexNum: 317080 
			 * maxId: 425956 (有空点)
			 * edgeNum: 1049866
			 * Diameter (longest shortest path) 	21
			 */
			queryPairNum = 1000;
			vertexNum = 425957; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/com-dblp.ungraph.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 21; //一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/cutGraph/dblp_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("facebook")) {
			/**
			 * facebook ： http://snap.stanford.edu/data/egonets-Facebook.html
			 * vertexNum: 4039
			 * maxId：4038 （没有空点）
			 * edgeNum: 88234
			 * Diameter (longest shortest path) 	8
			 */
			queryPairNum = 1000;
			vertexNum = 4039; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/facebook_combined.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 8; //8已经是一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/cutGraph/facebook_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("Douban")) {
			/**
			 * Douban  http://socialcomputing.asu.edu/datasets/Douban
			 * vertexNum: 154908
			 * maxId： 154907 （没有空点）
			 * edgeNum: 654188
			 */
			queryPairNum = 1000;
			vertexNum = 154908; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/Douban_ungraph.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 20; //20已经是一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/cutGraph/Douban_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("youtube")) {
			/**
			 * youtube http://snap.stanford.edu/data/com-Youtube.html
			 * vertexNum: 1134890
			 * maxId: 1157826
			 * edgeNum: 2987624
			 * Diameter (longest shortest path) 	20
			 */
			queryPairNum = 1000;
			vertexNum = 1157827;  
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/youtube.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 20; //20在社交网络中已经是一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/cutGraph/Youtube_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("youtube_4945382")) {
			/**
			 * youtube http://socialnetworks.mpi-sws.org/data-imc2007.html
			 * vertexNum: 1138499
			 * maxId: 1157826
			 * edgeNum: 2990443
			 * Diameter (longest shortest path) 	20
			 */
			queryPairNum = 1000;
			vertexNum = 1157827;  
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/youtube_from0.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 20; //20在社交网络中已经是一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/cutGraph/Youtube_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_4945382/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("youtube_undirect")) {
			/**
			 * youtube http://socialnetworks.mpi-sws.org/data-imc2007.html
			 * vertexNum: 525883
			 * maxId: 1157821
			 * edgeNum: 1954939
			 * Diameter (longest shortest path) 	20
			 */
			queryPairNum = 1000;
			vertexNum = 1157822;  
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/Youtube.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/1000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 20; //20在社交网络中已经是一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/cutGraph/Youtube_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube_undirect/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("unweightedNYRN")) {
			/**
			 * unweightedNYRN http://www.dis.uniroma1.it/challenge9/download.shtml
			 * vertexNum: 264,346
			 * maxId: 264345
			 * edgeNum: 733,846
			 * Diameter: 652
			 */
			queryPairNum = 1000;
			vertexNum = 264346;
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/NYRN_unweighted.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/10000_pairs.json";
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/10000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //10000作为不连通判定
			upperBoundDis = 700; //600在社交网络中已经是一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/cutGraph/NYRN_unweighted_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}else if (dataSet.equals("NYRN")) {
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
			bWriterShortestPathLenFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/1000_pairsShortestPathLen.json";
			
			disconnectJudge = (int) 1E6; //不连通判定
			upperBoundDis = 1550723; //1550723在社交网络中已经是一个超大值，作为连不通的惩罚
			adjHundredPartGraph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/cutGraph/NYRN_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}
	}
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public String from, to;
	public int shortestPathLen;
	
	/**
	 * creatUnweightedGraph(全图)
	 * @return Map<String, Set<String>>全图
	 */
	public static Map<String, Set<String>> creatUnweightedGraph(){
		System.out.println("func creatUnweightedGraph(全图) is running!");
		Map<String, Set<String>> thisGraph = new HashMap<>();
		
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new FileReader(graphFilePath));
			String tempString = null;  
	        int line = 0;  
	        while ((tempString = bReader.readLine()) != null) {
	        	if(++line <= 4)
	        		continue;
	        	if(line % 100000 == 0)
	        		System.out.println("line : " + line);
	        	String fromVertex = tempString.split("\t")[0];
	        	String toVertex = tempString.split("\t")[1];
	        	if(fromVertex.equals(toVertex))
	        		continue;
	        	if(thisGraph.containsKey(fromVertex)) {
	        		thisGraph.get(fromVertex).add(toVertex);
	        	}else {
	        		thisGraph.put(fromVertex, new HashSet<String>() {
						private static final long serialVersionUID = 509329333037163623L;
						{
							add(toVertex);
						}
					});
				}
	        	
	        	if(thisGraph.containsKey(toVertex)) {
	        		thisGraph.get(toVertex).add(fromVertex);
	        	}else {
	        		thisGraph.put(toVertex, new HashSet<String>() {
						private static final long serialVersionUID = -1269799273215955482L;
						{
							add(fromVertex);
						}
					});
				}
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		System.out.println("func creatUnweightedGraph(全图） is over!");
		return thisGraph;
	}
	
	/**
	 * creatWeightedGraph
	 * @return 图-SimpleWeightedGraph
	 */
	public static SimpleWeightedGraph<String, DefaultWeightedEdge> creatWeightedGraph(){
		System.out.println("func creatWeightedGraph(全图） is running!");
		SimpleWeightedGraph<String, DefaultWeightedEdge> thisGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
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
	        	
	        	thisGraph.addVertex(fromVertex);
	        	thisGraph.addVertex(toVertex);
	        	thisGraph.addEdge(fromVertex, toVertex);
	        	thisGraph.setEdgeWeight(thisGraph.getEdge(fromVertex, toVertex), thisDis);
	        }
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		System.out.println("func creatWeightedGraph(全图） is over!");
		return thisGraph;
	}
	public static Map<String, Set<String>> myUnweightedGraph = null;
	public static SimpleWeightedGraph<String, DefaultWeightedEdge> myWeightedGraph  = null;
	static {
		if (isWightedGraph) {
			myWeightedGraph  = creatWeightedGraph();
		}else {
			myUnweightedGraph = creatUnweightedGraph();
		}
	}
	
	/**
	 * 空参默认构造函数
	 */
	public Class1RandomPairAndShortestPathLen() {
		
	}
	
	/**
	 * getBfsShortestPathLenForUnweightedGraph
	 * @param graph 工作图
	 * @param from 出节点
	 * @param to 入节点
	 * @return shortestPathLen
	 */
	public static int getBfsShortestPathLenForUnweightedGraph(Map<String, Set<String>> graph, String from, String to) {
		Set<String> visited = new HashSet<>();
		Map<String, Integer> dis = new HashMap<>();
		
		Queue<String> queue = new LinkedList<>();
		queue.add(from);
		visited.add(from);
		dis.put(from, 0);
		while(!queue.isEmpty()) {
			String top = queue.poll();
			int nextDis = dis.get(top) + 1;
			Set<String> topAdjSet = graph.get(top);
//			//20180513: 保证非空,针对BSQ's Robustness 的随机取点且相当Batch子图大小的子图查询
//			if(topAdjSet.size() == 0)
//				continue;
			for(String i: topAdjSet) {
				if(!visited.contains(i)) {
					queue.add(i);
					visited.add(i);
					dis.put(i, nextDis);
					if(i.equals(to))
						break;
				}
			}
			
			if(visited.contains(to)) 
				return dis.get(to);
		}
		System.out.println("Warning : " + from + " " + to + "    Unreachable!!!");
		return Integer.MAX_VALUE;
	}
	
	/**
	 * 无权图构造函数
	 * @param from 出点
	 * @param to 入点
	 * @param graph 工作图
	 */
	public Class1RandomPairAndShortestPathLen(String from, String to, Map<String, Set<String>> graph) {
		this.from = from;
		this.to = to;
		this.shortestPathLen = getBfsShortestPathLenForUnweightedGraph(graph, this.from, this.to);
	}
	
	/**
	 * 有权图构造函数,只构造一次静态成员变量myGraph：保存全图原始信息
	 * @param from 出点
	 * @param to 入点
	 * @param graph 工作图
	 */
	public Class1RandomPairAndShortestPathLen(String from, String to, SimpleWeightedGraph<String, DefaultWeightedEdge> graph) {
		this.from = from;
		this.to = to;
		//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
		DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
				new DijkstraShortestPath<String, DefaultWeightedEdge>(graph, this.from, this.to);
		this.shortestPathLen = (int) myDijkstraShortestPath.getPathLength();
	}
	
	/**
	 * 随机生成queryPairNum对pair，并利用RandomPair的getBfsShortestPath（）计算pair之间最短距离. 这里同时适用有权图和无权图
	 * @param queryPairNum 待生成pair组数
	 * @param vertexNum 节点个数，最大节点编号+1
	 * @return 长度为queryPairNum的RandomPair数组
	 */
	public static Class1RandomPairAndShortestPathLen[] getRandomPairArray(int queryPairNum, int vertexNum) {
		startTime = System.nanoTime();
		Random random = new Random();
		Class1RandomPairAndShortestPathLen randomPairArray[] = new Class1RandomPairAndShortestPathLen[queryPairNum];
		
		for(int i = 0; i < queryPairNum; ++i) {
			int fromInt = -1, toInt = -1;
			while(fromInt == toInt) {
				fromInt = random.nextInt(vertexNum);
				toInt = random.nextInt(vertexNum);
			}
			if (! isWightedGraph) {
				if (! (myUnweightedGraph.containsKey(String.valueOf(fromInt)) && myUnweightedGraph.containsKey(String.valueOf(toInt))) ) {
					--i;
					continue;
				}
				randomPairArray[i] = new Class1RandomPairAndShortestPathLen(String.valueOf(fromInt), String.valueOf(toInt), myUnweightedGraph);
			}else {
				if (! (myWeightedGraph.containsVertex(String.valueOf(fromInt)) && myWeightedGraph.containsVertex(String.valueOf(toInt))) ) {
					--i;
					continue;
				}
				randomPairArray[i] = new Class1RandomPairAndShortestPathLen(Integer.toString(fromInt), Integer.toString(toInt), myWeightedGraph);
			}
			
			if(randomPairArray[i].shortestPathLen > disconnectJudge) { //这里确保所有pair都是可以连通的
				--i;
				continue;
			}
			System.out.println("getRandomPairArray has proccessed " + i + " pairs ! fromVertex : " + Integer.toString(fromInt) + " || toVertex : " + Integer.toString(toInt) + " || shortestPathLen : " + randomPairArray[i].shortestPathLen);
		}
		
		endTime = System.nanoTime();
		System.out.println(dataSet + " : compute " + queryPairNum + " pairs using " + (endTime - startTime) / 1E6 + " ms!");
		return randomPairArray;
	}
	
	/**
	 * 主函数
	 * @param args
	 * @throws IOException
	 */
	public static void main(String []args) throws IOException {
		Class1RandomPairAndShortestPathLen[] randomPairArray = getRandomPairArray(queryPairNum, vertexNum);
		BufferedWriter bWriterPair = new BufferedWriter(new FileWriter(bWriterPairFilePath));
		BufferedWriter bWriterminimumDis = new BufferedWriter(new FileWriter(bWriterShortestPathLenFilePath));
		for(int i = 0; i < queryPairNum; ++i) {
			bWriterPair.write(randomPairArray[i].from + " " + randomPairArray[i].to + "\n");
			bWriterminimumDis.write(String.valueOf(randomPairArray[i].shortestPathLen) + "\n");
		}
		bWriterPair.close();
		bWriterminimumDis.close();
	}
}

/*
 * 20180206---------
facebook : compute 1000 pairs using 7871.07705 ms!

Slashdot : compute 1000 pairs using 29850 ms!

youtube : compute 1000 pairs using 428340 ms!

20180206---------





youtube_4945382 : compute 1000 pairs using 444476.942822 ms!

youtube_undirect : compute 1000 pairs using 280932.262886 ms!

unweightedNYRN : compute 1000 pairs using 63113.133979 ms!

----- -----

NYRN : compute 1000 pairs using 114330.650277 ms!

*/
