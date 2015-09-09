import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Random;

/* This class will perform the simulation given the parameters. These are more explained in the README.
 * In this Javadoc we try to give our reader a better understanding on the mechanisms we use.*/
public class Simulation {

	int number_features;
	int computation_cost;
	int random_move_cost;
	double Pr;	 //probability of random move
	HashMap<String,Double> graph;
	int MAX_TRIES;
	int restart_parameter;	//commonly MAX_FLIPS
	double restart_probability;
	Random randomGen;
	double p_init;
	int delta;
	int mu;
	Graph g = null;
	double maximum_score;
	
	public Simulation(int computation_cost,int random_move_cost,double Pr, int number_features,int MAX_TRIES,int restart,int mu,int delta,double p_init){
		this.computation_cost = computation_cost;
		this.random_move_cost =random_move_cost;
		this.Pr=Pr;
		this.number_features = number_features;
		//g = new DeceptiveMC(number_features,mu, delta);
		g = new FeatureSelection(number_features);
		maximum_score = g.get_max_score();
		graph = g.get_scores();
		this.MAX_TRIES=MAX_TRIES;
		restart_parameter=restart;
		restart_probability = 1/((double) restart);
		randomGen = new Random();
		this.p_init = p_init;
		this.delta=delta;
		this.mu=mu;
	}
	
	/* Initialization procedure: binomial initialization, it takes as argument
	 * p_init which is the probability to correctly initialize one bit. */
	
	public String initialization(){
		String result = new String();
		for(int i=0; i<number_features;++i){
			int alpha = randomGen.nextInt(100)+1;
			Integer r = null;
			if(alpha<=p_init*100){
				r=new Integer(1);
			}
			else {
				r=new Integer(0);
			}
			result = result.concat(r.toString());
		}
		
		return result;
	}
	
	public HashMap<String,Double> getGraph(){
		return graph;
	}
	/* This method operates the bit flipping. It can be seen as the edges of our graph*/
	public static String flipBit(String old, int index){
		char replace;
		if (old == null){System.out.println("error Index");}
		
		if (old.charAt(index) == '0') {
			 replace = '1';
		}
		else {
			replace ='0';
		}
		String result = old.substring(0, index)+replace+old.substring(index+1);
		return result;
	}
	
	/* In case of greedy search, it determines the best neighbor but calculating every score around*/
	public String bestNeighbor(String old){
		double best_score=0;
		String best_neighbor=null;
		for (int i = 0; i< number_features;++i){
			String neighbor =flipBit(old,i);
			double score = graph.get(neighbor);
			if (score > best_score) {
				best_score=score;
				best_neighbor=neighbor;
			}
	
		}
		if (best_score > graph.get(old)){
		return best_neighbor;}
		else {
			return old;
		}
	}
	
	public boolean converged(double score){
		return (score >= this.maximum_score);
	}
	
