package undirected_unweighted_version;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LocalOneBfsApproShortestPathAlgo extends LocalEnsembleBfsApproShortestPathAlgo{
	
	public static final boolean isCentrality = false;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static double runningTime = 0;
	
	public static int getEdgeNumOfGraph(Map<String, Set<String>> graph) {
		int edgeNum = 0;
		for(String vec: graph.keySet()) 
			edgeNum += graph.get(vec).size();
		edgeNum /= 2;
		return edgeNum;
	}
	
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
				
				long thisRunningStartTime = System.currentTimeMillis();
				for(int i = 0; i < queryArrayLength; ++i) {
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].a)) 
						onePathVertexSet.addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b)) 
						onePathVertexSet.addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
				}
				long thisRunningEndTime = System.currentTimeMillis();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
		}
		
		long thisRunningStartTime = System.currentTimeMillis();
		Map<String, Set<String>> oneLocalEnsembleBfsGraph = creatBiggerLocalGraph(onePathVertexSet);
		for(int i = 0; i < queryPairNum; ++i) {
			if(i % 1000 == 0)
				System.out.println("queryPair : " + i);
			approShortestPathArray[i] = getBfsShortestPathLen(oneLocalEnsembleBfsGraph, queryArray[i].a, queryArray[i].b);
		}
		long thisRunningEndTime = System.currentTimeMillis();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		
		System.out.println("onePathVertexSet.size() : " + onePathVertexSet.size());
		System.out.println("oneLocalEnsembleBfsGraph.vecNum : " + oneLocalEnsembleBfsGraph.size());
		System.out.println("oneLocalEnsembleBfsGraph.edgeNum : " + getEdgeNumOfGraph(oneLocalEnsembleBfsGraph));
		System.out.println("func getLocalOneBfsApproShortestPathArray is over");
		return approShortestPathArray;
	}
	
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
		approShortestPathArray = getLocalOneBfsApproShortestPathArray(queryArray, landmardEmbeddingDirFileList);
		double avgError = getAvgError(approShortestPathArray, pairsMiniDisArray);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime + " ms!\n");		
	}

}

//onePathVertexSet.size() : 33988
//oneLocalEnsembleBfsGraph.vecNum : 33988
//oneLocalEnsembleBfsGraph.edgeNum : 371874
//func getLocalOneBfsApproShortestPathArray is over
//rightCount : 9990
//wrongCount : 0
//Slashdot Centrality avgError : 2.866666666666667E-4
//Slashdot Centrality runningTime : 175388.8313253012 ms!


//onePathVertexSet.size() : 33943
//oneLocalEnsembleBfsGraph.vecNum : 33943
//oneLocalEnsembleBfsGraph.edgeNum : 370616
//func getLocalOneBfsApproShortestPathArray is over
//rightCount : 9985
//wrongCount : 0
//Slashdot Random avgError : 4.3666666666666664E-4
//Slashdot Random runningTime : 174971.6746987952 ms!

//------------------------------------

//onePathVertexSet.size() : 79553
//oneLocalEnsembleBfsGraph.vecNum : 79553
//oneLocalEnsembleBfsGraph.edgeNum : 712849
//func getLocalOneBfsApproShortestPathArray is over
//rightCount : 9928
//youtube Random avgError : 0.0015411904761904749
//youtube Random runningTime : 798621.0500863558 ms!

//onePathVertexSet.size() : 83323
//oneLocalEnsembleBfsGraph.vecNum : 83323
//oneLocalEnsembleBfsGraph.edgeNum : 758353
//func getLocalOneBfsApproShortestPathArray is over
//rightCount : 9941
//youtube Centrality avgError : 0.0012342063492063486
//youtube Centrality runningTime : 861905.7962003454 ms!

//------------------------------------

//onePathVertexSet.size() : 73431
//oneLocalEnsembleBfsGraph.vecNum : 73431
//oneLocalEnsembleBfsGraph.edgeNum : 85631
//func getLocalOneBfsApproShortestPathArray is over
//rightCount : 993
//wrongCount : 0
//unweightedNYRN Centrality avgError : 3.318377858927125E-4
//unweightedNYRN Centrality runningTime : 16464.683018867923 ms!

//onePathVertexSet.size() : 72440
//oneLocalEnsembleBfsGraph.vecNum : 72440
//oneLocalEnsembleBfsGraph.edgeNum : 84725
//func getLocalOneBfsApproShortestPathArray is over
//rightCount : 995
//wrongCount : 0
//unweightedNYRN Random avgError : 2.376392752332602E-4
//unweightedNYRN Random runningTime : 16692.837735849058 ms!



