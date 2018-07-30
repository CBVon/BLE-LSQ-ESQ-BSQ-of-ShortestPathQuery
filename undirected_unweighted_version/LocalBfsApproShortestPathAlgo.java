package undirected_unweighted_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalBfsApproShortestPathAlgo extends ApproShortestPathAlgo{
	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static double runningTime = 0;
	
	/**
	 * 根据 landmardEmbeddingDirFile 赋值 landmardEmbeddingPathVertexlist
	 * @param landmardEmbeddingPathVertexlist 函数目的就是直接在Map修改操作
	 * @param landmardEmbeddingDirFile 对应的embeddingPathVertex文件来源
	 */
	@SuppressWarnings("resource")
	public static void doGetLandmarkEmbeddingPathVertexList(Map<String, Set<String>> landmardEmbeddingPathVertexlist, File landmardEmbeddingDirFile) {
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
				Set<String> pathList = new HashSet<String>();
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
	}
	
	/**
	 * creatSmallerLocalGraph
	 * @param localGraphVertexSet 点集
	 * @return Map<String, Set<String>> myLocalGraph
	 */
	public static Map<String, Set<String>> creatSmallerLocalGraph(Set<String> localGraphVertexSet){
		Map<String, Set<String>> myLocalGraph = new HashMap<>();
		List<String> localGraphVertexList = new ArrayList<>(localGraphVertexSet);
		int localGraphVertexSetLen = localGraphVertexList.size();
		for(int i = 0; i < localGraphVertexSetLen; ++i) {
			for(int j = i + 1; j < localGraphVertexSetLen; ++j) {
				String vertexI = localGraphVertexList.get(i);
				String vertexJ = localGraphVertexList.get(j);
				if(myGraph.containsKey(vertexI) && myGraph.get(vertexI).contains(vertexJ)) {
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
	 * 求解 基于landmark的近似path上的点构成集合，生成局部图，执行局部bfs近似算法，获取近似最短路径
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList 
	 * @return approShortestPathArray[landmardEmbeddingNum][queryPairNum] 比如approShortestPathArray[5][10000]表示用前5个landmark，近似求出10000个pair的近似最短路径解
	 */
	@SuppressWarnings("unchecked")
	public static int[][] getLocalBfsApproShortestPathArrayStepbyLandmark(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		System.out.println("func getLocalBfsApproShortestPathArray is running");
		long startTime = System.currentTimeMillis();
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		
		int landmardEmbeddingDirFileListLen = landmardEmbeddingDirFileList.length;
		int eachLandmarkGroupSize = landmardEmbeddingDirFileListLen / landmarkNum;
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) { //遍历每个landmark
			System.out.println("landmarkId : " + thisLandmarkNum + "----- -----");
			
			long thisRunningStartTime = System.currentTimeMillis();
			if(thisLandmarkNum == 0) {
				for(int i = 0; i < queryPairNum; ++i)
					approShortestPathArray[thisLandmarkNum][i] = upperBoundDis; //初始一个上界值， 在计算过程中不断刷新（降低）近似最短距离
			}else {
				//approShortestPathArray[index] = approShortestPathArray[index - 1]; //浅复制
				approShortestPathArray[thisLandmarkNum] = approShortestPathArray[thisLandmarkNum - 1].clone(); //深复制
			}
			long thisRunningEndTime = System.currentTimeMillis();
			runningTime += (thisRunningEndTime - thisRunningStartTime);
			
			Set<String>[] pathVertexSet = new Set[queryArrayLength];
			for(int i = 0; i < queryArrayLength; ++i)
				pathVertexSet[i] = new HashSet<>();
			
			for(int thisGroupId = 0; thisGroupId < eachLandmarkGroupSize; ++thisGroupId) {
				int thisFileId = thisLandmarkNum * eachLandmarkGroupSize + thisGroupId;//当前landmark文件 在文件夹中实际编号
				Map<String, Set<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点Set>
				doGetLandmarkEmbeddingPathVertexList(landmardEmbeddingPathVertexlist, landmardEmbeddingDirFileList[thisFileId]);
				
				thisRunningStartTime = System.currentTimeMillis();
				for(int i = 0; i < queryArrayLength; ++i) {
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].a)) 
						pathVertexSet[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b))
						pathVertexSet[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
				}
				thisRunningEndTime = System.currentTimeMillis();
				runningTime += (Double.valueOf((thisRunningEndTime - thisRunningStartTime)) / eachLandmarkGroupSize);
			}
			
			thisRunningStartTime = System.currentTimeMillis();
			for(int i = 0; i < queryArrayLength; ++i) {
				//System.out.println(i + " " + pathVertexSet[i].size());
				Map<String, Set<String>> thisLocalBfsGraph = creatSmallerLocalGraph(pathVertexSet[i]);
				int thisLocalBfsApproShortestPath = getBfsShortestPathLen(thisLocalBfsGraph, queryArray[i].a, queryArray[i].b);
				if(thisLocalBfsApproShortestPath < approShortestPathArray[thisLandmarkNum][i]) //当前landmark 可以取得更精确（更小）的 近似
					approShortestPathArray[thisLandmarkNum][i] = thisLocalBfsApproShortestPath; 
			}
			thisRunningEndTime = System.currentTimeMillis();
			runningTime += (thisRunningEndTime - thisRunningStartTime);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getLocalBfsApproShortestPathArray using ： " + (endTime - startTime) + " ms!");
		System.out.println("func getLocalBfsApproShortestPathArray is over!");
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
		approShortestPathArray = getLocalBfsApproShortestPathArrayStepbyLandmark(queryArray, landmardEmbeddingDirFileList);
		for(int thisLandmarkNum = 10; thisLandmarkNum <= 100; thisLandmarkNum += 10) {
			System.out.println("thisLandmarkNum : " + thisLandmarkNum);
			double avgError = getAvgError(approShortestPathArray[thisLandmarkNum - 1], pairsMiniDisArray);
			System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		}
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime + " ms!\n");		
	}
}

//rightCount : 9743
//Slashdot Random avgError : 0.006894285714285726
//Slashdot Random runningTime : 4020.3614457831463 ms!

//local Centrality
//Slashdot Centrality avgError : 0.006937619047619059
//Slashdot Centrality runningTime : 4936 ms!

//getLocalMaxDegreeLandmarkSet
//rightCount : 9749
//Slashdot Centrality avgError : 0.006724285714285726
//Slashdot Centrality runningTime : 3445.9036144578463 ms!

//-------------------------

//rightCount : 9718
//youtube Random avgError : 0.00598496031746033
//youtube Random runningTime : 6340.337651125863 ms!

//rightCount : 9779
//youtube Centrality avgError : 0.004629444444444451
//youtube Centrality runningTime : 5387.044905010434 ms!

//-------------------------

//rightCount : 810
//unweightedNYRN Centrality avgError : 0.008526638017733361
//unweightedNYRN Centrality runningTime : 222812.96981131475 ms!

//rightCount : 714
//unweightedNYRN Random avgError : 0.010290043531265869
//unweightedNYRN Random runningTime : 227770.73584905034 ms!


