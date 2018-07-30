package landmark_based_shortest_distance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

import landmark_based_shortest_distance.ApproShortestPathAlgo.Pair;

public class LocalDijkApproShortestPathAlgo extends ApproShortestPathAlgo{
	
	public LocalDijkApproShortestPathAlgo(String from, String to) {
		super(from, to);
		// TODO Auto-generated constructor stub
	}
	
	//public static final String pairsFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/10000_pairs_1.json";
	//public static final String pairsMiniDisFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/10000_pairs_minimum_dis_1.json";
	
	//Slashdot
//	public static final String landmardEmbeddingDirBasedCentrality = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedCentrality20171031";
//	public static final String landmarkEmbeddingBasedCentralityReverse = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedCentralityReverse20171031";
//	public static final String landmardEmbeddingDirBasedRandom = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedRandom20171101";
	
	//Epinions1_adjFormat
//	public static final String landmardEmbeddingDirBasedCentrality = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedCentrality";
//	public static final String landmarkEmbeddingBasedCentralityReverse = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedCentralityReverse20171031";
//	public static final String landmardEmbeddingDirBasedRandom = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedRandom";

	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix : bWriteLandmarkEmbeddingBasedRandomPrefix;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	/**
	 * 根据 landmardEmbeddingDirFile 赋值 landmardEmbeddingPathVertexlist
	 * @param landmardEmbeddingPathVertexlist 函数目的就是直接在map修改操作
	 * @param landmardEmbeddingDirFile 对应的embeddingPathVertex文件来源
	 */
	public static void doGetLandmarkEmbeddingPathVertexList(Map<String, List<String>> landmardEmbeddingPathVertexlist, File landmardEmbeddingDirFile) {
		
		System.out.println("func doGetLandmarkEmbeddingPathVertexList is running!");
		startTime = System.currentTimeMillis();
		
		if(landmardEmbeddingDirFile.toString().endsWith("_embedding.json"))  //"_embedding.json" 结尾是pathWeight 不是pathVertex
			return;
		
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(landmardEmbeddingDirFile));
			String thisString = bReader.readLine();
			String mapString = thisString.substring(1, thisString.length() - 2); //去除首尾 {   ]} //20171105 }前面的 ‘]’ bug：导致最后一个node3627无法获取
			String[] mapStringList = mapString.split("], ");
			
			int mapStringListLen = mapStringList.length;
			for(int i1 = 0; i1 < mapStringListLen; ++i1) {
				String thisNode = mapStringList[i1].split("=")[0];
				String thisPathListString = mapStringList[i1].split("=")[1]; //[2053, 381, 3640
				String pathListString = thisPathListString.substring(1, thisPathListString.length()); //去除首尾 [   
				String[] path = pathListString.split(", ");
				List<String> pathList = new LinkedList<>();
				for(String i2: path)
					pathList.add(i2);
				landmardEmbeddingPathVertexlist.put(thisNode, pathList);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		endTime = System.currentTimeMillis();
		System.out.println("func doGetLandmarkEmbeddingPathVertexList using ： " + (endTime - startTime) + " ms!");
		System.out.println("func doGetLandmarkEmbeddingPathVertexList is over!");
		
	}
	
	public static long sumRunTime = 0;
	
