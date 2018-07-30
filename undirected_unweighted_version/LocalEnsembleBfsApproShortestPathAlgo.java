package undirected_unweighted_version;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LocalEnsembleBfsApproShortestPathAlgo extends LocalBfsApproShortestPathAlgo{
	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
		
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static double runningTime = 0;
	
	/**
	 * 求解 基于“所有的”landmark的近似path上所有点构成集合生成局部图，利用局部bfs的近似算法计算的近似最短路径
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList
	 * @return approShortestPathArray[queryPairNum]
	 */
	@SuppressWarnings("unchecked")
	public static int[] getLocalEnsembleBfsApproShortestPathArray(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		System.out.println("func getLocalEnsembleBfsApproShortestPathArrayStepbyLandmark is running");
		long startTime = System.currentTimeMillis();
		int[] approShortestPathArray = new int[queryPairNum];
		
		Set<String>[] pathVertexSetArray = new Set[queryPairNum];
		for(int i = 0; i < queryPairNum; ++i) 
			pathVertexSetArray[i] = new HashSet<String>();
		
		int landmardEmbeddingDirFileListLen = landmardEmbeddingDirFileList.length;
		int eachLandmarkGroupSize = landmardEmbeddingDirFileListLen / landmarkNum;
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) { //遍历每个landmark
			System.out.println("landmarkId : " + thisLandmarkNum + "----- -----");
			
			for(int thisGroupId = 0; thisGroupId < eachLandmarkGroupSize; ++thisGroupId) {
				int thisFileId = thisLandmarkNum * eachLandmarkGroupSize + thisGroupId;//当前landmark文件 在文件夹中实际编号
				Map<String, Set<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点List>
				doGetLandmarkEmbeddingPathVertexList(landmardEmbeddingPathVertexlist, landmardEmbeddingDirFileList[thisFileId]);
				
				long thisRunningStartTime = System.currentTimeMillis();
				for(int i = 0; i < queryArrayLength; ++i) { 
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].a)) 
						pathVertexSetArray[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b)) 
						pathVertexSetArray[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
					//System.out.println(i + " " + pathVertexSetArray[i].size());
				}
				long thisRunningEndTime = System.currentTimeMillis();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
		}
		
		long thisRunningStartTime = System.currentTimeMillis();
		for(int i = 0; i < queryPairNum; ++i) {
			if(i % 1000 == 0)
				System.out.println("queryPair : " + i);
			Map<String, Set<String>> localEnsembleBfsGraph = null;
			if (! dataSet.equals("unweightedNYRN")) {
				localEnsembleBfsGraph = creatSmallerLocalGraph(pathVertexSetArray[i]);
			}else if (dataSet.equals("unweightedNYRN")) {
				localEnsembleBfsGraph = creatBiggerLocalGraph(pathVertexSetArray[i]);
			}
			approShortestPathArray[i] = getBfsShortestPathLen(localEnsembleBfsGraph, queryArray[i].a, queryArray[i].b);
		}
		long thisRunningEndTime = System.currentTimeMillis();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getLocalEnsembleBfsApproShortestPathArrayStepbyLandmark using ： " + (endTime - startTime) + " ms!");
		System.out.println("func getLocalEnsembleBfsApproShortestPathArrayStepbyLandmark is over!");
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
		approShortestPathArray = getLocalEnsembleBfsApproShortestPathArray(queryArray, landmardEmbeddingDirFileList);
		double avgError = getAvgError(approShortestPathArray, pairsMiniDisArray);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime + " ms!\n");		
	}
}


//rightCount : 9874
//Slashdot Random avgError : 0.0033592857142857114
//Slashdot Random runningTime : 16792.096385542165 ms!

//local
//Slashdot Centrality avgError : 0.003656666666666666
//Slashdot Centrality runningTime : 8820 ms!

//getLocalMaxDegreeLandmarkSet
//rightCount : 9871
//Slashdot Centrality avgError : 0.003446666666666664
//Slashdot Centrality runningTime : 7535.891566265057 ms!

//----------------------------

//rightCount : 9813
//youtube Random avgError : 0.0040313492063492095
//youtube Random runningTime : 20172.609671848026 ms!

//rightCount : 9850
//youtube Centrality avgError : 0.0031966666666666645
//youtube Centrality runningTime : 9984.610535405891 ms!

//----------------------------

//rightCount : 986
//unweightedNYRN Centrality avgError : 0.001258629181676357
//unweightedNYRN Centrality runningTime : 81125.65283018867 ms!

//rightCount : 987
//unweightedNYRN Random avgError : 8.352531195969161E-4
//unweightedNYRN Random runningTime : 82880.96981132076 ms!