	public int Stochastic_Local_Search(){
		int t=0;
		String best= null;
		double best_score = 0;
		int hitting_time=0;
		while(t<MAX_TRIES){
			String current = initialization();
			double init_score = graph.get(current);
			hitting_time+=random_move_cost;					//each time we compute to see the score, it is a ML computation (we omit the repetition which can be avoided using an appropriate data structure)
			
			if (init_score>=best_score){
				best=current;
				best_score=init_score;
			}
			if (converged(best_score)){
				return hitting_time;
			}
			int restart = 1;
			while (restart<restart_parameter){
				//We assume that Pr has two numbers after the comma, no reason to be more precise than that
				Integer random = randomGen.nextInt(100)+1;
				if (random <= (Pr*100)) { //random move
					int flipped_bit = randomGen.nextInt(number_features);
					current = flipBit(current,flipped_bit);
					hitting_time+=random_move_cost;
					if (graph.get(current)>best_score){
						best = current;
						best_score= graph.get(current);
					}
				}
				else {			//greedy move
					String temp=bestNeighbor(current);
					hitting_time+=computation_cost;
					if(temp != null){
						current = temp;
					}
				
					if (graph.get(current) > best_score){
						best_score=graph.get(current);
						best=current;
					}
				}
				restart++;
				if (converged(best_score)){
					return hitting_time;
				}
			}
			t++;
		}
		return hitting_time;
	}
	
	
	public int Hoos(double gamma){
		int t=0;
		String best= null;
		double best_score = 0;
		int hitting_time=0;
		while(t<MAX_TRIES){
			String current = initialization();
			double init_score = graph.get(current);
			hitting_time+=random_move_cost;					//each time we compute to see the score, it is a ML computation (we omit the repetition which can be avoided using an appropriate data structure)
			
			if (init_score>=best_score){
				best=current;
				best_score=init_score;
			}
			if (converged(best_score)){
				return hitting_time;
			}
			// reinitialize noise
			this.Pr = 0;
			int restart = 0;
			while (restart<restart_parameter){
				//We assume that Pr has two numbers after the comma, no reason to be more precise than that
				Integer random = randomGen.nextInt(100)+1;
				if (random <= (Pr*100)) { //random move
					int flipped_bit = randomGen.nextInt(number_features);
					current = flipBit(current,flipped_bit);
					hitting_time+=random_move_cost;
					if (graph.get(current)>best_score){
						best = current;
						best_score= graph.get(current);
					}
					// Decreasing adaptive noise prob
					Pr= Pr - Pr * gamma/2;
					
				}
				else {			//greedy move
					String temp=bestNeighbor(current);
					hitting_time+=computation_cost;
					if(current.equals(temp)){ 
						// Stagnation
						Pr=Pr + (1-Pr)*gamma;
					}
					else {
						// We moved so it drops
						Pr= Pr - Pr * gamma/2;
					}
					
					//Update current
					if(temp != null){
						current = temp;
					}
				
					if (graph.get(current) > best_score){
						best_score=graph.get(current);
						best=current;
					}
				}
				restart++;
				if (converged(best_score)){
					return hitting_time;
				}
			}
			t++;
		}
		return hitting_time;
	}
	
	public int Simulated_Annealing(double initial_temp, double temp_change){
		int t=0;
		String best= null;
		Double best_score= new Double(0);
		int hitting_time=0;
		double temp = initial_temp;
		Random r = new Random();
		while(t<MAX_TRIES){
			String current = initialization();
			Double init_score = graph.get(current);
			hitting_time+=random_move_cost;					//each time we compute to see the score, it is a ML computation (we omit the repetition which can be avoided using an appropriate data structure)
			if (init_score>=best_score){
				best=current;
				best_score=init_score;
			}
			if (converged(best_score)){
				return hitting_time;
			}
			int restart = 0;
			while (restart<restart_parameter){
				// Compute temperature and pick random neighbor
				int flipped_bit = randomGen.nextInt(number_features);
				String candidate = flipBit(current,flipped_bit);
				Double cand_score = graph.get(candidate);
				temp = temp * temp_change;
				//We assume that Pr has two numbers after the comma, no reason to be more precise than that
				double random_choice = r.nextDouble();
				// If candidate is better go for a greedy move otherwise 
				if (cand_score > graph.get(current)) { 
					current = candidate;
					hitting_time+=computation_cost;
					if (cand_score > best_score){
						best = candidate;
						best_score= cand_score;
					}
				}
				else if (Math.exp((graph.get(current)-cand_score)/temp) > random_choice){
					// check whether to make random move
					current = candidate;
					hitting_time+=computation_cost;
				}
				restart++;
				if (converged(best_score)){
					return hitting_time;
				}
			}
			t++;
		}
		return hitting_time;
	}
	
	
	public int Soft_SLS(){
		int t=0;
		String best= null;
		double best_score = 0;
		int hitting_time = 0;
		
		while(t<MAX_TRIES){
			String current = initialization();
			double init_score = graph.get(current);
			hitting_time+=random_move_cost;		//the initialization can be seen as a first try, considering it as random we give it that "cost"
			if (init_score>=best_score){
				best=current;
				best_score=init_score;
			}
			if (converged(best_score)){
				return hitting_time;
			}
			
			boolean restart = false;
			while (!restart){
				
				Integer random = 0;
				//determine if we restart or not
				random = randomGen.nextInt(1000)+1;
				//again we round the probability to the percentile accuracy
				if (random <= restart_probability*1000) {
					restart=true;
				}
				else {
					random = randomGen.nextInt(100)+1;
					
					if (random <= (Pr*100)) { //Random move
						int flipped_bit = randomGen.nextInt(number_features);
						current = flipBit(current,flipped_bit);
						hitting_time+=random_move_cost;
						if (graph.get(current)>best_score){
							best = current;
							best_score= graph.get(current);
						}
					}
					else {			//greedy move
						String temp=bestNeighbor(current);
						hitting_time+=computation_cost;
						if(temp != null){
							current = temp;
						}
						if (graph.get(current) > best_score){
							best_score=graph.get(current);
							best=current;
						}
					}
				}
				if (hitting_time>=15000){
					return hitting_time;
				}
				if (converged(best_score)){
					return hitting_time;
				}
			}
			t++;
		}
		return hitting_time;
	}
	
