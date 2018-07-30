package landmark_based_shortest_distance;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

import landmark_based_shortest_distance.ApproShortestPathAlgo.Pair;

public class LocalEnsembleDijkApproShortestPathAlgo extends LocalDijkApproShortestPathAlgo{
	
	public LocalEnsembleDijkApproShortestPathAlgo(String from, String to) {
		super(from, to);
		// TODO Auto-generated constructor stub
	}
	
	//Slashdot
//	public static final String landmardEmbeddingDirBasedCentrality = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedCentrality20171031";
//	public static final String landmarkEmbeddingBasedCentralityReverse = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedCentralityReverse20171031";
//	public static final String landmardEmbeddingDirBasedRandom = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedRandom20171030";
	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix : bWriteLandmarkEmbeddingBasedRandomPrefix;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	
	/**
	 * 求解 基于“所有的”landmark的近似path上所有点构成集合的 局部dijk的 近似算法计算的近似最短路径
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList
	 * @return approShortestPathArray[queryPairNum]
	 */
	public static double[] getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is running");
		long startTime = System.currentTimeMillis();
		double[] approShortestPathArray = new double[queryPairNum];
		
		Set<String>[] pathVertexSetArray = new Set[queryPairNum];
		//UndirectedGraph<String, DefaultEdge>[] localEnsembleDijkGraph = new SimpleGraph[queryPairNum]; //为每一对pair 维护一个图，按照 landmark取更新图
		for(int i = 0; i < queryPairNum; ++i) 
			pathVertexSetArray[i] = new HashSet<String>();
			
		int index = 0; //表示有效下标（非 "_embedding.json" 结尾）
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum * 2; ++thisLandmarkNum) { //遍历每个landmark
			
			if(landmardEmbeddingDirFileList[thisLandmarkNum].toString().endsWith("_embedding.json"))  //"_embedding.json" 结尾是pathWeight 不是pathVertex
				continue;
			System.out.println(index + "----- -----");
			
			Map<String, List<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点List>
			doGetLandmarkEmbeddingPathVertexList(landmardEmbeddingPathVertexlist, landmardEmbeddingDirFileList[thisLandmarkNum]);
			
			int queryArrayLength = queryArray.length;
			for(int i = 0; i < queryArrayLength; ++i) { //对 10000pair 进行LocalDijk
				Set<String> pathVertexSet = new HashSet<>();
				
				List<String> landmardEmbeddingPathVertexlistA = landmardEmbeddingPathVertexlist.get(queryArray[i].a);
				for(String str: landmardEmbeddingPathVertexlistA)
					pathVertexSet.add(str);
				
				List<String> landmardEmbeddingPathVertexlistB = landmardEmbeddingPathVertexlist.get(queryArray[i].b);
				for(String str: landmardEmbeddingPathVertexlistB)
					pathVertexSet.add(str);
				//List<String> thisPathVertexList = new ArrayList<>(pathVertexSet);
				pathVertexSetArray[i].addAll(pathVertexSet);
			}
			++index;
			
		}
		
		for(int i = 0; i < queryPairNum; ++i) {
			
			UndirectedGraph<String, DefaultEdge> localEnsembleDijkGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
			List<String>  thispathVertexList = new ArrayList<>(pathVertexSetArray[i]);;
			
			System.out.println("queryPair : " + i);
			//faster creatLocalGraph func !
			int thispathVertexListLen = thispathVertexList.size();
			System.out.println("thispathVertexListLen : " + thispathVertexListLen); //子图300多点
			for(int j = 0; j < thispathVertexListLen; ++j) {
				String vertexA = thispathVertexList.get(j);
				for(int k = j + 1; k < thispathVertexListLen; ++k) {
					String vertexB = thispathVertexList.get(k);
					if(RandomPair.myGraph.containsEdge(vertexA, vertexB)) {
						localEnsembleDijkGraph.addVertex(vertexA);
						localEnsembleDijkGraph.addVertex(vertexB);
						localEnsembleDijkGraph.addEdge(vertexA, vertexB);
					}
				}
			}
			
			//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
			DijkstraShortestPath<String, DefaultEdge> myDijkstraShortestPath = 
					new DijkstraShortestPath<String, DefaultEdge>(localEnsembleDijkGraph, queryArray[i].a, queryArray[i].b);
			approShortestPathArray[i] = myDijkstraShortestPath.getPathLength();

		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： " + (endTime - startTime) + " ms!");
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!");
		return approShortestPathArray;
		
	}
	
	
	public static void main(String[] args) {
		
		//本段：获取待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); //queryArray.length = 10000 //调用父的父类ApproShortestPathAlgo的pairsFilePath
		
		//本段：从文件系统获取所有landmark（默认10000个）的landmarkEmbedding
		File landmardEmbeddingDirFile = new File(landmardEmbeddingDir);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径 100个landmark_pathWeight 100个landmark_pathVertexList
		
		//本段：从文件系统获取所有pair的精确最短距离
		double[] pairsMiniDisArray = new double[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); // pairsMiniDisArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsMiniDisFilePath
		
		////本段：使用landmark数目为 100个计算平均误差
		double[] approShortestPathArray = new double[queryPairNum];
		approShortestPathArray = getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark(queryArray, landmardEmbeddingDirFileList);
		double avgError = getAvgError(approShortestPathArray, pairsMiniDisArray);
		System.out.println("avgError : " + avgError + "\n"); 
		
	}

}
