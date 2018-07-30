package landmark_based_shortest_distance;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleGraph;

public class landmarkEmbedding extends RandomPair{
	
	public landmarkEmbedding(String from, String to) {
		super(from, to);
		// TODO Auto-generated constructor stub
	}
	
	public static final int landmarkNum = 100;
	
	/**
	 * Slashdot
	 */
	public final static double disconnectJudge = 10000.0; //10000作为不连通判定
	public final static double upperBoundDis = 20.0; //20在社交网络中已经是一个超大值，作为连不通的惩罚
	public final static String adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/"
			+ "cutGraph/Slashdot_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
	public final static String bWriteLandmarkEmbeddingBasedCentralityPrefix = 
			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/landmarkEmbeddingBasedCentrality20171031/";
	public final static String bWriteLandmarkEmbeddingBasedRandomPrefix = 
			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Slashdot/landmarkEmbeddingBasedRandom20171031/";

	//Epinions1_adjFormat
//	public final static double disconnectJudge = 10000.0; //10000作为不连通判定
//	public final static double upperBoundDis = 20; //20已经是一个超大值，作为连不通的惩罚
//	public final static String adjGraphPath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/cutGraph/soc-Epinions1_adjFormat"; //邻接图文件路径
//	public final static String adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/cutGraph/soc-Epinions1_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
//	public final static String bWriteLandmarkEmbeddingBasedCentralityPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedCentrality/";
//	public final static String bWriteLandmarkEmbeddingBasedRandomPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Epinions/landmarkEmbeddingBasedRandom/";
	
	//dblp 节点
//	public final static double disconnectJudge = 10000.0; //10000作为不连通判定
//	public final static double upperBoundDis = 20.0; //20已经是一个超大值，作为连不通的惩罚
//	public final static String adjGraphPath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/cutGraph/dblp_adjFormat"; //邻接图文件路径
//	public final static String adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/cutGraph/dblp_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
//	public final static String bWriteLandmarkEmbeddingBasedCentralityPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/landmarkEmbeddingBasedCentrality/";
//	public final static String bWriteLandmarkEmbeddingBasedRandomPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/dblp/landmarkEmbeddingBasedRandom/";
	
	//facebook
//	public final static double disconnectJudge = 10000.0; //10000作为不连通判定
//	public final static double upperBoundDis = 20.0; //20已经是一个超大值，作为连不通的惩罚
//	public final static String adjGraphPath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/cutGraph/facebook_adjFormat"; //邻接图文件路径
//	public final static String adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/cutGraph/facebook_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
//	public final static String bWriteLandmarkEmbeddingBasedCentralityPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/landmarkEmbeddingBasedCentrality/";
//	public final static String bWriteLandmarkEmbeddingBasedRandomPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/facebook/landmarkEmbeddingBasedRandom/";
	
	//Douban
//	public final static double disconnectJudge = 10000.0; //10000作为不连通判定
//	public final static double upperBoundDis = 20.0; //20已经是一个超大值，作为连不通的惩罚
//	public final static String adjGraphPath = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/cutGraph/Douban_adjFormat"; //邻接图文件路径
//	public final static String adjPart100Graph = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/cutGraph/Douban_adjFormat.part.100";//邻接图的分图（100分）文件路径.文件形式：每行一个整数代表当前以行数为id的节点所处的子图编号
//	public final static String bWriteLandmarkEmbeddingBasedCentralityPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/landmarkEmbeddingBasedCentrality/";
//	public final static String bWriteLandmarkEmbeddingBasedRandomPrefix = 
//			"/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/Douban/landmarkEmbeddingBasedRandom/";

	
	public final static boolean isCentrality = false;//true:按照中心性取landmark； false：按照随机取landmark
	
	//http://jgrapht.org/javadoc/org/jgrapht/alg/shortestpath/DijkstraShortestPath.html
	public final static DijkstraShortestPath<String, DefaultEdge> dijkstraShortestPath = new DijkstraShortestPath(RandomPair.myGraph);
	//注意这里的声明只运行一次，一定建立了强大图的索引； RandomPair.myGraph 是静态对象，只需要初始化一次
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	/**
	 * 随机100个整数，作为landmark
	 * @return
	 */
	public static Set<String> getRandomLandmarkSet(){
		
		Random random = new Random();
		Set<String> randomLandmarkSet = new HashSet<>();
		
		while(randomLandmarkSet.size() < landmarkNum) {
			
			String thisLandmark = String.valueOf(random.nextInt(vertexNum));
			if(!randomLandmarkSet.contains(thisLandmark) && myGraph.degreeOf(thisLandmark) > 0 && RandomPair.myGraph.containsVertex(thisLandmark)) 
				randomLandmarkSet.add(thisLandmark);
				
		}
		return randomLandmarkSet;
		
	}
	
