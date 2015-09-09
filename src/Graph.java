import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/* This class creates a graph containing bit strings. In our case a graph i given by a 
 * Map that associates each bit string to a value (score of the features combination).
 * Given the number of bits and slope change index defined by the Trap Markov Chain
 * analysis, it creates an abstract graph. The edges are determined by the bit flipping
 * in the Simulation class. Here we just create the Map and distribute scores accordingly.
 * If we want a completely random scores distribution, there is a commented part in the
 * create_map which would initialize each node with a score between 0 and 19 uniformly
 * and only one node has a 20 score.*/

public class Graph {

	protected HashMap<String, Double> scores=null;
	protected int number_bits;
	
	public Graph(int number_bits){
		this.number_bits = number_bits;
	}
	
	public HashMap<String, Double> get_scores() {
		return scores;
	}
	
	/*This method helps us to determine how many bits at 1 are in the given bit string */
	public int contain_1bits(String s){
		int accu=0;
		for (int i=0; i< number_bits;++i){
			if(s.charAt(i)=='1'){
				accu++;
			}
		}
		return accu;
	}
	
	/* Method to determine the maximum value of our graph*/
	public double get_max_score(){
		double max_tmp = 0;
		for (String s: scores.keySet()){
			if (scores.get(s) > max_tmp){
				max_tmp = scores.get(s);
			}
		}
		return max_tmp;
	}

}
