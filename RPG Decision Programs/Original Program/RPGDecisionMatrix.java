import java.io.*;
import java.util.*;

/**
 * RPG Battle Decision Matrix System
 * Parallels the Adjusted Learning Matrix Model using Pokemon data
 * 
 * Input Matrix (x): Battle state [HP, Threat, Resources, TeamAdvantage]
 * Transformation Matrix (A): 3x4 weighted matrix
 * Output Matrix (y): Tactical decisions [Aggression, Defensiveness, ResourceUsage]
 */

class PokemonCharacter
{
    String name;
    int hp;
    int attack;
    int defense;
    int speed;
    
    public PokemonCharacter(String name, int hp, int attack, int defense, int speed)
    {
        this.name = name;
        this.hp = hp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
    }
    
    public String toString() {
        return String.format("%s (HP:%d, Atk:%d, Def:%d, Spd:%d)", name, hp, attack, defense, speed);
    }
}

class BattleState
{
    double currentHP; // 0-10 scale
    double enemyThreat; // 0-10 scale
    double resourcesRemaining; // 0-10 scale
    double teamAdvantage; // 0-10 scale
    
    public BattleState(double hp, double threat, double resources, double advantage)
    {
        this.currentHP = hp;
        this.enemyThreat = threat;
        this.resourcesRemaining = resources;
        this.teamAdvantage = advantage;
    }
    
    // Normalize to [-1, 1]
    public double[] getNormalizedInputs()
    {
        return new double[]
        {
            (currentHP - 5) / 5,
            (enemyThreat - 5) / 5,
            (resourcesRemaining - 5) / 5,
            (teamAdvantage - 5) / 5
        };
    }
}

class TacticalDecision
{
    double aggression;
    double defensiveness;
    double resourceUsage;
    
    public TacticalDecision(double agg, double def, double res)
    {
        this.aggression = agg;
        this.defensiveness = def;
        this.resourceUsage = res;
    }
    
    private String getIntensity(double value)
    {
        double abs = Math.abs(value);
        if (abs < 0.3) return "slightly";
        if (abs < 0.7) return "moderately";
        return "significantly";
    }
    
    private String getDirection(double value, String increase, String decrease)
    {
        if (Math.abs(value) < 0.1) return "maintain";
        if (value > 0) return increase + " " + getIntensity(value);
        return decrease + " " + getIntensity(value);
    }
    
    public String getRecommendedAction()
    {
        if (aggression > 0.5 && resourceUsage > 0.3) return "All-Out Offensive";
        if (defensiveness > 0.5) return "Defensive Stance";
        if (aggression > 0.3 && defensiveness < 0) return "Aggressive Push";
        if (resourceUsage < -0.5) return "Resource Conservation";
        if (Math.abs(aggression) < 0.2 && Math.abs(defensiveness) < 0.2) return "Wait and Observe";
        return "Balanced Attack";
    }
    
    public void printDecision()
    {
        System.out.println("\nTactical Decision Output:");
        System.out.printf("Recommended Action: %s\n\n", getRecommendedAction());
        
        System.out.printf("Aggression Level: %s (%.3f)\n", 
            getDirection(aggression, "increase", "decrease"), aggression);
        System.out.printf("Defensiveness: %s (%.3f)\n", 
            getDirection(defensiveness, "increase", "decrease"), defensiveness);
        System.out.printf("Resource Usage: %s (%.3f)\n", 
            getDirection(resourceUsage, "use resources", "conserve resources"), 
            resourceUsage);
    }
}

public class RPGDecisionMatrix
{
    // Path to Pokemon CSV data    
    String filename = "/Users/racheleglash/Adjusted-Learning-Matrix-Model/Adjusted-Learning-Matrix-Model/ALMM Program/Pokemon.csv";

    // Transformation Matrix A (3x4)
    // Rows: [aggression, defensiveness, resourceUsage]
    // Cols: [currentHP, enemyThreat, resourcesRemaining, teamAdvantage]
    private static final double[][] TRANSFORMATION_MATRIX =
    {
        {0.25, 0.40, 0.20, 0.15},  // Aggression weights
        {0.40, 0.20, 0.15, 0.25},  // Defensiveness weights
        {0.15, 0.25, 0.45, 0.15}   // Resource usage weights
    };
    
    /**
     * Performs matrix multiplication: y = Ax
     * Similar to your learning matrix calculation
     */
    public static TacticalDecision calculateDecision(BattleState state)
    {
        double[] x = state.getNormalizedInputs();
        double[] y = new double[3];
        
        // Matrix multiplication
        for (int i = 0; i < 3; i++) {
            y[i] = 0;
            for (int j = 0; j < 4; j++) {
                y[i] += TRANSFORMATION_MATRIX[i][j] * x[j];
            }
        }
        
        return new TacticalDecision(y[0], y[1], y[2]);
    }
    
