
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author yiwen zhong
 *
 */
public class Simulations {
	
	public static void main(String[] args) {
		String filePath = ""; 
		filePath = (new File("")).getAbsolutePath() + "/../uncorrelated/";//"/../paras/";// "/../stronglycorrelated/";//"/../uncorrelated/";//"/../weaklycorrelated/"; 
		filePath = (new File("")).getAbsolutePath() + "/../kpKong/";
		filePath = (new File("")).getAbsolutePath() + "/../paras/";
		
		if (Simulations.TEST_TYPE == ETestType.SINGLE_INSTANCE) {
			String fileName = filePath+"KP09.txt";
			testSingleInstance(fileName, Simulations.TIMES);
		} else if (Simulations.TEST_TYPE == ETestType.MULTIPLE_INSTANCE) {
			System.out.println("\n"+Simulations.getParaSetting());
			testPerformance(filePath,Simulations.TIMES);
		} else if (Simulations.TEST_TYPE == ETestType.PARAMETER_TUNING_HARMONY_MEMORY_SIZE) {
			System.out.println("\n"+Simulations.getParaSetting());
			parametersTunningHarmonyMemorySize(filePath);
		} else {
			System.out.println("Error test type, Cannot reach here!");
		}
	}
	
	/**
	 * This function is used to fine-tune harmony memory size
	 * 
	 * @param filePath
	 */
	private static void parametersTunningHarmonyMemorySize(String filePath) {
		java.io.File dir = new java.io.File(filePath);
		java.io.File[] files = dir.listFiles();
		String pathName = filePath.substring(filePath.lastIndexOf("/", filePath.length()-2)).substring(1);
		pathName = pathName.substring(0, pathName.length()-1);
		
		String fileName = (new File("")).getAbsolutePath() + "/results/Parameters/";
		fileName += pathName + "-" + Simulations.methodType + "-" + Simulations.considerOrderType + "-" + Simulations.pitchOrderType + "-";
		fileName += "harmony size para tunning results.csv";

		int[] paras = new int[20];
		for (int i = 0; i < paras.length; i++) {
		    paras[i] = 100 + 100 * i;
		}
		List<double[]> resultsList = new ArrayList<>();
		for (File file : files) {
			//if (!file.getName().contains("KP15")) continue;
			for (int i = 0; i < paras.length; i++) {
				Simulations.harmonyMemorySize = paras[i];
				System.out.println("\n"+Simulations.getParaSetting());
				double[] results = testSingleInstance(file.getAbsolutePath(), Simulations.TIMES);
				resultsList.add(results);
                
                Simulations.saveParaTunningResults(fileName, paras, resultsList);
			}			
		}
	}
	

