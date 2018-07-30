package pers.cbvon.shortestPath.undirectedGraph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class Class2LandmarkEmbeddingPathLenAndVertexSet extends Class1RandomPairAndShortestPathLen{
	
	public final static boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static long runningTime = 0;
	public static long externalStorageTime = 0;
	
	public static Random random = new Random();
	
	public static DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraShortestPath = null;
	static {
		if (isWightedGraph) {
			//http://jgrapht.org/javadoc/org/jgrapht/alg/shortestpath/DijkstraShortestPath.html
			//注意这里的声明只运行一次，一定建立了强大图的索引； RandomPair.myGraph 是静态对象，只需要初始化一次
			dijkstraShortestPath = new DijkstraShortestPath<String, DefaultWeightedEdge>(Class1RandomPairAndShortestPathLen.myWeightedGraph);
		}
	}
	
	public Map<String, Integer> singleSourceShortestPathLen = new HashMap<>();
	public Map<String, Set<String>> singleSourceShortestPathVertexSet = new HashMap<>();
	
	/**
	 * 随机生成100个整数（0～vertexNum - 1），作为landmark节点Id. 同时适用有权图和无权图
	 * @return Set<String> randomLandmarkSet
	 */
	public static Set<String> getRandomLandmarkSet(){
		Set<String> randomLandmarkSet = new HashSet<>();
		while(randomLandmarkSet.size() < landmarkNum) {
			String thisLandmark = String.valueOf(random.nextInt(vertexNum));
			if (isWightedGraph) {
				if(!randomLandmarkSet.contains(thisLandmark) && myWeightedGraph.containsVertex(thisLandmark) && myWeightedGraph.degreeOf(thisLandmark) > 0) 
					randomLandmarkSet.add(thisLandmark);
			}else {
				if(!randomLandmarkSet.contains(thisLandmark) && myUnweightedGraph.containsKey(thisLandmark) && myUnweightedGraph.get(thisLandmark).size() > 0) 
					randomLandmarkSet.add(thisLandmark);
			}
		}
		return randomLandmarkSet;
	}
	
	@SuppressWarnings("resource")
	public static Vector<Integer[]> getGraphFilePathBReaderForUnweightedGraph(){
		long thisExternalStorageStartTime = System.nanoTime();  
		Vector<Integer[]> graphFilePathBReaderVector = new Vector<Integer[]>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(graphFilePath));
			String tempString = null;  
	        int line = 0;
	        while ((tempString = bReader.readLine()) != null) {// 一次读入一行，直到读入null为文件结束 
	        	if(++line <= 4)//前4行无用信息
	        		continue;
	        	Integer[] thisArray = new Integer[3];
	        	thisArray[0] = Integer.valueOf(tempString.split("\t")[0]);
	        	thisArray[1] = Integer.valueOf(tempString.split("\t")[1]);
	        	if(thisArray[0] == thisArray[1])
	        		continue;
	        	graphFilePathBReaderVector.add(thisArray);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long thisExternalStorageEndTime = System.nanoTime();  
		externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
		return graphFilePathBReaderVector;
	}
	
	/**
	 * 遍历原始graphFilePath，如果某行（pair（a，b））两点都在集合Set中,就添加到thisLocalGraph中. 注意这里针对bigger子图,因为子图很小的话没必要去遍历全图所有边.针对无权图
	 * @param localGraphVertexSet 生成当前图的节点集合
	 * @return 无权图Map<String, Set<String>> thisLocalGraph
	 */
	public static Map<String, Set<String>> creatBiggerLocalGraphForUnweightedGraph(Vector<Integer[]> graphFilePathBReaderVector, Set<String> localGraphVertexSet){
		Map<String, Set<String>> thisLocalGraph = new HashMap<>();
		for(Integer[] thisVec: graphFilePathBReaderVector) {
			String fromVertex = String.valueOf(thisVec[0]);
			String toVertex = String.valueOf(thisVec[1]);
			if(!(localGraphVertexSet.contains(fromVertex) && localGraphVertexSet.contains(toVertex)))//如果当前边的两个端点不同时都在子图集合中，没必要进行下去
				continue;
			
			if(thisLocalGraph.containsKey(fromVertex)) {
				thisLocalGraph.get(fromVertex).add(toVertex);
			}else {
				thisLocalGraph.put(fromVertex, new HashSet<String>() {
					private static final long serialVersionUID = 2113398549231985272L;
					{
						add(toVertex);
					}
				});
			}
			
			if(thisLocalGraph.containsKey(toVertex)) {
				thisLocalGraph.get(toVertex).add(fromVertex);
			}else {
				thisLocalGraph.put(toVertex, new HashSet<String>() {
					private static final long serialVersionUID = 5263894297326742091L;
					{
						add(fromVertex);
					}
				});
			}
		}
		return thisLocalGraph;
	}
	
	@SuppressWarnings("resource")
	public static Vector<Integer[]> getGraphFilePathBReaderForWeightedGraph(){
		long thisExternalStorageStartTime = System.nanoTime();  
		Vector<Integer[]> graphFilePathBReaderVector = new Vector<Integer[]>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(graphFilePath));
			String tempString = null;  
	        int line = 0;
	        while ((tempString = bReader.readLine()) != null) {// 一次读入一行，直到读入null为文件结束 
	        	if(++line <= 4)//前4行无用信息
	        		continue;
	        	Integer[] thisArray = new Integer[3];
	        	thisArray[0] = Integer.valueOf(tempString.split("\t")[0]);
	        	thisArray[1] = Integer.valueOf(tempString.split("\t")[1]);
	        	thisArray[2] = Integer.valueOf(tempString.split("\t")[2]);
	        	if(thisArray[0] == thisArray[1])
	        		continue;
	        	graphFilePathBReaderVector.add(thisArray);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long thisExternalStorageEndTime = System.nanoTime();  
		externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
		return graphFilePathBReaderVector;
	}
	
	/**
	 * 遍历原始graphFilePath，如果某行（pair（a，b））两点都在集合Set中，就添加到myLocalGraph中。注意这里针对bigger子图，因为子图很小的话没必要去遍历全图所有边.针对有权图
	 * @param localGraphVertexSet 生成当前图的节点集合
	 * @return 无权图 SimpleWeightedGraph<String, DefaultWeightedEdge> myLocalGraph
	 */
	public static SimpleWeightedGraph<String, DefaultWeightedEdge> creatBiggerLocalGraphForWeightedGraph(Vector<Integer[]> graphFilePathBReaderVector, Set<String> localGraphVertexSet){
		SimpleWeightedGraph<String, DefaultWeightedEdge> thisLocalGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		for(Integer[] thisVec: graphFilePathBReaderVector) {
			String fromVertex = String.valueOf(thisVec[0]);
			String toVertex = String.valueOf(thisVec[1]);
			int thisDis = thisVec[2];
			if(!(localGraphVertexSet.contains(fromVertex) && localGraphVertexSet.contains(toVertex)))//如果当前边的两个端点不同时都在子图集合中，没必要进行下去
				continue;
			thisLocalGraph.addVertex(fromVertex);
			thisLocalGraph.addVertex(toVertex);
			thisLocalGraph.addEdge(fromVertex, toVertex);
			thisLocalGraph.setEdgeWeight(thisLocalGraph.getEdge(fromVertex, toVertex), thisDis);
		}
		return thisLocalGraph;
	}
	
	/**
     * getBfsSingleSourceShortestPathLen.针对无权图
     * @param graph 工作图
     * @param from 源点
     * @return Map<String, Integer> singleSourceShortestPathLen，即sssp单源最短路径长度
     */
    public static Map<String, Integer> getBfsSingleSourceShortestPathLenForUnweightedGraph(Map<String, Set<String>> graph, String from) {
    	Map<String, Integer> singleSourceShortestPathLen = new HashMap<>();
    	Set<String> visited = new HashSet<>();//初始为空，表示没有元素被访问过
    	
    	Queue<String> queue = new LinkedList<>();
		queue.add(from);
		visited.add(from);
		singleSourceShortestPathLen.put(from, 0);
		while(!queue.isEmpty()) {
			String top = queue.poll();
			int nextDis = singleSourceShortestPathLen.get(top) + 1;
			Set<String> topAdjSet = graph.get(top);
			for(String i: topAdjSet) {
				if(!visited.contains(i)) {
					queue.add(i);
					visited.add(i);
					singleSourceShortestPathLen.put(i, nextDis);
				}
			}
		}
    	return singleSourceShortestPathLen;
    }
    
    /**
     * getBfsSingleSourceShortestPathVertexSet.针对无权图
     * @param graph 工作图
     * @param from 源点
     * @return Map<String, Set<String>> singleSourceShortestPathVertexSet，即sssp单源最短路径节点集合
     */
    public static Map<String, Set<String>> getBfsSingleSourceShortestPathVertexSetForUnweightedGraph(Map<String, Set<String>> graph, String from){
    	Map<String, Set<String>> singleSourceShortestPathVertexSet = new HashMap<>();
    	Set<String> visited = new HashSet<>();//初始为空，表示没有元素被访问过
    	
    	Queue<String> queue = new LinkedList<>();
		queue.add(from);
		visited.add(from);
		singleSourceShortestPathVertexSet.put(from, new HashSet<String>() {
			private static final long serialVersionUID = 8711463521245510429L;
			{
				add(from);
			}
		});
		while(!queue.isEmpty()) {
			String top = queue.poll();
			Set<String> thisPathVertexSet = singleSourceShortestPathVertexSet.get(top);
			Set<String> topAdjSet = graph.get(top);
			for(String i: topAdjSet) {
				if(!visited.contains(i)) {
					queue.add(i);
					visited.add(i);
					Set<String> tempSet = new HashSet<>(thisPathVertexSet);
					tempSet.add(i);
					singleSourceShortestPathVertexSet.put(i, tempSet);
				}
			}
		}
		
    	return singleSourceShortestPathVertexSet;
    }
    
    /**
     * getBfsSingleSourceShortestPathLenAndVertexSet.针对无权图
     * @param graph 工作图
     * @param from 源点
     * @return LandmarkEmbeddingPathLenAndVecList thisLandmarkEmbeddingPathLenAndVecList，即sssp单源最短路径长度和节点集合
     */
    public static Class2LandmarkEmbeddingPathLenAndVertexSet getBfsSingleSourceShortestPathLenAndVertexSetForUnweightedGraph(Map<String, Set<String>> graph, String from) {
    	Class2LandmarkEmbeddingPathLenAndVertexSet thisLandmarkEmbeddingPathLenAndVecList = new Class2LandmarkEmbeddingPathLenAndVertexSet();
    	
    	Set<String> visited = new HashSet<>();//初始为空，表示没有元素被访问过
    	Queue<String> queue = new LinkedList<>();
		queue.add(from);
		visited.add(from);
		thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.put(from, 0);
		thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.put(from, new HashSet<String>() {
			private static final long serialVersionUID = -7194555953872187732L;
			{
				add(from);
			}
		});
		while(!queue.isEmpty()) {
			String top = queue.poll();
			int nextDis = thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.get(top) + 1;
			Set<String> thisPathVertexSet = thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.get(top);
			Set<String> topAdjSet = graph.get(top);
			if(topAdjSet == null) {
				continue;
			}
			for(String i: topAdjSet) {
				if(!visited.contains(i)) {
					queue.add(i);
					visited.add(i);
					Set<String> tempSet = new HashSet<>(thisPathVertexSet);
					tempSet.add(i);
					thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.put(i, nextDis);
					thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.put(i, tempSet);
				}
			}
		}
		
    	return thisLandmarkEmbeddingPathLenAndVecList;
    }
    
	/**
	 * Map按照值进行排序，java-1.7
	 * @param 待排序map
	 * @return 排序后的Map结果
	 */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                //return (o1.getValue()).compareTo(o2.getValue()); //reverse : 得到中心性最差的100个landmark
                return (o2.getValue()).compareTo(o1.getValue()); // 得到中心性最好的100个landmark
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
            if(result.size() == 100) { //特殊处理： 到达100个元素就够了,因为最多总共只需要100个landmark
            	break;
            }
        }
        return result;
    }
    
    /**
     * getGraphPartSetList 
     * @return Set<String>[] graphPartSetList, graphPartSetList[i]表示第i组包含的元素
     */
    @SuppressWarnings({ "unchecked", "resource" })
	public static Set<String>[] getGraphPartSetList(){
    	// metis path： 	
    	// adj_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat
    	// adj_part_100_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat.part.100
    	
    	long thisExternalStorageStartTime = System.nanoTime(); 
    	Set<String>[] graphPartSetList = new Set[landmarkNum];
    	for(int i = 0; i < landmarkNum; ++i)
			graphPartSetList[i] = new HashSet<String>();
    	
    	try {
	    	BufferedReader bReader = new BufferedReader(new FileReader(adjHundredPartGraph));
	    	String tempStr = null;
			int thisVertexId = 0;
			while((tempStr = bReader.readLine()) != null) {
				int thisSetIndex = Integer.valueOf(tempStr);
				graphPartSetList[thisSetIndex].add(String.valueOf(thisVertexId++));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	long thisExternalStorageEndTime = System.nanoTime();  
		externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
		return graphPartSetList;
    }
    
    /**
	 * getPriorityMap
	 * @return
	 */
	public static Map<String, Double> getPriorityMap(Set<String>[] graphPartSetList){
		Map<String, Double> avgLocalDisMap = new HashMap<>(); //存储每个节点在局部的距离每个子图的其余节点的平均距离
		Vector<Integer[]> graphFilePathBReaderVector = isWightedGraph ? getGraphFilePathBReaderForWeightedGraph() : getGraphFilePathBReaderForUnweightedGraph();
		for(Set<String> i: graphPartSetList) {
			System.out.println("avgLocalDisMap.size() : " + avgLocalDisMap.size());
			SimpleWeightedGraph<String, DefaultWeightedEdge> localWeightedGraph = isWightedGraph ? creatBiggerLocalGraphForWeightedGraph(graphFilePathBReaderVector, i) : null;
			DijkstraShortestPath<String, DefaultWeightedEdge> localDijkstraShortestPath = isWightedGraph ? new DijkstraShortestPath<String, DefaultWeightedEdge>(localWeightedGraph) : null;
			Map<String, Set<String>> localUnweightedGraph = isWightedGraph ? null : creatBiggerLocalGraphForUnweightedGraph(graphFilePathBReaderVector, i);
			
			for(String j: i) {
				if (isWightedGraph) {
					if(!localWeightedGraph.containsVertex(j)) {
						avgLocalDisMap.put(j, (double) upperBoundDis);
						continue;
					}
				}else {
					if(!localUnweightedGraph.containsKey(j)) {
						avgLocalDisMap.put(j, (double) upperBoundDis);
						continue;
					}
				}
				
				SingleSourcePaths<String, DefaultWeightedEdge> singleSourcePathsForJ = isWightedGraph ? localDijkstraShortestPath.getPaths(j) : null;
				Map<String, Integer> singleSourceShortestPathLen = isWightedGraph ? null : getBfsSingleSourceShortestPathLenForUnweightedGraph(localUnweightedGraph, j);
				
				double thisDisSum = 0.0;
				for(String k: i) { //注意：这里 k 有可能通过 子图i 无法连接到j;即使 j 和 k都在局部子图中，也会存在j k不连通的问题！ 因为子图内部也划分成独立子图
					if((isWightedGraph && !localWeightedGraph.containsVertex(k)) || (!isWightedGraph && !localUnweightedGraph.containsKey(k)))
						continue;
					if(j.equals(k))
						continue;
					
					//RandomPair thisPair = new RandomPair(j, k, localGraph); 
					//注意这里RandomPair不能基于全图，需要基于局部子图//每次求k所有pair效率太低，应当改为对k集合求一次dijk的sssp//利用localSssp效率提升1000倍
					int minimumDis = Integer.MAX_VALUE;
					if (isWightedGraph || (!isWightedGraph && singleSourceShortestPathLen.containsKey(k))) 
						minimumDis = isWightedGraph ? (int) singleSourcePathsForJ.getWeight(k) : singleSourceShortestPathLen.get(k);
					if(minimumDis > upperBoundDis)
						minimumDis = upperBoundDis; 
					thisDisSum += minimumDis;
				}
				double thisAvgDis = thisDisSum / i.size();
				avgLocalDisMap.put(j, thisAvgDis);
				//System.out.println(j + " " + Double.valueOf(myUnweightedGraph.get(j).size()) / Double.valueOf(avgLocalDisMap.get(j)));
			}
		}

		Map<String, Double> priorityMap = new HashMap<>(); // 存储 每个节点 选作landmark优先级
		if (isWightedGraph) {
			for(String i: myWeightedGraph.vertexSet())
				priorityMap.put(i, Double.valueOf(myWeightedGraph.degreeOf(i) / avgLocalDisMap.get(i))); //全局度数越大越好， 局部平均距离越小越好
		}else {
			for(String i: myUnweightedGraph.keySet()) {
				priorityMap.put(i, Double.valueOf(myUnweightedGraph.get(i).size()) / Double.valueOf(avgLocalDisMap.get(i))); //全局度数越大越好， 局部平均距离越小越好
			}
		}
		
		return priorityMap;
	}
	
	/**
	 * * 根据中心性 centrality，选择优先级最高的100个节点作为landmark
	 * @param graphPartSetList
	 * @param priorityMap
	 * @return
	 */
	public static Set<String> getLocalCentralLandmarkSet(Set<String>[] graphPartSetList, Map<String, Double> priorityMap){
		Set<String> centralLandmarkSet = new HashSet<>();
		for(Set<String> thisGraphPartSet: graphPartSetList) {
			double maxPriority = -1.0;
			String maxPriorityVecId = null;
			for(String thisVecId: thisGraphPartSet) {
				if(maxPriorityVecId == null) {
					maxPriorityVecId = thisVecId;
				}
				if(!priorityMap.containsKey(thisVecId))//20180206: 确保存在;否则 null 错误
					continue;
				double thisPriority = priorityMap.get(thisVecId);
				if(thisPriority > maxPriority) {
					System.out.println(thisPriority + " 20180207 刷新");
					maxPriority = thisPriority;
					maxPriorityVecId = thisVecId;
				}
			}
			centralLandmarkSet.add(maxPriorityVecId);
		}
		return centralLandmarkSet;
	}
	
	/**
	 * getLocalMaxDegreeLandmarkSet 只用度（degree）来衡量中心性，选取局部度数最高作为landmark，每个局部选取一个
	 * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
	 * @return Set<String> localMaxDegreeLandmarkSet
	 */
	public static Set<String> getLocalMaxDegreeLandmarkSet(Set<String>[] graphPartSetList){
		Set<String> localMaxDegreeLandmarkSet = new HashSet<>();
		for(Set<String> thisGraphPartSet: graphPartSetList) {
			int maxDegree = -1;
			String maxDegreeVecId = null;
			for(String thisVecId: thisGraphPartSet) {
				int thisDegree = 0;
				if ((isWightedGraph && !myWeightedGraph.containsVertex(thisVecId)) || (!isWightedGraph && !myUnweightedGraph.containsKey(thisVecId)))  
					continue;
				thisDegree = isWightedGraph ? myWeightedGraph.degreeOf(thisVecId) : myUnweightedGraph.get(thisVecId).size();
				
				if(thisDegree > maxDegree) {
					maxDegree = thisDegree;
					maxDegreeVecId = thisVecId;
				}
			}
			if(maxDegree == -1) { //当前分区中没有合适（不存在或者独立）元素，则全图任选一个（合适的）作为landmark
				do {
					maxDegreeVecId = String.valueOf(random.nextInt(vertexNum));
				} while((isWightedGraph && !(myWeightedGraph.containsVertex(maxDegreeVecId) && myWeightedGraph.degreeOf(maxDegreeVecId) > 0 && !localMaxDegreeLandmarkSet.contains(maxDegreeVecId))) ||
						(!isWightedGraph && !(myUnweightedGraph.containsKey(maxDegreeVecId) && myUnweightedGraph.get(maxDegreeVecId).size() > 0 && !localMaxDegreeLandmarkSet.contains(maxDegreeVecId))));
			}
			localMaxDegreeLandmarkSet.add(maxDegreeVecId);
		}
		return localMaxDegreeLandmarkSet;
	}
	
	/**
	 * 一个Java递归删除目录的方法
	 * @param f
	 */
	public static void delDir(File f) {
	    if(f.isDirectory()) {
	        File[] subFiles = f.listFiles();
	        for (File subFile : subFiles) 
	            delDir(subFile);
	    }
	    f.delete();
	}
	
	/**
	 * clearDir()
	 */
	public static void clearDir() {
		long thisExternalStorageStartTime = System.nanoTime();  
		if(isCentrality) {
			File CentralityPrefix_pathLenEmbeddingDir = new File(bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding);
			if(CentralityPrefix_pathLenEmbeddingDir.exists())
				delDir(CentralityPrefix_pathLenEmbeddingDir);
			CentralityPrefix_pathLenEmbeddingDir.mkdirs();
			
			File CentralityPrefix_pathVecListEmbeddingDir = new File(bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding);
			if(CentralityPrefix_pathVecListEmbeddingDir.exists())
				delDir(CentralityPrefix_pathVecListEmbeddingDir);
			CentralityPrefix_pathVecListEmbeddingDir.mkdirs();
		}else {
			File RandomPrefix_pathLenEmbeddingDir = new File(bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding);
			if(RandomPrefix_pathLenEmbeddingDir.exists())
				delDir(RandomPrefix_pathLenEmbeddingDir);
			RandomPrefix_pathLenEmbeddingDir.mkdirs();
			
			File RandomPrefix_pathVecListEmbeddingDir = new File(bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding);
			if(RandomPrefix_pathVecListEmbeddingDir.exists())
				delDir(RandomPrefix_pathVecListEmbeddingDir);
			RandomPrefix_pathVecListEmbeddingDir.mkdirs();
		}
		long thisExternalStorageEndTime = System.nanoTime();  
		externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
	}
	
	/**
	 * getLandmarkEmbedding
	 * @param thisLandmark 本次有待生成landmarkEmbedding的节点编号
	 * 这里没有生成一个数据结构，而是直接将embedding（Map<String, int>类型;Map<String, Set<String>>）数据结构保存到 文件系统中
	 */
	public static void getLandmarkEmbedding(String thisLandmark) {
		System.out.println("thisLandmark : " + thisLandmark);
		
		SingleSourcePaths<String, DefaultWeightedEdge> singleSourcePaths = isWightedGraph ? dijkstraShortestPath.getPaths(thisLandmark) : null; //weighted
		Class2LandmarkEmbeddingPathLenAndVertexSet thisLandmarkEmbeddingPathLenAndVecList = isWightedGraph ? null : getBfsSingleSourceShortestPathLenAndVertexSetForUnweightedGraph(myUnweightedGraph, thisLandmark);; //unweighted
		
		long thisExternalStorageStartTime = System.nanoTime();
		//如果一次write所有点的landmarkEmbedding信息会导致内存溢出；需要采用批处理的方式，每次写入1000个点的embedding信息
		int groupNum = (vertexNum + (partGroupSize - 1)) / partGroupSize;
		for(int thisGroupNum = 0; thisGroupNum < groupNum; ++thisGroupNum) {
			int startVecNum = thisGroupNum * partGroupSize;
			int endVecNum = (groupNum - thisGroupNum == 1)? vertexNum: startVecNum + partGroupSize; //是否为最后一组 //20171110：因为末尾是否包含，导致所有 ...999的embedding都丢失了
			
			Map<String, Integer> thisLandmarkEmbeddingPathLenMap = new HashMap<>();
			Map<String, Set<String>> thisLandmarkEmbeddingPathVertexListMap = new HashMap<>();
			
			for(int i = startVecNum; i < endVecNum; ++i) { 
				//这里从startVecNum便利到endVecNum（所有节点）作为汇点， 计算从源点thisLandmark到所有汇点的最短距离
				if (isWightedGraph) {
					//http://jgrapht.org/javadoc/org/jgrapht/alg/interfaces/ShortestPathAlgorithm.SingleSourcePaths.html
					int thisMinimumDis = (int) singleSourcePaths.getWeight(String.valueOf(i));
					if(thisMinimumDis > disconnectJudge) //不连通, landmarkEmbedding 不要计入不连通节点的信息
						continue;
					thisLandmarkEmbeddingPathLenMap.put(String.valueOf(i), thisMinimumDis); 
					
					GraphPath<String, DefaultWeightedEdge> thisGraphPath = singleSourcePaths.getPath(String.valueOf(i));
					Set<String> thisVertexList = new HashSet<>(thisGraphPath.getVertexList());//thisVertexList format : [63873, 14453, 37263, 3123, 19830] 
					thisLandmarkEmbeddingPathVertexListMap.put(String.valueOf(i), thisVertexList); 
				}else {
					if(!thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.containsKey(String.valueOf(i)))
						continue;
					thisLandmarkEmbeddingPathLenMap.put(String.valueOf(i), thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.get(String.valueOf(i)));
					thisLandmarkEmbeddingPathVertexListMap.put(String.valueOf(i), thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.get(String.valueOf(i)));
				}
			}
			
			try {
				BufferedWriter bWriterPathLen = null;
				BufferedWriter bWriterPathVertexSet = null;
				if(! isCentrality) {
					bWriterPathLen = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathLen.json"));
					bWriterPathVertexSet = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathVertexList.json"));
				}else if(isCentrality){
					bWriterPathLen = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathLen.json"));
					bWriterPathVertexSet = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathVertexList.json"));
				}
				
				String thisLandmarkEmbeddingPathLenString = thisLandmarkEmbeddingPathLenMap.toString();
				bWriterPathLen.write(thisLandmarkEmbeddingPathLenString);
				bWriterPathLen.close();
				
				String thisLandmarkEmbeddingPathVertexListMapString = thisLandmarkEmbeddingPathVertexListMap.toString();
				bWriterPathVertexSet.write(thisLandmarkEmbeddingPathVertexListMapString);
				bWriterPathVertexSet.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		long thisExternalStorageEndTime = System.nanoTime();  
		externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
	}
	
	/**
	 * doWriteBfsSingleSourceShortestPathLenAndVertexSet 相当于getLandmarkEmbedding的长最短路径版本，本质是对getLandmarkEmbedding()和getBfsSingleSourceShortestPathLenAndVertexSet()的融合；
	 * 因为长路径的数据结构过大，必须采取边生成边write的发布而不是全局Map一次写入.针对无权图,长路径
	 * @graph 工作图
	 * @param thisLandmark 本次有待生成landmarkEmbedding的节点编号
	 * 这里没有生成一个数据结构，而是直接将embedding（Map<String, int>类型;Map<String, Set<String>>）数据结构保存到 文件系统中
	 */
	public static void doWriteBfsSingleSourceShortestPathLenAndVertexSetForUnweightedGraph(Map<String, Set<String>> graph, String thisLandmark) {
		System.out.println("thisLandmark : " + thisLandmark);
		
		Map<String, Integer> thisWriteLandmarkEmbeddingMap = new HashMap<>();
		Map<String, Set<String>> thisWriteLandmarkEmbeddingPathVertexListMap = new HashMap<>();
		int thisGroupNum = 0;
		
		Class2LandmarkEmbeddingPathLenAndVertexSet thisLandmarkEmbeddingPathLenAndVecList = new Class2LandmarkEmbeddingPathLenAndVertexSet();
    	
    	Set<String> visited = new HashSet<>();//初始为空，表示没有元素被访问过
    	Queue<String> queue = new LinkedList<>();
		queue.add(thisLandmark);
		visited.add(thisLandmark);
		thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.put(thisLandmark, 0);
		thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.put(thisLandmark, new HashSet<String>() {
			private static final long serialVersionUID = -7194555953872187732L;
			{
				add(thisLandmark);
			}
		});
		
		while(!queue.isEmpty()) {
			String top = queue.poll();
			int nextDis = thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.get(top) + 1;
			Set<String> thisPathVertexSet = thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.get(top);
			
			thisWriteLandmarkEmbeddingMap.put(top, thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.get(top));
			thisWriteLandmarkEmbeddingPathVertexListMap.put(top, thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.get(top));
			thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.remove(top);
			thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.remove(top);
			
			Set<String> topAdjSet = graph.get(top);
			for(String i: topAdjSet) {
				if(!visited.contains(i)) {
					queue.add(i);
					visited.add(i);
					Set<String> tempSet = new HashSet<>(thisPathVertexSet);
					tempSet.add(i);
					thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.put(i, nextDis);
					thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.put(i, tempSet);
				}
			}
			
			if(thisWriteLandmarkEmbeddingMap.size() == partGroupSize || queue.isEmpty()) {
				long thisExternalStorageStartTime = System.nanoTime();
				try {
					BufferedWriter bWriterPathLen = null;
					BufferedWriter bWriterPathVertexSet = null;
					if(! isCentrality) {
						bWriterPathLen = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathLen.json"));
						bWriterPathVertexSet = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedRandomPrefix_pathVecListEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathVertexList.json"));
					}else if(isCentrality){
						bWriterPathLen = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathLen.json"));
						bWriterPathVertexSet = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedCentralityPrefix_pathVecListEmbedding + thisLandmark + "_" + thisGroupNum + "_embedding_pathVertexList.json"));
					}

					String thisLandmarkEmbeddingPathLenString = thisWriteLandmarkEmbeddingMap.toString();
					bWriterPathLen.write(thisLandmarkEmbeddingPathLenString);
					bWriterPathLen.close();
					
					String thisLandmarkEmbeddingPathVertexSetString = thisWriteLandmarkEmbeddingPathVertexListMap.toString();
					bWriterPathVertexSet.write(thisLandmarkEmbeddingPathVertexSetString);
					bWriterPathVertexSet.close();
					
					thisWriteLandmarkEmbeddingMap.clear();
					thisWriteLandmarkEmbeddingPathVertexListMap.clear();
					
					++thisGroupNum;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				long thisExternalStorageEndTime = System.nanoTime();
				externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
			}
		}
	}
	
	/**
	 * main()
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String args[]) {
		
		long mainStartTime = System.nanoTime();
		
		final boolean isDegreeCentrality = false; //true:用度作为度量; false:用priority作为设置.	需要设置!!!!! !!!!! 
		Set<String>[] graphPartSetList = isCentrality ? getGraphPartSetList() : null;
		Map<String, Double> priorityMap = (isCentrality && !isDegreeCentrality) ? getPriorityMap(graphPartSetList) : null;
		Set<String> landmarkSet = isCentrality ? (isDegreeCentrality ? getLocalMaxDegreeLandmarkSet(graphPartSetList) : getLocalCentralLandmarkSet(graphPartSetList, priorityMap)) : getRandomLandmarkSet(); //中心性生成
		
		clearDir();
		
		for(String i: landmarkSet) {
			if(!(dataSet.equals("unweightedNYRN"))) {
				getLandmarkEmbedding(i);
			}else if (dataSet.equals("unweightedNYRN") ) {
				doWriteBfsSingleSourceShortestPathLenAndVertexSetForUnweightedGraph(myUnweightedGraph, i);
			}
		}
		long mainEndTime = System.nanoTime();
		long mainTime = (mainEndTime - mainStartTime);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime(生成集合,bfs, write) : " + (mainTime / 1E6) + " ms!");
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime(生成集合，bfs) : " + (mainTime - externalStorageTime) / 1E6 + " ms!\n");
		//Slashdot Centrality runningTime(生成集合，bfs) : 24893 ms!
	}
}

/*
20180206------------
facebook Centrality runningTime(生成集合,bfs, write) : 2209.971151 ms!
facebook Centrality runningTime(生成集合，bfs) : 1667.347064 ms!

facebook Random runningTime(生成集合,bfs, write) : 1282.532102 ms!
facebook Random runningTime(生成集合，bfs) : 867.171463 ms!
-----
Slashdot Centrality runningTime(生成集合,bfs, write) : 52545.810149 ms!
Slashdot Centrality runningTime(生成集合，bfs) : 41896.633355 ms!

Slashdot Random runningTime(生成集合,bfs, write) : 26033.455516 ms!
Slashdot Random runningTime(生成集合，bfs) : 15203.864049 ms!
-----
youtube Random runningTime(生成集合,bfs, write) : 1520529.532466 ms!
youtube Random runningTime(生成集合，bfs) : 475995.097425 ms!

youtube Centrality runningTime(生成集合,bfs, write) : 5143818.354302 ms!
youtube Centrality runningTime(生成集合，bfs) : 4212080.633139 ms!
20180206------------



----- -----公路nouse
unweightedNYRN Centrality runningTime(生成集合,bfs, write) : 707968.596997 ms!
unweightedNYRN Centrality runningTime(生成集合，bfs) : 144941.910408 ms!
----- -----
NYRN Centrality runningTime(生成集合,bfs, write) : 1503100.709014 ms!

NYRN Centrality runningTime(生成集合，bfs) : 25595.406236 ms!
*/

