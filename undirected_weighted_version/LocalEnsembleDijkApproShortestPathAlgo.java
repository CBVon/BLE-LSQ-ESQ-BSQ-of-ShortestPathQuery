package undirected_weighted_version;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

@SuppressWarnings("deprecation")
public class LocalEnsembleDijkApproShortestPathAlgo extends LocalDijkApproShortestPathAlgo{
		
	public static final boolean isCentrality = false;//true:按照中心性取landmark； false：按照随机取landmar
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
	public static int[] getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is running");
		long startTime = System.currentTimeMillis();
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
				
				long thisRunningStartTime = System.currentTimeMillis();
				for(int i = 0; i < queryArrayLength; ++i) { 
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].a))
						pathVertexSetArray[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b)) 
						pathVertexSetArray[i].addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
				}
				long thisRunningEndTime = System.currentTimeMillis();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
		}
		
		//下面构图部分是weighted和unweighted唯一不同
		long thisRunningStartTime = System.currentTimeMillis();
		for(int i = 0; i < queryPairNum; ++i) {
			//http://jgrapht.org/javadoc/org/jgrapht/graph/UndirectedWeightedSubgraph.html 相比源生实现 2 / 3
			UndirectedWeightedSubgraph<String, DefaultWeightedEdge> localEnsembleDijkGraph = new UndirectedWeightedSubgraph<String, DefaultWeightedEdge>(myGraph, pathVertexSetArray[i]);
			System.out.println("pathVertexSetArray[i] : " + pathVertexSetArray[i].size());
			
			//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
			DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
					new DijkstraShortestPath<String, DefaultWeightedEdge>(localEnsembleDijkGraph, queryArray[i].a, queryArray[i].b);
			approShortestPathArray[i] = (int) myDijkstraShortestPath.getPathLength();
		}
		long thisRunningEndTime = System.currentTimeMillis();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： " + (endTime - startTime) + " ms!");
		System.out.println("func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!");
		return approShortestPathArray;
	}
	
	public static void main(String[] args) {
		//本段：获取待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); //queryArray.length = 10000 //调用父的父类ApproShortestPathAlgo的pairsFilePath
		
		//本段：从文件系统获取所有landmark（默认100个）的landmarkEmbedding
		File landmardEmbeddingDirFile = new File(landmarkEmbeddingDir_pathVecListEmbedding);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径 100个landmark_pathWeight 100个landmark_pathVertexList
		Arrays.sort(landmardEmbeddingDirFileList);
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); // pairsMiniDisArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsMiniDisFilePath
		
		////本段：使用landmark数目为 100个计算平均误差
		int[] approShortestPathArray = new int[queryPairNum];
		approShortestPathArray = getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark(queryArray, landmardEmbeddingDirFileList);
		double avgError = getAvgError(approShortestPathArray, pairsMiniDisArray);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime + " ms!\n");	
	}
}
/**
 * 
 * centrality
 func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： 2832588 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!
rightCount : 698
wrongCount : 0
avgError : 0.039825152151981354


random
Run using ： 74934 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： 4757738 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!
rightCount : 975
wrongCount : 0
avgError : 0.001709632957261482


landmarknum : 20
pathVertexSetArray[i] : 7120
pathVertexSetArray[i] : 6066
pathVertexSetArray[i] : 6457
Run using ： 16601 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： 349425 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!
rightCount : 623
wrongCount : 0
avgError : 0.04564393881524994

landmarknum : 50
pathVertexSetArray[i] : 10505
pathVertexSetArray[i] : 11566
pathVertexSetArray[i] : 11713
Run using ： 22627 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： 899863 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!
rightCount : 873
wrongCount : 0
avgError : 0.013169449518077256

landmarknum : 60
pathVertexSetArray[i] : 12204
pathVertexSetArray[i] : 13233
pathVertexSetArray[i] : 13439
Run using ： 28740 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： 1068637 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!
rightCount : 916
wrongCount : 0
avgError : 0.010179124412912917

landmarknum : 70
pathVertexSetArray[i] : 13287
pathVertexSetArray[i] : 14397
pathVertexSetArray[i] : 14465
Run using ： 42187 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark using ： 1381919 ms!
func getLocalEnsembleDijkApproShortestPathArrayStepbyLandmark is over!
rightCount : 930
wrongCount : 0
avgError : 0.009545814343726769



 */
