package source;

import java.util.ArrayList;
import java.util.List;

public abstract class ChessPiece {

    protected String name = "";
    protected int color = 0;
    protected int rank = 0;
    protected int file = 0;
    protected boolean moved = false;
    protected int moveCount = 0;
    protected int lastMoveId = -1;

    public class Move {

        ArrayList<int[]> submoves;
        boolean valid = false;

        Move(int fromRank, int fromFile, int rank, int file) {
            this.submoves = new ArrayList<int[]>();

            if (rank < 0 || rank >= 8 || file < 0 || file >= 8) return;

            this.valid = true;
            this.submoves.add(new int[] { fromRank, fromFile, rank, file });
        }

        void addMove(int fromRank, int fromFile, int rank, int file) {
            this.submoves.add(new int[] { fromRank, fromFile, rank, file });
        }

        public void invalidate() {
            this.valid = false;
        }

        public void execute(ChessBoard board) {
            for (int[] move : submoves) {
                if (move[2] < 0 || move[3] < 0) {
                    board.board[move[0]][move[1]] = null;
                    board.hot.add(new int[] { move[0], move[1] });
                    continue;
                }

                board.board[move[2]][move[3]] = board.board[move[0]][move[1]];
                board.board[move[0]][move[1]] = null;
                board.board[move[2]][move[3]].move(move[2], move[3]);
                board.board[move[2]][move[3]].lastMoveId = board.moveIdx;

                board.hot.add(new int[] { move[0], move[1] });
                board.hot.add(new int[] { move[2], move[3] });
            }
        }

        public boolean equals(int fromRank, int fromFile, int rank, int file) {
            for (int[] move : submoves) {
                if (
                    (fromRank == -1 || move[0] == fromRank) &&
                    (fromFile == -1 || move[1] == fromFile) &&
                    move[2] == rank &&
                    move[3] == file
                ) {
                    return true;
                }
            }

            return false;
        }

        private String coordsToAlgebraic(int row, int col) {
            return "" + Character.toString('a' + col) + (8 - row);
        }

        public String toString() {
            String s =
                getName() + "/" + getColor() + "/" + submoves.size() + " ";
            for (int[] mv : submoves) {
                s +=
                    "| " +
                    coordsToAlgebraic(mv[0], mv[1]) +
                    " -> " +
                    coordsToAlgebraic(mv[2], mv[3]) +
                    " |";
            }
            return s;
        }
    }

    ChessPiece(String name, int rank, int file, int color) {
        this.rank = rank;
        this.file = file;
        this.color = color;
        this.name = name;
    }

    public int getRank() {
        return rank;
    }

