package undirected_weighted_version;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.Random;
import java.util.Set;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

public class LandmarkEmbeddingPathLenAndVecList extends RandomPairDis{

	public final static boolean isCentrality = false;//true:按照中心性取landmark； false：按照随机取landmark
	
	//http://jgrapht.org/javadoc/org/jgrapht/alg/shortestpath/DijkstraShortestPath.html
	public final static DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraShortestPath = new DijkstraShortestPath<String, DefaultWeightedEdge>(RandomPairDis.myGraph);
	//注意这里的声明只运行一次，一定建立了强大图的索引； RandomPair.myGraph 是静态对象，只需要初始化一次
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static long runningTime = 0;
	
	public static Random random = new Random();
	
	/**
	 * 随机100个整数，作为landmark
	 * @return
	 */
	public static Set<String> getRandomLandmarkSet(){
		Set<String> randomLandmarkSet = new HashSet<>();
		while(randomLandmarkSet.size() < landmarkNum) {
			String thisLandmark = String.valueOf(random.nextInt(vertexNum));
			if(!randomLandmarkSet.contains(thisLandmark) && myGraph.containsVertex(thisLandmark) && myGraph.degreeOf(thisLandmark) > 0) 
				randomLandmarkSet.add(thisLandmark);
		}
		return randomLandmarkSet;
	}
	
	/**
	 * 遍历原始graphFilePath，如果某行（pair（a，b））两点都在集合Set中，就添加到myLocalGraph中。注意这里针对bigger子图，因为子图很小的话没必要去遍历全图所有边
	 * @param localGraphVertexSet 生成当前图的节点集合
	 * @return 无向图 SimpleWeightedGraph<String, DefaultWeightedEdge> myLocalGraph
	 */
	@SuppressWarnings("resource")
	public static SimpleWeightedGraph<String, DefaultWeightedEdge> creatBiggerLocalGraph(Set<String> localGraphVertexSet){
		SimpleWeightedGraph<String, DefaultWeightedEdge> myLocalGraph = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(graphFilePath));
			String tempString = null;  
	        int line = 0; 
	        while ((tempString = bReader.readLine()) != null) {// 一次读入一行，直到读入null为文件结束 
	        	if(++line <= 4)//前4行无用信息
	        		continue;
	        	String fromVertex = tempString.split("\t")[0];
	        	String toVertex = tempString.split("\t")[1];
	        	int thisDis = Integer.valueOf(tempString.split("\t")[2]);
	        	if(fromVertex.equals(toVertex))
	        		continue;
	        	if(!(localGraphVertexSet.contains(fromVertex) && localGraphVertexSet.contains(toVertex)))//如果当前边的两个端点不同时都在子图集合中，没必要进行下去
	        		continue;
	        	
	        	myLocalGraph.addVertex(fromVertex);
	        	myLocalGraph.addVertex(toVertex);
	        	myLocalGraph.addEdge(fromVertex, toVertex);
	        	myLocalGraph.setEdgeWeight(myLocalGraph.getEdge(fromVertex, toVertex), thisDis);
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
    	// metis path： /home/cbvon/metis-5.1.0/build/Linux-x86_64/programs/gpmetis
    	// adj_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat
    	// adj_part_100_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat.part.100
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
		return graphPartSetList;
    }
    
