package algorithms;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DefaultTeam {
	public static void main(String[] args) {
		Object[] ret100 = test(1,100, 50);
		Object[] ret500 = test(1,500, 50);
		Object[] ret1000 = test(1,1000, 50);
		Object[] ret5000 = test(1,5000, 50);
		Object[] ret10000 = test(1,10000, 50);
		
		Object[] ret2100 = test(2,100, 5);
		Object[] ret2500 = test(2,500, 25);
		Object[] ret21000 = test(2,1000, 50);
		Object[] ret25000 = test(2,5000, 250);
		Object[] ret210000 = test(2,10000, 500);
		
		System.out.println("1 - 100 points - Average size : " + ret100[0] + " points - Average time : " + ret100[1] + " s");
		
		System.out.println("1 - 500 points - Average size : " + ret500[0] + " points - Average time : " + ret500[1] + " s");

		System.out.println("1 - 1000 points - Average size : " + ret1000[0] + " points - Average time : " + ret1000[1] + " s");
		
		System.out.println("1 - 5000 points - Average size : " + ret5000[0] + " points - Average time : " + ret5000[1] + " s");

		System.out.println("1 - 10000 points - Average size : " + ret10000[0] + " points - Average time : " + ret10000[1] + " s");
		
		
		System.out.println("2 - 100 points - Average size : " + ret2100[0] + " points - Average time : " + ret2100[1] + " s");
		
		System.out.println("2 - 500 points - Average size : " + ret2500[0] + " points - Average time : " + ret2500[1] + " s");

		System.out.println("2 - 1000 points - Average size : " + ret21000[0] + " points - Average time : " + ret21000[1] + " s");
		
		System.out.println("2 - 5000 points - Average size : " + ret25000[0] + " points - Average time : " + ret25000[1] + " s");

		System.out.println("2 - 10000 points - Average size : " + ret210000[0] + " points - Average time : " + ret210000[1] + " s");
	}
	
	public static Object[] test(int id, int nb, int edgeTreshold) {
		DefaultTeam d = new DefaultTeam();
		int[] sizes = new int[100];
		double[] times = new double[100];
		for(int i=0; i<100; i++) {
			System.out.println("Test "+i);
			ArrayList<Point> points = d.readFromFile("tests"+id+"-"+nb+"/test"+(i+1)+".points");
			long time1 = System.currentTimeMillis();
			ArrayList<Point> pts = d.calculConnectedDominatingSet(points, edgeTreshold);
			long time2 = System.currentTimeMillis();
			times[i] = (time2-time1);
			sizes[i] = pts.size();
			System.out.println("END - size : " + pts.size() + " - time : " + times[i]/1000 + " s");
		}
		int avsize = 0;
		for(int s : sizes) avsize+=s;
		avsize/=100;
		double avtime = 0;
		for(double t : times) avtime+=t;
		avtime/=100.0*1000.0;
		System.out.println("Average size : " + avsize + " points - Average time : " + avtime + " s");
		return new Object[]{avsize, avtime};
	}
	
	public ArrayList<Point> calculConnectedDominatingSet(ArrayList<Point> points, int edgeThreshold) {

		ArrayList<VertexDS> graph = new ArrayList<VertexDS>();

		for(int i=0; i<points.size(); i++) {
			graph.add(new VertexDS(points.get(i), i));
		}
		for(VertexDS v : graph) {
			for(VertexDS v2 : graph) {
				if(v==v2) continue;
				if(v.p.distance(v2.p) < edgeThreshold) {
					v.neighbors.add(v2);
				}
			}
		}
		// START

		//CALCUL MIS
		ArrayList<VertexDS> rest = (ArrayList<VertexDS>) graph.clone();
		ArrayList<VertexDS> MIS = new ArrayList<VertexDS>();
		while(!rest.isEmpty()) {
			VertexDS v = rest.remove(0);
			MIS.add(v);
			rest.removeAll(v.neighbors);
		}

		//BLACK COMPONENTS
		for(VertexDS vi : MIS) {
			vi.color = Color.BLACK;
			vi.makeBBComp();
		}

		//GRAY NODES
		ArrayList<VertexDS> grayNodes = (ArrayList<VertexDS>) graph.clone();
		grayNodes.removeAll(MIS);
		//Collections.shuffle(grayNodes); TEST

		//TODO sort graynodes degree ++>--
		//grayNodes.sort((x1,x2) -> x1.degree() - x2.degree());

		for(int i=5; i>=2; i--) {
			boolean cont = true;
			System.out.println(i);
			while(cont) {
				int newblueId = -1;
				for(int j=0; j<grayNodes.size(); j++) {
					VertexDS vgray = grayNodes.get(j);
					if(vgray.degree()<i) continue;

					ArrayList<VertexDS> blackneighbors = new ArrayList<VertexDS>();
					for(VertexDS vgraynei : vgray.neighbors) 
						if(vgraynei.color == Color.BLACK) blackneighbors.add(vgraynei);
					if(blackneighbors.size()<i) continue;

					ArrayList<DisjointSetElement<VertexDS>> neibbcompsroots = new ArrayList<DisjointSetElement<VertexDS>>();
					for(VertexDS blackn : blackneighbors) {
						neibbcompsroots.add(blackn.bbcomp.find());
					}

					//TODO utiliser directement les indices
					HashSet<Integer> idsbbcomps = new HashSet<Integer>();
					for(DisjointSetElement<VertexDS> rootneibbcomp : neibbcompsroots) {
						idsbbcomps.add(rootneibbcomp.index);
					}
					if(idsbbcomps.size()<i) continue;

					vgray.color = Color.BLUE;
					newblueId = j;
					//TODO optimisable en utilisant rootneibbcomp
					for(int id=1; id<neibbcompsroots.size(); id++) {
						blackneighbors.get(0).bbcomp.union(blackneighbors.get(id).bbcomp);
					}
					break;
				}
				if(newblueId != -1)
					grayNodes.remove(newblueId);
				else
					cont = false;
			}

		}

		ArrayList<Point> result = new ArrayList<Point>(); 
		for(VertexDS v : graph) {
			if(v.color.equals(Color.BLUE)) result.add(v.p);
			if(v.color.equals(Color.BLACK)) result.add(v.p);
		}

		return result;
	}


	//FILE PRINTER
	private void saveToFile(String filename,ArrayList<Point> result){
		int index=0;
		try {
			while(true){
				BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename+Integer.toString(index)+".points")));
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("I/O exception: unable to close "+filename+Integer.toString(index)+".points");
				}
				index++;
			}
		} catch (FileNotFoundException e) {
			printToFile(filename+Integer.toString(index)+".points",result);
		}
	}
	private void printToFile(String filename,ArrayList<Point> points){
		try {
			PrintStream output = new PrintStream(new FileOutputStream(filename));
			int x,y;
			for (Point p:points) output.println(Integer.toString((int)p.getX())+" "+Integer.toString((int)p.getY()));
			output.close();
		} catch (FileNotFoundException e) {
			System.err.println("I/O exception: unable to create "+filename);
		}
	}

	//FILE LOADER
	private ArrayList<Point> readFromFile(String filename) {
		String line;
		String[] coordinates;
		ArrayList<Point> points=new ArrayList<Point>();
		try {
			BufferedReader input = new BufferedReader(
					new InputStreamReader(new FileInputStream(filename))
					);
			try {
				while ((line=input.readLine())!=null) {
					coordinates=line.split("\\s+");
					points.add(new Point(Integer.parseInt(coordinates[0]),
							Integer.parseInt(coordinates[1])));
				}
			} catch (IOException e) {
				System.err.println("Exception: interrupted I/O.");
			} finally {
				try {
					input.close();
				} catch (IOException e) {
					System.err.println("I/O exception: unable to close "+filename);
				}
			}
		} catch (FileNotFoundException e) {
			System.err.println("Input file not found.");
		}
		return points;
	}
}
