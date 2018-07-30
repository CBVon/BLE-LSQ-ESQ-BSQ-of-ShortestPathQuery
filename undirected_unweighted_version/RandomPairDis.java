package undirected_unweighted_version;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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

public class RandomPairDis {
	
	public static int queryPairNum = 0;
	public static int vertexNum = 0;
	public static String graphFilePath = null;
	public static String bWriterPairFilePath = null;
	public static String bWriterminimumDisFilePath = null;
	
	//landmarkEmbedding
	public static final int landmarkNum = 100;
	public static final int partGroupSize = 1000;
	
	public static int disconnectJudge = 0; //10000作为不连通判定
	public static int upperBoundDis = 0; //20在社交网络中已经是一个超大值，作为连不通的惩罚
	public static String adjPart100Graph = null;//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
	public static String bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = null;
	public static String bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = null;
	
	/**
	 * Slashdot
	 * Epinions
	 * dblp
	 * facebook
	 * Douban
	 * youtube
	 * unweightedNYRN
	 */
	public static final String dataSet = "Slashdot";
	
	static {
		if (dataSet.equals("Slashdot")) {
			/**
			 * Slashdot		social network   http://snap.stanford.edu/data/soc-Slashdot0902.html
			 * vertexNum: 82168 (0~82167 没有空点)
			 * maxId: 82167
			 * edgNum: 948464 (包含 节点本身的自环)
			 * Diameter (longest shortest path): 	11
			 */
			queryPairNum = 10000;
			vertexNum = 82168;
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/soc-Slashdot0902.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/10000_pairs.json";
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/10000_pairsShortestPathLen.json";
			
			disconnectJudge = 10000; //10000作为不连通判定
			upperBoundDis = 20; //20在社交网络中已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/cutGraph/Slashdot_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
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
			queryPairNum = 10000;
			vertexNum = 75888; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/soc-Epinions1.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/10000_pairs.json";
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/10000_pairsShortestPathLen.json";
			
			disconnectJudge = 10000; //10000作为不连通判定
			upperBoundDis = 20; //20已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/cutGraph/soc-Epinions1_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
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
			queryPairNum = 10000;
			vertexNum = 425957; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/com-dblp.ungraph.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/10000_pairs.json";
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/10000_pairsShortestPathLen.json";
			
			disconnectJudge = 10000; //10000作为不连通判定
			upperBoundDis = 20; //20已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/cutGraph/dblp_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
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
			queryPairNum = 10000;
			vertexNum = 4039; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/facebook_combined.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/10000_pairs.json";
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/10000_pairsShortestPathLen.json";
			
			disconnectJudge = 10000; //10000作为不连通判定
			upperBoundDis = 20; //20已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/cutGraph/facebook_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
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
			queryPairNum = 10000;
			vertexNum = 154908; 
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/Douban_ungraph.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/10000_pairs.json";
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/10000_pairsShortestPathLen.json";
			
			disconnectJudge = 10000; //10000作为不连通判定
			upperBoundDis = 20; //20已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/cutGraph/Douban_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
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
			queryPairNum = 10000;
			vertexNum = 1157827;  
			graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/youtube.txt";
			bWriterPairFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/10000_pairs.json";
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/10000_pairsShortestPathLen.json";
			
			disconnectJudge = 10000; //10000作为不连通判定
			upperBoundDis = 20; //20在社交网络中已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/cutGraph/Youtube_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Youtube/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
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
			bWriterminimumDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/10000_pairsShortestPathLen.json";
			
			disconnectJudge = 10000; //10000作为不连通判定
			upperBoundDis = 1000; //600在社交网络中已经是一个超大值，作为连不通的惩罚
			adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/cutGraph/NYRN_unweighted_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedCentrality/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedCentrality/pathVecListEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedRandom/pathLenEmbedding/";
			bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/NYRN_unweighted/landmarkEmbeddingBasedRandom/pathVecListEmbedding/";
		}
	}
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public String from, to;
	public int minimumDis;
	
	public RandomPairDis() {
		
	}
	
	public RandomPairDis(String from, String to) {
		this.from = from;
		this.to = to;
		this.minimumDis = getBfsShortestPathLen(myGraph ,this.from, this.to);
	}
	
	/**
	 * getBfsShortestPath
	 * @param graph 工作图
	 * @param from 出节点
	 * @param to 入节点
	 * @return shortestPathLen
	 */
	public static int getBfsShortestPathLen(Map<String, Set<String>> graph, String from, String to) {
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
		System.out.println(from + " " + to + "    Unreachable!");
		return Integer.MAX_VALUE;
	}
	
	public static Map<String, Set<String>> myGraph = creatGraph();
	
	/**
	 * creatGraph(全图)
	 * @return Map<String, Set<String>>全图
	 */
	public static Map<String, Set<String>> creatGraph(){
		System.out.println("func creatGraph(全图) is running!");
		Map<String, Set<String>> graph = new HashMap<>();
		
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
	        	if(graph.containsKey(fromVertex)) {
	        		graph.get(fromVertex).add(toVertex);
	        	}else {
					graph.put(fromVertex, new HashSet<String>() {
						private static final long serialVersionUID = 509329333037163623L;
						{
							add(toVertex);
						}
					});
				}
	        	
	        	if(graph.containsKey(toVertex)) {
	        		graph.get(toVertex).add(fromVertex);
	        	}else {
					graph.put(toVertex, new HashSet<String>() {
						private static final long serialVersionUID = -1269799273215955482L;
						{
							add(fromVertex);
						}
					});
				}
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
		}

		System.out.println("func creatGraph(全图） is over!");
		return graph;
	}
	
	/**
	 * 随机生成queryPairNum对pair，并利用RandomPair的getBfsShortestPath（）计算pair之间最短距离
	 * @param queryPairNum 待生成pair组数
	 * @param vertexNum 节点个数，最大节点编号+1
	 * @return 长度为queryPairNum 的RandomPair数组
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
			if (! (myGraph.containsKey(String.valueOf(fromInt)) && myGraph.containsKey(String.valueOf(toInt))) ) {
				--i;
				continue;
			}
			randomPairArray[i] = new RandomPairDis(String.valueOf(fromInt), String.valueOf(Integer.toString(toInt)));
			if(randomPairArray[i].minimumDis > 1E10) { //不连通.这里确保10000pair都是可以连通的
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
//slashdot ： compute 10000 pairs using 283089 ms!

//Epinions1 ： compute 10000 pairs using 261101 ms!

//dblp ： compute 10000 pairs using 1122209 ms!

//facebook ： compute 10000 pairs using 16278 ms!

//Douban ： compute 10000 pairs using 298094 ms!

//Youtube : compute 10000 pairs using 4480953 ms!

//unweightedNYRN : compute 10000 pairs using 620168 ms!
//unweightedNYRN : compute 1000 pairs using 63582 ms!

