import java.util.Scanner;

/**
 * AdjustedLearningMM - Adjusted Learning Matrix Model
 * This class analyzes student learning metrics and provides recommendations
 * for difficulty, support, and pacing adjustments.
 */
public class AdjustedLearningMM
{
    // Instance variables
    private double[][] transformationMatrix;
    private int[][] inputMatrix;
    private double[][] normalizedInputMatrix;
    
    /**
     * Default constructor
     * Initializes the transformation matrix with predefined weights
     */
    public AdjustedLearningMM()
    {
        // Initialize the transformation matrix (4x3)
        // Each column represents: [Difficulty, Support, Time/Pacing]
        // Each row represents weights for: [Independence, Confidence, Persistence, Accuracy]
        this.transformationMatrix = new double[][]
        {
            {0.35, 0.15, 0.15, 0.35},  // Weights for difficulty adjustment
            {0.30, 0.30, 0.30, 0.10},  // Weights for support adjustment
            {0.30, 0.15, 0.15, 0.40},  // Weights for pacing adjustment
        };
    }

    /**
     * Gets input matrix from user via console
     * Prompts for 4 metrics: independence, confidence, persistence, and accuracy
     * @return 4x1 matrix containing user input values
     */
    public int[][] getInputMatrix()
    {
        // Create scanner object to read console input
        Scanner scanner = new Scanner(System.in);

        // Prompt user for independence metric (0-10 scale)
        System.out.print("Enter student's independence/amount of help provided (0 = no independence, 10 = full independence): ");
        int help = scanner.nextInt();

        // Prompt user for confidence metric (0-10 scale)
        System.out.print("Enter observed student confidence (0 = no confidence, 10 = full confidence): ");
        int confidence = scanner.nextInt();

        // Prompt user for persistence metric (0-10 scale)
        System.out.print("Enter student's persistence (0 = gave up immediately, 10 = never gave up): ");
        int persistence = scanner.nextInt();

        // Prompt user for accuracy metric (0-10 scale)
        System.out.print("Enter student's accuracy & mistake severity/type* (0 = full conceptual error, 10 = no errors): ");
        int mistake = scanner.nextInt();

        // Close the scanner to prevent resource leak
        scanner.close();

        // Store input in instance variable
        this.inputMatrix = new int[][] { {help}, {confidence}, {persistence}, {mistake} };

        // Return the 4x1 input matrix
        return this.inputMatrix;
    }

    /**
     * Normalizes the input matrix values to range [-1, 1]
     * Formula: (value - 5) / 5.0
     * This centers the 0-10 scale around 0
     * @return 4x1 normalized matrix
     */
    public double[][] getNormalizedInputMatrix()
    {        
        // Get user input and populate the input matrix
        this.inputMatrix = getInputMatrix();

        // Initialize normalized matrix with same dimensions
        this.normalizedInputMatrix = new double[4][1];

        // Iterate through each input value
        for (int i = 0; i < 4; i++)
        {
            // Normalize: subtract midpoint (5) and divide by range (5)
            // This maps 0->-1, 5->0, 10->1
            this.normalizedInputMatrix[i][0] = (this.inputMatrix[i][0] - 5) / 5.0;
        }

        // Return the normalized matrix
        return this.normalizedInputMatrix;
    }

    /**
     * Calculates the adjusted learning matrix by multiplying
     * transformation matrix with normalized input
     * @param normalizedInputMatrix - 4x1 normalized input values
     * @param transformationMatrix - 3x4 transformation weights
     * @return 3x1 matrix with adjustment values for [Difficulty, Support, Pacing]
     */
    public double[][] getAdjustedLearningMatrix(double[][] normalizedInputMatrix, double[][] transformationMatrix)
    {
        // Perform matrix multiplication: (3x4) * (4x1) = (3x1)
        double[][] adjustedLearningMatrix = matrixMultiply(transformationMatrix, normalizedInputMatrix);

        // Return the resulting adjustment matrix
        return adjustedLearningMatrix;
    }

    /**
     * Performs matrix multiplication
     * @param tMatrix - First matrix (transformation matrix)
     * @param inMatrix - Second matrix (input matrix)
     * @return Result of matrix multiplication
     */
    public double[][] matrixMultiply(double[][] tMatrix, double[][] inMatrix)
    {
        // Get dimensions of input matrix
        int tMatrixRows = tMatrix.length;        // Number of rows in transformation matrix
        int tMatrixCols = tMatrix[0].length;     // Number of columns in transformation matrix
        int inMatrixCols = inMatrix[0].length;   // Number of columns in input matrix

        // Initialize output matrix with appropriate dimensions
        double[][] outMatrix = new double[tMatrixRows][inMatrixCols];

        // Iterate through rows of first matrix
        for (int i = 0; i < tMatrixRows; i++)
        {
            // Iterate through columns of second matrix
            for (int j = 0; j < inMatrixCols; j++)
            {
                // Perform dot product of row i and column j
                for (int k = 0; k < tMatrixCols; k++)
                {
                    // Multiply corresponding elements and accumulate sum
                    outMatrix[i][j] += tMatrix[i][k] * inMatrix[k][j];
                }
            }
        }

        // Return the result matrix
        return outMatrix;
    }

