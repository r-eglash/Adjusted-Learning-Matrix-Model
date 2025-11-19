import java.io.*;
import java.util.*;

/**
 * Pokemon Battle Decision Matrix System
 * - Input: 4 battle metrics (normalized to [-1, 1])
 * - Transformation Matrix: 3x4 weights
 * - Output: 3 tactical recommendations
 * 
 * Parallels to ALMM:
 * ALMM Input: [independence, confidence, persistence, accuracy]
 * This Input: [HP advantage, offensive power, defensive strength, speed advantage]
 * 
 * ALMM Output: [difficulty, support, pacing]
 * This Output: [aggression, defense, speed priority]
 */

/**
 * This class stores Pokemon stats from CSV
 * Each Pokemon has 6 core battle statistics
 */
class Pokemon {
    String name;      // Pokemon name
    int hp;           // Hit points (health)
    int attack;       // Physical attack power
    int defense;      // Physical defense
    int spAtk;        // Special attack power
    int spDef;        // Special defense
    int speed;        // Speed (determines turn order)
    
    /**
     * Constructor to create a Pokemon with all stats
     * @param name Pokemon's name
     * @param hp Hit points
     * @param attack Physical attack stat
     * @param defense Physical defense stat
     * @param spAtk Special attack stat
     * @param spDef Special defense stat
     * @param speed Speed stat
     */
    public Pokemon(String name, int hp, int attack, int defense, int spAtk, int spDef, int speed) {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.spAtk = spAtk;
        this.spDef = spDef;
        this.speed = speed;
    }
    
    /**
     * Returns formatted string representation of Pokemon and its stats
     * @return String with name and all stats
     */
    public String toString() {
        return String.format("%s (HP:%d Atk:%d Def:%d SpA:%d SpD:%d Spd:%d)", 
            name, hp, attack, defense, spAtk, spDef, speed);
    }
}

/**
 * This class represents the 4 input metrics for a battle
 * These are the "x" values in the equation y = Ax
 * All values are on a 0-10 scale before normalization
 * 
 * Parallels to ALMM input metrics:
 * ALMM: independence, confidence, persistence, accuracy
 * This: hpAdvantage, offensivePower, defensiveStrength, speedAdvantage
 */
class BattleMetrics {
    double hpAdvantage;      // Player HP vs Enemy HP (0-10)
    double offensivePower;   // Player Attack vs Enemy Defense (0-10)
    double defensiveStrength;// Player Defense vs Enemy Attack (0-10)
    double speedAdvantage;   // Player Speed vs Enemy Speed (0-10)
    
    /**
     * Constructor for BattleMetrics
     * @param hp HP advantage value (0-10)
     * @param offense Offensive power value (0-10)
     * @param defense Defensive strength value (0-10)
     * @param speed Speed advantage value (0-10)
     */
    public BattleMetrics(double hp, double offense, double defense, double speed) {
        this.hpAdvantage = hp;
        this.offensivePower = offense;
        this.defensiveStrength = defense;
        this.speedAdvantage = speed;
    }
    
    /**
     * Normalize metrics to [-1, 1] range (same as ALMM normalization)
     * Formula: (value - 5) / 5.0
     * This centers the 0-10 scale around 0:
     *  0 maps to -1 (worst)
     *  5 maps to 0 (neutral)
     *  10 maps to 1 (best)
     * @return 4x1 matrix with normalized values
     */
    public double[][] getNormalizedMatrix() {
        return new double[][] {
            {(hpAdvantage - 5) / 5.0},
            {(offensivePower - 5) / 5.0},
            {(defensiveStrength - 5) / 5.0},
            {(speedAdvantage - 5) / 5.0}
        };
    }
}

/**
 * This class uses linear algebra to make tactical battle decisions.
 * Structure: y = Ax (identical in structure to ALMM)
 *  x: 4x1 input matrix (battle metrics)
 *  A: 3x4 transformation matrix (weights)
 *  y: 3x1 output matrix (tactical decisions)
 */
public class PokemonBattleDecisionMatrix {
    
    /**
     * Transformation Matrix (3x4)
     * The "A" in y = Ax (smilar to ALMM's transformation matrix)
     * Rows represent output decisions:
     *   Row 0: Aggression Level
     *   Row 1: Defensive Stance
     *   Row 2: Speed Priority
     * Columns represent input metrics:
     *   Col 0: HP Advantage weight
     *   Col 1: Offensive Power weight
     *   Col 2: Defensive Strength weight
     *   Col 3: Speed Advantage weight
     * Each weight determines how much that input affects that output.
     * The initial weights will be refined through batch testing.
     */
    private double[][] transformationMatrix;
    
