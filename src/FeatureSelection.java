import java.util.HashMap;
import java.util.HashSet;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class FeatureSelection extends Graph {
	
	int slope;
	int delta;

	public FeatureSelection(int number_bits) {
		super(number_bits);
		scores = create_map(number_bits);
	}

	/* This method is the core of the class. It returns an HashMap that we define
	 * as our graph. It works as follows:
	 * Assuming TMC scores distribution (not uniform), we first create all the
	 * nodes by determining all the possible bit strings. Once we have the 
	 * HashMap that contains all the Strings (keys), we need to assign scores.
	 * To do so, we create a scores set that contains the number of bits 
	 * in the bit string + 1 (number of possible states in the TMC model).
	 * using the methods we defined before, we assign to each state its node.
	 * For instance, in case of 5 bits and slope change index 3, state 5 has 
	 * the maximum, then we remove that score from the scores set. And iterate:
	 * 4 gets the remaining maximum. We would do so until we reach the change
	 * slope index which is the minimum. and we continue with minimums.
	 * The scores range from 0 to number of bits in the bit string*/



	private HashMap<String,Double> create_map(int number_bits){
		HashMap<String,Double> result = new HashMap<String,Double>();
		GrayCode.gray("", number_bits, result);
		try{
			FileReader fr = new FileReader("score_featureselection.txt");
        		BufferedReader br = new BufferedReader(fr);
	        	while (br.ready()) {
	            		//System.out.println(br.readLine());
				String[] line = br.readLine().split(" ");	
				String bits = line[0];
				Double max_score = Double.parseDouble(line[1]);
				//System.out.println(bits);
				//System.out.println(max_score);
				result.put(bits, max_score);	
			}
		}catch(Exception e){  
            		System.err.println(e.getMessage());  
        	}
		//System.out.println(result.size());
		return result;
	}
	
	
	/* These methods just return maximums and minimums in a given set to build t */
	protected double max_set(HashSet<Double> set){
		double max=0;
		for(Double i: set){
			if (i> max){
				max=i;
			}
		}
		return max;
	}
	
	protected double min_set(HashSet<Double> set){
		double min=100;
		for(Double i: set){
			if (i < min){
				min=i;
			}
		}
		return min;
	}

}
