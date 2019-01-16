import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ImageEditor {

    private enum ProgramState {P3, HEIGHT_WIDTH, MAX_COLOR,  PIXELS}

    private static int getNum(String num) {
        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static void main(String[] args) {
        if (args.length < 3 || (args[2].equals("motionblur") && (args.length < 4 || getNum(args[3]) <= 0))) {
            System.out.println("USAGE: java ImageEditor in-file out-file (grayscale|invert|emboss|motionblur motion-blur-length)");
            System.exit(1);
        }

        String inputFileName = args[0];
        String outputfileName = args[1];
        String transformation = args[2];
        int blurNum = Integer.parseInt(args[3]);

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
                    } else if(currentState == ProgramState.HEIGHT_WIDTH) {

                        try {
                            if (width == -1) {
                                width = Integer.parseInt(item);
                            } else {
                                height = Integer.parseInt(item);
                                currentState = ProgramState.MAX_COLOR;
                                rows = new Pixel[height][width];
                            }
                        } catch (NumberFormatException e) {
                            throw new RuntimeException("Height and width must be positive integers");
                        }
                    } else if(currentState == ProgramState.MAX_COLOR) {

                        if (!item.trim().equals("255")) {
                            throw new RuntimeException("Expected maximum color of 255");
                        }

                        currentState = ProgramState.PIXELS;
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

            if(transformation.equals("invert")) {

                for(Pixel[] pixelRow : image.getRows()) {

                    for(Pixel pixel : pixelRow) {

                        pixel.setRed(255 - pixel.getRed());
                        pixel.setGreen(255 - pixel.getGreen());
                        pixel.setBlue(255 - pixel.getBlue());
                    }
                }
            } else if(transformation.equals("grayscale")) {

                for(Pixel[] pixelRow : image.getRows()) {

                    for(Pixel pixel : pixelRow) {

                        int average = (pixel.getRed() + pixel.getGreen() + pixel.getBlue()) / 3;
                        pixel.setRed(average);
                        pixel.setGreen(average);
                        pixel.setBlue(average);
                    }
                }
            } else if(transformation.equals("emboss")) {

                for(int rowNum = image.getRows().length - 1; rowNum >= 0; rowNum--) {

                    Pixel[] pixelRow = image.getRows()[rowNum];

                    for(int colNum = pixelRow.length - 1; colNum >= 0; colNum--) {

                        Pixel pixel = pixelRow[colNum];
                        int v;

                        if(rowNum == 0 || colNum == 0) {

                            v = 128;
                        } else {

                            Pixel topLeft = image.getRows()[rowNum - 1][colNum - 1];

                            int redDif = pixel.getRed() - topLeft.getRed();
                            int greenDif = pixel.getGreen() - topLeft.getGreen();
                            int blueDif = pixel.getBlue() - topLeft.getBlue();

                            if (Math.abs(redDif) >= Math.abs(greenDif) && Math.abs(redDif) >= Math.abs(blueDif)) {

                                v = 128 + redDif;
                            } else if (Math.abs(greenDif) >= Math.abs(blueDif)) {

                                v = 128 + greenDif;
                            } else {

                                v = 128 + blueDif;
                            }

                            v = v < 0 ? 0 : v;
                            v = v > 255 ? 255 : v;
                        }

                        pixel.setRed(v);
                        pixel.setGreen(v);
                        pixel.setBlue(v);
                    }
                }
            } else if(transformation.equals("motionblur")) {

                for(int rowNum = 0; rowNum < height; rowNum++) {

                    Pixel[] pixelRow = image.getRows()[rowNum];

                    for(int colNum = 0; colNum < width; colNum++) {

                        Pixel pixel = pixelRow[colNum];

                        int redTotal = 0;
                        int greenTotal = 0;
                        int blueTotal = 0;
                        int count = 0;

                        for(int i = colNum; i < colNum + blurNum; i++) {

                            if (i >= width) {

                                break;
                            }

                            Pixel newPixel = pixelRow[i];

                            redTotal += newPixel.getRed();
                            greenTotal += newPixel.getGreen();
                            blueTotal += newPixel.getBlue();

                            count++;
                        }

                        if (count > 0) {
                            pixel.setRed(redTotal / count);
                            pixel.setGreen(greenTotal / count);
                            pixel.setBlue(blueTotal / count);
                        }
                    }
                }
            }

            PrintWriter writer = new PrintWriter(outputfileName);
            writer.println("P3");
            writer.println(image.getWidth() + " " + image.getHeight());
            writer.println("255");

            for(Pixel[] pixelRow : image.getRows()) {

                for(Pixel pixel : pixelRow) {

                    writer.println(pixel.getRed() + " " + pixel.getGreen() + " " + pixel.getBlue());
                }
            }

            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}


