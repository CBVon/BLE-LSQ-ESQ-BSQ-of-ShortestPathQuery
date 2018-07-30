package undirected_unweighted_version;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

public class LandmarkEmbeddingPathLenAndVecList extends RandomPairDis{
	
	public Map<String, Integer> singleSourceShortestPathLen = new HashMap<>();
	public Map<String, Set<String>> singleSourceShortestPathVertexSet = new HashMap<>();
	
	public final static boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static long runningTime = 0;
	public static long externalStorageTime = 0;
	
	public static Random random = new Random();
	
	/**
	 * 随机生成100个整数（0～vertexNum - 1），作为landmark节点Id
	 * @return Set<String> randomLandmarkSet
	 */
	public static Set<String> getRandomLandmarkSet(){
		Set<String> randomLandmarkSet = new HashSet<>();
		while(randomLandmarkSet.size() < landmarkNum) {
			String thisLandmark = String.valueOf(random.nextInt(vertexNum));
			if(!randomLandmarkSet.contains(thisLandmark) && myGraph.containsKey(thisLandmark) && myGraph.get(thisLandmark).size() > 0) 
				randomLandmarkSet.add(thisLandmark);
		}
		return randomLandmarkSet;
	}
	
	/**
	 * 遍历原始graphFilePath，如果某行（pair（a，b））两点都在集合Set中，就添加到myLocalGraph中。注意这里针对bigger子图，因为子图很小的话没必要去遍历全图所有边
	 * @param localGraphVertexSet 生成当前图的节点集合
	 * @return 无向图Map<String, Set<String>> myLocalGraph
	 */
	@SuppressWarnings("resource")
	public static Map<String, Set<String>> creatBiggerLocalGraph(Set<String> localGraphVertexSet){
		Map<String, Set<String>> myLocalGraph = new HashMap<>();
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(graphFilePath));
			String tempString = null;  
	        int line = 0; 
	        while ((tempString = bReader.readLine()) != null) {
	        	if(++line <= 4)//前4行无用信息
	        		continue;
	        	String fromVertex = tempString.split("\t")[0];
	        	String toVertex = tempString.split("\t")[1];
	        	if(fromVertex.equals(toVertex))
	        		continue;
	        	if(!(localGraphVertexSet.contains(fromVertex) && localGraphVertexSet.contains(toVertex)))//如果当前边的两个端点不同时都在子图集合中，没必要进行下去
	        		continue;
	        	
	        	if(myLocalGraph.containsKey(fromVertex)) {
	        		myLocalGraph.get(fromVertex).add(toVertex);
	        	}else {
	        		myLocalGraph.put(fromVertex, new HashSet<String>() {
						private static final long serialVersionUID = 2113398549231985272L;
						{
							add(toVertex);
						}
					});
				}
	        	
	        	if(myLocalGraph.containsKey(toVertex)) {
	        		myLocalGraph.get(toVertex).add(fromVertex);
	        	}else {
	        		myLocalGraph.put(toVertex, new HashSet<String>() {
						private static final long serialVersionUID = 5263894297326742091L;
						{
							add(fromVertex);
						}
					});
				}
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return myLocalGraph;
	}
    
