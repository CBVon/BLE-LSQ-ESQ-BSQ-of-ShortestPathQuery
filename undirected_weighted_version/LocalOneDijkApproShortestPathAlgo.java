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
public class LocalOneDijkApproShortestPathAlgo extends LocalEnsembleDijkApproShortestPathAlgo{
		
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
	public static int[] getLocalOneDijkApproShortestPathArray(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		System.out.println("func getLocalOneDijkApproShortestPathArray is running");
		int[] approShortestPathArray = new int[queryPairNum];
		Set<String> onePathVertexSet = new HashSet<String>();
		
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
						onePathVertexSet.addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].a));
					if(landmardEmbeddingPathVertexlist.containsKey(queryArray[i].b)) 
						onePathVertexSet.addAll(landmardEmbeddingPathVertexlist.get(queryArray[i].b));
				}
				long thisRunningEndTime = System.currentTimeMillis();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
		}
		
		//下面构图部分是weighted和unweighted唯一不同
		long thisRunningStartTime = System.currentTimeMillis();
		//http://jgrapht.org/javadoc/org/jgrapht/graph/UndirectedWeightedSubgraph.html 相比源生实现 2 / 3
		UndirectedWeightedSubgraph<String, DefaultWeightedEdge> localEnsembleDijkGraph = new UndirectedWeightedSubgraph<String, DefaultWeightedEdge>(myGraph, onePathVertexSet);
		
		System.out.println("pathVertexSet.size : " + onePathVertexSet.size());
		System.out.println("localEnsembleDijkGraph.vertexSet.size : " + localEnsembleDijkGraph.vertexSet().size());
		System.out.println("localEnsembleDijkGraph.edgeSet.size : " + localEnsembleDijkGraph.edgeSet().size());
		
		for(int i = 0; i < queryPairNum; ++i) {
			//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
			DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
					new DijkstraShortestPath<String, DefaultWeightedEdge>(localEnsembleDijkGraph, queryArray[i].a, queryArray[i].b);
			approShortestPathArray[i] = (int) myDijkstraShortestPath.getPathLength();
		}
		long thisRunningEndTime = System.currentTimeMillis();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		
		System.out.println("func getLocalOneDijkApproShortestPathArray is over!");
		return approShortestPathArray;
	}
	
	public static void main(String[] args) {
		//本段：获取待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); //queryArray.length = 10000 //调用父的父类ApproShortestPathAlgo的pairsFilePath
		
		//本段：从文件系统获取所有landmark（默认10000个）的landmarkEmbedding
		File landmardEmbeddingDirFile = new File(landmarkEmbeddingDir_pathVecListEmbedding);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径 100个landmark_pathWeight 100个landmark_pathVertexList
		Arrays.sort(landmardEmbeddingDirFileList);
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); // pairsMiniDisArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsMiniDisFilePath
		
		////本段：使用landmark数目为 100个计算平均误差
		int[] approShortestPathArray = new int[queryPairNum];
		approShortestPathArray = getLocalOneDijkApproShortestPathArray(queryArray, landmardEmbeddingDirFileList);
		double avgError = getAvgError(approShortestPathArray, pairsMiniDisArray);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime + " ms!\n");
	}
}

/**
 * 
 *

landmark 10
库函数
pathVertexSet.size : 70920
CreatLocalGraph running : 261 ms!
Run using ： 50240 ms!
func getLocalOneDijkApproShortestPathArray using ： 199280 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 285
wrongCount : 0
avgError : 0.06854951723214876

节点平方建土    太慢了！！！！！
thispathVertexListLen : 70920
pathVertexSet.size : 70920
CreatLocalGraph running : 187505 ms!
Run using ： 216581 ms!
func getLocalOneDijkApproShortestPathArray using ： 365489 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 285
wrongCount : 0
avgError : 0.06854951723214876

creatLocalGraph
pathVertexSet.size : 70920
CreatLocalGraph running : 292 ms!
Run using ： 25273 ms!
func getLocalOneDijkApproShortestPathArray using ： 174600 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 285
wrongCount : 0
avgError : 0.06854951723214876



landmark 50
creatLocalGraph
pathVertexSet.size : 101024
CreatLocalGraph running : 333 ms!
Run using ： 40515 ms!
func getLocalOneDijkApproShortestPathArray using ： 878836 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 920
wrongCount : 0
avgError : 0.008493239266312713

UndirectedWeightedSubgraph  --库函数真的快
pathVertexSet.size : 101024
CreatLocalGraph running : 239 ms!
Run using ： 76046 ms!
func getLocalOneDijkApproShortestPathArray using ： 923460 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 920
wrongCount : 0
avgError : 0.008493239266312713

landmark 100
pathVertexSet.size : 110511
CreatLocalGraph running : 281 ms!
Run using ： 80499 ms!
func getLocalOneDijkApproShortestPathArray using ： 1794552 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 980
wrongCount : 0
avgError : 6.671621443670972E-4

centrality 1000-pair
pathVertexSet.size : 80867
CreatLocalGraph running : 153 ms!
Run using ： 61392 ms!
func getLocalOneDijkApproShortestPathArray using ： 1361474 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 628
wrongCount : 0
avgError : 0.039766374231323474



10000-pair
pathVertexSet.size : 175513
CreatLocalGraph running : 241 ms!
Run using ： 1292946 ms!
func getLocalOneDijkApproShortestPathArray using ： 3060527 ms!
func getLocalOneDijkApproShortestPathArray is over!
rightCount : 9954
wrongCount : 0
avgError : 1.1764466291133433E-4


 * 
 */