	public int AdaptiveSLS(double gamma, double gamma_restart){

		double table_restart_prob[] = new double[8000];
		double table_noise_prob[] = new double[8000];
		double advancement[]=new double[8000]; 
		int location[]=new int[8000];
		
		double theta = 1/6;

		int t=0;
		String best= null;
		double best_score = 0;
		int hitting_time = 0;

		while(t<MAX_TRIES){
			String current = initialization();
			double init_score = graph.get(current);
			hitting_time+=random_move_cost;		//the initialization can be seen as a first try, considering it as random we give it that "cost"
			
			table_restart_prob[hitting_time-1]=this.restart_probability;
			table_noise_prob[hitting_time-1]=this.Pr;
			advancement[hitting_time-1]=this.graph.get(current);
			location[hitting_time-1]=g.contain_1bits(current);
			
			if (init_score>=best_score){
				best=current;
				best_score=init_score;
			}
			if (converged(best_score)){
				return hitting_time;
			}
			
			boolean restart = false;
			while (!restart){
				Integer random = 0;
				//determine if we restart or not
				random = randomGen.nextInt(1000)+1;
				//again we round the probability to the percentile accuracy
				if (random <= restart_probability*1000) {
					restart=true;
					// Decreasing adaptive restart and noise prob
					Pr= Pr - Pr * gamma/2;
					restart_probability= restart_probability - restart_probability * gamma_restart/2;
				}
				else {
					random = randomGen.nextInt(100)+1;
					
					if (random <= (Pr*100)) { //Random move
						int flipped_bit = randomGen.nextInt(number_features);
						current = flipBit(current,flipped_bit);
						hitting_time+=random_move_cost;
						if (graph.get(current)>best_score){
							best = current;
							best_score= graph.get(current);
							
							// Decreasing adaptive restart and noise prob
							Pr= Pr - Pr * gamma/2;
							restart_probability= restart_probability - restart_probability * gamma_restart/2;
						}
					}
					else {			//greedy move
						String temp=bestNeighbor(current);
						hitting_time+=computation_cost;
						if((temp!= null) && current.equals(temp)){ 
							// Stagnation
							Pr=Pr + (1-Pr)*gamma;
							restart_probability=restart_probability+(1-restart_probability)*gamma_restart;
						}
						else if (Pr>0.1){
							Pr= Pr - Pr * gamma/2;
							restart_probability= restart_probability - restart_probability * gamma_restart/2;
						}
						
						
						if(temp != null){
							current = temp;
						}
					
					
						if (graph.get(current) > best_score){
							best_score=graph.get(current);
							best=current;
						}
					}
				}
				// Keeping track of evolution of parameters
				table_restart_prob[hitting_time-1]=this.restart_probability;
				table_noise_prob[hitting_time-1]=this.Pr;
				advancement[hitting_time-1]=this.graph.get(current);
				location[hitting_time-1]=g.contain_1bits(current);
				
				if (hitting_time>=15000){
					return hitting_time;
				}
				if (converged(best_score)){
					// Converged
					PrintWriter writer;
					// Writing evolution of parameters
					try {
						writer= new PrintWriter("results", "UTF-8");
						writer.printf("for parameters (%f, %f), delta=%d, mu=%d, we have:\n",gamma,gamma_restart,delta,mu);
						writer.printf("restart evolution:\n");
						for(int param=0;param<500;param++){
									writer.print(table_restart_prob[param]);
									writer.print("  ");
							
						}
						
						writer.printf("\n\n\n");
						writer.printf("noise evolution:\n");
						for(int param=0;param<500;param++){
							writer.print(table_noise_prob[param]);
							writer.print("  ");
					
						}
						writer.printf("\n\n\n");
						writer.printf("Advancement:\n");
						for(int param=0;param<500;param++){
							writer.print(advancement[param]);
							writer.print("  ");
						}
						writer.printf("\n\n\n");
						writer.printf("Number of bits\n");
						for(int param=0;param<500;param++){
							writer.print(location[param]);
							writer.print("  ");
						}
						writer.printf("\n\n\n");
						
					writer.close();
					}
					catch (Exception e){
						e.printStackTrace();
					}
					
					
					return hitting_time;
				}

			}
			t++;
		}
		return hitting_time;
	}
	
	
	
