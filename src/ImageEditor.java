import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class ImageEditor {

    private enum ProgramState {P3, HEIGHT_WIDTH, PIXELS}

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("USAGE: java ImageEditor in-file out-file (grayscale|invert|emboss|motionblur motion-blur-length)");
            System.exit(1);
        }

        String inputFileName = args[0];
        String outputfileName = args[1];
        String transformation = args[2];

        ProgramState currentState = ProgramState.P3;

        File inputFile = new File(inputFileName);
        try {
            Scanner scan = new Scanner(inputFile);
            int height = -1, width = -1;
            Pixel[][] rows = null;
            int row = 0, column = 0;
            int red = -1, green = -1, blue;

            while (scan.hasNext()) {
                String line = scan.nextLine().split("#")[0].trim();

                if(line.equals("")) {

                    continue;
                }

                String[] lineResult = line.split("\\s");

                for (String item : lineResult) {
                    if(currentState == ProgramState.P3) {

                        if(!item.equals("P3")) {

                            throw new RuntimeException("Must start with P3");
                        }

                        currentState = ProgramState.HEIGHT_WIDTH;
                    }
                    else if(currentState == ProgramState.HEIGHT_WIDTH) {

                        try {
                            if (height == -1) {
                                height = Integer.parseInt(item);
                            } else {
                                width = Integer.parseInt(item);
                                currentState = ProgramState.PIXELS;
                                rows = new Pixel[height][width];
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Height and width must be positive integers");
                        }
                    } else {
                        try {
                            int colorInt = Integer.parseInt(item);
                            if (red == -1) {
                                red = colorInt;
                            } else if (green == -1) {
                                green = colorInt;
                            } else {
                                blue = colorInt;
                                rows[row][column++] = new Pixel(red, green, blue);

                                if (column == width) {
                                    column = 0;
                                    row++;
                                }

                                red = -1;
                                green = -1;
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Colors must be positive integers");
                        }
                    }
                }
            }

            MyImage image = new MyImage(height, width, rows);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}

