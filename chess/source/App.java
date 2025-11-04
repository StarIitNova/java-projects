package source;

class App {

    public static void main(String[] args) {
        SConsole.ConsoleDimensions dimensions = SConsole.getConsoleDimensions();
        ConsoleIO io = new ConsoleIO(dimensions.width, dimensions.height);

        ChessBoard board = new ChessBoard(io); // this is probably important, I'm not sure though.

        io.pushColor(ChessColorConf.MAIN_BG);
        io.clearbuf();

        while (true) {
            boolean redraw = board.draw();

            io.mvcur(dimensions.height - 2, 0);
            String command = io.poll().trim();

            // magic clearing commands :)
            io.mvcur(dimensions.height - 2, 0);
            for (int i = 0; i < command.length() * 2; ++i) {
                System.out.print(" ");
            }

            io.mvcur(dimensions.height - 2, 0);

            if (command.equalsIgnoreCase("exit")) break;

            if (command.equalsIgnoreCase("redraw")) redraw = true;

            redraw = board.processCommand(command) || redraw;

            if (redraw) io.redraw();
        }

        io.popColor();

        io.cls();
        io.destruct();
    }
}