	/* Here we run the simulation and store them on a (Matlab friendly) text file.
	 * For each argument we want to study, we create a loop and we vary it. For now
	 * this code fixes Pr to its optimal value and loop on MAX_FLIPS, slopes (difficulty),
	 * and the initialization bound. Please note, in the loop, we discovered that Java
	 * has sometimes problems computing small probabilities. The lack of accuracy might
	 * lead to errors that would propagate through the whole loop, so we use integer bounds
	 * and we compute the probability each time.
	 * Once we get our results after averaging 10000 experiments, we store it in a 3D
	 * table that will contain the result for each specific set of parameters. And we print
	 * that table into a file that we'll use to plot on Matlab.
	 * Please note, the commented part helps to compute the variance we have on our experiments.
	 * Please look at the README to know exactly what are our parameters and how are we using them */
	 
	public static void main(String[] args) {
		
		double total_table[][][]=new double[50][10][51];
		
		
		int number_op=1000;
		int f=0;
		int number_features=9;
		int delta=0;
		for(int slope=1;slope<2;slope+=1){
			//We always do loops on bound so that it is more precise. It is sometimes inaccurate when conditioning on a double
			for (int restart_bound = 1 ;restart_bound<=256;restart_bound*=2){
				for (int noise_bound = 0; noise_bound< 51; noise_bound++){				
					double p = ((double)(noise_bound))/50;
					double total_hitting_time =0.0;
					for (int i=0;i<number_op;i++){
						Simulation sim=new Simulation(1,1,p,number_features,15000,restart_bound,slope,delta,0.5);
						double result_op=sim.Soft_SLS();
						total_hitting_time+=result_op;
					}
					// Average running time
					double expected_HT = total_hitting_time/number_op;
					System.out.printf("Pn = %f restart bound = %d, slope =%d \n", p,restart_bound,slope);
					System.out.printf("SoftSLS:%f \n",expected_HT);
					total_table[slope][f][noise_bound] = expected_HT;
				}
				f+=1;	
			}
			f=0;
		}
		
		//writing this big table into a file
		PrintWriter writer;
		try {
			writer= new PrintWriter("results", "UTF-8");
			for(int slope=1;slope<10;slope++){
				for (int restart=0;restart<10;restart++){
					for(int prob=0; prob<51;prob++){
						writer.print(total_table[slope][restart][prob]);
						writer.print("  ");
					}
					writer.printf(";");
					writer.printf("\n");
				}
				writer.printf("\n\n\n");
			}	
		writer.close();
		}
		catch (Exception e){
			e.printStackTrace();
		}		
	}
}
	