	/**
	 * 求解 基于landmark的近似path上所有点构成集合的 局部dijk的 近似算法计算的近似最短路径
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList 
	 * @return approShortestPathArray[landmardEmbeddingNum][queryPairNum] 比如approShortestPathArray[5][10000]表示用前5个landmark，近似求出10000个pair的近似最短路径解
	 */
	public static double[][] getLocalDijkApproShortestPathArrayStepbyLandmark(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		
		System.out.println("func getLocalDijkApproShortestPathArray is running");
		long startTime = System.currentTimeMillis();
		double[][] approShortestPathArray = new double[landmarkNum][queryPairNum];
		
		int index = 0; //表示有效下标（非 "_embedding.json" 结尾）
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum * 2; ++thisLandmarkNum) { //遍历每个landmark
			
			if(landmardEmbeddingDirFileList[thisLandmarkNum].toString().endsWith("_embedding.json"))  //"_embedding.json" 结尾是pathWeight 不是pathVertex
				continue;
			System.out.println(index + "----- -----");
			if(index == 0)
				for(int i = 0; i < queryPairNum; ++i)
					approShortestPathArray[index][i] = 10000.0; //初始一个上界值， 在计算过程中不断刷新（降低）近似最短距离
			else
				//approShortestPathArray[index] = approShortestPathArray[index - 1]; //浅复制
				approShortestPathArray[index] = approShortestPathArray[index - 1].clone(); //深复制
			
			Map<String, List<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点List>
			doGetLandmarkEmbeddingPathVertexList(landmardEmbeddingPathVertexlist, landmardEmbeddingDirFileList[thisLandmarkNum]);
			
			int queryArrayLength = queryArray.length;
			for(int i = 0; i < queryArrayLength; ++i) { //对 10000pair 进行LocalDijk
				Set<String> pathVertexSet = new HashSet<>();
				for(String str: landmardEmbeddingPathVertexlist.get(queryArray[i].a))
					pathVertexSet.add(str);
				for(String str: landmardEmbeddingPathVertexlist.get(queryArray[i].b))
					pathVertexSet.add(str);
				List<String> pathVertexList = new ArrayList<>(pathVertexSet);
				
//				每次遍历所有 几十万条边的建立局部图方法，对于这里每一个pair/每一个landmark都要建立一个局部图，效率过于低
//				String graphFilePath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/soc-Slashdot0902.txt";
//				UndirectedGraph<String, DefaultEdge> thisLocalDijkGraph = creatLocalGraph(graphFilePath, pathVertexSet);
				
				
				long thisStart = System.currentTimeMillis();
				//faster creatLocalGraph func !
				UndirectedGraph<String, DefaultEdge> thisLocalDijkGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
				int pathVertexListLen = pathVertexList.size();
				for(int j = 0; j < pathVertexListLen; ++j) {
					for(int k = j + 1; k < pathVertexListLen; ++k) {
						String vertexA = pathVertexList.get(j);
						String vertexB = pathVertexList.get(k);
						if(RandomPair.myGraph.containsEdge(vertexA, vertexB)) {
							thisLocalDijkGraph.addVertex(vertexA);
							thisLocalDijkGraph.addVertex(vertexB);
							thisLocalDijkGraph.addEdge(vertexA, vertexB);
						}
					}
				}
				
				long thisEnd = System.currentTimeMillis();
				/*
				if(! (thisLocalDijkGraph.containsVertex(queryArray[i].a) && thisLocalDijkGraph.containsVertex(queryArray[i].b))) {//有可能该landmark能力很弱，无法到达查询pair
					System.out.println(queryArray[i].a + " " + queryArray[i].b);
					continue;
				}
				*/
				//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
				DijkstraShortestPath<String, DefaultEdge> myDijkstraShortestPath = 
						new DijkstraShortestPath<String, DefaultEdge>(thisLocalDijkGraph, queryArray[i].a, queryArray[i].b);
				double thisLocalDijkApproShortestPath = myDijkstraShortestPath.getPathLength();
				
				
				sumRunTime += thisEnd - thisStart;
				
				if(thisLocalDijkApproShortestPath < approShortestPathArray[index][i]) //当前landmark 可以取得更精确（更小）的 近似
					approShortestPathArray[index][i] = thisLocalDijkApproShortestPath; 
			}
			++index;
			
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getLocalDijkApproShortestPathArray using ： " + (endTime - startTime) + " ms!");
		System.out.println("func getLocalDijkApproShortestPathArray is over!");
		System.out.println("sumRunTime : " + sumRunTime);
		return approShortestPathArray;
		
	}
	
	public static void main(String[] args) {
		
		//本段：获取待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); //queryArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsFilePath
		
		
		//本段：从文件系统获取所有landmark（默认10000个）的landmarkEmbedding
		File landmardEmbeddingDirFile = new File(landmardEmbeddingDir);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径 100个landmark_pathWeight 100个landmark_pathVertexList
		
		//本段：从文件系统获取所有pair的精确最短距离
		double[] pairsMiniDisArray = new double[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); // pairsMiniDisArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsMiniDisFilePath
		
		////本段：使用landmark数目依次为 10， 20， ...... 100个，分别在不同landmark数目下计算平均误差，观测landmark数目对近似的影响
		double[][] approShortestPathArray = new double[landmarkNum][queryPairNum];
		approShortestPathArray = getLocalDijkApproShortestPathArrayStepbyLandmark(queryArray, landmardEmbeddingDirFileList);
		for(int thisLandmarkNum = 10; thisLandmarkNum <= 100; thisLandmarkNum += 10) {
			System.out.println("thisLandmarkNum : " + thisLandmarkNum);
			double avgError = getAvgError(approShortestPathArray[thisLandmarkNum - 1], pairsMiniDisArray);
			System.out.println("avgError : " + avgError + "\n"); 
		}
		
		/*代码运行很快，没有必要保存结果
		try {
			BufferedWriter bWriter = new BufferedWriter(new FileWriter("/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/localDijkResultBasedRandom_1030.txt"));
			//bWriter.write(String.valueOf(approShortestPathArray));
			for(int i = 0; i < landmarkNum; ++i) {
				for(int j = 0; j < queryPairNum; ++j) {
					bWriter.write(String.valueOf(approShortestPathArray[i][j]) + " ");
				}
				bWriter.write("\n");
			}
			bWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}

}
/**func getLocalDijkApproShortestPathArray using ： 17676 ms!
 * sumRunTime : 7379
 * sumRunTime : 2022 qiu dijk
 * sumRunTime : 5335 gou tu
 * 
thisLandmarkNum : 100
rightCount : 9646
wrongCount : 0
avgError : 0.009280952380952383
 * 
 * 
 */
