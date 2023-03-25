import java.io.*;
import java.util.*;

public class Main {
    static int R;
    static int C;
    static int nSnakes; // S
    static int[] lengthSnakes;
    static int[][] system; // wormhole = -10,001
    static boolean[][] occupied;
    static int numWormHoles = 0;
    static int[][] wormHoles;
    static String[] paths;
    static int possibleScore = 0;

    public static void main(String args[]) { // static?

        try {
            String inputFile = args[0];
            readFile(inputFile);
            int score = 0;

            for (int i = 0; i < nSnakes; i++) {
                int[] coords = findMaxXY();

                Path snakePath = createSnake(lengthSnakes[i], coords[0], coords[1], coords[0] + " " + coords[1] + " ", 0, i); // create initial snake

                paths[i] = snakePath.getPath();
                setOccupied(snakePath.getPath());
                score += snakePath.getScore();
                System.out.println(String.format("Snake Number: %d, Score: %d", i, snakePath.getScore()));
            }

            for (int i = 0; i < nSnakes; i++) { // output
                System.out.println(paths[i]);
            }

            // for testing purposes:
            System.out.println("Total Score: " + score + " / " + possibleScore); // TODO: comment out at the end


        } catch (RuntimeException e) {
            System.out.println("Usage: java Main input_file.txt");
            e.printStackTrace();
            return;
        } catch (FileNotFoundException e) {
            System.out.println("File Not Found Exception");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    public static void readFile(String filename) throws Exception {
        try {
            File f = new File(filename);
            FileInputStream fileStream = new FileInputStream(f);

            Scanner scan = new Scanner(fileStream);
            
            // first input line - read the input values for system
            C = scan.nextInt();
            R = scan.nextInt();
            nSnakes = scan.nextInt();
            paths = new String[nSnakes];
            scan.nextLine(); // clear white space

            lengthSnakes = new int[nSnakes];
            occupied = new boolean[R][C];
            system = new int[R][C];
            wormHoles = new int[R*C][2];

            // second input line - snake lengths
            for (int i = 0; i < nSnakes; i++) {
                lengthSnakes[i] = scan.nextInt();
            }

            scan.nextLine(); // clear white space

            // rest of input - make the system
            for (int row = 0; row < R; row++) { // for each row

                for (int col = 0; col < C; col++) {
                    String input = scan.next();

                    if (input.equals("*")) { // wormhole
                        system[row][col] = -10001;
                        wormHoles[numWormHoles] = new int[]{row, col};
                        numWormHoles++;

                    } else {    // int value (points)
                        system[row][col] = Integer.parseInt(input);
                        possibleScore += Integer.parseInt(input);
                    }

                    occupied[row][col] = false;
                }
                // scan.nextLine();
            }
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Recursive function made to create a snake
     * Starts at the highest value available
     */

    // TODO: Defaults to up on last because return 0 score
    public static Path createSnake(int length, int x, int y, String path, int score, int index) {
        // Any value of -10001 is a wormhole
        if (length <= 0) {
            Path path_result = new Path(score, path.substring(0, path.length() - 3));
            return path_result;
        }

        // Adjust x and y if needed
        if (x < 0) {
            x += C;
        }
        if (x >= C) {
            x -= C;
        }
        if (y < 0) {
            y += R;
        }
        if (y >= R) {
            y -= R;
        }

        // Penalize biggest score if path is blocked
        if (occupied[y][x]) {
            return new Path(score  + (length * -10001), path);
        } else {
            Path[] list_paths = new Path[4];
            // Path array goes U L D R

            if (system[y][x] == -10001) { // wormhole - still counts as a move
                //length--; // Travel to the wormhole
                // TODO: go to each wormhole?
                for (int i = 0; i < numWormHoles; i++) {
                    int newY = wormHoles[i][0];
                    int newX = wormHoles[i][1];

                    if (x != newX && y != newY) {
                        path += newX + " " + newY + " ";
                        x = newX;
                        y = newY;

                        list_paths[0] = createSnake(length - 1, x, y - 1, path + "U ", score, index);
                        list_paths[1] = createSnake(length - 1, x - 1, y, path + "L ", score, index);
                        list_paths[2] = createSnake(length - 1, x, y + 1, path + "D ", score, index);
                        list_paths[3] = createSnake(length - 1, x + 1, y, path + "R ", score, index);

                        // Find the path with the biggest score
                        int[] list_scores = { list_paths[0].getScore(), list_paths[1].getScore(), list_paths[2].getScore(), list_paths[3].getScore() };
                        int score_index = 0;
                        int maxScore = list_scores[score_index];

                        for (int j = 0; j < list_scores.length; j++) {
                            if (list_scores[j] > maxScore) {
                                maxScore = list_scores[j];
                                score_index = j;
                            }
                        }

                        return list_paths[score_index];

                    }
                }


            } else { // value
                score += system[y][x];
                occupied[y][x] = true;
                list_paths[0] = createSnake(length - 1, x, y - 1, path + "U ", score, index);
                list_paths[1] = createSnake(length - 1, x - 1, y, path + "L ", score, index);
                list_paths[2] = createSnake(length - 1, x, y + 1, path + "D ", score, index);
                list_paths[3] = createSnake(length - 1, x + 1, y, path + "R ", score, index);
                occupied[y][x] = false;


                // Find the path with the biggest score
                int[] list_scores = { list_paths[0].getScore(), list_paths[1].getScore(), list_paths[2].getScore(), list_paths[3].getScore() };
                int score_index = 0;
                int maxScore = list_scores[score_index];

                for (int j = 0; j < list_scores.length; j++) {
                    if (list_scores[j] > maxScore) {
                        maxScore = list_scores[j];
                        score_index = j;
                    }
                }

                return list_paths[score_index];
            }
        }

        return null;
    }

    public static void setOccupied(String path) {

        Scanner scan = new Scanner(path);

        int x = scan.nextInt();
        int y = scan.nextInt();
        occupied[y][x] = true;

        String input;

        while (scan.hasNext()) {

            input = scan.next();
            switch (input) {
                case("L"):
                    x -= 1;
                    if (x < 0) {
                        x += C;
                    }
                    occupied[y][x] = true;
                    break;
                case("D"):
                    y += 1;
                    if (y >= R) {
                        y -= R;
                    }
                    occupied[y][x] = true;
                    break;

                case("R"):
                    x += 1;
                    if (x >= C) {
                        x -= C;
                    }
                    occupied[y][x] = true;
                    break;

                case("U"):
                    y -= 1;
                    if (y < 0) {
                        y += R;
                    }
                    occupied[y][x] = true;

                    break;

                default:
                    x = Integer.parseInt(input);
                    y = scan.nextInt();

            }
        }

    }

    /*
     * Returns the index of the max value found in system
     */
    public static int[] findMaxXY() {
        // Find index of max non-taken value
        int maxX = 0, maxY = 0;
        int maxValue = -10000;
        for (int x = 0; x < C; x++){
            for (int y = 0; y < R; y++) {
                // If square is not occupied and is bigger than given maxValue
                if (!occupied[y][x] && (system[y][x] > maxValue)) {
                    maxX = x;
                    maxY = y;
                    maxValue = system[y][x];
                }
            }
        }

        int[] indexes = new int[2];
        indexes[0] = maxX;
        indexes[1] = maxY;

        return indexes;
    }


}

class Path {
    public int score;
    public String path;

    public Path(int score, String path) {
        this.score = score;
        this.path = path;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int newScore) {
        this.score = newScore;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
