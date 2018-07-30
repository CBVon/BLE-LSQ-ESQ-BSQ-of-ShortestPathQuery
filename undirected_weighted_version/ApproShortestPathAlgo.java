package undirected_weighted_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 近似最短路算法，本算法weighted和unweighted版本相同
 * @author cbvon
 */
public class ApproShortestPathAlgo extends LandmarkEmbeddingPathLenAndVecList{
		
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
	 * 获取 查询（pair类型）数组
	 * @return
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
	 * 获取近似算法计算的近似最短路径 20171105仿LocalDijk的实现，每次取一组landmarkEmbedding，节省内存
	 * 20171109针对新的分组landmark文件形式，实现对单独landmarkId分批处理（每个分组landmark特定存储表示1000个节点的embedding信息）
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList 所有的landmardEmbeddingDirFile
	 * @return int[landmarkNum][queryPairNum] approShortestPathArray
	 */
	public static int[][] getApproShortestPathArray(Pair[] queryArray, File[] landmardEmbeddingDirFileList) {
		System.out.println("func getApproShortestPathArray is running!");
		long startTime = System.currentTimeMillis();
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		
		int landmardEmbeddingDirFileList_len = landmardEmbeddingDirFileList.length;
		int eachLandmarkGroupSize = landmardEmbeddingDirFileList_len / landmarkNum;
		int queryArrayLength = queryArray.length;
		
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum; ++thisLandmarkNum) {
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
			
			int a_thisLandmark_dis[] = new int[queryArrayLength];
			int b_thisLandmark_dis[] = new int[queryArrayLength];
			for(int thisGroupId = 0; thisGroupId < eachLandmarkGroupSize; ++thisGroupId) {
				int thisFileId = thisLandmarkNum * eachLandmarkGroupSize + thisGroupId;//当前landmark文件 在文件夹中实际编号
				Map<String, Integer> thisLandmardEmbeddingList = new HashMap<>();
				doGetLandmardEmbeddingList(thisLandmardEmbeddingList, landmardEmbeddingDirFileList[thisFileId]);
				
				thisRunningStartTime = System.currentTimeMillis();
				for(int i = 0; i < queryArrayLength; ++i) { //对 10000pair 进行LocalDijk
					if(thisLandmardEmbeddingList.containsKey(queryArray[i].a))
						a_thisLandmark_dis[i] = thisLandmardEmbeddingList.get(queryArray[i].a);
					if(thisLandmardEmbeddingList.containsKey(queryArray[i].b))
						b_thisLandmark_dis[i] = thisLandmardEmbeddingList.get(queryArray[i].b);
				}
				thisRunningEndTime = System.currentTimeMillis();
				runningTime += (Double.valueOf(thisRunningEndTime - thisRunningStartTime) / eachLandmarkGroupSize);
			}
			
			thisRunningStartTime = System.currentTimeMillis();
			for(int i = 0; i < queryArrayLength; ++i) {
				int thisApproDis = a_thisLandmark_dis[i] + b_thisLandmark_dis[i];
				if(thisApproDis < approShortestPathArray[thisLandmarkNum][i])
					approShortestPathArray[thisLandmarkNum][i] = thisApproDis;
			}
			thisRunningEndTime = System.currentTimeMillis();
			runningTime += (thisRunningEndTime - thisRunningStartTime);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getApproShortestPathArray using ： " + (endTime - startTime) + " ms!");
		System.out.println("func getApproShortestPathArray is over!");
		return approShortestPathArray;
		
	}
	
	/**
	 * 获取 实际的精确的最短路径
	 * @return
	 */
	@SuppressWarnings("resource")
	public static int[] getPairsMiniDisArray(int queryPairNum) {
		int[] pairsMiniDisArray = new int[queryPairNum];
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(bWriterminimumDisFilePath));
			String tempString = null;
			int index = 0;
			while((tempString = bReader.readLine()) != null) {
				pairsMiniDisArray[index++] = Integer.valueOf(tempString);
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
	 * 获取 平均误差
	 * @param approShortestPathArray 近似最短路径
	 * @param pairsMiniDisArray 精确最短路径
	 * @return
	 */
	public static double getAvgError(int[] approShortestPathArray, int[] pairsMiniDisArray) {
		int rightCount = 0;
		int wrongCount = 0;
		double sumError = 0.0;
		double avgError = 0.0; 
		
		int pairsMiniDisArrayLen = pairsMiniDisArray.length;
		for(int i = 0; i < pairsMiniDisArrayLen; ++i) {
			if (approShortestPathArray[i] < pairsMiniDisArray[i]) {
				System.out.println("wrong : " + approShortestPathArray[i] + " " + pairsMiniDisArray[i]);
				++wrongCount;
			}else if (approShortestPathArray[i] == pairsMiniDisArray[i]) {
				++rightCount;
			}
			sumError += (Double.valueOf(approShortestPathArray[i] - pairsMiniDisArray[i]) / Double.valueOf(pairsMiniDisArray[i]));
		}
		avgError = sumError / pairsMiniDisArrayLen;
		System.out.println("rightCount : " + rightCount);
		System.out.println("wrongCount : " + wrongCount);
		return avgError;
	}
	
	public static void main(String args[]) {
		
		//本段：获取 待查询pair数组，默认10000对pair
		Pair[] queryArray = new Pair[queryPairNum];
		queryArray = getQueryArray(queryPairNum); //queryArray.length : 10000
		
		//本段：从文件系统获取所有landmark（默认10000个）的landmarkEmbedding
		File landmardEmbeddingDirFile_pathLenEmbedding = new File(landmardEmbeddingDir);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile_pathLenEmbedding.listFiles();//embedding目录下所有子文件的路径,总共100个landmark
		Arrays.sort(landmardEmbeddingDirFileList);
		
		//本段：从文件系统获取所有pair的精确最短距离
		int[] pairsMiniDisArray = new int[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); //pairsMiniDisArray.length : 10000
		
		//本段：使用landmark数目依次为 10， 20， ...... 100个，分别在不同landmark数目下计算平均误差，观测landmark数目对近似的影响
		int[][] approShortestPathArray = new int[landmarkNum][queryPairNum];
		approShortestPathArray = getApproShortestPathArray(queryArray, landmardEmbeddingDirFileList);
		
		for(int thisLandmarkNum = 10; thisLandmarkNum <= 100; thisLandmarkNum += 10) {
			System.out.println("thisLandmarkNum : " + thisLandmarkNum);
			double avgError = getAvgError(approShortestPathArray[thisLandmarkNum - 1], pairsMiniDisArray);
			System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + "avgError : " + avgError + "\n"); 
		}
		System.out.println(dataSet + " " + (isCentrality? "Centrality": "Random") + " runningTime : " + runningTime + " ms!\n");
	}
}
/**
 * 
 * 
 * random
thisLandmarkNum : 100
rightCount : 129
wrongCount : 0
avgError : 0.052295053554066065

centrality
thisLandmarkNum : 100
rightCount : 470
wrongCount : 0
NYRN CentralityavgError : 0.03836595883761379
NYRN Centrality runningTime : 2.2264150943396355 ms!

 * 
 * 
 * 
 */