    /**
     * This constructor initializes transformation matrix with default weights
     * These weights are starting values that will be refined through testing
     */
    public PokemonBattleDecisionMatrix() {
        // Initial weights (will be refined through testing)
        this.transformationMatrix = new double[][] {
            {0.20, 0.45, 0.15, 0.20},  // Aggression: emphasizes offensive power
            {0.30, 0.15, 0.40, 0.15},  // Defense: emphasizes defensive strength and HP
            {0.25, 0.20, 0.20, 0.35}   // Speed: emphasizes speed advantage
        };
    }
    
    /**
     * Calculate battle metrics from two Pokemon's stats
     * This converts raw Pokemon stats into the 4 normalized metrics we need
     * @param player The player's Pokemon
     * @param enemy The enemy Pokemon
     * @return BattleMetrics object with 4 calculated values (0-10 scale)
     */
    public static BattleMetrics calculateMetrics(Pokemon player, Pokemon enemy) {
        // 1. HP Advantage (0-10 scale)
        // Higher HP gives advantage
        double maxHP = Math.max(player.hp, enemy.hp);
        double hpRatio = player.hp / maxHP;
        double hpAdvantage = hpRatio * 10;
        
        // 2. Offensive Power: player's attack vs enemy's defense (0-10 scale)
        // Combines physical and special attack, compares to enemy's defenses
        double avgAtk = (player.attack + player.spAtk) / 2.0;
        double avgEnemyDef = (enemy.defense + enemy.spDef) / 2.0;
        double offensiveRatio = avgAtk / (avgEnemyDef + 1);  // +1 prevents division by zero
        double offensivePower = Math.min(offensiveRatio * 3.5, 10);  // Scale and cap at 10
        
        // 3. Defensive Strength: player's defense vs enemy's attack (0-10 scale)
        // Combines physical and special defense, compares to enemy's attacks
        double avgDef = (player.defense + player.spDef) / 2.0;
        double avgEnemyAtk = (enemy.attack + enemy.spAtk) / 2.0;
        double defensiveRatio = avgDef / (avgEnemyAtk + 1);  // +1 prevents division by zero
        double defensiveStrength = Math.min(defensiveRatio * 3.5, 10);  // Scale and cap at 10
        
        // 4. Speed Advantage (0-10 scale)
        // Higher speed means you attack first
        double maxSpeed = Math.max(player.speed, enemy.speed);
        double speedRatio = player.speed / maxSpeed;
        double speedAdvantage = speedRatio * 10;
        
        // Return all 4 metrics in a BattleMetrics object
        return new BattleMetrics(hpAdvantage, offensivePower, defensiveStrength, speedAdvantage);
    }
    
    /**
     * This method performs matrix multiplication which calculates y = Ax
     * (identical to ALMM's matrix multiplication method)
     * @param tMatrix Transformation matrix (3x4)
     * @param inMatrix Input matrix (4x1)
     * @return Result matrix (3x1) containing decision values
     */
    public double[][] matrixMultiply(double[][] tMatrix, double[][] inMatrix) {
        // Get dimensions
        int tRows = tMatrix.length;         // Number of rows in transformation matrix (3)
        int tCols = tMatrix[0].length;      // Number of columns in transformation matrix (4)
        int inCols = inMatrix[0].length;    // Number of columns in input matrix (1)
        
        // Initialize result matrix (3x1)
        double[][] result = new double[tRows][inCols];
        
        // Perform matrix multiplication
        // For each row in transformation matrix
        for (int i = 0; i < tRows; i++) {
            // For each column in input matrix
            for (int j = 0; j < inCols; j++) {
                // Calculate dot product
                for (int k = 0; k < tCols; k++) {
                    result[i][j] += tMatrix[i][k] * inMatrix[k][j];
                }
            }
        }
        
        return result;
    }
    
    /**
     * This method gets the decision matrix (output y = Ax)
     * This is the main calculation that produces tactical recommendations
     * @param metrics BattleMetrics containing the 4 input values
     * @return 3x1 matrix with [aggression, defense, speed priority]
     */
    public double[][] getDecisionMatrix(BattleMetrics metrics) {
        // Get normalized input matrix (4x1 with values in [-1, 1])
        double[][] normalizedInput = metrics.getNormalizedMatrix();
        
        // Multiply: (3x4) * (4x1) = (3x1)
        // This produces the tactical decision values
        return matrixMultiply(transformationMatrix, normalizedInput);
    }
    