    /**
     * getBfsSingleSourceShortestPathLen
     * @param graph 工作图
     * @param from 源点
     * @return Map<String, Integer> singleSourceShortestPathLen，即sssp单源最短路径长度
     */
    public static Map<String, Integer> getBfsSingleSourceShortestPathLen(Map<String, Set<String>> graph, String from) {
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
     * getBfsSingleSourceShortestPathVertexSet
     * @param graph 工作图
     * @param from 源点
     * @return Map<String, Set<String>> singleSourceShortestPathVertexSet，即sssp单源最短路径节点集合
     */
    public static Map<String, Set<String>> getBfsSingleSourceShortestPathVertexSet(Map<String, Set<String>> graph, String from){
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
     * getBfsSingleSourceShortestPathLenAndVertexSet
     * @param graph 工作图
     * @param from 源点
     * @return LandmarkEmbeddingPathLenAndVecList thisLandmarkEmbeddingPathLenAndVecList，即sssp单源最短路径长度和节点集合
     */
    public static LandmarkEmbeddingPathLenAndVecList getBfsSingleSourceShortestPathLenAndVertexSet(Map<String, Set<String>> graph, String from) {
    	LandmarkEmbeddingPathLenAndVecList thisLandmarkEmbeddingPathLenAndVecList = new LandmarkEmbeddingPathLenAndVecList();
    	
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
     * getGraphPartSetList 
     * @return Set<String>[] graphPartSetList, graphPartSetList[i]表示第i组包含的元素
     */
    @SuppressWarnings({ "unchecked", "resource" })
	public static Set<String>[] getGraphPartSetList(){
    	// metis path： /home/cbvon/metis-5.1.0/build/Linux-x86_64/programs/gpmetis
    	// adj_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat
    	// adj_part_100_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat.part.100
    	
    	long thisExternalStorageStartTime = System.nanoTime();
    	Set<String>[] graphPartSetList = new Set[landmarkNum];
    	for(int i = 0; i < landmarkNum; ++i)
			graphPartSetList[i] = new HashSet<String>();
    	
    	try {
	    	BufferedReader bReader = new BufferedReader(new FileReader(adjPart100Graph));
	    	String tempStr = null;
			int thisVertexId = 0;
			while((tempStr = bReader.readLine()) != null) {
				int thisSetIndex = Integer.valueOf(tempStr);
				graphPartSetList[thisSetIndex].add(String.valueOf(thisVertexId++));
			}
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	long thisExternalStorageEndTime = System.nanoTime();
		externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
		return graphPartSetList;
    }
    
    /**
     * getAvgLocalDisMap
     * @param graphPartSetList
     * @return
     */
    public static Map<String, Double> getAvgLocalDisMap(Set<String>[] graphPartSetList){
    	Map<String, Double> avgLocalDisMap = new HashMap<>(); //存储每个节点在局部的距离每个子图的其余节点的平均距离
		for(Set<String> i: graphPartSetList) {
			System.out.println("avgLocalDisMap.size() : " + avgLocalDisMap.size());
			Map<String, Set<String>> localGraph = creatBiggerLocalGraph(i);
			
			for(String j: i) {
				if(!localGraph.containsKey(j)) {
					avgLocalDisMap.put(j, (double) disconnectJudge);
					continue;
				}
				
				Map<String, Integer> singleSourceShortestPathLen = getBfsSingleSourceShortestPathLen(localGraph, j);
				double thisDisSum = 0.0;
				for(String k: i) { //注意：这里 k 有可能通过 子图i 无法连接到j;即使 j 和 k都在局部子图中，也会存在j k不连通的问题！ 因为子图内部也划分成独立子图
					if(!localGraph.containsKey(k))
						continue;
					if(j.equals(k))
						continue;
					//RandomPair thisPair = new RandomPair(j, k, localGraph); 
					//注意这里RandomPair不能基于全图，需要基于局部子图//每次求k所有pair效率太低，应当改为对k集合求一次bfs的sssp//利用localSssp效率提升1000倍
					int minimumDis = upperBoundDis;
					if(singleSourceShortestPathLen.containsKey(k) && singleSourceShortestPathLen.get(k) < minimumDis)
						minimumDis = singleSourceShortestPathLen.get(k);
					thisDisSum += minimumDis;
				}
				double thisAvgDis = thisDisSum / i.size();
				avgLocalDisMap.put(j, thisAvgDis);
			}
		}
		return avgLocalDisMap;
    }
    
    /**
     * getPriorityMap
     * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
     * @param avgLocalDisMap 
     * @return Map<String, Double> priorityMap, 表示每个元素的‘全局度数/平均局部距离’作为优先级
     */
    public static Map<String, Double> getPriorityMap(Set<String>[] graphPartSetList, Map<String, Double> avgLocalDisMap){
		Map<String, Double> priorityMap = new HashMap<>(); // 存储 每个节点 选作landmark优先级
		for(String i: myGraph.keySet()) 
			priorityMap.put(i, Double.valueOf(myGraph.get(i).size()) / Double.valueOf(avgLocalDisMap.get(i))); //全局度数越大越好， 局部平均距离越小越好
		return priorityMap;
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
     * getGlobalCentralLandmarkSet 根据中心性（centrality），选择优先级最高的100个节点作为landmark,这里的前100指的是对全集合的最高优先100
     * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
     * @param avgLocalDisMap
     * @return Set<String> centralLandmarkSet
     */
	public static Set<String> getGlobalCentralLandmarkSet(){
		Set<String>[] graphPartSetList = getGraphPartSetList();
		Map<String, Double> avgLocalDisMap = getAvgLocalDisMap(graphPartSetList);
		Map<String, Double> priorityMap = getPriorityMap(graphPartSetList, avgLocalDisMap);
		Map<String, Double> priorityMapSorted = sortByValue(priorityMap);
		Set<String> centralLandmarkSet = priorityMapSorted.keySet();
		return centralLandmarkSet;
	}
	
	/**
	 * getLocalCentralLandmarkSet 根据中心性（centrality），在100个分区中每个分区选择优先级最高的1个节点作为landmark
	 * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
	 * @param avgLocalDisMap
	 * @return Set<String> localCentralLandmarkSet 局部中心landmark
	 */
	public static Set<String> getLocalCentralLandmarkSet(){
		Set<String>[] graphPartSetList = getGraphPartSetList();
		Map<String, Double> avgLocalDisMap = getAvgLocalDisMap(graphPartSetList);
		Map<String, Double> priorityMap = getPriorityMap(graphPartSetList, avgLocalDisMap);
		Set<String> centralLandmarkSet = new HashSet<>();
		for(Set<String> thisGraphPartSet: graphPartSetList) {
			double maxPriority = -1.0;
			String maxPriorityVecId = null;
			for(String thisVecId: thisGraphPartSet) {
				double thisPriority = priorityMap.get(thisVecId);
				if(thisPriority > maxPriority) {
					maxPriority = thisPriority;
					maxPriorityVecId = thisVecId;
				}
			}
			centralLandmarkSet.add(maxPriorityVecId);
		}
		return centralLandmarkSet;
	}
	
	/**
	 * getLocalRandomLandmarkSet 在100个分区中每个分区随机选择1个节点作为landmark
	 * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
	 * @return Set<String> localRandomLandmarkSet
	 */
	public static Set<String> getLocalRandomLandmarkSet(){
		Set<String>[] graphPartSetList = getGraphPartSetList();
		Set<String> localRandomLandmarkSet = new HashSet<>();
		for(Set<String> thisGraphPartSet: graphPartSetList) {
			List<String> thisGraphPartList = new ArrayList<>(thisGraphPartSet);
			int randomIndex = random.nextInt(thisGraphPartList.size());
			String thisRandomVec = thisGraphPartList.get(randomIndex);
			localRandomLandmarkSet.add(thisRandomVec);
		}
		return localRandomLandmarkSet;
	}
	
	/**
	 * getLocalMaxAvgLocalDisLandmarkSet 获取局部最大的局部平均距离的landmark集合
	 * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
	 * @param avgLocalDisMap 
	 * @return Set<String> localMaxAvgLocalDisLandmarkSet
	 */
	public static Set<String> getLocalMaxAvgLocalDisLandmarkSet(){
		Set<String>[] graphPartSetList = getGraphPartSetList();
		Map<String, Double> avgLocalDisMap = getAvgLocalDisMap(graphPartSetList);
		Set<String> localMaxAvgLocalDisLandmarkSet = new HashSet<>();
		for(Set<String> thisGraphPartSet: graphPartSetList) {
			double maxAvgLocalDis = -1.0;
			String maxAvgLocalDisVecId = null;
			for(String thisVecId: thisGraphPartSet) {
				double thisAvgLocalDis = avgLocalDisMap.get(thisVecId);
				if(thisAvgLocalDis > maxAvgLocalDis) {
					maxAvgLocalDis = thisAvgLocalDis;
					maxAvgLocalDisVecId = thisVecId;
				}
			}
			localMaxAvgLocalDisLandmarkSet.add(maxAvgLocalDisVecId);
		}
		return localMaxAvgLocalDisLandmarkSet;
	}
	
	/**
	 * getLocalMaxAvgLocalDisMulDegreeLandmarkSet 获取‘局部最大的局部平均距离乘以度数’的landmark集合
	 * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
	 * @param avgLocalDisMap 
	 * @return Set<String> localMaxAvgLocalDisLandmarkSet
	 */
	public static Set<String> getLocalMaxAvgLocalDisMulDegreeLandmarkSet(){
		Set<String>[] graphPartSetList = getGraphPartSetList();
		Map<String, Double> avgLocalDisMap = getAvgLocalDisMap(graphPartSetList);
		Set<String> localMaxAvgLocalDisLandmarkSet = new HashSet<>();
		for(Set<String> thisGraphPartSet: graphPartSetList) {
			double maxAvgLocalDis = -1.0;
			String maxAvgLocalDisVecId = null;
			for(String thisVecId: thisGraphPartSet) {
				double thisAvgLocalDis = avgLocalDisMap.get(thisVecId) * myGraph.get(thisVecId).size(); //和 getLocalMaxAvgLocalDisLandmarkSet 唯一不同
				if(thisAvgLocalDis > maxAvgLocalDis) {
					maxAvgLocalDis = thisAvgLocalDis;
					maxAvgLocalDisVecId = thisVecId;
				}
			}
			localMaxAvgLocalDisLandmarkSet.add(maxAvgLocalDisVecId);
		}
		return localMaxAvgLocalDisLandmarkSet;
	}
	
	/**
	 * getLocalMaxDegreeLandmarkSet 只用度（degree）来衡量中心性，选取局部度数最高作为landmark，每个局部选取一个
	 * @param graphPartSetList graphPartSetList[i]表示第i组包含的元素
	 * @return Set<String> localMaxDegreeLandmarkSet
	 */
	public static Set<String> getLocalMaxDegreeLandmarkSet(){
		Set<String>[] graphPartSetList = getGraphPartSetList();
		Set<String> localMaxDegreeLandmarkSet = new HashSet<>();
		for(Set<String> thisGraphPartSet: graphPartSetList) {
			int maxDegree = -1;
			String maxDegreeVecId = null;
			for(String thisVecId: thisGraphPartSet) {
				if(!myGraph.containsKey(thisVecId))
					continue;
				int thisDegree = myGraph.get(thisVecId).size(); //和 getLocalMaxAvgLocalDisLandmarkSet 唯一不同
				if(thisDegree > maxDegree) {
					maxDegree = thisDegree;
					maxDegreeVecId = thisVecId;
				}
			}
			if(maxDegree == -1) { //当前分区中没有合适（不存在或者独立）元素，则全图任选一个（合适的）作为landmark
				do {
					maxDegreeVecId = String.valueOf(random.nextInt(vertexNum));
				} while(!(myGraph.containsKey(maxDegreeVecId) && myGraph.get(maxDegreeVecId).size() > 0 && !localMaxDegreeLandmarkSet.contains(maxDegreeVecId)));
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
	}
	
	/**
	 * getLandmarkEmbedding
	 * @param thisLandmark 本次有待生成landmarkEmbedding的节点编号
	 * 这里没有生成一个数据结构，而是直接将embedding（Map<String, int>类型;Map<String, Set<String>>）数据结构保存到 文件系统中
	 */
	public static void getLandmarkEmbedding(String thisLandmark) {
		System.out.println("thisLandmark : " + thisLandmark);
		
		long thisRunningStartTime = System.nanoTime();
		LandmarkEmbeddingPathLenAndVecList thisLandmarkEmbeddingPathLenAndVecList = getBfsSingleSourceShortestPathLenAndVertexSet(myGraph, thisLandmark);
		long thisRunningEndTime = System.nanoTime();
		runningTime += (thisRunningEndTime - thisRunningStartTime); //bfs
		
		//如果一次write所有点的landmarkEmbedding信息会导致内存溢出；需要采用批处理的方式，每次写入1000个点的embedding信息
		int groupNum = (vertexNum + (partGroupSize - 1)) / partGroupSize; //每个vecter分割成的组数
		for(int thisGroupNum = 0; thisGroupNum < groupNum; ++thisGroupNum) {
			int startVecNum = thisGroupNum * partGroupSize;
			int endVecNum = (groupNum - thisGroupNum == 1)? vertexNum: startVecNum + 1000; //是否为最后一组 //20171110：因为末尾是否包含，导致所有 ...999的embedding都丢失了
			Map<String, Integer> thisLandmarkEmbeddingPathLenMap = new HashMap<>();
			Map<String, Set<String>> thisLandmarkEmbeddingPathVertexListMap = new HashMap<>();
			for(int i = startVecNum; i < endVecNum; ++i) {
				//这里从startVecNum便利到endVecNum（所有节点）作为汇点， 计算从源点thisLandmark到所有汇点的最短距离
				if(!thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.containsKey(String.valueOf(i)))
					continue;
				thisLandmarkEmbeddingPathLenMap.put(String.valueOf(i), thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.get(String.valueOf(i)));
				thisLandmarkEmbeddingPathVertexListMap.put(String.valueOf(i), thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.get(String.valueOf(i)));
			}
			
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
				
				String thisLandmarkEmbeddingPathLenString = thisLandmarkEmbeddingPathLenMap.toString();
				bWriterPathLen.write(thisLandmarkEmbeddingPathLenString);
				bWriterPathLen.close();
				
				String thisLandmarkEmbeddingPathVertexSetString = thisLandmarkEmbeddingPathVertexListMap.toString();
				bWriterPathVertexSet.write(thisLandmarkEmbeddingPathVertexSetString);
				bWriterPathVertexSet.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long thisExternalStorageEndTime = System.nanoTime();
			externalStorageTime += (thisExternalStorageEndTime - thisExternalStorageStartTime);
		}
	}
	
	/**
	 * doWriteBfsSingleSourceShortestPathLenAndVertexSet 相当于getLandmarkEmbedding的长最短路径版本，本质是对getLandmarkEmbedding()和getBfsSingleSourceShortestPathLenAndVertexSet()的融合；
	 * 因为长路径的数据结构过大，必须采取边生成边write的发布而不是全局Map一次写入
	 * @graph 工作图
	 * @param thisLandmark 本次有待生成landmarkEmbedding的节点编号
	 * 这里没有生成一个数据结构，而是直接将embedding（Map<String, int>类型;Map<String, Set<String>>）数据结构保存到 文件系统中
	 */
	public static void doWriteBfsSingleSourceShortestPathLenAndVertexSet(Map<String, Set<String>> graph, String thisLandmark) {
		System.out.println("thisLandmark : " + thisLandmark);
		
		Map<String, Integer> thisWriteLandmarkEmbeddingMap = new HashMap<>();
		Map<String, Set<String>> thisWriteLandmarkEmbeddingPathVertexListMap = new HashMap<>();
		int thisGroupNum = 0;
		
		LandmarkEmbeddingPathLenAndVecList thisLandmarkEmbeddingPathLenAndVecList = new LandmarkEmbeddingPathLenAndVecList();
    	
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
		
		long writeTime = 0;
		long thisRunningStartTime = System.nanoTime();
		while(!queue.isEmpty()) {
			String top = queue.poll();
			int nextDis = thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.get(top) + 1;
			Set<String> thisPathVertexSet = thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.get(top);
			
			thisWriteLandmarkEmbeddingMap.put(top, thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.get(top));
			thisWriteLandmarkEmbeddingPathVertexListMap.put(top, thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.get(top));
			thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathLen.remove(top);
			thisLandmarkEmbeddingPathLenAndVecList.singleSourceShortestPathVertexSet.remove(top);
			
			if(thisWriteLandmarkEmbeddingMap.size() == partGroupSize || queue.isEmpty()) {
				long thisCostStartTime = System.nanoTime();
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
				long thisCostEndTime = System.nanoTime();
				writeTime += (thisCostEndTime - thisCostStartTime);
			}
			
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
		}
		long thisRunningEndTime = System.nanoTime();
		runningTime += (thisRunningEndTime - thisRunningStartTime);
		runningTime -= writeTime;
	}
	
	/**
	 * main()
	 * @param args
	 */
	public static void main(String args[]) {
		long startTime = System.nanoTime();
		
		long thisRunningStartTime = System.nanoTime();
		/*
		 * 生成landmark集合方法，任选一种
		 */
		//Set<String> landmarkSet = isCentrality ? getGlobalCentralLandmarkSet() : getRandomLandmarkSet(); //全局中心性 生成； 不建议使用，在各分区上分布不均匀，效果差全局随机很多， 不考虑
		//Set<String> landmarkSet = isCentrality ? getLocalCentralLandmarkSet() : getRandomLandmarkSet(); //局部中心性（全局度数/局部平均距离） 生成， 目前最好
		//Set<String> landmarkSet = isCentrality ? getLocalRandomLandmarkSet(): getRandomLandmarkSet(); //局部随机性生成；效果没有明显提升，不稳定，不建议使用，不考虑
		//Set<String> landmarkSet = isCentrality ? getLocalMaxAvgLocalDisLandmarkSet(): getRandomLandmarkSet(); //局部最大平均局部距离 生成，为了获取更多路径信息； 效果真的差，绝对不使用，不考虑
		//Set<String> landmarkSet = isCentrality ? getLocalMaxAvgLocalDisMulDegreeLandmarkSet(): getRandomLandmarkSet(); //局部最大平均局部距离*度数 生成，为了获取更多路径信息； 效果不出色，不考虑
		Set<String> landmarkSet = isCentrality ? getLocalMaxDegreeLandmarkSet(): getRandomLandmarkSet(); //局部最大度数 生成； 相比局部中心 效果ok，度数是核心因素
		long thisRunningEndTime = System.nanoTime();
		runningTime += (thisRunningEndTime - thisRunningStartTime); //生成集合
		
		clearDir();
		
		for(String i: landmarkSet) {
			if(!(dataSet.equals("unweightedNYRN"))) {
				getLandmarkEmbedding(i);
			}else if (dataSet.equals("unweightedNYRN") ) {
				doWriteBfsSingleSourceShortestPathLenAndVertexSet(myGraph, i);
			}
		}
		long endTime = System.nanoTime();
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " : 生成所有landmark用时（生成集合, bfs, write） ： " + (endTime - startTime) / 1E6 + " ms!");
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime(生成集合，bfs) : " + ((endTime - startTime) - externalStorageTime) / 1E6 + " ms!\n");
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime(生成集合，bfs) : " + runningTime / 1E6 + " ms!\n");
	}
}

//Slashdot Random : 生成所有landmark用时（生成集合, bfs, write） ： 24611 ms!
//Slashdot Random runningTime(生成集合，bfs) : 14117 ms!

//getLocalCentralLandmarkSet
//Slashdot Centrality : 生成所有landmark用时（生成集合, bfs, write） ： 56677 ms!
//Slashdot Centrality runningTime(生成集合，bfs) : 49501 ms!

//getLocalMaxDegreeLandmarkSet
//Slashdot Centrality : 生成所有landmark用时（生成集合, bfs, write） ： 23544 ms!
//Slashdot Centrality runningTime(生成集合，bfs) : 13526 ms!

//20171123
//Slashdot Centrality : 生成所有landmark用时（生成集合, bfs, write） ： 23345.494829 ms!
//Slashdot Centrality runningTime(生成集合，bfs) : 17795.005377 ms!
//Slashdot Centrality runningTime(生成集合，bfs) : 13644.55414 ms!

//-----------------------------------------------------------

//youtube Random : 生成所有landmark用时（生成集合, bfs, write） ： 1404035 ms!
//youtube Random runningTime(生成集合，bfs) : 415614 ms!


//1123
//youtube Centrality : 生成所有landmark用时（生成集合, bfs, write） ： 1260339.678355 ms!
//youtube Centrality runningTime(生成集合，bfs) : 689159.768538 ms!
//
//youtube Centrality runningTime(生成集合，bfs) : 365321.418116 ms!

//-----------------------------------------------------------

//unweightedNYRN Centrality : 生成所有landmark用时（生成集合, bfs, write） ： 609457 ms!
//unweightedNYRN Centrality runningTime(生成集合，bfs) : 143141 ms!

//unweightedNYRN Random : 生成所有landmark用时（生成集合, bfs, write） ： 653133 ms!
//unweightedNYRN Random runningTime(生成集合，bfs) : 148106 ms!

