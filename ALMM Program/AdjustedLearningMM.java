import java.util.Scanner;

public class AdjustedLearningMM
{

    //public int[][] inputMatrix;
    //public int[][] normalizedInputMatrix;

    int[][] getInputMatrix()
    {
        // Method to get input matrix from user

        // Initialize 4x1 input matrix
        //inputMatrix = new int[4][1];

        // Create scanner object to get user input from the console
        Scanner scanner = new Scanner(System.in);

        // Prompt user for input values
        System.out.print("Enter student's independence/amount of help provided (0=no independence, 10=full independence): ");
        int help = scanner.nextInt();
        System.out.print("Enter observed student confidence (0=no confidence, 10=full confidence): ");
        int confidence = scanner.nextInt();
        System.out.print("Enter student's persistence (0=gave up immediatly, 10=never gave up): ");
        int persistence = scanner.nextInt();
        System.out.print("Enter student's accuracy & mistake severity/type* (0=full conceptual error, 10=no errors): ");
        int mistake = scanner.nextInt();

        // Create and return the input matrix
        return new int[][] { {help}, {confidence}, {persistence}, {mistake} };

       // Close the scaner
       //scanner.close();
   }

   // Method to normalize the input matrix
    double[][] getNormalizedInputMatrix()
    {        
        // Call getInputMatri to populate the input matrix
        int[][] inputMatrix = getInputMatrix();

        double[][] normalizedInputMatrix = new double[4][1];

        for (int i = 0; i < 4; i++)
        {
            normalizedInputMatrix[i][0] = (inputMatrix[i][0] - 5) / 5.0;
        } 

        return normalizedInputMatrix;
    }

    // Method to print the normalized input matrix
    static void printNormalizedInputMatrix(double[][] normalizedInputMatrix)
    {
        System.out.println("\nNormalized Input Matrix:");
        for (int i = 0; i < normalizedInputMatrix.length; i++)
        {
            System.out.println(normalizedInputMatrix[i][0]);
        }
    }

    // Main method
    public static void main(String[] args)
    {
        System.out.println("Adjusted Learning Matrix Model\n");
        
        AdjustedLearningMM almm = new AdjustedLearningMM();

        double[][] normalizedInputMatrix = almm.getNormalizedInputMatrix();
        
        // Print the adjusted input matrix
        printNormalizedInputMatrix(normalizedInputMatrix);
    }

}