    /**
     * This method loads Pokemon data from CSV file
     * It reads Pokemon.csv and parses each line into Pokemon objects
     * CSV Format:
     * "ID","Name","Form","Type1","Type2","Total","HP","Attack","Defense","Sp. Atk","Sp. Def","Speed","Generation"
     * @param filename Path to Pokemon.csv file
     * @return List of Pokemon objects loaded from file
     */
    public static List<Pokemon> loadPokemon(String filename) {
        List<Pokemon> pokemonList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read and skip header line
            String line = br.readLine();
            
            // Read each subsequent line
            while ((line = br.readLine()) != null) {
                // Split by comma (CSV format)
                String[] values = line.split(",");
                
                // Ensure line has all required fields
                if (values.length >= 12) {
                    try {
                        // Parse Pokemon data from CSV columns
                        // Remove quotes if present and trim whitespace
                        String name = values[1].replace("\"", "").trim();
                        int hp = Integer.parseInt(values[5].trim());
                        int attack = Integer.parseInt(values[6].trim());
                        int defense = Integer.parseInt(values[7].trim());
                        int spAtk = Integer.parseInt(values[8].trim());
                        int spDef = Integer.parseInt(values[9].trim());
                        int speed = Integer.parseInt(values[10].trim());
                        
                        // Create and add Pokemon to list
                        pokemonList.add(new Pokemon(name, hp, attack, defense, spAtk, spDef, speed));
                    } catch (NumberFormatException e) {
                        // Skip malformed lines (can't parse integers)
                    }
                }
            }
        } catch (IOException e) {
            // File not found or read error
            System.err.println("Error reading Pokemon.csv: " + e.getMessage());
            System.err.println("\nPlace Pokemon.csv in the same directory as this program.");
        }
        
