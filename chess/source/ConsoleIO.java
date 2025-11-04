package source;

import java.util.ArrayList;
import java.util.Scanner;

class RGB {

    int r, g, b;

    RGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    RGB(int z) {
        this.r = z;
        this.g = z;
        this.b = z;
    }
}

class ColorProfile {

    int r, g, b;
    int br, bg, bb;

    ColorProfile(int r, int g, int b, int br, int bg, int bb) {
        this.r = r;
        this.g = g;
        this.b = b;

        this.br = br;
        this.bg = bg;
        this.bb = bb;
    }

    ColorProfile(RGB a, RGB b) {
        this.r = a.r;
        this.g = a.g;
        this.b = a.b;

        this.br = b.r;
        this.bg = b.g;
        this.bb = b.b;
    }
}

class ConsoleIO {

    private Scanner keyScanner;

    private Cell[][] scrnBuffer;
    private int width, height;

    private ArrayList<ColorProfile> colorBuffer;

    private class Cell {

        char character;
        String color;

        public Cell(char character, String color) {
            this.character = character;
            this.color = color;
        }

        public void set(char character, String color) {
            this.character = character;
            this.color = color;
        }
    }

    ConsoleIO(int width, int height) {
        keyScanner = new Scanner(System.in);
        this.colorBuffer = new ArrayList<ColorProfile>();
        this.width = width;
        this.height = height;

        this.scrnBuffer = new Cell[height][width];
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                this.scrnBuffer[i][j] = new Cell(' ', "\033[0m");
            }
        }

        redraw(false);
    }

    public void destruct() {
        keyScanner.close();
    }

    public String poll() {
        System.out.print(getColor());

        String line = keyScanner.nextLine();

        return line;
    }

    private String getColor() {
        if (colorBuffer.size() > 0) {
            ColorProfile current = colorBuffer.get(colorBuffer.size() - 1);
            return String.format(
                "\033[38;2;%d;%d;%dm\033[48;2;%d;%d;%dm",
                current.r,
                current.g,
                current.b,
                current.br,
                current.bg,
                current.bb
            );
        }

        return "\033[0m";
    }

    private void drawch(int row, int col, char c, String color, boolean raw) {
        System.out.printf(
            "\033[%d;%df%s%c" + (raw ? "\033[0m" : ""),
            row + 1,
            col + 1,
            color,
            c
        );
        System.out.flush();
    }

    public void mvputch(int row, int col, char c) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            scrnBuffer[row][col].set(c, getColor());
            drawch(row, col, c, getColor(), false);
        }
    }

    public void mvwrite(int row, int col, String str) {
        for (int i = 0; i < str.length(); ++i) {
            mvputch(row, col + i, str.charAt(i));
        }
    }

    public void mvcur(int row, int col) {
        System.out.printf("\033[%d;%df", row + 1, col + 1);
        System.out.flush();
    }

    public void clearbuf(boolean useColorBuffer) {
        String color = useColorBuffer ? getColor() : "\033[0m";
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
                this.scrnBuffer[i][j].set(' ', color);
            }
        }

        redraw();
    }

    public void clearbuf() {
        clearbuf(true);
    }

    public void cls() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void redraw(boolean clear) {
        if (clear) {
            cls();
        }

        for (int i = 0; i < height; ++i) {
            System.out.println(getColor());

            for (int j = 0; j < width; ++j) {
                drawch(
                    i,
                    j,
                    scrnBuffer[i][j].character,
                    scrnBuffer[i][j].color,
                    false
                );
            }
        }
    }

    public void redraw() {
        redraw(true);
    }

    public void pushColor(ColorProfile profile) {
        colorBuffer.add(profile);
    }

    public void popColor() {
        colorBuffer.remove(colorBuffer.size() - 1);
    }

    public void bold() {
        System.out.print("\033[1m");
    }

    public void unbold() {
        System.out.print("\033[22m");
    }
}
