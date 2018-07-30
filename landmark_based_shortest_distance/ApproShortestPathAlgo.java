package landmark_based_shortest_distance;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 近似最短路算法
 * @author cbvon
 */
public class ApproShortestPathAlgo extends landmarkEmbedding{
	
	public ApproShortestPathAlgo(String from, String to) {
		super(from, to);
		// TODO Auto-generated constructor stub
	}
	
	//public static final String landmarkEmbeddingBasedCentralityReverse = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/landmarkEmbeddingBasedCentralityReverse20171031";
	
	public static final boolean isCentrality = true;//true:按照中心性取landmark； false：按照随机取landmark
	public static final String landmardEmbeddingDir = isCentrality ? bWriteLandmarkEmbeddingBasedCentralityPrefix : bWriteLandmarkEmbeddingBasedRandomPrefix;
	
	public static long startTime = 0;
	public static long endTime = 0;
	
	/**
	 * 如果Pair不是static，那么Pair是动态类；
	 * 在静态方法中不能调用 动态类；
	 * 所以需要需改为静态类Pair
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
			} //thisIndex： //10000
				
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
	public static void doGetLandmardEmbeddingList(Map<String, Double> landmardEmbedding, File landmardEmbeddingDirFile) {
		
		System.out.println("func doGetLandmardEmbeddingList is running!");
		startTime = System.currentTimeMillis();
		
		
		try {
			//根据每个 landmarkEmbedding文件 ，生成对应的Map<String, Double>[] landmardEmbedding
			BufferedReader bReader = new BufferedReader(new FileReader(landmardEmbeddingDirFile));
			String thisString = bReader.readLine();
			String mapString = thisString.substring(1, thisString.length() - 1); //去除首尾 {   }
			String[] mapStringList = mapString.split(", ");
			int mapStringListLen = mapStringList.length;
			for(int i1 = 0; i1 < mapStringListLen; ++i1) {
				String thisNode = mapStringList[i1].split("=")[0];
				double thisMinDis = Double.parseDouble(mapStringList[i1].split("=")[1]);
				landmardEmbedding.put(thisNode, thisMinDis);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		endTime = System.currentTimeMillis();
		System.out.println("func doGetLandmardEmbeddingList using ： " + (endTime - startTime) + " ms!");//7263 ms!
		System.out.println("func doGetLandmardEmbeddingList is over!");
	}
	
	/**
	 * 获取近似算法计算的近似最短路径 20171105仿LocalDijk的实现，每次取一组landmarkEmbedding，节省内存
	 * @param queryArray 待查询（pair）数组
	 * @param landmardEmbeddingDirFileList
	 * @return
	 */
	public static double[][] getApproShortestPathArray(Pair[] queryArray, File[] landmardEmbeddingDirFileList) {
		
		System.out.println("func getApproShortestPathArray is running!");
		long startTime = System.currentTimeMillis();
		double[][] approShortestPathArray = new double[landmarkNum][queryPairNum];
		
		int index = 0; //表示有效下标（非 "_embedding.json" 结尾）
		for(int thisLandmarkNum = 0; thisLandmarkNum < landmarkNum * 2; ++thisLandmarkNum) { //遍历每个landmark
			if(!landmardEmbeddingDirFileList[thisLandmarkNum].toString().endsWith("_embedding.json"))  //"_embedding.json" 结尾是pathWeight 不是pathVertex
				continue;
			
			System.out.println(index + "----- -----");
			if(index == 0)
				for(int i = 0; i < queryPairNum; ++i)
					approShortestPathArray[index][i] = 10000.0; //初始一个上界值， 在计算过程中不断刷新（降低）近似最短距离
			else
				//approShortestPathArray[index] = approShortestPathArray[index - 1]; //浅复制
				approShortestPathArray[index] = approShortestPathArray[index - 1].clone(); //深复制
			
			Map<String, Double> landmardEmbeddingListJ = new HashMap<>();
			doGetLandmardEmbeddingList(landmardEmbeddingListJ, landmardEmbeddingDirFileList[thisLandmarkNum]);
			
			int queryArrayLength = queryArray.length;
			for(int i = 0; i < queryArrayLength; ++i) { //对 10000pair 进行LocalDijk
				double a_landmarkJ_dis = landmardEmbeddingListJ.get(queryArray[i].a);
				double b_landmarkJ_dis = landmardEmbeddingListJ.get(queryArray[i].b);
				double approDisBetweenAB = a_landmarkJ_dis + b_landmarkJ_dis;
				
				if(approDisBetweenAB < approShortestPathArray[index][i]) //当前landmark 可以取得更精确（更小）的 近似
					approShortestPathArray[index][i] = approDisBetweenAB; 
			}
			++index;
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("func getApproShortestPathArray using ： " + (endTime - startTime) + " ms!");
		//233 ms!  相比于工具包的2881658 ms 快很多很多很多
		System.out.println("func getApproShortestPathArray is over!");
		return approShortestPathArray;
		
	}
	
	/**
	 * 获取 实际的精确的最短路径
	 * @return
	 */
	public static  double[] getPairsMiniDisArray(int queryPairNum) {
		
		double[] pairsMiniDisArray = new double[queryPairNum];
		try {
			
			BufferedReader bReader = new BufferedReader(new FileReader(bWriterminimumDisFilePath));
			String tempString = null;
			//System.out.println(tempString);
			int index = 0;
			while((tempString = bReader.readLine()) != null) {
				pairsMiniDisArray[index++] = Double.valueOf(tempString);
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
	public static double getAvgError(double[] approShortestPathArray, double[] pairsMiniDisArray) {
		
		//assert (approShortestPathArray.length == 10000 && pairsMiniDisArray.length == 10000);
		
		int rightCount = 0;
		int wrongCount = 0;
		double sumError = 0.0;
		double avgError = 0.0; 
		
		int pairsMiniDisArrayLen = pairsMiniDisArray.length;
		for(int i = 0; i < pairsMiniDisArrayLen; ++i) {
			
			if (approShortestPathArray[i] < pairsMiniDisArray[i]) {
				++wrongCount;
			}else if (approShortestPathArray[i] == pairsMiniDisArray[i]) {
				++rightCount;
			}/*else {
				System.out.println(approShortestPathArray[i] + " " + pairsMiniDisArray[i]);//最大误差为1
			}*/
			sumError += ((approShortestPathArray[i] - pairsMiniDisArray[i]) / pairsMiniDisArray[i]);
			
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
		File landmardEmbeddingDirFile = new File(landmardEmbeddingDir);
		File landmardEmbeddingDirFileList[] = landmardEmbeddingDirFile.listFiles();//embedding目录下所有子文件的路径,总共100个landmark
//		Map<String, Double>[] landmardEmbeddingList = new Map[landmarkNum * 2];// * 2,是因为对于一个landmark有一个距离的landmark和一个路径的landmark
//		for(int i = 0; i < landmarkNum * 2; ++i)
//			landmardEmbeddingList[i] = new HashMap<>();
//		doGetLandmardEmbeddingList(landmardEmbeddingList, landmardEmbeddingDirFileList); //20171105 存储空间不足，装不下全局landmarkEmbedding
		
		//本段：从文件系统获取所有pair的精确最短距离
		double[] pairsMiniDisArray = new double[queryPairNum];
		pairsMiniDisArray = getPairsMiniDisArray(queryPairNum); //pairsMiniDisArray.length : 10000
		
		//本段：使用landmark数目依次为 10， 20， ...... 100个，分别在不同landmark数目下计算平均误差，观测landmark数目对近似的影响
		double[][] approShortestPathArray = new double[landmarkNum][queryPairNum];
		approShortestPathArray = getApproShortestPathArray(queryArray, landmardEmbeddingDirFileList);
		
		for(int thisLandmarkNum = 10; thisLandmarkNum <= 100; thisLandmarkNum += 10) {
			System.out.println("thisLandmarkNum : " + thisLandmarkNum);
			double avgError = getAvgError(approShortestPathArray[thisLandmarkNum - 1], pairsMiniDisArray);
			System.out.println("avgError : " + avgError + "\n"); 
		}
		//刚写出这个算法，当 landmark为100， 平均误差0.45894488095237174 和论文（0.4535）基本一致。1031：发现近似效果和随机数据集有很大关系
	
	}

}