    /**
     * Load Pokemon from CSV file
     * Download from: https://github.com/lgreski/pokemonData/blob/master/Pokemon.csv
     * Click "Raw" button to download
     */
    public static List<PokemonCharacter> loadPokemonData(String filename)
    {
        List<PokemonCharacter> pokemonList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filename)))
        {
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null && pokemonList.size() < 20)
            {
                String[] values = line.split(",");
                
                // CSV format: ID,Name,Type1,Type2,Total,HP,Attack,Defense,Sp.Atk,Sp.Def,Speed,Generation
                if (values.length >= 11) {
                    try
                    {
                        String name = values[1].trim();
                        int hp = Integer.parseInt(values[5].trim());
                        int attack = Integer.parseInt(values[6].trim());
                        int defense = Integer.parseInt(values[7].trim());
                        int speed = Integer.parseInt(values[10].trim());
                        
                        pokemonList.add(new PokemonCharacter(name, hp, attack, defense, speed));
                    }
                    catch (NumberFormatException e)
                    {
                        // Skip malformed lines
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("Error reading Pokemon data: " + e.getMessage());
            System.err.println("\nTo download the dataset:");
            System.err.println("1. Go to: https://github.com/lgreski/pokemonData");
            System.err.println("2. Click on 'Pokemon.csv'");
            System.err.println("3. Click 'Raw' button");
            System.err.println("4. Save as 'Pokemon.csv' in same folder as this program");
        }
        
        return pokemonList;
    }
    
    /**
     * Estimate battle state from Pokemon stats
     */
    public static BattleState estimateBattleState(PokemonCharacter player, PokemonCharacter enemy)
    {
        // Normalize stats to 0-10 scale
        double hpRatio = (double) player.hp / Math.max(player.hp, enemy.hp);
        double currentHP = hpRatio * 10;
        
        double threatRatio = (double) enemy.attack / (player.defense + 1);
        double enemyThreat = Math.min(threatRatio * 3, 10);
        
        double speedRatio = (double) player.speed / Math.max(player.speed, enemy.speed);
        double resources = speedRatio * 10;
        
        double statAdvantage = (player.attack + player.defense) - 
                              (enemy.attack + enemy.defense);
        double teamAdvantage = 5 + (statAdvantage / 50.0); // Normalize around 5
        teamAdvantage = Math.max(0, Math.min(10, teamAdvantage));
        
        return new BattleState(currentHP, enemyThreat, resources, teamAdvantage);
    }
    
    public static void printMatrix()
    {
        System.out.println("\nTransformation Matrix (A)");
        System.out.println("\nColumns: [HP, Threat, Resources, Advantage]\n");
        
        String[] rowNames = {"Aggression:   ", "Defensiveness:", "Resource Use: "};
        for (int i = 0; i < 3; i++)
        {
            System.out.print(rowNames[i] + " [");
            for (int j = 0; j < 4; j++)
            {
                System.out.printf("%.2f", TRANSFORMATION_MATRIX[i][j]);
                if (j < 3) System.out.print(", ");
            }
            System.out.println("]");
        }
        System.out.println("");
    }

    public static void manualBattleStateInput(Scanner scanner)
    {
        // Scanner scanner = new Scanner(System.in);

        System.out.println("\n--- Enter Battle State (0-10 scale) ---");
        System.out.print("Current HP (0=critical, 10=full): ");
        double hp = scanner.nextDouble();
                
        System.out.print("Enemy Threat (0=weak, 10=dangerous): ");
        double threat = scanner.nextDouble();
                
        System.out.print("Resources Remaining (0=depleted, 10=full): ");
        double resources = scanner.nextDouble();
                
        System.out.print("Team Advantage (0=disadvantage, 10=advantage): ");
        double advantage = scanner.nextDouble();
                
        BattleState state = new BattleState(hp, threat, resources, advantage);
        TacticalDecision decision = calculateDecision(state);
                
        System.out.println("\nInput Matrix (x) Normalized");
        double[] inputs = state.getNormalizedInputs();
        System.out.printf("[%.3f, %.3f, %.3f, %.3f]\n", inputs[0], inputs[1], inputs[2], inputs[3]);
                
        decision.printDecision();
    }

    public static void pokemonBattle(List<PokemonCharacter> pokemonCharacter, Scanner scanner)
    {
        // Scanner scanner = new Scanner(System.in);

        // Pokemon battle
        System.out.println("\nPokemon Battle Simulation");
        System.out.println("Available Pokemon:");
        for (int i = 0; i < pokemonCharacter.size(); i++)
        {
            System.out.println("  " + (i+1) + ". " + pokemonCharacter.get(i).name);
        }
                
        System.out.print("\nChoose your Pokemon (1-" + pokemonCharacter.size() + "): ");
        int p1 = scanner.nextInt() - 1;
                
        System.out.print("Choose enemy Pokemon (1-" + pokemonCharacter.size() + "): ");
        int p2 = scanner.nextInt() - 1;
                
        if (p1 >= 0 && p1 < pokemonCharacter.size() && p2 >= 0 && p2 < pokemonCharacter.size())
        {
            PokemonCharacter player = pokemonCharacter.get(p1);
            PokemonCharacter enemy = pokemonCharacter.get(p2);
                    
            System.out.println("\n" + player.name + " vs " + enemy.name);
            System.out.println(player);
            System.out.println(enemy);
                    
            BattleState state = estimateBattleState(player, enemy);
            TacticalDecision decision = calculateDecision(state);
                    
            System.out.println("\nEstimated Battle State");
            System.out.printf("HP Level: %.1f/10\n", state.currentHP);
            System.out.printf("Enemy Threat: %.1f/10\n", state.enemyThreat);
            System.out.printf("Resources: %.1f/10\n", state.resourcesRemaining);
            System.out.printf("Advantage: %.1f/10\n", state.teamAdvantage);
                    
            decision.printDecision();
        }
    }
    
    public static void testExtremeScenarios()
    {
        // Test scenarios
        System.out.println("\nTesting Extreme Scenarios:");
                
        System.out.println("\nScenario 1: Perfect Conditions");
        BattleState perfect = new BattleState(10, 2, 10, 10);
        calculateDecision(perfect).printDecision();
                
        System.out.println("\nScenario 2: Critical Danger");
        BattleState danger = new BattleState(2, 10, 3, 2);
        calculateDecision(danger).printDecision();
                
        System.out.println("\nScenario 3: Balanced Fight");
        BattleState balanced = new BattleState(5, 5, 5, 5);
        calculateDecision(balanced).printDecision();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\nRPG Battle Decision Matrix System\n");
    
        // Try to load Pokemon data
        List<PokemonCharacter> pokemonCharacter = loadPokemonData("/Users/racheleglash/Adjusted-Learning-Matrix-Model/Adjusted-Learning-Matrix-Model/ALMM Program/Pokemon.csv");
        
        if (pokemonCharacter.isEmpty())
        {
            System.out.println("\nNo Pokemon data loaded.");
        }
        else
        {
            System.out.println("\nLoaded " + pokemonCharacter.size() + " Pokemon!");
            System.out.println("\nFirst 5 Pokemon:");
            for (int i = 0; i < Math.min(5, pokemonCharacter.size()); i++)
            {
                System.out.println("  " + (i+1) + ". " + pokemonCharacter.get(i));
            }
        }
        
        while (true)
        {
            System.out.println("\nMenu\n");
            System.out.println("1. Manual Battle State Input");
            System.out.println("2. Pokemon vs Pokemon (if data loaded)");
            System.out.println("3. View Transformation Matrix");
            System.out.println("4. Test Extreme Scenarios");
            System.out.println("5. Exit");
            System.out.print("\nChoice: ");
    
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1)
            {
                manualBattleStateInput(scanner);
            }
            else if (choice == 2 && !pokemonCharacter.isEmpty())
            {
                pokemonBattle(pokemonCharacter, scanner);
            }
            else if (choice == 3)
            {
                printMatrix();
            }
            else if (choice == 4)
            {
                testExtremeScenarios();
            }
            else if (choice == 5)
            {
                System.out.println("\nThanks for using RPG Decision Matrix!");
                break;
            }
        }
        
        scanner.close();
    }
}
/*
    public static void demonstrateParallel() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  CONNECTION TO YOUR LEARNING MATRIX PROJECT                    ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println("\nYour Adjusted Learning Matrix Model:");
        System.out.println("  Input (x):  [independence, confidence, persistence, accuracy]");
        System.out.println("  Matrix (A): 3x4 transformation weights");
        System.out.println("  Output (y): [difficulty, support, pacing]");
        System.out.println("  Purpose:    Adapt instruction to student performance");
        
        System.out.println("\nThis RPG Battle Decision Matrix:");
        System.out.println("  Input (x):  [HP, threat, resources, advantage]");
        System.out.println("  Matrix (A): 3x4 transformation weights");
        System.out.println("  Output (y): [aggression, defensiveness, resource usage]");
        System.out.println("  Purpose:    Adapt tactics to battle conditions");
        
        System.out.println("\nKey Insight:");
        System.out.println("  Both use the SAME mathematical structure (y = Ax) to make");
        System.out.println("  adaptive decisions based on observable state!");
        System.out.println("  Same linear algebra → Different applications");
        System.out.println("════════════════════════════════════════════════════════════════\n");
    }
    */