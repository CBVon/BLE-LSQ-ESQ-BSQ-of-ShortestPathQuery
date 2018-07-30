package pers.cbvon.shortestPath.undirectedGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 近似最短路径算法.针对有权图和无权图完全相同
 * @author cbvon
 */
public class Class3ApproShortestPathAlgo extends Class2LandmarkEmbeddingPathLenAndVertexSet{
	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix_pathLenEmbedding : bWriteLandmarkEmbeddingBasedRandomPrefix_pathLenEmbedding;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	public static double runningTime = 0;
	
	/**
	 * 如果Pair不是static，那么Pair是动态类；但是在静态方法中不能调用动态类，因此需要需改为静态类Pair
	 * @author cbvon
	 */
	public static class Pair{
		String a, b;
		public Pair(String a, String b) {
			// TODO Auto-generated constructor stub
			this.a = a;
			this.b = b;
		}
	}
	
	/**
	 * getQueryArray
	 * @return Pair[] queryArray
	 */
	@SuppressWarnings("resource")
	public static Pair[] getQueryArray(int queryPairNum) {
		Pair[] queryArray = new Pair[queryPairNum];
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(bWriterPairFilePath));
			String thisLandmarkEmbeddingMapString = null;
			int thisIndex = 0;
			while((thisLandmarkEmbeddingMapString = bReader.readLine()) != null) {
				queryArray[thisIndex++] = new Pair(thisLandmarkEmbeddingMapString.split(" ")[0], 
						thisLandmarkEmbeddingMapString.split(" ")[1]);
				if(thisIndex >= queryPairNum)
					break;
			} 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryArray;
	}
	
	/**
	 * 执行 获取 landmarkEmbedding 操作
	 * @param landmardEmbeddingList 函数目的就是在改数组上对应操作
	 * @param landmardEmbeddingDirFileList landmardEmbeddingDir下的所有子文件
	 */
	@SuppressWarnings("resource")
	public static void doGetLandmardEmbeddingList(Map<String, Integer> landmardEmbedding, File landmardEmbeddingDirFile) {	
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(landmardEmbeddingDirFile));
			String thisString = bReader.readLine();
			String mapString = thisString.substring(1, thisString.length() - 1); //去除首尾 {   }
			if(mapString.equals("")) {
				return;
			}
			String[] mapStringList = mapString.split(", ");
			int mapStringListLen = mapStringList.length;
			for(int i1 = 0; i1 < mapStringListLen; ++i1) {
				String thisNode = mapStringList[i1].split("=")[0];
				int thisMinDis = Integer.valueOf(mapStringList[i1].split("=")[1]);
				landmardEmbedding.put(thisNode, thisMinDis);
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
	 * 获取近似算法计算的近似最短路径.20171105实现每次取一组landmarkEmbedding，节省内存；避免以前一次生成所有的landmark，内存浪费
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList 所有的landmardEmbeddingDirFile
	 * @return int[landmarkNum][queryPairNum] approShortestPathArray
	 */
	public static int[][] getApproShortestPathArray(Pair[] queryArray, File[] landmardEmbeddingDirFileList) {
		System.out.println("func getApproShortestPathArray is running!");
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		
		int landmardEmbeddingDirFileList_len = landmardEmbeddingDirFileList.length; //embedding文件总数
		int eachLandmarkGroupSize = landmardEmbeddingDirFileList_len / landmarkNum; //每个landmarkId 对应的embedding文件数量
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) { //遍历每个landmark
			System.out.println("landmarkId : " + thisLandmarkNum + "----- -----");
			
			if(thisLandmarkNum == 0) {
				for(int i = 0; i < queryPairNum; ++i)
					approShortestPathArray[thisLandmarkNum][i] = upperBoundDis; //初始一个上界值， 在计算过程中不断刷新（降低）近似最短距离
			}else {
				//approShortestPathArray[index] = approShortestPathArray[index - 1]; //浅复制
				approShortestPathArray[thisLandmarkNum] = approShortestPathArray[thisLandmarkNum - 1].clone(); //深复制
			}
			
			int a_thisLandmark_dis[] = new int[queryArrayLength];
			int b_thisLandmark_dis[] = new int[queryArrayLength];
			for(int thisGroupId = 0; thisGroupId < eachLandmarkGroupSize; ++thisGroupId) {
				int thisFileId = thisLandmarkNum * eachLandmarkGroupSize + thisGroupId;//当前landmark文件 在文件夹中实际编号
				Map<String, Integer> thisLandmardEmbeddingList = new HashMap<>();
				doGetLandmardEmbeddingList(thisLandmardEmbeddingList, landmardEmbeddingDirFileList[thisFileId]);
				
				long thisRunningStartTime = System.nanoTime();
				for(int i = 0; i < queryArrayLength; ++i) { //对 10000pair 进行LocalDijk
					if(thisLandmardEmbeddingList.containsKey(queryArray[i].a)) 
						a_thisLandmark_dis[i] = thisLandmardEmbeddingList.get(queryArray[i].a);
					if(thisLandmardEmbeddingList.containsKey(queryArray[i].b))
						b_thisLandmark_dis[i] = thisLandmardEmbeddingList.get(queryArray[i].b);
				}
				long thisRunningEndTime = System.nanoTime();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
			long thisRunningStartTime = System.nanoTime();
			for(int i = 0; i < queryArrayLength; ++i) {
				int thisApproDis = a_thisLandmark_dis[i] + b_thisLandmark_dis[i];
				if(thisApproDis != 0 && thisApproDis < approShortestPathArray[thisLandmarkNum][i])
					approShortestPathArray[thisLandmarkNum][i] = thisApproDis;
			}
			long thisRunningEndTime = System.nanoTime();
			runningTime += (thisRunningEndTime - thisRunningStartTime);
		}
		System.out.println("func getApproShortestPathArray is over!");
		return approShortestPathArray;
	}
	
	/**
	 * 获取实际的精确的最短路径
	 * @return int[queryPairNum] pairsMiniDisArray
	 */
	@SuppressWarnings("resource")
	public static  int[] getPairsMiniDisArray(int queryPairNum) {
		int[] pairsMiniDisArray = new int[queryPairNum];
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(bWriterShortestPathLenFilePath));
			String tempString = null;
			int index = 0;
			while((tempString = bReader.readLine()) != null) {
				pairsMiniDisArray[index++] = Double.valueOf(tempString).intValue();
				if(index >= queryPairNum)
					break;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pairsMiniDisArray;
	}
	
	/**
	 * 获取平均误差
	 * @param approShortestPathArray 近似最短路径
	 * @param pairsMiniDisArray 精确最短路径
	 * @return double avgError
	 */
	public static double getAvgError(int[] approShortestPathArray, int[] pairsMiniDisArray) {
		int rightCount = 0;
		int wrongCount = 0;
		double sumError = 0.0;
		double avgError = 0.0; 
		
		int pairsMiniDisArrayLen = pairsMiniDisArray.length;
		for(int i = 0; i < pairsMiniDisArrayLen; ++i) {
			if (approShortestPathArray[i] < pairsMiniDisArray[i]) {
				System.out.println("Warning !!! WrongAppro!!! " + approShortestPathArray[i] + " " + pairsMiniDisArray[i]);
				++wrongCount;
			}else if (approShortestPathArray[i] == pairsMiniDisArray[i]) {
				++rightCount;
			}else {
				System.out.println(i + " : " + approShortestPathArray[i] + " " + pairsMiniDisArray[i]);
			}
			sumError += (Double.valueOf((approShortestPathArray[i] - pairsMiniDisArray[i])) / Double.valueOf(pairsMiniDisArray[i]));
		}
		avgError = sumError / pairsMiniDisArrayLen;
		System.out.println("rightCount : " + rightCount);
		System.out.println("wrongCount : " + wrongCount);
		return avgError;
	}
	
	/**
	 * main()
	 * @param args
	 */
	public static void main(String args[]) {
		//本段：获取 待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); 
		
		//本段：从文件系统获取所有landmark（默认100个）的landmarkEmbedding文件
		File landmardEmbeddingDirFile = new File(landmardEmbeddingDir);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();
		Arrays.sort(landmardEmbeddingDirFileList);
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); 
		
		//本段：使用landmark数目依次为 10， 20， ...... 100个，分别在不同landmark数目下计算平均误差，观测landmark数目对近似的影响
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		approShortestPathArray = getApproShortestPathArray(queryArray, landmardEmbeddingDirFileList);
		
		for(int thisLandmarkNum = 10; thisLandmarkNum <= 100; thisLandmarkNum += 10) {
			System.out.println("thisLandmarkNum : " + thisLandmarkNum);
			double avgError = getAvgError(approShortestPathArray[thisLandmarkNum - 1], pairsMiniDisArray);
			System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " avgError : " + avgError + "\n"); 
		}
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime / 1E6 + " ms!\n");
	}

}

