
/**
 * Java solution to the Quora Datacenter cooling problem (basically find all hamiltonian paths in a graph)
 * Solution: 301,716 paths. Currently runs at 38.8 seconds on my 2.4 GHz Intel Core 2 Duo
 * 
 * Output: prints out the time at every 10000 solutions, in order to stem impatience
 * 
 * Strategies: I prune partial paths when I find the graph is no longer connected, and I use
 * 		integers to represent a graph, which seems to have achieve a large speedup. Bit manipulation
 * 		allows for quick comparisons as well. 
 * 
 * Constraints: This solution does not work for more than 64 nodes in the graph, because it uses longs
 * 		in order to store the graph. 
 * 
 * @author nv6
 *
 */
public class DatacenterChallenge
{
	public static int[][] GRID;
	public static int WIDTH;
	public static int HEIGHT;
	
	// Will initialize this to be a graph with all of the zeros pre-flagged
	public static long graphWithNoZeroesLeft;
	
	// Running count of the number of solutions
	public static long NUM_PATHS;
	
	// For use in flooding to find connectivity
	public static long FLOODED_PATH = 0;
	
	public static final long ONE = 1;
	
	public static long START_TIME;
	
	// Keep Track of possible movements around a node (for the flood function)
	public static int[] movements = {1, 0, -1, 0, 0, 1, 0, -1};

	
	public static void main(String[] args)
	{
		//Initialize array just so I can visualize it easily		
		/*int[] array = 
	   {2, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0,
		3, 0, 0, 0, 0, 1, 1};
		
		// Initialize everything by hand because I'm not sure you will want to provide input
		WIDTH = 7;
		HEIGHT = 8;
		GRID = new int[HEIGHT][WIDTH];
		GRID[0][0] = 2;
		GRID[7][0] = 3;
		GRID[7][5] = 1;
		GRID[7][6] = 1;		
		*/
		WIDTH = 5;
		HEIGHT = 5;
		GRID = new int[HEIGHT][WIDTH];
		GRID[0][0] = 2;
		GRID[4][4] = 3;
		//GRID[7][5] = 1;
		//GRID[7][6] = 1;		
		
		
		graphWithNoZeroesLeft = 0;
		NUM_PATHS = 0;
		
		// Since I'm using integers to represent the graph, use the index (i) of a node, and flag
		// its position in a long (a 2^64 bit integer) 
		int count = 0;
		
		for(int i = 0; i < HEIGHT; i++)
		{
			for(int j = 0; j < WIDTH; j++)
			{
				if(GRID[i][j] == 0)
					graphWithNoZeroesLeft += ONE << count;
				
				count++;
			}
		}		

		START_TIME = System.currentTimeMillis();
		
		//Begin traversal for this specific graph only!
		traverse(1, 0, 1);
		traverse(1, 1, 0);
		
		System.out.println("Number of paths: " + NUM_PATHS);
		
		System.out.println("Total time taken " + (System.currentTimeMillis() - START_TIME));
	}
	
	/**
	 * Traversal function for finding all of the paths
	 * 
	 * The hamiltonian path can only travel through 0s until a 3 node and never backwards, so make sure
	 * you cannot pass through 1, 2, or go backwards
	 *  
	 * @param pathSoFar  Keeps track of previously visited nodes
	 * @param row        Current row
	 * @param column     Current column
	 */
	public static void traverse(long pathSoFar, int row, int column)
	{
		if(row < 0 || column < 0 || row >= HEIGHT || column >= WIDTH)
		{
			return;
		}
		else if(GRID[row][column] == 1)
		{
			return;
		}
		else if(GRID[row][column] == 2)
		{
			return;
		}
		else if(GRID[row][column] == 3)
		{
			// pathSoFar includes start so subtract to compare with an all zeros graph
			if(pathSoFar - 1 == graphWithNoZeroesLeft)
			{
				NUM_PATHS++;

				if(NUM_PATHS % 10000 == 0)
				{
					System.out.println("Number of paths so far: " + NUM_PATHS + 
							"  and the time so far   " + (System.currentTimeMillis() - START_TIME));
				}
			}
			
			return;
		}
		// Check if the bit for this particular row and column is already high in this graph
		else if(((ONE << (row*WIDTH + column)) & pathSoFar) != 0)
		{
			return;
		}
		// Check if the graph is still connected (every node can still reach every other node)
		else if(isArticulationPoint(pathSoFar, row, column))
		{
			return;
		}

		// Flag this row and column as visited
		pathSoFar += ONE << (row*WIDTH + column);

		// Continue on to the neighbors
		traverse(pathSoFar, row + 1, column);
		traverse(pathSoFar, row - 1, column);
		traverse(pathSoFar, row, column + 1);
		traverse(pathSoFar, row, column - 1);		
	}
	
	/**
	 * This function tells whether a graph is still connected or not. At each node, 
	 * flood all of your neighbors and then check if any zeros (from the original graph) exist
	 * in the the flooded graph. If they do, then this graph is no longer connected
	 * @param pathSoFar
	 * @param row
	 * @param column
	 * @return
	 */
	public static boolean isArticulationPoint(long pathSoFar, int row, int column)
	{
		pathSoFar += ONE << (row*WIDTH + column);

		// Check each neighbor of the current node to see if it is connected to every
		// other node
		for(int i = 0; i < movements.length; i+=2)
		{
			int r = row + movements[i];
			int c = column + movements[i + 1];
			
			FLOODED_PATH = pathSoFar;
			
			flood(r, c);
			
			if(FLOODED_PATH - 1 == graphWithNoZeroesLeft)
				return false;
		}
	
		return true;
	}
	
	/**
	 * Floods a zero in the graph and marks all connected nodes as visited
	 * FLOODED_PATH is a static variable that keeps track of the current path
	 * @param row
	 * @param column
	 */
	public static void flood(int row, int column)
	{
		if(row < 0 || column < 0 || row >= HEIGHT || column >= WIDTH)
		{
			return;
		}
		else if(GRID[row][column] != 0)
		{
			return;
		}
		else if(((ONE << (row*WIDTH + column)) & FLOODED_PATH) != 0)
		{
			return;
		}
		
		FLOODED_PATH += ONE << (row*WIDTH + column);
		
		flood(row + 1, column);
		flood(row - 1, column);
		flood(row, column + 1);
		flood(row, column - 1);
	}
}
