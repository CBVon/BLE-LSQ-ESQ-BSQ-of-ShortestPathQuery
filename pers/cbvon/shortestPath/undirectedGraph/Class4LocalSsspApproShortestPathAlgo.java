package pers.cbvon.shortestPath.undirectedGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

/**
 * 局部最短路径的近似算法.这里的sssp指的是dijk和bfs
 * @author cbvon
 */
@SuppressWarnings("deprecation")
public class Class4LocalSsspApproShortestPathAlgo extends Class3ApproShortestPathAlgo{
	
	public static final boolean isCentrality = false;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static double runningTime = 0;
	
	/**
	 * 根据 landmardEmbeddingDirFile 赋值 landmardEmbeddingPathVertexlist
	 * @param landmardEmbeddingPathVertexlist 函数目的就是直接在map修改操作
	 * @param landmardEmbeddingDirFile 对应的embeddingPathVertex文件来源
	 */
	@SuppressWarnings("resource")
	public static void doGetLandmarkEmbeddingPathVertexList(Map<String, Set<String>> landmardEmbeddingPathVertexlist, File landmardEmbeddingDirFile) {
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(landmardEmbeddingDirFile));
			String thisString = bReader.readLine();
			if(thisString.equals("{}")) {
				return;
			}
			String mapString = thisString.substring(1, thisString.length() - 2); //去除首尾 {   ]} //20171105 }前面的 ‘]’ bug：导致最后一个node3627无法获取
			String[] mapStringList = mapString.split("], ");
			
