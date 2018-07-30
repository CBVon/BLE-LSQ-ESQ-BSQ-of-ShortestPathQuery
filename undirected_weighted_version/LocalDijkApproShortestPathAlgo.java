package undirected_weighted_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.UndirectedWeightedSubgraph;

@SuppressWarnings("deprecation")
public class LocalDijkApproShortestPathAlgo extends ApproShortestPathAlgo{
	
	public static final boolean isCentrality = false;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static final String landmarkEmbeddingDir_pathVecListEmbedding = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding;
	
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
	 * 求解 基于landmark的近似path上的点构成集合，生成局部图，执行局部dijk近似算法，获取近似最短路径
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList 
	 * @return approShortestPathArray[landmardEmbeddingNum][queryPairNum] 比如approShortestPathArray[5][10000]表示用前5个landmark，近似求出10000个pair的近似最短路径解
	 */
	@SuppressWarnings({ "unchecked" })
	public static int[][] getLocalDijkApproShortestPathArrayStepbyLandmark(Pair[] queryArray, File[] landmardEmbeddingDirFileList){
		System.out.println("func getLocalDijkApproShortestPathArray is running");
		long startTime = System.currentTimeMillis();
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		
		int landmardEmbeddingDirFileList_len = landmardEmbeddingDirFileList.length;
		int eachLandmarkGroupSize = landmardEmbeddingDirFileList_len / landmarkNum; 
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) { //遍历每个landmark
			System.out.println("thisLandmarkNum ---------- " + thisLandmarkNum);
			
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
				Map<String, Set<String>> landmardEmbeddingPathVertexlist = new HashMap<>(); //当前landmark 对应的 Map<目标节点， 目标节点路径上节点List>
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
			
			//下面构图部分是weighted和unweighted唯一不同
			thisRunningStartTime = System.currentTimeMillis();
			for(int i = 0; i < queryArrayLength; ++i) {
				//http://jgrapht.org/javadoc/org/jgrapht/graph/UndirectedWeightedSubgraph.html 相比源生实现 2 / 3
				UndirectedWeightedSubgraph<String, DefaultWeightedEdge> thisLocalDijkGraph = new UndirectedWeightedSubgraph<String, DefaultWeightedEdge>(myGraph, pathVertexSet[i]);
				
				if(! (thisLocalDijkGraph.containsVertex(queryArray[i].a) && thisLocalDijkGraph.containsVertex(queryArray[i].b)))
					continue;
				
				//http://jgrapht.org/javadoc/org/jgrapht/alg/DijkstraShortestPath.html
				DijkstraShortestPath<String, DefaultWeightedEdge> myDijkstraShortestPath = 
						new DijkstraShortestPath<String, DefaultWeightedEdge>(thisLocalDijkGraph, queryArray[i].a, queryArray[i].b);
				int thisLocalDijkApproShortestPath = (int) myDijkstraShortestPath.getPathLength();
				if(thisLocalDijkApproShortestPath < approShortestPathArray[thisLandmarkNum][i]) //当前landmark 可以取得更精确（更小）的 近似
					approShortestPathArray[thisLandmarkNum][i] = thisLocalDijkApproShortestPath; 
			}
			thisRunningEndTime = System.currentTimeMillis();
			runningTime += (thisRunningEndTime - thisRunningStartTime);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getLocalDijkApproShortestPathArray using ： " + (endTime - startTime) + " ms!");
		System.out.println("func getLocalDijkApproShortestPathArray is over!");
		return approShortestPathArray;
		
	}
	
	public static void main(String[] args) {
		
		//本段：获取待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); //queryArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsFilePath
		
		//本段：从文件系统获取所有landmark（默认10000个）的landmarkEmbedding
		File landmardEmbeddingDirFile = new File(landmarkEmbeddingDir_pathVecListEmbedding);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径 100个landmark_pathWeight 100个landmark_pathVertexList
		Arrays.sort(landmardEmbeddingDirFileList);
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); // pairsMiniDisArray.length = 10000 //调用父类ApproShortestPathAlgo的pairsMiniDisFilePath
		
		////本段：使用landmark数目依次为 10， 20， ...... 100个，分别在不同landmark数目下计算平均误差，观测landmark数目对近似的影响
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		approShortestPathArray = getLocalDijkApproShortestPathArrayStepbyLandmark(queryArray, landmardEmbeddingDirFileList);
		for(int thisLandmarkNum = 10; thisLandmarkNum <= 100; thisLandmarkNum += 10) {
			System.out.println("thisLandmarkNum : " + thisLandmarkNum);
			double avgError = getAvgError(approShortestPathArray[thisLandmarkNum - 1], pairsMiniDisArray);
			System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		}
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime + " ms!\n");
	}
}
/**
 
 centrality:
 func getLocalDijkApproShortestPathArray using ： 12783693 ms!
all_runTime : 11231766 ms!
func getLocalDijkApproShortestPathArray is over!
thisLandmarkNum : 10
rightCount : 825
wrongCount : 0
avgError : 0.3864286357361383

thisLandmarkNum : 20
rightCount : 1626
wrongCount : 0
avgError : 0.2265168693811566

thisLandmarkNum : 30
rightCount : 1988
wrongCount : 0
avgError : 0.16684481376264176

thisLandmarkNum : 40
rightCount : 2826
wrongCount : 0
avgError : 0.16232418781843086

thisLandmarkNum : 50
rightCount : 2872
wrongCount : 0
avgError : 0.16196073984174145

thisLandmarkNum : 60
rightCount : 2886
wrongCount : 0
avgError : 0.161940200523136

thisLandmarkNum : 70
rightCount : 3251
wrongCount : 0
avgError : 0.15992332163194378

thisLandmarkNum : 80
rightCount : 3723
wrongCount : 0
avgError : 0.1543946266176078

thisLandmarkNum : 90
rightCount : 3771
wrongCount : 0
avgError : 0.15408579556904725

thisLandmarkNum : 100
rightCount : 4523
wrongCount : 0
avgError : 0.09450203359053345


 random:
 func getLocalDijkApproShortestPathArray using ： 2901631 ms!
all_runTime : 1100979 ms!
func getLocalDijkApproShortestPathArray is over!
thisLandmarkNum : 10
rightCount : 48
wrongCount : 0
avgError : 0.271845261878096

thisLandmarkNum : 20
rightCount : 96
wrongCount : 0
avgError : 0.09394738304110196

thisLandmarkNum : 30
rightCount : 145
wrongCount : 0
avgError : 0.054690019630993625

thisLandmarkNum : 40
rightCount : 168
wrongCount : 0
avgError : 0.04697319996493628

thisLandmarkNum : 50
rightCount : 206
wrongCount : 0
avgError : 0.040321465370714904

thisLandmarkNum : 60
rightCount : 252
wrongCount : 0
avgError : 0.031227365563905457

thisLandmarkNum : 70
rightCount : 307
wrongCount : 0
avgError : 0.026506804313161218

thisLandmarkNum : 80
rightCount : 320
wrongCount : 0
avgError : 0.023770691371089083

thisLandmarkNum : 90
rightCount : 341
wrongCount : 0
avgError : 0.01364711540378216

thisLandmarkNum : 100
rightCount : 375
wrongCount : 0
avgError : 0.011324337937293453
 
 
 
 
 
 * 
 */
