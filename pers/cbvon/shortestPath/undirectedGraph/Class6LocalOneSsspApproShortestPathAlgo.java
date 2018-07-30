package pers.cbvon.shortestPath.undirectedGraph;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

@SuppressWarnings("deprecation")
public class Class6LocalOneSsspApproShortestPathAlgo extends Class5LocalEnsembleDijkApproShortestPathAlgo{
	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static double runningTime = 0;
	
	/**
	 * getEdgeNumForUnweightedGraph
	 * @param graph
	 * @return int edgeNum
	 */
	public static int getEdgeNumForUnweightedGraph(Map<String, Set<String>> graph) {
		int edgeNum = 0;
		for(String vec: graph.keySet()) 
			edgeNum += graph.get(vec).size();
		edgeNum /= 2;
		return edgeNum;
	}
	
	/**
	 * getLocalOneBfsApproShortestPathArray
	 * @param queryArray
	 * @param landmardEmbeddingDirFileList
	 * @return int[queryPairNum] approShortestPathArray
	 */
	public static int[] getLocalOneBfsApproShortestPathArray(Pair[] queryArray, File[] landmardEmbeddingDirFileList) {
		System.out.println("func getLocalOneBfsApproShortestPathArray is running");
		int[] approShortestPathArray = new int[queryPairNum];
		Set<String> onePathVertexSet = new HashSet<>();
		
		int landmardEmbeddingDirFileListLen = landmardEmbeddingDirFileList.length;
		int eachLandmarkGroupSize = landmardEmbeddingDirFileListLen / landmarkNum; 
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) {
			System.out.println("landmarkId : " + thisLandmarkNum + "----- -----");
			
			for(int thisGroupId = 0; thisGroupId < eachLandmarkGroupSize; ++thisGroupId) {
				int thisFileId = thisLandmarkNum * eachLandmarkGroupSize + thisGroupId;//当前landmark文件 在文件夹中实际编号
				Map<String, Set<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点List>
				doGetLandmarkEmbeddingPathVertexList(landmardEmbeddingPathVertexlist, landmardEmbeddingDirFileList[thisFileId]);
				long thisRunningStartTime = System.nanoTime();
				for(int i = 0; i < queryArrayLength; ++i) {
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].a)) 
						onePathVertexSet.addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b)) 
						onePathVertexSet.addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
				}
				long thisRunningEndTime = System.nanoTime();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
		}
		
		long thisRunningStartTime = System.nanoTime();
		if (isWightedGraph) {
			//http://jgrapht.org/javadoc/org/jgrapht/graph/UndirectedWeightedSubgraph.html 相比源生实现 2 / 3
			UndirectedWeightedSubgraph<String, DefaultWeightedEdge> localEnsembleDijkGraph = new UndirectedWeightedSubgraph<String, DefaultWeightedEdge>(myWeightedGraph, onePathVertexSet);
			
			System.out.println("pathVertexSet.size : " + onePathVertexSet.size());
			System.out.println("localEnsembleDijkGraph.vertexSet.size : " + localEnsembleDijkGraph.vertexSet().size());
			System.out.println("localEnsembleDijkGraph.edgeSet.size : " + localEnsembleDijkGraph.edgeSet().size());
			
			for(int i = 0; i < queryPairNum; ++i) {
				//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
				DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
						new DijkstraShortestPath<String, DefaultWeightedEdge>(localEnsembleDijkGraph, queryArray[i].a, queryArray[i].b);
				approShortestPathArray[i] = (int) myDijkstraShortestPath.getPathLength();
			}
		}else {
			Vector<Integer[]> graphFilePathBReaderVector = getGraphFilePathBReaderForUnweightedGraph();
			
			Map<String, Set<String>> oneLocalEnsembleBfsGraph = creatBiggerLocalGraphForUnweightedGraph(graphFilePathBReaderVector, onePathVertexSet);
			for(int i = 0; i < queryPairNum; ++i) 
				approShortestPathArray[i] = getBfsShortestPathLenForUnweightedGraph(oneLocalEnsembleBfsGraph, queryArray[i].a, queryArray[i].b);

			System.out.println("onePathVertexSet.size() : " + onePathVertexSet.size());
			System.out.println("oneLocalEnsembleBfsGraph.vecNum : " + oneLocalEnsembleBfsGraph.size());
			System.out.println("oneLocalEnsembleBfsGraph.edgeNum : " + getEdgeNumForUnweightedGraph(oneLocalEnsembleBfsGraph));
		}
		long thisRunningEndTime = System.nanoTime();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		
		System.out.println("func getLocalOneBfsApproShortestPathArray is over");
		return approShortestPathArray;
	}
	
	/**
	 * nouse
	 * 20180513补充实验,选择相同规模的点,构成相当的batch查询子图,验证BSQ有效性
	 * 坑: query 不一定可以联通. 所以这个验证本身是无效的. 进一步说明BSQ有效性
	 */
	public static int[] getESQRobustnessApproShortestPathArray(Pair[] queryArray, int oneVertexSetSize) {
		System.out.println("func getLocalOneBfsApproShortestPathArray is running");
		int[] approShortestPathArray = new int[queryPairNum];
		Set<String> onePathVertexSet = new HashSet<>();
		
		int queryArrayLength = queryArray.length;
		for(int i = 0; i < queryArrayLength; ++i) {
			onePathVertexSet.add(queryArray[i].a);
			onePathVertexSet.add(queryArray[i].b);
		}
		
		//构建 和BSQ相同大小的,查询子图
		while(onePathVertexSet.size() < oneVertexSetSize) {
			String aInt = String.valueOf(random.nextInt(vertexNum));
			onePathVertexSet.add(aInt);
		}
		
		long thisRunningStartTime = System.nanoTime();
		if (isWightedGraph) {
			//http://jgrapht.org/javadoc/org/jgrapht/graph/UndirectedWeightedSubgraph.html 相比源生实现 2 / 3
			UndirectedWeightedSubgraph<String, DefaultWeightedEdge> localEnsembleDijkGraph = new UndirectedWeightedSubgraph<String, DefaultWeightedEdge>(myWeightedGraph, onePathVertexSet);
			
			System.out.println("pathVertexSet.size : " + onePathVertexSet.size());
			System.out.println("localEnsembleDijkGraph.vertexSet.size : " + localEnsembleDijkGraph.vertexSet().size());
			System.out.println("localEnsembleDijkGraph.edgeSet.size : " + localEnsembleDijkGraph.edgeSet().size());
			
			for(int i = 0; i < queryPairNum; ++i) {
				//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
				DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
						new DijkstraShortestPath<String, DefaultWeightedEdge>(localEnsembleDijkGraph, queryArray[i].a, queryArray[i].b);
				approShortestPathArray[i] = (int) myDijkstraShortestPath.getPathLength();
			}
		}else {
			Vector<Integer[]> graphFilePathBReaderVector = getGraphFilePathBReaderForUnweightedGraph();
			
			Map<String, Set<String>> oneLocalEnsembleBfsGraph = creatBiggerLocalGraphForUnweightedGraph(graphFilePathBReaderVector, onePathVertexSet);
			for(int i = 0; i < queryPairNum; ++i) 
				approShortestPathArray[i] = getBfsShortestPathLenForUnweightedGraph(oneLocalEnsembleBfsGraph, queryArray[i].a, queryArray[i].b);

			System.out.println("onePathVertexSet.size() : " + onePathVertexSet.size());
			System.out.println("oneLocalEnsembleBfsGraph.vecNum : " + oneLocalEnsembleBfsGraph.size());
			System.out.println("oneLocalEnsembleBfsGraph.edgeNum : " + getEdgeNumForUnweightedGraph(oneLocalEnsembleBfsGraph));
		}
		long thisRunningEndTime = System.nanoTime();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		
		System.out.println("func getLocalOneBfsApproShortestPathArray is over");
		return approShortestPathArray;
	}
	
	/**
	 * main()
	 * @param args
	 */
	public static void main(String[] args) {
		//本段：获取待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); //queryArray.length = 10000 //调用父的父类ApproShortestPathAlgo的pairsFilePath
		
		//本段：从文件系统获取所有landmark（默认100个）的landmarkEmbedding
		File landmardEmbeddingDirFile = new File(landmardEmbeddingDir);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径 100个landmark_pathWeight 100个landmark_pathVertexList
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); // pairsMiniDisArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsMiniDisFilePath
		
		//本段：使用landmark数目为100,计算平均误差
		int[] approShortestPathArray = new int[queryPairNum];
		//BSQ
		approShortestPathArray = getLocalOneBfsApproShortestPathArray(queryArray, landmardEmbeddingDirFileList);
		//BSQ's Robustness
		//approShortestPathArray = getESQRobustnessApproShortestPathArray(queryArray, 20000); //20000是针对YouTube BSQ验证//nouse
		double avgError = getAvgError(approShortestPathArray, pairsMiniDisArray);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime / 1E6 + " ms!\n");		
	}
}
/*
20180206---------------------------------------
onePathVertexSet.size() : 1936
oneLocalEnsembleBfsGraph.vecNum : 1936
oneLocalEnsembleBfsGraph.edgeNum : 24206
func getLocalOneBfsApproShortestPathArray is over
rightCount : 1000
wrongCount : 0
facebook Random avgError : 0.0

facebook Random runningTime : 868.6126758 ms!
-----
onePathVertexSet.size() : 2116
oneLocalEnsembleBfsGraph.vecNum : 2116
oneLocalEnsembleBfsGraph.edgeNum : 31833
func getLocalOneBfsApproShortestPathArray is over
rightCount : 1000
wrongCount : 0
facebook Random avgError : 0.0

facebook Random runningTime : 857.9185064 ms!
----------
onePathVertexSet.size() : 11141
oneLocalEnsembleBfsGraph.vecNum : 11141
oneLocalEnsembleBfsGraph.edgeNum : 218486
func getLocalOneBfsApproShortestPathArray is over
180 : 5 4
973 : 5 4
rightCount : 998
wrongCount : 0
Slashdot Random avgError : 5.0E-4

Slashdot Random runningTime : 12360.01095340964 ms!
-----
onePathVertexSet.size() : 11215
oneLocalEnsembleBfsGraph.vecNum : 11215
oneLocalEnsembleBfsGraph.edgeNum : 221462
func getLocalOneBfsApproShortestPathArray is over
rightCount : 1000
wrongCount : 0
Slashdot Centrality avgError : 0.0

Slashdot Centrality runningTime : 10925.140342084338 ms!
----------
onePathVertexSet.size() : 19796
oneLocalEnsembleBfsGraph.vecNum : 19796
oneLocalEnsembleBfsGraph.edgeNum : 245350
func getLocalOneBfsApproShortestPathArray is over
rightCount : 991
wrongCount : 0
youtube Random avgError : 0.0020611111111111108

youtube Random runningTime : 24007.812343527632 ms!
-----
onePathVertexSet.size() : 20799
oneLocalEnsembleBfsGraph.vecNum : 20799
oneLocalEnsembleBfsGraph.edgeNum : 261611
func getLocalOneBfsApproShortestPathArray is over
379 : 6 5
rightCount : 999
wrongCount : 0
youtube Centrality avgError : 2.0E-4

youtube Centrality runningTime : 21596.19162218221 ms!
20180206--------------------------------------





----- ----- 公路 nouse
onePathVertexSet.size() : 73491
oneLocalEnsembleBfsGraph.vecNum : 73491
oneLocalEnsembleBfsGraph.edgeNum : 86170
func getLocalOneBfsApproShortestPathArray is over
rightCount : 995
wrongCount : 0
unweightedNYRN Centrality avgError : 5.734340794126105E-4

unweightedNYRN Centrality runningTime : 16004.990492611321 ms!
----- -----
pathVertexSet.size : 111470
localEnsembleDijkGraph.vertexSet.size : 111470
localEnsembleDijkGraph.edgeSet.size : 138885
func getLocalOneBfsApproShortestPathArray is over
rightCount : 991
wrongCount : 0
NYRN Centrality avgError : 4.3624743731475723E-4

NYRN Centrality runningTime : 81609.9134477736 ms!

















*/