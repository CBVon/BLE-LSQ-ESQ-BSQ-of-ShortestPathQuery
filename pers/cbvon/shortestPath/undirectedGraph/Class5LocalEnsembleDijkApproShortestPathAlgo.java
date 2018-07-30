package pers.cbvon.shortestPath.undirectedGraph;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

@SuppressWarnings("deprecation")
public class Class5LocalEnsembleDijkApproShortestPathAlgo extends Class4LocalSsspApproShortestPathAlgo{
	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmar
	public static final String landmarkEmbeddingDir_pathVecListEmbedding = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static double runningTime = 0;
	
	/**
	 * 求解 基于“所有的”landmark的近似path上所有点构成集合的 局部dijk的 近似算法计算的近似最短路径
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList
	 * @return approShortestPathArray[queryPairNum]
	 */
	@SuppressWarnings("unchecked")
	public static int[] getLocalEnsembleSsspApproShortestPathArrayStepbyLandmark(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is running");
		int[] approShortestPathArray = new int[queryPairNum];
		
		Set<String>[] pathVertexSetArray = new Set[queryPairNum];
		for(int i = 0; i < queryPairNum; ++i) 
			pathVertexSetArray[i] = new HashSet<String>();
		
		int landmardEmbeddingDirFileListLen = landmardEmbeddingDirFileList.length;
		int eachLandmarkGroupSize = landmardEmbeddingDirFileListLen / landmarkNum;
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) { //遍历每个landmark
			System.out.println("thisLandmarkNum ---------- " + thisLandmarkNum);
			
			for(int thisGroupId = 0; thisGroupId < eachLandmarkGroupSize; ++thisGroupId) {
				int thisFileId = thisLandmarkNum * eachLandmarkGroupSize + thisGroupId;//当前landmark文件 在文件夹中实际编号
				Map<String, Set<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点List>
				doGetLandmarkEmbeddingPathVertexList(landmardEmbeddingPathVertexlist, landmardEmbeddingDirFileList[thisFileId]);
				
				long thisRunningStartTime = System.nanoTime();
				for(int i = 0; i < queryArrayLength; ++i) { 
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].a))
						pathVertexSetArray[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b)) 
						pathVertexSetArray[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
				}
				long thisRunningEndTime = System.nanoTime();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
		}
		
		//下面构图部分是weighted和unweighted唯一不同
		long thisRunningStartTime = System.nanoTime();
		for(int i = 0; i < queryPairNum; ++i) {
			if (isWightedGraph) {
				//http://jgrapht.org/javadoc/org/jgrapht/graph/UndirectedWeightedSubgraph.html 相比源生实现 2 / 3
				UndirectedWeightedSubgraph<String, DefaultWeightedEdge> localEnsembleDijkGraph = new UndirectedWeightedSubgraph<String, DefaultWeightedEdge>(myWeightedGraph, pathVertexSetArray[i]);
				
				//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
				DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
						new DijkstraShortestPath<String, DefaultWeightedEdge>(localEnsembleDijkGraph, queryArray[i].a, queryArray[i].b);
				approShortestPathArray[i] = (int) myDijkstraShortestPath.getPathLength();
			}else {
				Map<String, Set<String>> localEnsembleBfsGraph = null;
				if (! dataSet.equals("unweightedNYRN")) {
					localEnsembleBfsGraph = creatSmallerLocalGraphForUnweightedGraph(pathVertexSetArray[i]);
				}else if (dataSet.equals("unweightedNYRN")) {
					localEnsembleBfsGraph = creatSmallerLocalGraphForUnweightedGraph(pathVertexSetArray[i]);
				}
				approShortestPathArray[i] = getBfsShortestPathLenForUnweightedGraph(localEnsembleBfsGraph, queryArray[i].a, queryArray[i].b);
			}
			
		}
		long thisRunningEndTime = System.nanoTime();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!");
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
		File landmardEmbeddingDirFile = new File(landmarkEmbeddingDir_pathVecListEmbedding);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径 100个landmark_pathWeight 100个landmark_pathVertexList
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); // pairsMiniDisArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsMiniDisFilePath
		
		//本段：使用landmark数目为100,计算平均误差
		int[] approShortestPathArray = new int[queryPairNum];
		approShortestPathArray = getLocalEnsembleSsspApproShortestPathArrayStepbyLandmark(queryArray, landmardEmbeddingDirFileList);
		double avgError = getAvgError(approShortestPathArray, pairsMiniDisArray);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime / 1E6 + " ms!\n");		
	}
	
}

/*
20180206---------------------------------
rightCount : 1000
wrongCount : 0
facebook Random avgError : 0.0

facebook Random runningTime : 435.06738859999996 ms!
-----
rightCount : 1000
wrongCount : 0
facebook Centrality avgError : 0.0

facebook Centrality runningTime : 381.90409379999994 ms!
----------
rightCount : 990
wrongCount : 0
Slashdot Random avgError : 0.0028166666666666665

Slashdot Random runningTime : 1726.8459232048192 ms!
-----
rightCount : 998
wrongCount : 0
Slashdot Centrality avgError : 5.833333333333333E-4

Slashdot Centrality runningTime : 832.4165636746989 ms!
----------
rightCount : 984
wrongCount : 0
youtube Random avgError : 0.0033273809523809528

youtube Random runningTime : 2370.193052462004 ms!
-----
rightCount : 997
wrongCount : 0
youtube Centrality avgError : 6.5E-4

youtube Centrality runningTime : 1179.5415117780656 ms!
20180206---------------------------------






----- ----- 公路-nouse

func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： 8164622 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!
rightCount : 985
wrongCount : 0
NYRN Centrality avgError : 5.915366669454216E-4

NYRN Centrality runningTime : 108997.39622641509 ms!














*/