        return pokemonList;
    }
    
    /**
     * This method prints recommendations based on decision matrix output
     * (similar to ALMM's printRecommendations method)
     * Interprets the numerical output values into readable tactical advice
     * @param decisionMatrix 3x1 matrix with [aggression, defense, speed priority]
     * @param player Player's Pokemon
     * @param enemy Enemy Pokemon
     */
    public static void printRecommendations(double[][] decisionMatrix, Pokemon player, Pokemon enemy) {
        // Extract the three decision values
        double aggression = decisionMatrix[0][0];      // How aggressive to be
        double defense = decisionMatrix[1][0];         // How defensive to be
        double speedPriority = decisionMatrix[2][0];   // How much to prioritize speed
        
        System.out.println("\n=== Battle Recommendations ===\n");
        
        // Print aggression recommendation
        System.out.print("Aggression Level: ");
        printAdjustment(aggression, "increase", "decrease");
        
        // Print defense recommendation
        System.out.print("Defensive Stance: ");
        printAdjustment(defense, "increase", "decrease");
        
        // Print speed priority recommendation
        System.out.print("Speed Priority: ");
        printAdjustment(speedPriority, "prioritize speed", "deprioritize speed");
        
        // Overall strategy recommendation based on all three values
        System.out.println("\nRecommended Strategy: " + determineStrategy(aggression, defense, speedPriority));
    }
    
    /**
     * This method prints a single adjustment recommendation
     * Interprets magnitude and direction of the adjustment value
     * (similar to ALMM's printDifficultyRecommendation, printSupportRecommendation, etc.)
     * @param value The adjustment value (negative = decrease, positive = increase)
     * @param increaseText Text to display for positive values
     * @param decreaseText Text to display for negative values
     */
    private static void printAdjustment(double value, String increaseText, String decreaseText) {
        // Near zero means no change
        if (Math.abs(value) < 0.1) {
            System.out.println("maintain current level");
            return;
        }
        
        // Determine direction
        String direction = value > 0 ? increaseText : decreaseText;
        
        // Determine magnitude based on absolute value
        String magnitude;
        if (Math.abs(value) >= 0.7) {
            magnitude = "significantly";      // Large change needed
        } else if (Math.abs(value) > 0.3) {
            magnitude = "moderately";         // Medium change needed
        } else {
            magnitude = "slightly";           // Small change needed
        }
        
        // Print formatted recommendation with actual value
        System.out.printf("%s %s (%.3f)\n", direction, magnitude, value);
    }
    
    /**
     * This method determines the overall battle strategy based on decision values.
     * It looks at combinations of aggression, defense, and speed to pick best strategy.
     * @param agg Aggression value
     * @param def Defense value
     * @param spd Speed priority value
     * @return String describing recommended overall strategy
     */
    private static String determineStrategy(double agg, double def, double spd) {
        // High aggression + high speed = go for fast sweep
        if (agg > 0.5 && spd > 0.3) return "Fast Offensive Sweep";
        
        // High defense + low aggression = stall/wall strategy
        if (def > 0.5 && agg < 0) return "Defensive Stall";
        
        // High aggression = push forward aggressively
        if (agg > 0.4) return "Aggressive Push";
        
        // High defense = play defensively
        if (def > 0.4) return "Defensive Play";
        
        // High speed priority = focus on speed control
        if (spd > 0.5) return "Speed Control";
        
        // Everything moderate = balanced approach
        return "Balanced Approach";
    }
    
    /**
     * This method prints a matrix in readable format.
     * It displays each row on a separate line with values formatted to 3 decimal places.
     * @param matrix The matrix to print
     */
    public static void printMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            System.out.print("[");
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%.3f", matrix[i][j]);
                if (j < matrix[i].length - 1) System.out.print(", ");
            }
            System.out.println("]");
        }
    }
    
    /**
     * This method runs batch analysis on multiple random battles.
     * This testing feature evaluates if the transformation matrix is balanced.
     * A well-balanced matrix should produce averages near 0 across many random battles,
     * indicating it doesn't favor one strategy over another.
     * @param pokemonList List of all Pokemon to randomly select from
     * @param numTests Number of random battles to simulate
     */
    public void runBatchAnalysis(List<Pokemon> pokemonList, int numTests) {
        System.out.println("\nBatch Analysis");
        System.out.println("Running " + numTests + " random battles to test transformation matrix\n");
        
        Random rand = new Random();
        
        // Accumulators for averaging
        double sumAgg = 0;   // Sum of all aggression values
        double sumDef = 0;   // Sum of all defense values
        double sumSpd = 0;   // Sum of all speed priority values
        
        // Run specified number of random battles
        for (int i = 0; i < numTests; i++) {
            // Select two random Pokemon
            Pokemon p1 = pokemonList.get(rand.nextInt(pokemonList.size()));
            Pokemon p2 = pokemonList.get(rand.nextInt(pokemonList.size()));
            
            // Calculate battle metrics and decision matrix
            BattleMetrics metrics = calculateMetrics(p1, p2);
            double[][] decision = getDecisionMatrix(metrics);
            
            // Accumulate decision values
            sumAgg += decision[0][0];
            sumDef += decision[1][0];
            sumSpd += decision[2][0];
        }
        
        // Print average values
        System.out.printf("Average Aggression:      %.3f\n", sumAgg / numTests);
        System.out.printf("Average Defense:         %.3f\n", sumDef / numTests);
        System.out.printf("Average Speed Priority:  %.3f\n", sumSpd / numTests);
        System.out.println("\nThese averages should be near 0 for a balanced matrix.");
        System.out.println("Large deviations indicate systematic bias in the transformation weights.");
    }
    
    /**
     * This main method provides an interactive menu for testing the decision matrix system.
     * @param args
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\nPokemon Battle Decision Matrix");
        
        // Load Pokemon data from CSV file
        List<Pokemon> pokemon = loadPokemon("/Users/racheleglash/Adjusted-Learning-Matrix-Model/Adjusted-Learning-Matrix-Model/RPG Decision Programs/PokemonBattleDecisionMatrix/Pokemon.csv");
        
        // Check if data loaded successfully
        if (pokemon.isEmpty()) {
            System.out.println("No Pokemon loaded. Please ensure Pokemon.csv is in the same directory.");
            return;
        }
        
        System.out.println("Loaded " + pokemon.size() + " Pokemon!\n");
        
        // Create decision matrix system instance
        PokemonBattleDecisionMatrix system = new PokemonBattleDecisionMatrix();
        
        // Main menu loop
        while (true) {
            System.out.println("\nMENU:");
            System.out.println("1. Analyze specific battle");
            System.out.println("2. View transformation matrix");
            System.out.println("3. Run batch analysis");
            System.out.println("4. Test extreme matchups");
            System.out.println("5. Exit");
            System.out.print("\nChoice: ");
            
            int choice = scanner.nextInt();
            
            if (choice == 1) {
                // OPTION 1: Analyze specific battle
                // Display first 20 Pokemon for selection
                System.out.println("\nFirst 20 Pokemon:");
                for (int i = 0; i < Math.min(20, pokemon.size()); i++) {
                    System.out.println((i+1) + ". " + pokemon.get(i).name);
                }
                
                // Get user's Pokemon choices
                System.out.print("\nChoose your Pokemon (1-" + Math.min(20, pokemon.size()) + "): ");
                int p1 = scanner.nextInt() - 1;
                System.out.print("Choose enemy Pokemon (1-" + Math.min(20, pokemon.size()) + "): ");
                int p2 = scanner.nextInt() - 1;
                
                // Validate choices
                if (p1 >= 0 && p1 < pokemon.size() && p2 >= 0 && p2 < pokemon.size()) {
                    Pokemon player = pokemon.get(p1);
                    Pokemon enemy = pokemon.get(p2);
                    
                    // Display battle matchup
                    System.out.println("\n" + player.name + " vs " + enemy.name);
                    System.out.println("Player: " + player);
                    System.out.println("Enemy:  " + enemy);
                    
                    // Calculate battle metrics
                    BattleMetrics metrics = system.calculateMetrics(player, enemy);
                    
                    // Display input metrics (0-10 scale)
                    System.out.println("\nInput Metrics (0-10 scale):");
                    System.out.printf("HP Advantage:        %.2f\n", metrics.hpAdvantage);
                    System.out.printf("Offensive Power:     %.2f\n", metrics.offensivePower);
                    System.out.printf("Defensive Strength:  %.2f\n", metrics.defensiveStrength);
                    System.out.printf("Speed Advantage:     %.2f\n", metrics.speedAdvantage);
                    
                    // Display normalized input matrix ([-1, 1] scale)
                    System.out.println("\nNormalized Input Matrix:");
                    printMatrix(metrics.getNormalizedMatrix());
                    
                    // Calculate and display decision matrix
                    double[][] decision = system.getDecisionMatrix(metrics);
                    System.out.println("\nDecision Matrix Output:");
                    printMatrix(decision);
                    
                    // Print readable recommendations
                    printRecommendations(decision, player, enemy);
                }
                
            } else if (choice == 2) {
                // OPTION 2: View transformation matrix
                System.out.println("\nTransformation Matrix (3x4):");
                System.out.println("Rows: [Aggression, Defense, Speed Priority]");
                System.out.println("Cols: [HP Adv, Offensive, Defensive, Speed Adv]\n");
                printMatrix(system.transformationMatrix);
                
            } else if (choice == 3) {
                // OPTION 3: Run batch analysis
                System.out.print("\nNumber of battles to simulate: ");
                int numTests = scanner.nextInt();
                system.runBatchAnalysis(pokemon, numTests);
                
            } else if (choice == 4) {
                // OPTION 4: Test extreme matchups
                // Find Pokemon with extreme stats for edge case testing
                System.out.println("\nTesting Extreme Matchups:\n");
                
                // Find strongest attacker (highest combined attack stats)
                Pokemon strongest = pokemon.stream()
                    .max((a, b) -> Integer.compare(a.attack + a.spAtk, b.attack + b.spAtk))
                    .orElse(pokemon.get(0));
                
                // Find tankiest (highest combined defensive stats + HP)
                Pokemon tankiest = pokemon.stream()
                    .max((a, b) -> Integer.compare(a.defense + a.spDef + a.hp, b.defense + b.spDef + b.hp))
                    .orElse(pokemon.get(0));
                
                // Find fastest
                Pokemon fastest = pokemon.stream()
                    .max((a, b) -> Integer.compare(a.speed, b.speed))
                    .orElse(pokemon.get(0));
                
                // Test 1: Strongest attacker vs tankiest defender
                System.out.println("Test 1: Strongest Attacker vs Tankiest");
                BattleMetrics m1 = system.calculateMetrics(strongest, tankiest);
                printRecommendations(system.getDecisionMatrix(m1), strongest, tankiest);
                
                // Test 2: Fastest vs slowest
                System.out.println("\n\nTest 2: Fastest vs Slowest");
                Pokemon slowest = pokemon.stream()
                    .min((a, b) -> Integer.compare(a.speed, b.speed))
                    .orElse(pokemon.get(0));
                BattleMetrics m2 = system.calculateMetrics(fastest, slowest);
                printRecommendations(system.getDecisionMatrix(m2), fastest, slowest);
                
            } else if (choice == 5) {
                // OPTION 5: Exit
                System.out.println("\nThanks for using the Battle Decision Matrix!");
                break;
            }
        }
        
        scanner.close();
    }
}