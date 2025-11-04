package source;

import java.util.ArrayList;
import java.util.List;

class ChessTimer implements Runnable {

    private int interval = 0;
    private int timerA;
    private int timerB;
    private boolean aActive = true;
    private boolean paused = true;
    private ChessBoard board;
    private boolean exit = false;

    public ChessTimer(int timerA, int timerB, int interval, ChessBoard board) {
        this.timerA = timerA;
        this.timerB = timerB;
        this.interval = interval;
        this.board = board;
    }

    @Override
    public void run() {
        while (!exit) {
            try {
                Thread.sleep(1000);
                if (!paused) {
                    if (aActive) {
                        if (timerA > 0) {
                            timerA--;
                        }
                    } else {
                        if (timerB > 0) {
                            timerB--;
                        }
                    }
                }

                board.updateTimerDisplay(timerA, timerB);

                if (timerA == 0 || timerB == 0) {
                    board.endGame(timerA == 0 ? 1 : 0);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void swapActive() {
        aActive = !aActive;
        if (aActive) {
            timerB += interval - 1; // subtract 1 since the timer usually rounds down
        } else {
            timerA += interval - 1;
        }
    }

    public int getBlackTime() {
        return timerB;
    }

    public int getWhiteTime() {
        return timerA;
    }

    public int getActiveTime() {
        return aActive ? timerA : timerB;
    }

    public void unpause() {
        paused = false;
    }

    public void pause() {
        paused = true;
    }

    public void stop() {
        exit = true;
    }
}

class ChessBoard {

    ConsoleIO io;

    ChessPiece[][] board;

    int colorPlaying = 0;
    ChessTimer timer;
    int moveIdx = 0; // for storing move ids in pieces
    boolean inCheck = false;

    int selX = -1, selY = -1;
    int lockX = -1, lockY = -1;

    ArrayList<int[]> hot;
    boolean superHot = true;

    List<ChessPiece.Move> moves;

    public ChessBoard(ConsoleIO io) {
        this.io = io;

        this.timer = new ChessTimer(60 * 5, 60 * 5, 5, this);
        Thread timerThread = new Thread(timer);
        timerThread.start();

        setupBoard();
    }

    public void setupBoard() {
        this.board = new ChessPiece[8][8];
        this.hot = new ArrayList<int[]>();
        this.moves = new ArrayList<ChessPiece.Move>();

        initializePieces(0, 1, 1); // black
        initializePieces(7, 6, 0); // white
    }

    private void initializePieces(int majorRank, int minorRank, int color) {
        board[majorRank][0] = new Rook(majorRank, 0, color);
        board[majorRank][1] = new Knight(majorRank, 1, color);
        board[majorRank][2] = new Bishop(majorRank, 2, color);
        board[majorRank][3] = new Queen(majorRank, 3, color);
        board[majorRank][4] = new King(majorRank, 4, color);
        board[majorRank][5] = new Bishop(majorRank, 5, color);
        board[majorRank][6] = new Knight(majorRank, 6, color);
        board[majorRank][7] = new Rook(majorRank, 7, color);

        for (int i = 0; i < 8; ++i) {
            board[minorRank][i] = new Pawn(minorRank, i, color);
        }
    }

    private String coordsToAlgebraic(int row, int col) {
        return "" + Character.toString('a' + col) + (8 - row);
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public boolean occupiedByAlly(int row, int col, ChessPiece piece) {
        return (
            isOccupied(row, col) &&
            board[row][col].getColor() == piece.getColor()
        );
    }

    public boolean occupiedByAlly(int row, int col) {
        return (
            isOccupied(row, col) && board[row][col].getColor() == colorPlaying
        );
    }

    public boolean isOccupied(int rank, int file) {
        return board[rank][file] != null;
    }

    public int[] findKing() {
        for (int y = 0; y < board.length; ++y) {
            for (int x = 0; x < board[y].length; ++x) {
                if (
                    isOccupied(y, x) && board[y][x].getColor() == colorPlaying
                ) {
                    if (board[y][x].getName().equals("King")) {
                        return new int[] { y, x };
                    }
                }
            }
        }

        return new int[] { -1, -1 };
    }

    public boolean checkInCheck() {
        inCheck = false;

        int[] kingCoords = findKing(); // from the screen to the ring to the pen to the king where's my crown that's my bling always drama when I ring

        for (ChessPiece[] pieces : board) {
            for (ChessPiece piece : pieces) {
                if (piece != null && piece.getColor() != colorPlaying) {
                    List<ChessPiece.Move> moves = piece.getValidMoves(this);

                    for (ChessPiece.Move move : moves) {
                        if (move.equals(-1, -1, kingCoords[0], kingCoords[1])) {
                            inCheck = true;
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean checkStalemate() {
        for (ChessPiece[] pieces : board) {
            for (ChessPiece piece : pieces) {
                if (piece != null && piece.getColor() == colorPlaying) {
                    List<ChessPiece.Move> moves = piece.getValidMoves(this);
                    if (moves.size() > 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean checkInsufficientMaterial() {
        return false;
        /*
        boolean whiteInsufficient = false;
        boolean blackInsufficient = false;

        int bishopValue = 0;

        int whb = 0, whk = 0, wht = 0, bhb = 0, bhk = 0, bht = 0;

        for (ChessPiece[] pieces : board) {
            for (ChessPiece piece : pieces) {
                if (piece == null) continue;

                if (
                    piece.getName().equals("Pawn") ||
                    piece.getName().equals("Rook") ||
                    piece.getName().equals("Queen")
                ) {
                    return false;
                }

                if (piece.getColor() == 0) {
                    wht++;
                    if (piece.getName().equals("Bishop")) {
                        whb++;
                        bishopValue += piece.getFile();
                    } else if (piece.getName().equals("Knight")) {
                        whk++;
                    }
                } else {
                    bht++;
                    if (piece.getName().equals("Bishop")) {
                        bhb++;
                        bishopValue += piece.getFile();
                    } else if (piece.getName().equals("Knight")) {
                        bhk++;
                    }
                }
            }
        }

        if (
            whb == 1 && bhb == 1 && whk == 0 && bhk == 0 && bishopValue % 2 == 0
        ) {
            return true;
        } else {
            if (whb + whk < 2 && bhb + bhk < 2) {
                return true;
            }

            if (whk == 2 && bhb + bhk == 0) {
                return true;
            }

            return false;
            }*/
    }

    public void endGame(int winner) {
        timer.stop();

        io.bold();
        System.out.println("Game Over!");
        io.unbold();

        String result = "";
        switch (winner) {
            case -2:
                result = "Draw by Insufficient Material";
                break;
            case -1:
                result = "Draw by Stalemate";
                break;
            case 0:
                result = "White wins!";
                break;
            case 1:
                result = "Black wins!";
                break;
            default:
                result = "bro I got no clue who won :skull:";
                break;
        }

        System.out.println(result);

        System.exit(0);
    }

    private ChessPiece[][] deepCopyBoard() {
        ChessPiece[][] copy = new ChessPiece[8][8];
        /*for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] != null) {
                    copy[i][j] = board[i][j].copy();
                }
            }
            }*/
        return copy;
    }

    private void drawSquare(
        int rank,
        int file,
        int row,
        int col,
        boolean white
    ) {
        ChessPiece piece = board[rank][file];
        char c = ' ';
        if (piece != null && piece.getColor() == 0) {
            if (white) io.pushColor(ChessColorConf.LIGHT_LIGHT);
            else io.pushColor(ChessColorConf.DARK_LIGHT);
            c = piece.getUnicode();
        } else if (piece != null && piece.getColor() == 1) {
            if (white) io.pushColor(ChessColorConf.LIGHT_DARK);
            else io.pushColor(ChessColorConf.DARK_DARK);
            c = piece.getUnicode();
        } else {
            c = ' ';
            if (white) io.pushColor(ChessColorConf.LIGHT_DARK);
            else io.pushColor(ChessColorConf.DARK_DARK);
        }

        io.bold();

        io.mvwrite(row, col, "       ");
        io.mvwrite(row + 1, col, "   " + c + "   ");
        io.mvwrite(row + 2, col, "       ");
        io.popColor();

        io.unbold();

        if (white) io.pushColor(ChessColorConf.LIGHT_DARK);
        else io.pushColor(ChessColorConf.DARK_DARK);

        if (
            inCheck &&
            isOccupied(rank, file) &&
            board[rank][file].getName().equals("King")
        ) {
            if (white) io.pushColor(ChessColorConf.LIGHT_RED);
            else io.pushColor(ChessColorConf.DARK_RED);

            io.mvputch(row, col + 1, '◆');
            io.mvputch(row + 2, col + 1, '◆');
            io.mvputch(row + 2, col + 5, '◆');
            io.mvputch(row, col + 5, '◆');

            io.popColor();
        }

        if (selY == rank && selX == file) {
            io.mvputch(row + 1, col + 1, '▶');
            io.mvputch(row + 1, col + 5, '◀');
            io.mvputch(row, col + 3, '▼');
            io.mvputch(row + 2, col + 3, '▲');
        }

        if (lockY == rank && lockX == file) {
            io.mvputch(row, col + 1, '◢');
            io.mvputch(row + 2, col + 1, '◥');
            io.mvputch(row + 2, col + 5, '◤');
            io.mvputch(row, col + 5, '◣');
        }

        for (ChessPiece.Move move : moves) {
            if (move.equals(-1, -1, rank, file)) {
                if (isOccupied(rank, file) && !occupiedByAlly(rank, file)) {
                    io.mvputch(row, col + 1, '◆');
                    io.mvputch(row + 2, col + 1, '◆');
                    io.mvputch(row + 2, col + 5, '◆');
                    io.mvputch(row, col + 5, '◆');
                } else {
                    io.mvputch(row, col + 1, '⟋');
                    io.mvputch(row + 2, col + 1, '⟍');
                    io.mvputch(row + 2, col + 5, '⟋');
                    io.mvputch(row, col + 5, '⟍');
                }

                hot.add(new int[] { rank, file });
            }
        }

        io.popColor();
    }

    private void drawSquare(int rank, int file) {
        drawSquare(
            rank,
            file,
            2 + rank * 3,
            1 + file * 7,
            (rank + file) % 2 == 0
        );
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    public void updateTimerDisplay(int whiteTimeSeconds, int blackTimeSeconds) {
        System.out.print("\033[s");
        io.pushColor(ChessColorConf.TIMER_BG);
        io.mvwrite(1, 2, " " + formatTime(blackTimeSeconds) + " ");
        io.mvwrite(2 + 8 * 3, 2, " " + formatTime(whiteTimeSeconds) + " ");
        io.popColor();
        System.out.print("\033[u");
    }

    public boolean draw() {
        updateTimerDisplay(timer.getWhiteTime(), timer.getBlackTime());
        io.mvwrite(1, 10, "Black");
        io.mvwrite(2 + 8 * 3, 10, "White");

        if (selX >= 0 && selY >= 0) {
            if (colorPlaying == 0) {
                io.mvwrite(
                    2 + 8 * 3,
                    17,
                    coordsToAlgebraic(selY, selX) + "   "
                );
            } else {
                io.mvwrite(1, 17, coordsToAlgebraic(selY, selX) + "   ");
            }
        }

        if (hot != null && hot.size() > 0) {
            List<int[]> copy = new ArrayList<int[]>(hot);
            hot.clear();
            for (int[] a : copy) {
                if (a[0] >= 0 && a[1] >= 0 && a[0] < 8 && a[1] < 8) {
                    drawSquare(a[0], a[1]);
                }
            }
        }

        if (superHot) {
            superHot = false;

            for (int rank = 0; rank < 8; rank++) {
                for (int file = 0; file < 8; file++) {
                    drawSquare(rank, file);
                }
            }
        }

        io.pushColor(ChessColorConf.LIGHT_DARK);
        io.mvputch(2, 1, '8');
        io.mvputch(8, 1, '6');
        io.mvputch(14, 1, '4');
        io.mvputch(20, 1, '2');

        io.mvputch(25, 14, 'b');
        io.mvputch(25, 28, 'd');
        io.mvputch(25, 42, 'f');
        io.mvputch(25, 56, 'h');
        io.popColor();

        io.pushColor(ChessColorConf.DARK_DARK);
        io.mvputch(5, 1, '7');
        io.mvputch(11, 1, '5');
        io.mvputch(17, 1, '3');
        io.mvputch(23, 1, '1');

        io.mvputch(25, 7, 'a');
        io.mvputch(25, 21, 'c');
        io.mvputch(25, 35, 'e');
        io.mvputch(25, 49, 'g');
        io.popColor();

        return false;
    }

    private void moveSelection(int xoff, int yoff) {
        if (selX < 0 || selY < 0) {
            int[] coord = findKing();
            selX = coord[1];
            selY = coord[0];

            if (selX > -1 && selY > -1) {
                hot.add(coord);
            }

            return;
        }

        hot.add(new int[] { selY, selX });

        selX += xoff;
        selY += yoff;

        if (selX < 0) selX = 7;
        if (selY < 0) selY = 7;
        if (selX > 7) selX = 0;
        if (selY > 7) selY = 0;

        hot.add(new int[] { selY, selX });
    }

    public boolean processCommand(String command) {
        if (command.length() == 1) {
            char c = command.charAt(0);

            switch (c) {
                case 'w':
                    moveSelection(0, -1);
                    break;
                case 's':
                    moveSelection(0, 1);
                    break;
                case 'd':
                    moveSelection(1, 0);
                    break;
                case 'a':
                    moveSelection(-1, 0);
                    break;
            }
        } else if (command.length() == 0) {
            if (selX >= 0 && selY >= 0) {
                if (isOccupied(selY, selX) && occupiedByAlly(selY, selX)) {
                    if (lockX != -1 && lockY != -1) {
                        hot.add(new int[] { lockY, lockX });
                    }

                    lockX = selX;
                    lockY = selY;
                    moves = board[selY][selX].getValidMoves(this);

                    for (ChessPiece.Move move : moves) {
                        for (int[] submove : move.submoves) {
                            hot.add(new int[] { submove[2], submove[3] });
                        }
                    }

                    hot.add(new int[] { selY, selX });
                } else if (lockX != -1 && lockY != -1) {
                    boolean executed = false;
                    for (ChessPiece.Move move : moves) {
                        if (move.equals(lockY, lockX, selY, selX)) {
                            move.execute(this);
                            executed = true;
                            break;
                        }
                    }

                    if (executed) {
                        moves.clear();
                        lockX = 0;
                        lockY = 0;
                        colorPlaying = colorPlaying == 0 ? 1 : 0;
                        moveIdx++;

                        checkInCheck();
                        if (inCheck) {
                            int[] kingCoords = findKing();
                            hot.add(kingCoords);
                        }

                        if (checkStalemate()) {
                            endGame(-1);
                        }

                        if (checkInsufficientMaterial()) {
                            endGame(-2);
                        }

                        timer.unpause();
                        timer.swapActive();
                    }
                }
            }
        }

        return false;
    }
}
