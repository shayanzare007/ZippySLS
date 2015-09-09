import java.util.HashMap;
import java.util.HashSet;


public class DeceptiveMC extends Graph {
	
	int slope;
	int delta;

	public DeceptiveMC(int number_bits, int slope, int delta) {
		super(number_bits);
		scores = create_map(number_bits,slope,delta);
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
	
	private HashMap<String,Double> create_map(int number_bits,int slope, int delta){
		HashMap<String,Double> result = new HashMap<String,Double>();
		GrayCode.gray("", number_bits, result);
		
		// Trap Markov chain score with variable delta and mu
		HashSet<Double> possible_scores = new HashSet<Double>();
		for (double i=0;i<= number_bits;++i){
			possible_scores.add(i);		//All possible scores for TMC
		}
		
		double max_score=max_set(possible_scores);
		if (!possible_scores.remove(max_score)){
			System.out.println("Problem when removing Max_score from possible_scores set");
		}
		for(String bits: result.keySet()){
			if(contain_1bits(bits)==number_bits){
				result.put(bits, max_score);
			}
		}
		max_score=max_set(possible_scores);
		if (!possible_scores.remove(max_score)){
			System.out.println("Problem when removing Max_score from possible_scores set");
		}
		for(String bits: result.keySet()){
			if(contain_1bits(bits)==delta){
				result.put(bits, max_score);
			}
		}
		
		
		// from global optimum to mu
		for (int j=number_bits-1; j>slope;j--){
			max_score=max_set(possible_scores);
			if (!possible_scores.remove(max_score)){
				System.out.println("Problem when removing Max_score from possible_scores set");
			}
			for(String bits: result.keySet()){
				if(contain_1bits(bits)==j){
					result.put(bits, max_score);
				}
			}
		}
		// from mu to delta
		for (int k=slope; k>delta;k--){
			double min_score=min_set(possible_scores);
			possible_scores.remove(min_score);
			for(String bits: result.keySet()){
				if(contain_1bits(bits)==k){
					result.put(bits, min_score);
				}
			}
		}
		// from delta to 0
		for(int k=delta-1;k>=0;k--){
			max_score=max_set(possible_scores);
			if (!possible_scores.remove(max_score)){
				System.out.println("Problem when removing Max_score from possible_scores set");
			}
			for(String bits: result.keySet()){
				if(contain_1bits(bits)==k){
					result.put(bits, max_score);
				}
			}
		}
		
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
