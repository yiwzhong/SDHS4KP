
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;


/**
 * 
 * @author yiwen zhong
 *
 */
public class Methods {
	
	/**
	 * Simplified Harmony search, 
	 * 
	 * 
	 * @param HMS  HARMONY MEMORY SIZE
	 * @param MET  MAX EVALUATION TIMES
	 * @return
	 */
	public static Solution simplifiedHarmonySearch(final int HMS, final int MET) {
		Problems problem = Problems.getProblem();
		Solution current = new Solution(true);
		Solution best = new Solution(current);
		int itemNumber = current.getItemNumber();
		int[][] x = new int[HMS][itemNumber]; 
		double[] values = new double[HMS]; 
		PriorityQueue<Harmony> harmonys = new PriorityQueue<>();
		double[] bvs = new double[MET];
		double bestValue = 0;
		int wIdx = 0; //index of worst solution
		for (int i = 0; i < x.length; i++) {
			for (int j = 0; j < x[i].length; j++) {
				x[i][j] = Methods.rand.nextDouble() > 0.5? 1 : 0;
			}
			Solution s = current.map(x[i], 1.0);
			values[i] = s.getValue();
			if ( values[i] > bestValue) {
				bestValue = values[i];
				best = s;
			}
			if (values[i] < values[wIdx]) {
				wIdx = i;
			}
			harmonys.add(new Harmony(values[i], i));
		}
		
		int[] naturalOrder = new int[itemNumber];
		for (int i = 0; i < itemNumber; i++) {
			naturalOrder[i] = i;
		}

		int met = 0;
		while (met < MET) {
			//improvisation a new solution y
			bvs[met] = bestValue;//entropy(x);//bestValue;
			met++;
			int[] y = new int[itemNumber];		
			double yValue = 0; 
			double yWeight = 0;
			int[] itemOrder = problem.getDensityOrder();
			if (Simulations.considerOrderType == EConsiderOrderType.NATURAL) {
				itemOrder = naturalOrder;
			}
			for (int item : itemOrder) {
				if (yWeight + problem.getItemWeight(item) <= problem.getCapacity()) {
					int i = Methods.rand.nextInt(HMS);
					y[item] = x[i][item];
					yValue += y[item] * problem.getItemValue(item);
					yWeight += y[item] * problem.getItemWeight(item); 
				} else {
					y[item] = 0;
				}
			}
			
			//pitch adjusting
			if (Simulations.pitchOrderType == EPitchOrderType.PROFIT) {
		        itemOrder = problem.getValueOrder();
			} else {
				itemOrder = problem.getDensityOrder();
			}
			for (int item : itemOrder) {
				if (y[item] == 0 && 
						yWeight + problem.getItemWeight(item) <= problem.getCapacity()) {
					y[item] = 1;
					yValue += problem.getItemValue(item);
					yWeight += problem.getItemWeight(item); 
				} 
			}
			
			if ( yValue > bestValue) {
				bestValue = yValue;
				best = current.map(y, 0);
				best.setLastImproving(met);
			}

            if (yValue > values[wIdx]) {
				x[wIdx] = y; // y.clone();
				values[wIdx] = yValue;
				harmonys.poll();
				harmonys.add(new Harmony(values[wIdx], wIdx));
				wIdx = harmonys.peek().index;
            }
			
//			if (best.getValue() >= Problems.getProblem().getBestValue()) {
//				return best;
//			}
		}//for each iteration
		//System.out.println(values[wIdx]);
		if (Simulations.SAVING_PROCESS_DATA) {
			saveConvergenceData(bvs);
		}
		return best;
	}
	
	private static double entropy(int[][] hm) {
		double total = hm.length;
		double en = 0;
		double log2 = 1.0/Math.log(2);
		for (int j = 0; j < hm[0].length; j++) {
			int[] count = new int[2];
			for (int r = 0; r < hm.length; r++) {
				count[hm[r][j]]++;
			}
			for (int i =0; i < count.length; i++) {
				if (count[i]!=0 && count[i]!=hm.length) {
				    double p = count[i]/total;
				    en += -p* Math.log(p)*log2;
				}
			}
		}
		return en/hm[0].length;
	}
	
	
	private static void saveConvergenceData(double[] bvs) {
		try {
			String f = Problems.fileName;
			File file = new File(f);
			f = (new File("")).getAbsolutePath() + "\\results\\Convergence\\" + file.getName();
			f += " " + Simulations.getParaSetting() + " for KP results entropy.csv";
			System.out.println(f);
			PrintWriter printWriter = new PrintWriter(new FileWriter(f));
			for (int idx=0; idx<bvs.length; idx++) {
				printWriter.println((idx+1) + ","+ bvs[idx]);
			}
			printWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private static Random rand = new Random();
}

class Harmony implements Comparable<Harmony>{
	public double value;
	public int index;
	
	public Harmony(double v, int i) {
		value = v;
		index = i;

    }
	
	@Override
	public int compareTo(Harmony o) {
		if (this.value > o.value) {
			return 1;
		} else if (this.value < o.value) { 
			return -1;
		} else {
			return 0;
		}
	}
}
