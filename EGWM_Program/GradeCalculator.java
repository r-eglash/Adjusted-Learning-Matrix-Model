package EGWM_Program;

import java.io.*;
import java.util.*;

/**
 * Grade Calculator Program
 * This program:
 *   reads student data from a CSV file,
 *   calculates weighted scores based on user-provided weights, and
 *   outputs results to a file.
 */
public class GradeCalculator {
    
    // Number of columns for grade calculations (a, h, e)
    private static final int COL_SIZE = 3;
    
    /**
     * Parses a CSV line and extracts all values into a list (values are separated by commas)
     * 
     * @param line CSV line to parse
     * @return list of all values in the line as strings
     */
    private static List<String> iterateThroughFullLine(String line) {
        List<String> currentFullLineValues = new ArrayList<>();
        StringBuilder currString = new StringBuilder();
        
        // Iterates through each character in the line
        for (int i = 0; i <= line.length(); i++) {
            // When a comma is hit or the end is reached, saves the current string
            if (i == line.length() || line.charAt(i) == ',') {
                currentFullLineValues.add(currString.toString());
                currString = new StringBuilder(); // Resets for next value
            } else {
                // Builds up the current string
                currString.append(line.charAt(i));
            }
        }
        
        return currentFullLineValues;
    }
    
    /**
     * Parses a CSV line and gets the three specific numeric values: a_i, h_i, and e_i
     * Skips the first 4 comma-separated values, then extracts the next 3 numbers
     * 
     * @param line CSV line to parse
     * @return list containing three double values (a_i, h_i, e_i)
     */
    private static List<Double> iterateThroughLine(String line) {
        List<Double> currentLineValues = new ArrayList<>();
        int commaTicker = 0; // Tracks how many commas have been encountered
        int i = 0;
        
        // Skips the first 4 comma-separated fields (columns that are not needed)
        while (commaTicker != 4 && i < line.length()) {
            if (line.charAt(i) == ',') {
                commaTicker++;
            }
            i++;
        }
        
        // Gets the next 3 numeric values (until we hit 3 more commas)
        StringBuilder currString = new StringBuilder();
        while (commaTicker != 7 && i < line.length()) {
            if (line.charAt(i) == ',') {
                commaTicker++;
                // Converts the string to a double and adds to list
                currentLineValues.add(Double.parseDouble(currString.toString()));
                currString = new StringBuilder(); // Resets for next number
            } else {
                // Accumulates digits and decimal points
                currString.append(line.charAt(i));
            }
            i++;
        }
        
        return currentLineValues;
    }
    
    /**
     * Reads the CSV file and populates two matrices:
     *  matrix A contains only the grade values (a_i, h_i, e_i) for each student
     *  fullMatrix contains all data fields (including demographics) for each student
     * 
     * @param A matrix to store grade values
     * @param fullMatrix matrix to store all student data
     */
    private static void interpretData(List<List<Double>> A, List<List<String>> fullMatrix) throws Exception {
        // Opens the CSV file
        BufferedReader reader = new BufferedReader(new FileReader("EGWM_Program/Appendix_A_Charted_Grades.csv"));
        // Skips the header line
        reader.readLine();
        
        String line;
        // Reads each line of the file
        while ((line = reader.readLine()) != null) {
            // Gets the three grade values
            List<Double> currentLineValues = iterateThroughLine(line);
            
            // Adds student's scores to matrix A
            A.add(currentLineValues);
            
            // Gets all values from the line for the full matrix
            List<String> currentFullLineValues = iterateThroughFullLine(line);
            fullMatrix.add(currentFullLineValues);
        }
        
        reader.close();
    }
    
    /**
     * Prompts user to enter three weights (w_a, w_h, w_e) that are used to calculate the weighted grade
     * 
     * @param scanner scanner object for reading user input
     * @return list containing the three weight values
     */
    private static List<Double> getWeights(Scanner scanner) {
        List<Double> w = new ArrayList<>();
        char[] weightChar = {'a', 'h', 'e'};
        
        // Prompts for each of the three weights
        for (int i = 0; i < 3; i++) {
            System.out.print("Please enter w_" + weightChar[i] + " (only floating-point #s allowed): ");
            double tempWeight = scanner.nextDouble();
            w.add(tempWeight);
        }
        
        return w;
    }
    
    /**
     * Calculates weighted grade for each student by performing matrix multiplication: g = A * w
     * 
     * @param A matrix of student scores (each row is one student's three scores)
     * @param w weight vector (three weights corresponding to a, h, e)
     * @return vector g containing the weighted grade for each student
     */
    private static List<Double> multiplyMatrix(List<List<Double>> A, List<Double> w) {
        List<Double> g = new ArrayList<>();
        
        // For each student (each row in matrix A)
        for (int row = 0; row < A.size(); row++) {
            double currGrade = 0;
            
            // Calculates weighted sum: grade = a*w_a + h*w_h + e*w_e
            for (int col = 0; col < COL_SIZE; col++) {
                currGrade += A.get(row).get(col) * w.get(col);
            }
            
            // Adds this student's final grade to the results
            g.add(currGrade);
        }
        
        return g;
    }
    
    /**
     * Writes the calculated grades and student information to an output file
     * Format: Student #, Weighted Score, Disability, Race, FRL, Gender
     * 
     * @param fullMatrix matrix containing all student data
     * @param g vector of calculated weighted grades
     * @param filename name of output file
     */
    private static void writeToFile(List<List<String>> fullMatrix, List<Double> g, 
                                   String filename) throws Exception {
        PrintWriter writer = new PrintWriter(new FileWriter(filename));
        
        // Writes header line
        writer.println("Student #, Weighted Score, Disability, Race, FRL, Gender");
        
        // Writes each student's data
        for (int i = 0; i < g.size(); i++) {
            // Student number
            writer.print(fullMatrix.get(i).get(0) + ": ");
            
            // Weighted score
            writer.print(g.get(i) + "   ");
            
            // Additional student information (disability, race, FRL, gender)
            writer.print(fullMatrix.get(i).get(8) + ", ");
            writer.print(fullMatrix.get(i).get(9) + ", ");
            writer.print(fullMatrix.get(i).get(10) + ", ");
            writer.println(fullMatrix.get(i).get(11) + ", ");
        }
        
        writer.close();
        System.out.println("\nFile successfully generated.\n");
    }
    
    // Main method
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        
        // Initializes data structures
        List<List<String>> fullMatrix = new ArrayList<>();
        List<List<Double>> A = new ArrayList<>();
        List<Double> w;
        List<Double> g;
        
        // Reads and parses the CSV file
        interpretData(A, fullMatrix);
        
        // Gets weights from user
        w = getWeights(scanner);
        
        // Calculates weighted grades
        g = multiplyMatrix(A, w);
        
        // Gets name of file to save results to from user
        System.out.print("\nPlease enter a valid filename for output matrix g (including .txt): ");
        String filename = scanner.nextLine();
        writeToFile(fullMatrix, g, filename);
        
        scanner.close();
    }
}