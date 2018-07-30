package no_use;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.SimpleGraph;

import landmark_based_shortest_distance.landmarkEmbedding;
import net.sf.json.JSONObject;

public class test {
	
	public static void main(String []args) throws IOException {
		
		int a = 1;
		int b = 10;
		float c = a + b;
		
		System.out.println("hello world!");
		
		System.out.println(c);
		
		Map<Integer, Integer> net = new HashMap<>();
		net.put(Integer.valueOf(1), Integer.valueOf(1));
		System.out.println(net);
		net.put(Integer.valueOf(1), 10);
		System.out.println(net);
		
		a = (int) 1E10;
		System.out.println(a);
		
		///////////////////////////////////////////////////////////////////////////////
		
		BufferedReader bReader = new BufferedReader(new FileReader("/home/cbvon/eclipse-workspace/landmark_based_shortest_distance/data/sssp_myDijkDisEmbedding_nouse.json"));
		String sssp_string = bReader.readLine();
		
		/*
		String sssp_string = "";
		String tempString = null;  
        int line = 0;  
        // 一次读入一行，直到读入null为文件结束  
        while ((tempString = bReader.readLine()) != null) {
        	System.out.println("line " + line);
        	sssp_string += tempString;
        }
        
		//sssp_string = "{0=1,2=3,3=4,4=5,5=6,6=7}";
		JSONObject json_obj = JSONObject.fromObject(sssp_string); 
		//报错 ： Found starting '{' but missing '}' at the end. at character 0 of null
		//https://segmentfault.com/q/1010000000611812  ,有可能是因为 json 过于太大
		Map<String, Integer> sssp = (Map<String, Integer>) json_obj;
		*/
		
		sssp_string = sssp_string.substring(1, sssp_string.length() - 1);
		//System.out.println(sssp_string);
		Map<Integer, Integer> sssp = new HashMap<>();
		String[] sssp_array = sssp_string.split(", ");
		for(String i: sssp_array) {
			String[] this_array = i.split("=");
			int to = Integer.parseInt(this_array[0]);
			int to_minest_dis = Integer.parseInt(this_array[1]);
			System.out.println(to + " : " + to_minest_dis);
			//解析json成功
		}
		
		int[][] aa = new int[2][3];
		for(int i = 0; i < 3; ++i)
			aa[0][i] = 6;
		
		for(int i = 0; i < 3; ++i)
			System.out.println(aa[1][i]);
		aa[1] = aa[0];//浅复制
		for(int i = 0; i < 3; ++i)
			System.out.println(aa[1][i]); //复制有效
		
		UndirectedGraph<String, DefaultEdge> myGraph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
		myGraph.addVertex("1");
		myGraph.addVertex("2");
		myGraph.addEdge("1", "2");
		System.out.println(myGraph.containsEdge("1", "2"));
		System.out.println(myGraph.containsEdge("2", "1"));
		
		
		
		
		
	}
	

}