	/**
	 * 根据中心性 centrality，选择优先级最高的100个节点作为landmark
	 * @return
	 */
	public static Set<String> getLocalCentralLandmarkSet(){
		startTime = System.currentTimeMillis();
		Map<String, Double> avgLocalDisMap = new HashMap<>(); //存储每个节点在局部的距离每个子图的其余节点的平均距离
		Set<String>[] graphPartSetList = getGraphPartSetList();
		for(Set<String> i: graphPartSetList) {
			System.out.println("avgLocalDisMap.size() : " + avgLocalDisMap.size());
			SimpleWeightedGraph<String, DefaultWeightedEdge> localGraph = creatBiggerLocalGraph(i);
			DijkstraShortestPath<String, DefaultWeightedEdge> localDijkstraShortestPath = new DijkstraShortestPath<String, DefaultWeightedEdge>(localGraph);
			
			for(String j: i) {
				if(!localGraph.containsVertex(j)) {
					avgLocalDisMap.put(j, (double) disconnectJudge);
					continue;
				}
				
				SingleSourcePaths<String, DefaultWeightedEdge> singleSourcePathsForJ = localDijkstraShortestPath.getPaths(j);
				double thisDisSum = 0.0;
				for(String k: i) { //注意：这里 k 有可能通过 子图i 无法连接到j;即使 j 和 k都在局部子图中，也会存在j k不连通的问题！ 因为子图内部也划分成独立子图
					if(!localGraph.containsVertex(k)) 
						continue;
					if(j.equals(k))
						continue;
					//RandomPair thisPair = new RandomPair(j, k, localGraph); 
					//注意这里RandomPair不能基于全图，需要基于局部子图//每次求k所有pair效率太低，应当改为对k集合求一次dijk的sssp//利用localSssp效率提升1000倍
					double minimumDis = singleSourcePathsForJ.getWeight(k);
					if(minimumDis > disconnectJudge)
						minimumDis = upperBoundDis; 
					thisDisSum += minimumDis;
				}
				double thisAvgDis = thisDisSum / i.size();
				avgLocalDisMap.put(j, thisAvgDis);
			}
		}
		
		Map<String, Double> priorityMap = new HashMap<>(); // 存储 每个节点 选作landmark优先级
		for(String i: myGraph.vertexSet())
			priorityMap.put(i, Double.valueOf(myGraph.degreeOf(i) / avgLocalDisMap.get(i))); //全局度数越大越好， 局部平均距离越小越好
		
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
				if(!myGraph.containsVertex(thisVecId))
					continue;
				int thisDegree = myGraph.degreeOf(thisVecId); //和 getLocalMaxAvgLocalDisLandmarkSet 唯一不同
				if(thisDegree > maxDegree) {
					maxDegree = thisDegree;
					maxDegreeVecId = thisVecId;
				}
			}
			if(maxDegree == -1) { //当前分区中没有合适（不存在或者独立）元素，则全图任选一个（合适的）作为landmark
				do {
					maxDegreeVecId = String.valueOf(random.nextInt(vertexNum));
				} while(!(myGraph.containsVertex(maxDegreeVecId) && myGraph.degreeOf(maxDegreeVecId) > 0 && !localMaxDegreeLandmarkSet.contains(maxDegreeVecId)));
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
		
		long thisRunningStartTime = System.currentTimeMillis();
		SingleSourcePaths<String, DefaultWeightedEdge> singleSourcePaths = dijkstraShortestPath.getPaths(thisLandmark);
		long thisRunningEndTime = System.currentTimeMillis();
		runningTime += (thisRunningEndTime - thisRunningStartTime); //dijk
		
		//如果一次write所有点的landmarkEmbedding信息会导致内存溢出；需要采用批处理的方式，每次写入1000个点的embedding信息
		int groupNum = (vertexNum + (partGroupSize - 1)) / partGroupSize;
		for(int thisGroupNum = 0; thisGroupNum < groupNum; ++thisGroupNum) {
			int startVecNum = thisGroupNum * partGroupSize;
			int endVecNum = (groupNum - thisGroupNum == 1)? vertexNum: startVecNum + 1000; //是否为最后一组 //20171110：因为末尾是否包含，导致所有 ...999的embedding都丢失了
			
			Map<String, Integer> thisLandmarkEmbeddingPathLenMap = new HashMap<>();
			Map<String, Set<String>> thisLandmarkEmbeddingPathVertexListMap = new HashMap<>();
			
			thisRunningStartTime = System.currentTimeMillis();
			for(int i = startVecNum; i < endVecNum; ++i) { 
				//这里从startVecNum便利到endVecNum（所有节点）作为汇点， 计算从源点thisLandmark到所有汇点的最短距离
				//http://jgrapht.org/javadoc/org/jgrapht/alg/interfaces/ShortestPathAlgorithm.SingleSourcePaths.html
				int thisMinimumDis = (int) singleSourcePaths.getWeight(String.valueOf(i));
				if(thisMinimumDis > disconnectJudge) //不连通, landmarkEmbedding 不要计入不连通节点的信息
					continue;
				thisLandmarkEmbeddingPathLenMap.put(String.valueOf(i), thisMinimumDis); 
				
				GraphPath<String, DefaultWeightedEdge> thisGraphPath = singleSourcePaths.getPath(String.valueOf(i));
				Set<String> thisVertexList = new HashSet<>(thisGraphPath.getVertexList());//thisVertexList format : [63873, 14453, 37263, 3123, 19830] 
				thisLandmarkEmbeddingPathVertexListMap.put(String.valueOf(i), thisVertexList); 
			}
			thisRunningEndTime = System.currentTimeMillis();
			runningTime += (thisRunningEndTime - thisRunningStartTime);
			
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
	}
	
	public static void main(String args[]) {
		long thisRunningStartTime = System.currentTimeMillis();
		//Set<String> landmarkSet = isCentrality ? getLocalCentralLandmarkSet() : getRandomLandmarkSet(); //中心性生成
		Set<String> landmarkSet = isCentrality ? getLocalMaxDegreeLandmarkSet() : getRandomLandmarkSet(); //中心性生成
		long thisRunningEndTime = System.currentTimeMillis();
		runningTime += (thisRunningEndTime - thisRunningStartTime); //生成集合
		
		clearDir();
		
		for(String i: landmarkSet)
			getLandmarkEmbedding(i);
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime(生成集合，bfs) : " + runningTime + " ms!\n");
	}
}
//NYRN Centrality runningTime(生成集合，bfs) : 27938 ms!

//NYRN Random runningTime(生成集合，bfs) : 870713 ms!
