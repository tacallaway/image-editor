public class MyImage {
    private int height;
    private int width;
    private Pixel[][] rows;

    public MyImage(int height, int width, Pixel[][] rows) {
        this.height = height;
        this.width = width;
        this.rows = rows;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public Pixel[][] getRows() {
        return rows;
    }

    public void setRows(Pixel[][] rows) {
        this.rows = rows;
    }
}