/*
 * 
20180206-------------------
rightCount : 246
wrongCount : 0
facebook Random avgError : 0.2749071428571428

facebook Random runningTime : 6.951275200000005 ms!
-----

rightCount : 990
wrongCount : 0
facebook Centrality avgError : 0.0095

facebook Centrality runningTime : 7.488978999999997 ms!
----------

rightCount : 24
wrongCount : 0
Slashdot Random avgError : 0.45784999999999954

Slashdot Random runningTime : 2.418482385542167 ms!
-----
rightCount : 767
wrongCount : 0
Slashdot Centrality avgError : 0.06795952380952393

Slashdot Centrality runningTime : 2.443157542168678 ms!
----------
rightCount : 1
wrongCount : 0
youtube Random avgError : 0.4461507936507918

youtube Random runningTime : 2.0971562037996754 ms!
-----
rightCount : 898
wrongCount : 0
youtube Centrality avgError : 0.022258730158730144

youtube Centrality runningTime : 2.292863716753015 ms!
20180206--------------------------




----- ----- 公路实验 nouse

rightCount : 470
unweightedNYRN Centrality avgError : 0.0382382616575914

unweightedNYRN Centrality runningTime : 2.9400340339622804 ms!

----- -----

rightCount : 470
NYRN Centrality avgError : 0.03836595883761379
NYRN Centrality runningTime : 3.5056603773585784 ms!









*/