	/**
	 * 遍历graphFilePath，如果某行（pair（a，b））两点都在集合Set中，就添加到myLocalGraph中
	 * @param localGraphVertexSet 生成当前图的 节点集合
	 * @return 无向图
	 */
	public static UndirectedGraph<String, DefaultEdge> creatLocalGraph(Set<String> localGraphVertexSet){
		
		//System.out.println("func creatGraph is running!");
		UndirectedGraph<String, DefaultEdge> myLocalGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(graphFilePath));
			String tempString = null;  
	        int line = 0; 
	        while ((tempString = bReader.readLine()) != null) {// 一次读入一行，直到读入null为文件结束 
	        	
	        	if(++line <= 4)//前4行无用信息
	        		continue;
	        	
	        	String fromVertex = tempString.split("\t")[0];
	        	String toVertex = tempString.split("\t")[1];
	        	if(fromVertex.equals(toVertex))
	        		continue;
	        	if(!(localGraphVertexSet.contains(fromVertex) && localGraphVertexSet.contains(toVertex)))//如果当前边的两个端点不同时都在子图集合中，没必要进行下去
	        		continue;
	        	
	        	myLocalGraph.addVertex(fromVertex);
	        	myLocalGraph.addVertex(toVertex);
	        	myLocalGraph.addEdge(fromVertex, toVertex);
	        	
	        }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		//System.out.println("func creatGraph is over!");
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
	 * 根据中心性 centrality，选择优先级最高的100个节点作为landmark
	 * @return
	 */
	public static Set<String> getCentralLandmarkSet(){
		
		// metis path： /home/cbvon/metis-5.1.0/build/Linux-x86_64/programs/gpmetis
		// adj_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat
		// adj_part_100_graph: /home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/cutGraph/Slashdot_adjFormat.part.100
		
		Map<String, Double> avgLocalDisMap = new HashMap<>(); //存储每个节点在局部的距离每个子图的其余节点的平均距离
		try {
			
			Set<String>[] graphPartSetList = new Set[landmarkNum];
			for(int i = 0; i < landmarkNum; ++i)
				graphPartSetList[i] = new HashSet<String>();
		
			BufferedReader bReader = new BufferedReader(new FileReader(adjPart100Graph));
			String tempStr = null;
			int thisVertexId = 0;
			while((tempStr = bReader.readLine()) != null) {
				int thisSetIndex = Integer.valueOf(tempStr);
				graphPartSetList[thisSetIndex].add(String.valueOf(thisVertexId++));//graphPartSetList中每个set元素个数都是800出头
			}
			
			//avgLocalDisMap = getAvgLocalDisMap(graphPartSetList);
			for(Set<String> i: graphPartSetList) {
				System.out.println("avgLocalDisMap.size() : " + avgLocalDisMap.size());
				UndirectedGraph<String, DefaultEdge> localGraph = creatLocalGraph(i);
				DijkstraShortestPath<String, DefaultEdge> localDijkstraShortestPath = new DijkstraShortestPath(localGraph);
				
				for(String j: i) {
					if(!localGraph.containsVertex(j)) {
						avgLocalDisMap.put(j, disconnectJudge);
						continue;
					}
					
					SingleSourcePaths<String, DefaultEdge> singleSourcePathsForJ = localDijkstraShortestPath.getPaths(j);
					double thisDisSum = 0.0;
					for(String k: i) { //注意：这里 k 有可能通过 子图i 无法连接到j
						if(!localGraph.containsVertex(k)) //注意：即使 j 和 k都在局部子图中，也会存在j k不连通的问题！ 因为子图内部也划分成独立子图
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
					double thisAvgDis = thisDisSum / i.size();//thisAvgDis 区别并不大 基本都在5~10之间
					avgLocalDisMap.put(j, thisAvgDis);
				}
			}//avgLocalDisMap.size() : 82168
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Map<String, Double> priorityMap = new HashMap<>(); // 存储 每个节点 选作landmark优先级
		for(String i: myGraph.vertexSet())
			priorityMap.put(i, Double.valueOf(myGraph.degreeOf(i) / avgLocalDisMap.get(i))); //全局度数越大越好， 局部平均距离越小越好
		
		Map<String, Double> priorityMapSorted = sortByValue(priorityMap);
		
		Set<String> centralLandmarkSet = priorityMapSorted.keySet();
		
		return centralLandmarkSet;
		
	}
	
	/**
	 * getLandmarkEmbedding
	 * @param thisLandmark 本次有待生成landmarkEmbedding的节点编号
	 * 这里没有生成一个数据结构，而是直接将embedding（Map<String, Double>类型）数据结构保存到 文件系统中
	 */
	public static void getLandmarkEmbedding(String thisLandmark) {
		
		System.out.println("thisLandmark : " + thisLandmark);
		startTime = System.currentTimeMillis();
		SingleSourcePaths<String, DefaultEdge> singleSourcePaths = dijkstraShortestPath.getPaths(thisLandmark);
		endTime = System.currentTimeMillis();
		System.out.println("sssp计算 ： " + (endTime - startTime) + " ms!");//sssp计算 ： 300+ ms!
		
		//http://jgrapht.org/javadoc/org/jgrapht/alg/interfaces/ShortestPathAlgorithm.SingleSourcePaths.html
		//这里从0便利到vertexNum（所有节点）作为汇点， 计算从源点thisLandmark到所有汇点的最短距离
		Map<String, Double> thisLandmarkEmbeddingMap = new HashMap<>();
		Map<String, List<String>> thisLandmarkEmbeddingPathVertexListMap = new HashMap<>();
		for(int i = 0; i < vertexNum; ++i) {
			double thisMinimumDis = singleSourcePaths.getWeight(String.valueOf(i));
			if(thisMinimumDis > disconnectJudge) //不连通, landmarkEmbedding 不要计入不连通节点的信息
				continue;
			thisLandmarkEmbeddingMap.put(String.valueOf(i), thisMinimumDis); 
			
			GraphPath<String, DefaultEdge> thisGraphPath = singleSourcePaths.getPath(String.valueOf(i));
			List<String> thisVertexList = thisGraphPath.getVertexList();//thisVertexList format : [63873, 14453, 37263, 3123, 19830] 
			thisLandmarkEmbeddingPathVertexListMap.put(String.valueOf(i), thisVertexList); 
		}
		endTime = System.currentTimeMillis();
		System.out.println("遍历取值 ： " + (endTime - startTime) + " ms!");//遍历取值 ： 400+ ms!
		
//		for(int i = 0; i <= 8; ++i)
//			System.out.println(i + " " + thisLandmarkEmbeddingMap.get(String.valueOf(i * 10000))); //用0作为sourse，验证正确
		
		try {//保存 此次landmark的embedding(Map<String, Double> 类型)
			BufferedWriter bWriter = null;
			BufferedWriter bWriterPath = null;
			if(! isCentrality) {
				bWriter = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedRandomPrefix + thisLandmark + "_embedding.json"));
				bWriterPath = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedRandomPrefix + thisLandmark + "_embedding_pathVertexList.json"));
			}else if(isCentrality){
				bWriter = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedCentralityPrefix + thisLandmark + "_embedding.json"));
				bWriterPath = new BufferedWriter(new FileWriter(bWriteLandmarkEmbeddingBasedCentralityPrefix + thisLandmark + "_embedding_pathVertexList.json"));
			}
			
			String thisLandmarkEmbeddingMapString = thisLandmarkEmbeddingMap.toString();
			bWriter.write(thisLandmarkEmbeddingMapString);
			bWriter.close();
			
			String thisLandmarkEmbeddingPathVertexListMapString = thisLandmarkEmbeddingPathVertexListMap.toString();
			bWriterPath.write(thisLandmarkEmbeddingPathVertexListMapString);
			bWriterPath.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String args[]) {
		
		long startTime = System.currentTimeMillis();
		Set<String> landmarkSet = isCentrality ? getCentralLandmarkSet() : getRandomLandmarkSet(); //中心性 生成
		long endTime = System.currentTimeMillis();
		System.out.println("生成优先级最高的100个中心节点集合用时 ： " + (endTime - startTime) + " ms!");
		
		Object[] randomLandmarkArray = landmarkSet.toArray();
		String LandmarkArray[] = new String[landmarkNum];
		for(int i = 0; i < randomLandmarkArray.length; ++i) 
			LandmarkArray[i] = (String) randomLandmarkArray[i];
		
		startTime = System.currentTimeMillis();
		for(String i: LandmarkArray)
			getLandmarkEmbedding(i);
		endTime = System.currentTimeMillis();
		System.out.println("生成所有landmark用时 ： " + (endTime - startTime) + " ms!");
		//生成所有landmark用时 ： 37995 ms!  这样看我的原始版本dijk算法（30000ms+） 落后100倍速度
		//						48780
		//						48209
		//						48948
		//                      171104
		
		//Epinions1 42562
		
		//dblp :生成所有landmark用时 ： 167018 ms!
		//                           167906
	}

}
