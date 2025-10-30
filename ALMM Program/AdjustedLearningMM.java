import java.util.Scanner;

public class AdjustedLearningMM
{

    static double[][] transformationMatrix =
    {
        {0.35, 0.15, 0.15, 0.35},
        {0.30, 0.30, 0.30, 0.10},
        {0.30, 0.15, 0.15, 0.40},
    };

    //public int[][] inputMatrix;
    //public int[][] normalizedInputMatrix;

    // Method to get input matrix from user
    int[][] getInputMatrix()
    {
        // Method to get input matrix from user

        // Initialize 4x1 input matrix
        //inputMatrix = new int[4][1];

        // Create scanner object to get user input from the console
        Scanner scanner = new Scanner(System.in);

        // Prompt user for input values
        System.out.print("Enter student's independence/amount of help provided (0 = no independence, 10 = full independence): ");
        int help = scanner.nextInt();
        System.out.print("Enter observed student confidence (0 = no confidence, 10 = full confidence): ");
        int confidence = scanner.nextInt();
        System.out.print("Enter student's persistence (0 = gave up immediatly, 10 = never gave up): ");
        int persistence = scanner.nextInt();
        System.out.print("Enter student's accuracy & mistake severity/type* (0 = full conceptual error, 10 = no errors): ");
        int mistake = scanner.nextInt();

        // Close the scaner
        scanner.close();

        // Create and return the input matrix
        return new int[][] { {help}, {confidence}, {persistence}, {mistake} };
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

    // Method to calculate the adjusted learning matrix
    double[][] getAdjustedLearningMatrix(double[][] normalizedInputMatrix, double[][] transformationMatrix)
    {
        
        double[][] adjustedLearningMatrix = matrixMultiply(normalizedInputMatrix, transformationMatrix);

        return adjustedLearningMatrix;
    }

    // Method for matrix multiplication
    double[][] matrixMultiply(double[][] tMatrix, double[][] inMatrix)
    {
        int inMatrixRows = inMatrix.length;
        int inMatrixCols = inMatrix[0].length;
        int tMatrixCols = tMatrix[0].length;

        double[][] outMatrix = new double[inMatrixRows][tMatrixCols];

        for (int i = 0; i < inMatrixRows; i++)
        {
            for (int j = 0; j < tMatrixCols; j++)
            {
                for (int k = 0; k < inMatrixCols; k++)
                {
                    outMatrix[i][j] += inMatrix[i][k] * tMatrix[k][j];
                }
            }
        }

        return outMatrix;
    }

    // Method to print a matrix
    static void printMatrix(double[][] matrix)
    {
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix[i].length; j++)
            {
                System.out.print(matrix[i][j] + "   ");
            }
            System.out.println();
        }
    }

    // Method to print difficulty recommendation
    static void printDifficultyRecommendation(double d)
    {
        System.out.print("\nThe difficulty level should ");

        if (d < 0)
        {
            System.out.print("decrease ");
        }
        else if (d > 0)
        {
            System.out.print("increase ");
        }
        else
        {
            System.out.println("stay the same.");
            return;
        }

        if (Math.abs(d) >= 0.7)
        {
            System.out.println("siginificantly.");
        }
        else if (Math.abs(d) > 0.3)
        {
            System.out.println("moderately.");
        }
        else
        {
            System.out.println("slightly.");
        }

        return;
    }

    // Method to print support recommendation
    static void printSupportRecommendation(double s)
    {
        System.out.print("\nThe support level should ");

        if (s < 0)
        {
            System.out.print("increase ");
        }
        else if (s > 0)
        {
            System.out.print("decrease ");
        }
        else
        {
            System.out.println("stay the same.");
            return;
        }

        if (Math.abs(s) >= 0.7)
        {
            System.out.println("siginificantly.");
        }
        else if (Math.abs(s) > 0.3)
        {
            System.out.println("moderately.");
        }
        else
        {
            System.out.println("slightly.");
        }

        return;
    }

    // Method to print time recommendation
    static void printTimeRecommendation(double t)
    {
        System.out.print("\nThe pacing should ");

        if (t < 0)
        {
            System.out.print("slow down ");
        }
        else if (t > 0)
        {
            System.out.print("speed up ");
        }
        else
        {
            System.out.println("stay the same.");
            return;
        }

        if (Math.abs(t) >= 0.7)
        {
            System.out.println("siginificantly.");
        }
        else if (Math.abs(t) > 0.3)
        {
            System.out.println("moderately.");
        }
        else
        {
            System.out.println("slightly.");
        }

        return;
    }
    
    // Method to print all recommendations
    static void printRecommendations(double[][] adjustedLearningMatrix)
    {
        double d = adjustedLearningMatrix[0][0];
        double s = adjustedLearningMatrix[1][0];
        double t = adjustedLearningMatrix[2][0];

        System.out.println("\nRecommendations:");

        printDifficultyRecommendation(d);
        printSupportRecommendation(s);
        printTimeRecommendation(t);
    }

    // Main method
    public static void main(String[] args)
    {
        //System.out.println("Adjusted Learning Matrix Model\n");
        
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