	private static void saveParaTunningResults(String fileName, int[] paras, List<double[]> results ) {
		if (!Simulations.SAVING_PARA_TUNNING) return;
		
		try {
			PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
			for (int idx = 0; idx < results.size(); idx++) {
				double[] rs = results.get(idx);
				printWriter.println();
				printWriter.print(paras[idx%paras.length]);
				for (int j = 0; j < rs.length; j++) {
					printWriter.print(","+rs[j]);
				}
			}
			
			printWriter.println();
			printWriter.println();
			//Number of Instances
			int num = 1 + (results.size()-1) / paras.length;
			for (int i = 0; i < paras.length; i++) {
				printWriter.print(paras[i]);
                for (int j = 0; j < num; j++) {
                	int row = i + paras.length*j;
                	if (row < results.size()) {
                	    double[] rs = results.get(row);
                	    printWriter.print("," + rs[3] + "," + rs[6] + "," + rs[8] + "," + rs[9]);
                	}
                }
                printWriter.println();
			}
			printWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static double[] testSingleInstance(String fileName, final int TIMES) {
		double[] results = runSimulation(fileName, TIMES);
		for (double d : results) {
			System.out.print(d + "\t");
		}
		System.out.println();
		return results;
	}

	private static double[] testPerformance(String filePath, final int TIMES) {
		java.io.File dir = new java.io.File(filePath);
		java.io.File[] files = dir.listFiles();
		String pathName = filePath.substring(filePath.lastIndexOf("/", filePath.length()-2)).substring(1);
		pathName = pathName.substring(0, pathName.length()-1);
		System.out.println(pathName);
		String fileName = (new File("")).getAbsolutePath() + "/results/Performance/" + pathName + "-";
		fileName += Simulations.getParaSetting();
		fileName += " results.csv";

		List<double[]> results = new ArrayList<>();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
//			if (!(file.getName().contains("14") || file.getName().contains("15"))) {
//				Simulations.learnFromBest = 1.0;
//			} else {
//				Simulations.learnFromBest = 0;
//			}
			Simulations.max_eval_time = Simulations.max_eval_times[i % 16]; //for Kong's instance
			double[] result = runSimulation(file.getAbsolutePath(), TIMES);
			results.add(result);
			System.out.println();
			System.out.print(file.getName()+"\t");
			for (double d : result) {
				System.out.print(d+"\t");
			}
			System.out.println();
		    Simulations.saveFinalResults(fileName, files, results);
		}
		
		//calculate statistics results
		double[] totals = new double[results.get(0).length];
		for (int i = 0; i < files.length; i++) {
			System.out.println();
			System.out.print(files[i].getName()+"\t");
			for (int j = 0; j < results.get(i).length; j++) {
				System.out.print(results.get(i)[j]+"\t");
				totals[j] += results.get(i)[j];
			}
		}
		System.out.println("\t");
		for (int j = 0; j < totals.length; j++) {
			totals[j] = Math.round(totals[j]/files.length*1000)/1000.0;
			System.out.print(totals[j]+"\t");
		}
		return totals; //average data for all files
	}
	
	
	private static void saveFinalResults(String fileName, File[] files, List<double[]> results) {
		if (!Simulations.SAVING_FINAL_RESULTS) return;
		
		try {
			PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
			for (int i = 0; i < results.size(); i++) {
				printWriter.println();
				printWriter.print(files[i%files.length].getName());
				for (int j = 0; j < results.get(i).length; j++) {
					printWriter.print(","+results.get(i)[j]);
				}
			}
			printWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static double[] runSimulation(String fileName, final int TIMES) {
		double duration = (new java.util.Date()).getTime();
		Problems.setFileName(fileName);
		double bValue= Problems.getProblem().getBestValue();
		Solution s = null;
		double[] makespans = new double[Simulations.TIMES];
		int[] iterations = new int[Simulations.TIMES]; //last improving iteration
		int met = Simulations.MAX_EVAL_TIMES;
		if (fileName.contains("Kong")) {
			met = Simulations.max_eval_time; 
		} 
		for (int i = 0; i < Simulations.TIMES; i++) {
			if (Simulations.methodType == EMethodType.SIMPLIFIED_HS ) {
				s = Methods.simplifiedHarmonySearch(Simulations.harmonyMemorySize, met); 
			} else {
				System.out.println("Cannot reach here!");;
			}
			makespans[i] = s.getValue();
			iterations[i] = s.getLastImproving();
			if (Simulations.OUT_INDIVIDUAL_RUNNING_DATA) {
				System.out.println( i + " -- " + bValue + "," + makespans[i] + "," + s.getWeight() + "," + Problems.getProblem().getCapacity() +"," + iterations[i]);
				//System.out.println( i + " -- " + bValue + "," + makespans[i] + "," + iterations[i]);
			}
		}
		duration = (new java.util.Date()).getTime()-duration;
		duration /= TIMES;
		duration = Math.round(duration/1000*1000)/1000.0;

		double min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, count = 0;
		double total = 0;
		double totalIterations = 0;
		for (int i = 0; i < Simulations.TIMES; i++) {
			double mk = makespans[i];
			total += mk;
			if ( Math.abs((mk-bValue)) * (1.0/bValue) *100 < 1) {
				count++;
			}
			if ( mk < min) {
				min = mk;
			}
			if (mk > max) {
				max = mk;
			}
			totalIterations += iterations[i];
		}
		double ave = total / Simulations.TIMES;
		double bpd = Math.abs(Math.round((min-bValue)) * (1.0/bValue) *100*1000)/1000.0;
		double wpd = Math.abs(Math.round((max-bValue)) * (1.0/bValue) *100*1000)/1000.0;
		double apd = Math.abs(Math.round((ave-bValue)) * (1.0/bValue) *100*1000)/1000.0;
		double itr = Math.round(totalIterations/iterations.length*10)/10; //average last improving iteration
		//return new double[] {bValue, min, max, ave, bpd, wpd, apd, count, itr, duration};
		return new double[] {bValue, min, max, ave, bValue - min, bValue - max, bValue - ave, count, itr, duration};
	}
	
	public static EMethodType getSaType() { return Simulations.methodType;}
	public static boolean isSavingFinalResults() { return Simulations.SAVING_FINAL_RESULTS;}
	public static boolean isSavingProcessData() { return Simulations.SAVING_PROCESS_DATA;}
	public static String getParaSetting() {
		String str = methodType + "-" + Simulations.considerOrderType + "-" + Simulations.pitchOrderType;
		str += " HMS=" + Simulations.harmonyMemorySize + " MET=" + Simulations.MAX_EVAL_TIMES;
		return str;
	}
	
	private static EMethodType methodType = EMethodType.SIMPLIFIED_HS;
	public static EConsiderOrderType considerOrderType = EConsiderOrderType.HEURISTIC;
	public static EPitchOrderType pitchOrderType = EPitchOrderType.DENSITY;
	public static final int TIMES = 200;
	public static final int MAX_EVAL_TIMES = 400 * 100; //IN LBSA, 200*200
	
	public static int harmonyMemorySize = 600;//600
	
	public static final boolean OUT_INDIVIDUAL_RUNNING_DATA = true;
	public static final boolean SAVING_PROCESS_DATA = false;
	public static final boolean SAVING_FINAL_RESULTS = false;
	public static final boolean SAVING_PARA_TUNNING = true;
	public static final ETestType TEST_TYPE = ETestType.PARAMETER_TUNING_HARMONY_MEMORY_SIZE;
	
    public static int max_eval_time = 15000;

    public static int[] max_eval_times = new int[]{15000, 15000, 20000, 20000, 
          30000, 30000, 40000, 40000, 
          50000, 50000, 60000, 80000, 
          100000, 120000, 150000, 200000}; 

}
