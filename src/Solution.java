

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author yiwen zhong
 *
 */
public class Solution implements Comparable<Solution> {

	public Solution(boolean initial) {
		problem = Problems.getProblem();
		match = new int[problem.getItemNumber()];
		if ( initial ) { 
			densityList = problem.getDensityList();
			valueList = problem.getValueList();
			this.randPick(); 
		}
	}

	/**
	 * Use parameter to clone a new Solution
	 * 
	 * @param solution
	 */
	public Solution(Solution solution) {
		problem = Problems.getProblem();
		match = solution.match.clone(); 
		lastImproving = solution.lastImproving;
		this.value = solution.value;
		this.weight = solution.weight;
		this.isValid = solution.isValid;
	}
	
	/**
	 * Use parameter to clone a new Solution
	 * 
	 * @param solution
	 */
	public Solution(int[] m) {
		problem = Problems.getProblem();
		match = m.clone(); 
		eval();
		repair(Solution.densityList);
		optimization(Solution.densityList);
	}
	
	
	public Solution map(int[] x, double densityProb) {
		Solution s = new Solution(false);
        s.match = x;//.clone();
		s.eval();
		s.repair(Solution.densityList);
		if (Solution.rand.nextDouble() < densityProb) {
	        s.optimization(Solution.densityList);
		} else {
			s.optimization(Solution.valueList);
		}
		return s;
	}
	
	/**
	 * Use parameter to update this object
	 * 
	 * @param s
	 */
	public void update(Solution s) {
		this.match = s.match.clone();
		this.lastImproving = s.lastImproving;
		this.value = s.value;
		this.isValid = s.isValid;
	}

	/*
	 * This method randomly selects items into knapsack
	 */
	private void randPick() {
		for (int i = 0; i < match.length; i++) {
			if (Solution.rand.nextDouble() < 0.5)  {
				match[i] = 1;
			} else {
				match[i] = 0;
			}
		}
		eval();
		repair(Solution.densityList);
		optimization(Solution.densityList);
	}
	
	public void eval() {
		value = 0;
		weight = 0;
		for (int i=0; i < match.length; i++) {
			if ( match[i] == 1) {
			    value += problem.getItemValue(i);
			    weight += problem.getItemWeight(i);
			} 
		}
		
		//check whether the solution is valid
		isValid = weight <= problem.getCapacity();
	}
	
	public void evalWithGreedyRepair() {
		int[] items = problem.getDensityOrder();
		value = 0;
		weight = 0;
		for (int i = 0; i < items.length; i++) {
			int item = items[i];
			if ( match[item] == 1 &&
					(weight + problem.getItemWeight(item) <= problem.getCapacity())) {
			    value += problem.getItemValue(item);
			    weight += problem.getItemWeight(item);
			} else {
				match[item] = 0;
			}
		}
	}


	/**
	 * repair the solution by dropping items from the package
	 * 
	 * @return
	 */
	private void repair(List<Integer> itemList) {
 		//repair invalid solution
		for(int i = itemList.size()-1; i >= 0 && !isValid; i--) {
			int item = itemList.get(i);
			if (match[item] == 1) {
 			    match[item] = 0;
			    value -= problem.getItemValue(item);
			    weight -= problem.getItemWeight(item);
			    isValid = weight <= problem.getCapacity();
			}
		}

	}
	

	/**
	 * optimization by adding items into knapsack
	 */
	private void optimization(List<Integer> itemList) {
		for (int i = 0; i < itemList.size(); i++) {
			int item = itemList.get(i);
			if (match[item] == 0 && weight + problem.getItemWeight(item) <= problem.getCapacity())  {
				match[item] = 1;
				value += problem.getItemValue(item);
				weight += problem.getItemWeight(item);
			}
		}
	}
	
	

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Solution s) {
		if ( value > s.value) {
			return 1;
		} else if ( value == s.value) {
			return 0;
		} else {
			return -1;
		}
	}

	public String toString() {
		String str = "";

		return str;
	}

	public int getItemNumber() { return match.length; }
	public double getValue() {return value;}
	public double getWeight() {return weight;}
	public void setLastImproving(int n) { this.lastImproving = n; }
	public int getLastImproving() { return lastImproving;}
	public boolean isValid() { return isValid;}
	public int[] getMatch() { return match; }

	private Problems problem;
	private int[] match;
	private double value;
	private boolean isValid;
	private double weight;
	private int lastImproving = 0; //
    private static List<Integer> densityList;
    private static List<Integer> valueList;
	
	public static void main(String[] args) {
		String filePath = (new File("")).getAbsolutePath() + "/../f1-10/"; 
		String fileName = filePath+"f1.txt";
		Problems.setFileName(fileName);
		Solution s = new Solution(true);
		double d = 0;
		for (int i = 0; i < 10; i++) {
			s.randPick();
			System.out.println(s.value);
			d += s.value;
		}
		System.out.println("Best known value:" + Problems.getProblem().getBestValue() + ", Random solution:" + d / 10);
	}

	private static Random rand = new Random();
}