			int mapStringListLen = mapStringList.length;
			for(int i1 = 0; i1 < mapStringListLen; ++i1) {
				String thisNode = mapStringList[i1].split("=")[0];
				String thisPathListString = mapStringList[i1].split("=")[1]; //[2053, 381, 3640
				String pathListString = thisPathListString.substring(1, thisPathListString.length()); //去除首尾 [   
				String[] path = pathListString.split(", ");
				Set<String> pathList = new HashSet<String>();
				for(String i2: path)
					pathList.add(i2);
				landmardEmbeddingPathVertexlist.put(thisNode, pathList);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/**
	 * creatSmallerLocalGraph
	 * @param localGraphVertexSet 点集
	 * @return Map<String, Set<String>> myLocalGraph
	 */
	public static Map<String, Set<String>> creatSmallerLocalGraphForUnweightedGraph(Set<String> localGraphVertexSet){
		Map<String, Set<String>> myLocalGraph = new HashMap<>();
		List<String> localGraphVertexList = new ArrayList<>(localGraphVertexSet);
		int localGraphVertexSetLen = localGraphVertexList.size();
		for(int i = 0; i < localGraphVertexSetLen; ++i) {
			for(int j = i + 1; j < localGraphVertexSetLen; ++j) {
				String vertexI = localGraphVertexList.get(i);
				String vertexJ = localGraphVertexList.get(j);
				if(myUnweightedGraph.containsKey(vertexI) && myUnweightedGraph.get(vertexI).contains(vertexJ)) {
					if(myLocalGraph.containsKey(vertexI)) {
						myLocalGraph.get(vertexI).add(vertexJ);
					}else {
						myLocalGraph.put(vertexI, new HashSet<String>() {
							private static final long serialVersionUID = 1L;
							{
								add(vertexJ);
							}
						});
					}
					
					if(myLocalGraph.containsKey(vertexJ)) {
						myLocalGraph.get(vertexJ).add(vertexI);
					}else {
						myLocalGraph.put(vertexJ, new HashSet<String>() {
							private static final long serialVersionUID = 2618264354283836211L;
							{
								add(vertexI);
							}
						});
					}
				}
			}
		}
		return myLocalGraph;
	}
	
	/**
	 * 求解 基于landmark的近似path上的点构成集合，生成局部图，执行局部dijk近似算法，获取近似最短路径
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList 
	 * @return approShortestPathArray[landmardEmbeddingNum][queryPairNum] 比如approShortestPathArray[5][10000]表示用前5个landmark，近似求出10000个pair的近似最短路径解
	 */
	@SuppressWarnings({ "unchecked" })
	public static int[][] getLocalSsspApproShortestPathArrayStepbyLandmark(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		System.out.println("func getLocalDijkApproShortestPathArray is running");
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		
		int landmardEmbeddingDirFileList_len = landmardEmbeddingDirFileList.length;
		int eachLandmarkGroupSize = landmardEmbeddingDirFileList_len / landmarkNum; 
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) { //遍历每个landmark
			System.out.println("thisLandmarkNum ---------- " + thisLandmarkNum);
			
			if(thisLandmarkNum == 0) {
				for(int i = 0; i < queryPairNum; ++i)
					approShortestPathArray[thisLandmarkNum][i] = upperBoundDis; //初始一个上界值， 在计算过程中不断刷新（降低）近似最短距离
			}else {
				//approShortestPathArray[index] = approShortestPathArray[index - 1]; //浅复制
				approShortestPathArray[thisLandmarkNum] = approShortestPathArray[thisLandmarkNum - 1].clone(); //深复制
			}
			
			Set<String>[] pathVertexSet = new Set[queryArrayLength];
			for(int i = 0; i < queryArrayLength; ++i)
				pathVertexSet[i] = new HashSet<>();
			
			for(int thisGroupId = 0; thisGroupId < eachLandmarkGroupSize; ++thisGroupId) {
				int thisFileId = thisLandmarkNum * eachLandmarkGroupSize + thisGroupId;//当前landmark文件 在文件夹中实际编号
				Map<String, Set<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点List>
				doGetLandmarkEmbeddingPathVertexList(landmardEmbeddingPathVertexlist, landmardEmbeddingDirFileList[thisFileId]);
				
				long thisRunningStartTime = System.nanoTime();
				for(int i = 0; i < queryArrayLength; ++i) { 
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].a)) 
						pathVertexSet[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b)) 
						pathVertexSet[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
				}
				long thisRunningEndTime = System.nanoTime();
				runningTime += (Double.valueOf((thisRunningEndTime - thisRunningStartTime)) / eachLandmarkGroupSize);
			}
			
			//下面构图部分是weighted和unweighted唯一不同
			long thisRunningStartTime = System.nanoTime();
			for(int i = 0; i < queryArrayLength; ++i) {
				if (isWightedGraph) {
					//http://jgrapht.org/javadoc/org/jgrapht/graph/UndirectedWeightedSubgraph.html 相比源生实现 2 / 3
					UndirectedWeightedSubgraph<String, DefaultWeightedEdge> thisLocalDijkGraph = new UndirectedWeightedSubgraph<String, DefaultWeightedEdge>(myWeightedGraph, pathVertexSet[i]);
					
					if(! (thisLocalDijkGraph.containsVertex(queryArray[i].a) && thisLocalDijkGraph.containsVertex(queryArray[i].b)))
						continue;
					
					//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
					DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
							new DijkstraShortestPath<String, DefaultWeightedEdge>(thisLocalDijkGraph, queryArray[i].a, queryArray[i].b);
					int thisLocalDijkApproShortestPath = (int) myDijkstraShortestPath.getPathLength();
					if(thisLocalDijkApproShortestPath < approShortestPathArray[thisLandmarkNum][i]) //当前landmark 可以取得更精确（更小）的 近似
						approShortestPathArray[thisLandmarkNum][i] = thisLocalDijkApproShortestPath; 
				}else {
					Map<String, Set<String>> thisLocalBfsGraph = creatSmallerLocalGraphForUnweightedGraph(pathVertexSet[i]);
					if(pathVertexSet[i].size() == 0)
						continue;
					int thisLocalBfsApproShortestPath = getBfsShortestPathLenForUnweightedGraph(thisLocalBfsGraph, queryArray[i].a, queryArray[i].b);
					if(thisLocalBfsApproShortestPath < approShortestPathArray[thisLandmarkNum][i]) //当前landmark 可以取得更精确（更小）的 近似
						approShortestPathArray[thisLandmarkNum][i] = thisLocalBfsApproShortestPath; 
				}
				
			}
			long thisRunningEndTime = System.nanoTime();
			runningTime += (thisRunningEndTime - thisRunningStartTime);
		}
		System.out.println("func getLocalDijkApproShortestPathArray is over!");
		return approShortestPathArray;
	}
	
	/**
	 * main()
	 * @param args
	 */
	public static void main(String[] args) {
		//本段：获取待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); 
		
		//本段：从文件系统获取所有landmark（默认100个）的landmarkEmbedding
		File landmardEmbeddingDirFile = new File(landmardEmbeddingDir);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();
		Arrays.sort(landmardEmbeddingDirFileList);
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); 
		
		////本段：使用landmark数目依次为 10， 20， ...... 100个，分别在不同landmark数目下计算平均误差，观测landmark数目对近似的影响
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		approShortestPathArray = getLocalSsspApproShortestPathArrayStepbyLandmark(queryArray, landmardEmbeddingDirFileList);
		for(int thisLandmarkNum = 10; thisLandmarkNum <= 100; thisLandmarkNum += 10) {
			System.out.println("thisLandmarkNum : " + thisLandmarkNum);
			double avgError = getAvgError(approShortestPathArray[thisLandmarkNum - 1], pairsMiniDisArray);
			System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		}
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime / 1E6 + " ms!\n");		
	}
}
/*
20180206-----------------------
thisLandmarkNum : 100
rightCount : 1000
wrongCount : 0
facebook Random avgError : 0.0

facebook Random runningTime : 358.92751239999984 ms!
------
thisLandmarkNum : 100
rightCount : 1000
wrongCount : 0
facebook Centrality avgError : 0.0

facebook Centrality runningTime : 350.16960299999965 ms!
----------
rightCount : 976
wrongCount : 0
Slashdot Random avgError : 0.0068

Slashdot Random runningTime : 446.3630830843391 ms!
-----
rightCount : 986
wrongCount : 0
Slashdot Centrality avgError : 0.0037333333333333333

Slashdot Centrality runningTime : 398.65759402409714 ms!
----------
rightCount : 978
wrongCount : 0
youtube Random avgError : 0.004544047619047619

youtube Random runningTime : 652.1810136105266 ms!
-----
rightCount : 986
wrongCount : 0
youtube Centrality avgError : 0.002916666666666667

youtube Centrality runningTime : 532.1502421347133 ms!
20180206------------------------




----- -----公路nouse
rightCount : 816
unweightedNYRN Centrality avgError : 0.006214548458720217
unweightedNYRN Centrality runningTime : 224421.46016120168 ms!
----- -----
rightCount : 607
wrongCount : 0
NYRN Centrality avgError : 0.008748533565843503
NYRN Centrality runningTime : 1333969.4943398 ms!













*/