    public int getFile() {
        return file;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public void move(int rank, int file) {
        this.rank = rank;
        this.file = file;

        onMove();
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void onMove() {
        moved = true;
        this.moveCount++;
    }

    public int getMoveCount() {
        return this.moveCount;
    }

    public abstract List<ChessPiece.Move> getValidMoves(ChessBoard board);

    public abstract char getUnicode();
}

class Pawn extends ChessPiece {

    public Pawn(int rank, int file, int color) {
        super("Pawn", rank, file, color);
    }

    public List<ChessPiece.Move> getValidMoves(ChessBoard board) {
        ArrayList<ChessPiece.Move> moves = new ArrayList<ChessPiece.Move>();

        int rank = getRank(), file = getFile();

        int dir = this.color == 0 ? -1 : 1;

        if (
            board.inBounds(rank + dir, file) &&
            !board.isOccupied(rank + dir, file)
        ) {
            moves.add(new ChessPiece.Move(rank, file, rank + dir, file));

            if (
                !this.moved &&
                board.inBounds(rank + dir * 2, file) &&
                !board.isOccupied(rank + dir * 2, file)
            ) {
                moves.add(
                    new ChessPiece.Move(rank, file, rank + dir * 2, file)
                );
            }
        }

        if (
            board.inBounds(rank + dir, file - 1) &&
            board.isOccupied(rank + dir, file - 1) &&
            !board.occupiedByAlly(rank + dir, file - 1, this)
        ) {
            moves.add(new ChessPiece.Move(rank, file, rank + dir, file - 1));
        }

        if (
            board.inBounds(rank + dir, file + 1) &&
            board.isOccupied(rank + dir, file + 1) &&
            !board.occupiedByAlly(rank + dir, file + 1, this)
        ) {
            moves.add(new ChessPiece.Move(rank, file, rank + dir, file + 1));
        }

        if (
            board.inBounds(rank, file - 1) &&
            board.isOccupied(rank, file - 1) &&
            !board.occupiedByAlly(rank, file - 1) &&
            board.board[rank][file - 1].getName().equals("Pawn") &&
            file == (this.color == 0 ? 4 : 3) &&
            board.board[rank][file - 1].getMoveCount() == 1 &&
            board.moveIdx == board.board[rank][file - 1].lastMoveId + 1
        ) {
            ChessPiece.Move move = new ChessPiece.Move(
                rank,
                file,
                rank + dir,
                file - 1
            );
            move.addMove(rank, file - 1, -1, -1);
            moves.add(move);
        }

        if (
            board.inBounds(rank, file + 1) &&
            board.isOccupied(rank, file + 1) &&
            !board.occupiedByAlly(rank, file + 1) &&
            board.board[rank][file + 1].getName().equals("Pawn") &&
            file == (this.color == 0 ? 4 : 3) &&
            board.board[rank][file + 1].getMoveCount() == 1 &&
            board.moveIdx == board.board[rank][file + 1].lastMoveId + 1
        ) {
            ChessPiece.Move move = new ChessPiece.Move(
                rank,
                file,
                rank + dir,
                file + 1
            );
            move.addMove(rank, file + 1, -1, -1);
            moves.add(move);
        }

        return moves;
    }

    public char getUnicode() {
        return '♟';
    }
}

class Rook extends ChessPiece {

    public Rook(int rank, int file, int color) {
        super("Rook", rank, file, color);
    }

    public boolean processPosition(
        int orank,
        int ofile,
        int rank,
        int file,
        ArrayList<ChessPiece.Move> moves,
        ChessBoard board
    ) {
        if (board.isOccupied(rank, file)) {
            if (!board.occupiedByAlly(rank, file, this)) {
                moves.add(new ChessPiece.Move(orank, ofile, rank, file));
            }

            return true;
        }

        moves.add(new ChessPiece.Move(orank, ofile, rank, file));
        return false;
    }

    public List<ChessPiece.Move> getValidMoves(ChessBoard board) {
        ArrayList<ChessPiece.Move> moves = new ArrayList<ChessPiece.Move>();

        int rank = getRank(), file = getFile();

        for (int i = file + 1; i < 8; ++i) {
            if (processPosition(rank, file, rank, i, moves, board)) {
                break;
            }
        }

        for (int i = file - 1; i >= 0; --i) {
            if (processPosition(rank, file, rank, i, moves, board)) {
                break;
            }
        }

        for (int i = rank + 1; i < 8; ++i) {
            if (processPosition(rank, file, i, file, moves, board)) {
                break;
            }
        }

        for (int i = rank - 1; i >= 0; --i) {
            if (processPosition(rank, file, i, file, moves, board)) {
                break;
            }
        }

        return moves;
    }

    public char getUnicode() {
        return '♜';
    }
}

class Knight extends ChessPiece {

    public Knight(int rank, int file, int color) {
        super("Knight", rank, file, color);
    }

    public void processPosition(
        int orank,
        int ofile,
        int rank,
        int file,
        ArrayList<ChessPiece.Move> moves,
        ChessBoard board
    ) {
        if (!board.inBounds(rank, file)) return;

        if (board.isOccupied(rank, file)) {
            if (!board.occupiedByAlly(rank, file, this)) {
                moves.add(new ChessPiece.Move(orank, ofile, rank, file));
            }
        } else {
            moves.add(new ChessPiece.Move(orank, ofile, rank, file));
        }
    }

    public List<ChessPiece.Move> getValidMoves(ChessBoard board) {
        ArrayList<ChessPiece.Move> moves = new ArrayList<ChessPiece.Move>();

        int rank = getRank(), file = getFile();

        processPosition(rank, file, rank + 2, file + 1, moves, board);
        processPosition(rank, file, rank + 2, file - 1, moves, board);
        processPosition(rank, file, rank + 1, file + 2, moves, board);
        processPosition(rank, file, rank - 1, file + 2, moves, board);
        processPosition(rank, file, rank - 2, file + 1, moves, board);
        processPosition(rank, file, rank - 2, file - 1, moves, board);
        processPosition(rank, file, rank + 1, file - 2, moves, board);
        processPosition(rank, file, rank - 1, file - 2, moves, board);

        return moves;
    }

    public char getUnicode() {
        return '♞';
    }
}

class Bishop extends ChessPiece {

    public Bishop(int rank, int file, int color) {
        super("Bishop", rank, file, color);
    }

    public boolean processPosition(
        int orank,
        int ofile,
        int rank,
        int file,
        ArrayList<ChessPiece.Move> moves,
        ChessBoard board
    ) {
        if (board.isOccupied(rank, file)) {
            if (!board.occupiedByAlly(rank, file, this)) {
                moves.add(new ChessPiece.Move(orank, ofile, rank, file));
            }

            return true;
        }

        moves.add(new ChessPiece.Move(orank, ofile, rank, file));
        return false;
    }

    public List<ChessPiece.Move> getValidMoves(ChessBoard board) {
        ArrayList<ChessPiece.Move> moves = new ArrayList<ChessPiece.Move>();

        int rank = getRank(), file = getFile();

        // Top-right diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank + i < 8 && file + i < 8) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank + i,
                        file + i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        // Bottom-left diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank - i >= 0 && file - i >= 0) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank - i,
                        file - i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        // Top-left diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank + i < 8 && file - i >= 0) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank + i,
                        file - i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        // Bottom-right diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank - i >= 0 && file + i < 8) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank - i,
                        file + i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        return moves;
    }

    public char getUnicode() {
        return '♝';
    }
}

class Queen extends ChessPiece {

    public Queen(int rank, int file, int color) {
        super("Queen", rank, file, color);
    }

    public boolean processPosition(
        int orank,
        int ofile,
        int rank,
        int file,
        ArrayList<ChessPiece.Move> moves,
        ChessBoard board
    ) {
        if (board.isOccupied(rank, file)) {
            if (!board.occupiedByAlly(rank, file, this)) {
                moves.add(new ChessPiece.Move(orank, ofile, rank, file));
            }

            return true;
        }

        moves.add(new ChessPiece.Move(orank, ofile, rank, file));
        return false;
    }

    public List<ChessPiece.Move> getValidMoves(ChessBoard board) {
        ArrayList<ChessPiece.Move> moves = new ArrayList<ChessPiece.Move>();

        int rank = getRank(), file = getFile();

        for (int i = file + 1; i < 8; ++i) {
            if (processPosition(rank, file, rank, i, moves, board)) {
                break;
            }
        }

        for (int i = file - 1; i >= 0; --i) {
            if (processPosition(rank, file, rank, i, moves, board)) {
                break;
            }
        }

        for (int i = rank + 1; i < 8; ++i) {
            if (processPosition(rank, file, i, file, moves, board)) {
                break;
            }
        }

        for (int i = rank - 1; i >= 0; --i) {
            if (processPosition(rank, file, i, file, moves, board)) {
                break;
            }
        }

        // Top-right diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank + i < 8 && file + i < 8) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank + i,
                        file + i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        // Bottom-left diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank - i >= 0 && file - i >= 0) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank - i,
                        file - i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        // Top-left diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank + i < 8 && file - i >= 0) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank + i,
                        file - i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        // Bottom-right diagonal
        for (int i = 1; i < 8; ++i) {
            if (rank - i >= 0 && file + i < 8) {
                if (
                    processPosition(
                        rank,
                        file,
                        rank - i,
                        file + i,
                        moves,
                        board
                    )
                ) {
                    break;
                }
            }
        }

        return moves;
    }

    public char getUnicode() {
        return '♛';
    }
}

class King extends ChessPiece {

    public King(int rank, int file, int color) {
        super("King", rank, file, color);
    }

    public void processPosition(
        int orank,
        int ofile,
        int rank,
        int file,
        ArrayList<ChessPiece.Move> moves,
        ChessBoard board
    ) {
        if (!board.inBounds(rank, file)) return;

        if (board.isOccupied(rank, file)) {
            if (!board.occupiedByAlly(rank, file, this)) {
                moves.add(new ChessPiece.Move(orank, ofile, rank, file));
            }
        } else {
            moves.add(new ChessPiece.Move(orank, ofile, rank, file));
        }
    }

    public List<ChessPiece.Move> getValidMoves(ChessBoard board) {
        ArrayList<ChessPiece.Move> moves = new ArrayList<ChessPiece.Move>();

        int rank = getRank(), file = getFile();

        processPosition(rank, file, rank + 1, file, moves, board);
        processPosition(rank, file, rank + 1, file - 1, moves, board);
        processPosition(rank, file, rank + 1, file + 1, moves, board);
        processPosition(rank, file, rank - 1, file, moves, board);
        processPosition(rank, file, rank - 1, file - 1, moves, board);
        processPosition(rank, file, rank - 1, file + 1, moves, board);
        processPosition(rank, file, rank, file - 1, moves, board);
        processPosition(rank, file, rank, file + 1, moves, board);

        return moves;
    }

    public char getUnicode() {
        return '♚';
    }
}
