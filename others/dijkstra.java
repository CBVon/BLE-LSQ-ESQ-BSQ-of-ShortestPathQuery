package no_use;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.sf.json.JSONObject;


/**
 * 功能：源生实现sssp，求解准确的单源最短路径
 * @author cbvon
 */
public class dijkstra {
	
	/** 
     * 以行为单位读取文件，常用于读面向行的格式化文件 
	 * @return 图 net
	 * 具体格式为：
	 * {{from1：{to1：距离1， to2：距离2}，
	 * {from2：{to1：距离1， to2：距离2}}
     */  
    public static Map<Integer, Map<Integer, Integer>> readFileByLines(String fileName) { 
    	
    	System.out.println("func readFileByLines is running !");
    	
    	Map<Integer, Map<Integer, Integer>> net = new HashMap<>();
    	
        File file = new File(fileName);  
        BufferedReader reader = null;  
        try {  
            // System.out.println("以行为单位读取文件内容，一次读一整行：");  
            reader = new BufferedReader(new FileReader(file));  
            String tempString = null;  
            int line = 0;  
            // 一次读入一行，直到读入null为文件结束  
            while ((tempString = reader.readLine()) != null) {  
                // 显示行号  
            	++line;
            	if(line <= 4)
            		continue;
            	
                // System.out.println("line " + line + ": " + tempString);  
            	int from_node = Integer.parseInt(tempString.split("\t")[0]);
            	int to_node = Integer.parseInt(tempString.split("\t")[1]);
            	
            	if(from_node == to_node)
            		continue;
            	
            	if(!net.containsKey(from_node)) {
            		Map<Integer, Integer> temp_net = new HashMap<>();
            		temp_net.put(to_node, 1);
            		net.put(from_node, temp_net);
            	}else {
            		Map<Integer, Integer> temp_net = net.get(from_node);
            		temp_net.put(to_node, 1);
            		net.put(from_node, temp_net);
				}
                
            }  
            reader.close();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                }  
            }  
        }  
        
        System.out.println("func readFileByLines is over !");
        return net;
    }  
    
    /*
    public class pair{
    	
    	public pair(int sourse, int i) {
			// TODO Auto-generated constructor stub
		}

		int first, second;
    }
    
    //匿名Comparator实现，直接new 
    public static Comparator<pair> pairComparator = new Comparator<pair>(){

		@Override
		public int compare(pair o1, pair o2) {
			// TODO Auto-generated method stub
			return (int) (o1.second - o2.second );
		}
		
    };
    */
    
    /**
     * 
     * @param net 图
     * @param sourse 源点
     * @return 关于源点的最短距离
     */
    public static Map<Integer, Integer> dijk(Map<Integer, Map<Integer, Integer>> net, int sourse){
    	
    	System.out.println("func dijk is running !");
    	
    	Map<Integer, Integer> sssp = new HashMap<>();
    	
    	Map<Integer, Integer> to_proccess = new HashMap<>();
    	//Queue<pair> to_proccess = new PriorityQueue<>(pairComparator); //优先队列有坑：中间元素不能更改和删除
    	
    	to_proccess.put(sourse, 0);
    	//to_proccess.add(new pair(sourse, 0));
    	
    	while(!to_proccess.isEmpty()) {
    		
    		int min_dis_key = -1;
    		int min_dis_value = (int) 1E8;
    		
    		Set<Integer> to_proccess_key_set = to_proccess.keySet();
    		for(int i: to_proccess_key_set) {
    			int this_dis_value = to_proccess.get(i);
    			if(this_dis_value < min_dis_value) {
    				min_dis_value = this_dis_value;
    				min_dis_key = i;
    			}
    		}
    		
    		/*
    		pair min_dis_pair = to_proccess.poll();
    		min_dis_key = min_dis_pair.first;
    		min_dis_value = min_dis_pair.second;
    		*/
    		
    		sssp.put(min_dis_key, min_dis_value);
    		to_proccess.remove(min_dis_key);
    		
    		Map<Integer, Integer> temp_map = new HashMap<>();
    		if(net.containsKey(min_dis_key)) {
    			temp_map = net.get(min_dis_key); //待更新的minest节点， 在图net的后继
    		}else {
    			continue; //temp_map 可能为空, 没有更新必要
			}
    		
    		Set<Integer> temp_map_key_set = temp_map.keySet();
    		for(int i: temp_map_key_set) {
    			if(sssp.containsKey(i))
    				continue; //后继节点i已经加入sssp， 不用考虑更新
    			int i_new_value = sssp.get(min_dis_key) + temp_map.get(i);
    			
    			//查找 优先队列to_proccess 中是否有待更新元素。 //优先队列有坑：中间元素不能更改和删除
    			
    			if((to_proccess.containsKey(i) && i_new_value < to_proccess.get(i)) || !to_proccess.containsKey(i))
    				to_proccess.put(i, i_new_value);
    		}
    		
    	}
    	
    	System.out.println("func dijk is over !");
    	return sssp;
    	
    }
    
	public static void main(String []args) throws IOException {
		
		/*
		 * Slashdot social network
		 * node_num: 82168
		 * edg_num: 948464 (包含 节点本身的自环)
		 */
		String file_path = "/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/soc-Slashdot0902.txt";
		Map<Integer, Map<Integer, Integer>> net = readFileByLines(file_path);
//		System.out.println(net);
//		for(int i : net.keySet()) {
//			System.out.println(i);
//			System.out.println(net.get(i));
//		}
		
		long start_time = System.currentTimeMillis();
		Map<Integer, Integer> sssp = dijk(net, 0);
		long end_time = System.currentTimeMillis();
		System.out.println("dijk has running : " + (end_time - start_time) + " ms");
		
		System.out.println(sssp.size()); //82168
//		for(int i = 1; i <= 8; ++i)
//			System.out.println(sssp.get(i*10000));
		//封装sssp到json
		BufferedWriter bWriter = new BufferedWriter(new FileWriter("/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/sssp.json"));
		//JSONObject json_obj = new JSONObject();
		String sssp_string = sssp.toString();
		bWriter.write(sssp_string);
		bWriter.close();
		
//		//System.out.println(sssp);
//		Set<Integer> sssp_key_set = sssp.keySet();
//		for(int i: sssp_key_set)
//			if(i % 10000 == 0)
//				System.out.println(i + " " +  sssp.get(i));
		
	}
	
}
/*
to_proccess 基于 set实现
func dijk is running !
func dijk is over !
dijk has running : 33756 ms
10倍慢于 landmarkEmbedding中的 SingleSourcePaths效率。 在排序/索引都没有做优化。 尝试优先队列对维护to_proccess做优化失败了



*/