    /**
     * Prints a matrix to console in formatted rows and columns
     * @param matrix - The matrix to print
     */
    public static void printMatrix(double[][] matrix)
    {
        // Iterate through each row
        for (int i = 0; i < matrix.length; i++)
        {
            // Iterate through each column in the row
            for (int j = 0; j < matrix[i].length; j++)
            {
                // Print the element with spacing
                System.out.print(matrix[i][j] + "   ");
            }
            // Move to next line after each row
            System.out.println();
        }
    }

    /**
     * Prints difficulty recommendation based on adjustment value
     * @param d - Difficulty adjustment value (negative = decrease, positive = increase)
     */
    public static void printDifficultyRecommendation(double d)
    {
        // Start the recommendation message
        System.out.print("\nThe difficulty level should ");

        // Determine direction of adjustment
        if (d < 0)
        {
            // Negative value means decrease difficulty
            System.out.print("decrease ");
        }
        else if (d > 0)
        {
            // Positive value means increase difficulty
            System.out.print("increase ");
        }
        else
        {
            // Zero means no change needed
            System.out.print("stay the same.");
            return;
        }

        // Determine magnitude of adjustment based on absolute value
        if (Math.abs(d) >= 0.7)
        {
            // Large adjustment needed (|d| >= 0.7)
            System.out.print("significantly.");
        }
        else if (Math.abs(d) > 0.3)
        {
            // Moderate adjustment needed (0.3 < |d| < 0.7)
            System.out.print("moderately.");
        }
        else
        {
            // Small adjustment needed (|d| <= 0.3)
            System.out.print("slightly.");
        }
    }

    /**
     * Prints support recommendation based on adjustment value
     * @param s - Support adjustment value (negative = increase, positive = decrease)
     */
    public static void printSupportRecommendation(double s)
    {
        // Start the recommendation message
        System.out.print("\nThe support level should ");

        // Determine direction of adjustment (inverse relationship)
        if (s < 0)
        {
            // Negative value means increase support
            System.out.print("increase ");
        }
        else if (s > 0)
        {
            // Positive value means decrease support
            System.out.print("decrease ");
        }
        else
        {
            // Zero means no change needed
            System.out.print("stay the same.");
            return;
        }

        // Determine magnitude of adjustment based on absolute value
        if (Math.abs(s) >= 0.7)
        {
            // Large adjustment needed (|s| >= 0.7)
            System.out.print("significantly.");
        }
        else if (Math.abs(s) > 0.3)
        {
            // Moderate adjustment needed (0.3 < |s| < 0.7)
            System.out.print("moderately.");
        }
        else
        {
            // Small adjustment needed (|s| <= 0.3)
            System.out.print("slightly.");
        }
    }

    /**
     * Prints time/pacing recommendation based on adjustment value
     * @param t - Pacing adjustment value (negative = slow down, positive = speed up)
     */
    public static void printTimeRecommendation(double t)
    {
        // Start the recommendation message
        System.out.print("\nThe pacing should ");

        // Determine direction of adjustment
        if (t < 0)
        {
            // Negative value means slow down
            System.out.print("slow down ");
        }
        else if (t > 0)
        {
            // Positive value means speed up
            System.out.print("speed up ");
        }
        else
        {
            // Zero means no change needed
            System.out.println("stay the same.");
            return;
        }

        // Determine magnitude of adjustment based on absolute value
        if (Math.abs(t) >= 0.7)
        {
            // Large adjustment needed (|t| >= 0.7)
            System.out.println("significantly.");
        }
        else if (Math.abs(t) > 0.3)
        {
            // Moderate adjustment needed (0.3 < |t| < 0.7)
            System.out.println("moderately.");
        }
        else
        {
            // Small adjustment needed (|t| <= 0.3)
            System.out.println("slightly.");
        }
    }
    
    /**
     * Prints all recommendations based on adjusted learning matrix
     * @param adjustedLearningMatrix - 3x1 matrix containing [Difficulty, Support, Pacing]
     */
    public static void printRecommendations(double[][] adjustedLearningMatrix)
    {
        // Extract individual adjustment values from matrix
        double d = adjustedLearningMatrix[0][0];  // Difficulty adjustment
        double s = adjustedLearningMatrix[1][0];  // Support adjustment
        double t = adjustedLearningMatrix[2][0];  // Pacing adjustment

        // Print header
        System.out.println("\nRecommendations:");

        // Print each recommendation
        printDifficultyRecommendation(d);
        printSupportRecommendation(s);
        printTimeRecommendation(t);
        
        // Add final newline for formatting
        System.out.println();
    }
    
    // Main method
    public static void main(String[] args)
    {
        System.out.println("\nAdjusted Learning Matrix Model:\n");
        
        AdjustedLearningMM almm = new AdjustedLearningMM();

        double[][] normalizedInputMatrix = almm.getNormalizedInputMatrix();
        
        // Print the adjusted input matrix
        //System.out.println("\nNormalized Input Matrix:");
        //printMatrix(normalizedInputMatrix);

        // Print the transformation matrix
        //System.out.println("\nTransformation Matrix:");
        //printMatrix(transformationMatrix);

        double[][] adjustedLearningMatrix = almm.getAdjustedLearningMatrix(normalizedInputMatrix, transformationMatrix);

        // Print the adjusted learning matrix
        //System.out.println("\nAdjusted Learning Matrix:");
        //printMatrix(adjustedLearningMatrix);

        // Print recommendations
        printRecommendations(adjustedLearningMatrix);
    }

}                                                                                                                                                                     