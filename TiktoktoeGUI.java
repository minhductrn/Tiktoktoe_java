import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class TiktoktoeGUI {
    static final int N = 9;
    static final int WIN = 5;
    //AI: tuning constants
    static final int INF = 1_000_000_000;
    static final int[] WINDOW_SCORE = {0, 1, 12, 140, 2000, 200000};
    static final int SEARCH_DEPTH = 3;
    static final int MAX_CANDIDATES = 12;
    private final JFrame frame;
    private final JLabel infoLabel;
    private final JButton[][] buttons = new JButton[N][N];
    private final char[][] board = new char[N][N];
    private final Random random = new Random();
    private final int gameMode;
    private char current = 'X';
    private int moves = 0;

    private static final int MODE_PVP = 1;
    private static final int MODE_PVC = 2;

    public TiktoktoeGUI() {
        gameMode = chooseMode();
        frame = new JFrame("Tiktoktoe 9x9");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel grid = new JPanel(new GridLayout(N, N));
        Font f = new Font(Font.SANS_SERIF, Font.BOLD, 20);

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                final int r = i, c = j;
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(f);
                buttons[i][j].setFocusPainted(false);
                grid.add(buttons[i][j]);
                board[i][j] = ' ';
                buttons[i][j].addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleMove(r, c);
                    }
                });
            }
        }

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoLabel = new JLabel();
        top.add(infoLabel);

        JButton reset = new JButton("Restart");
        reset.addActionListener(e -> resetBoard());
        top.add(reset);

        frame.add(top, BorderLayout.NORTH);
        frame.add(grid, BorderLayout.CENTER);
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);
        updateHeader();
        frame.setVisible(true);
    }

    private void handleMove(int r, int c) {
        if (isComputerTurn()) return;
        placeMove(r, c);
        if (isComputerTurn()) {
            computerMove();
        }
    }

    private void placeMove(int r, int c) {
        if (board[r][c] != ' ') return;
        board[r][c] = current;
        buttons[r][c].setText(String.valueOf(current));
        buttons[r][c].setEnabled(false);
        moves++;
        if (checkWin(r, c, current)) {
            String winner = (gameMode == MODE_PVC && current == 'O') ? "Computer" : "Player " + current;
            JOptionPane.showMessageDialog(frame, winner + " wins!");
            int opt = JOptionPane.showConfirmDialog(frame, "Play again?", "Restart", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) resetBoard();
            else System.exit(0);
            return;
        }
        if (moves >= N * N) {
            JOptionPane.showMessageDialog(frame, "It's a draw!");
            int opt = JOptionPane.showConfirmDialog(frame, "Play again?", "Restart", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) resetBoard();
            else System.exit(0);
            return;
        }
        current = (current == 'X') ? 'O' : 'X';
        updateHeader();
    }

    private void updateHeader() {
        String modeText = (gameMode == MODE_PVP) ? "P1 vs P2" : "P1 vs Computer";
        String turnText;
        if (gameMode == MODE_PVC && current == 'O') {
            turnText = "Computer";
        } else {
            turnText = "Player " + current;
        }
        infoLabel.setText("Mode: " + modeText + " | Current: " + turnText);
    }

    private void resetBoard() {
        current = 'X';
        moves = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                board[i][j] = ' ';
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        }
        updateHeader();
    }

    private int chooseMode() {
        String[] options = {"Player 1 vs Player 2", "Player 1 vs Computer"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Choose game mode",
                "Tiktoktoe Mode",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == 1) return MODE_PVC;
        return MODE_PVP;
    }

    private boolean isComputerTurn() {
        return gameMode == MODE_PVC && current == 'O';
    }

    //AI: computer turn entry
    private void computerMove() {
        int[] move = chooseBestMinimaxMove();
        if (move == null) {
            move = randomMove();
        }
        if (move != null) {
            placeMove(move[0], move[1]);
        }
    }

    //AI: root move selection
    private int[] chooseBestMinimaxMove() {
        List<int[]> candidates = getCandidateMoves();
        if (candidates.isEmpty()) return null;

        int bestScore = -INF;
        int[] bestMove = null;
        int alpha = -INF;
        int beta = INF;

        for (int[] move : candidates) {
            int r = move[0], c = move[1];
            board[r][c] = 'O';
            int score;
            if (checkWin(r, c, 'O')) {
                score = INF / 2;
            } else {
                score = minimax(SEARCH_DEPTH - 1, false, alpha, beta, r, c, 'O');
            }
            board[r][c] = ' ';

            if (score > bestScore || (score == bestScore && random.nextBoolean())) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
        }
        return bestMove;
    }

    //AI: depth-limited minimax with alpha-beta pruning
    private int minimax(int depth, boolean maximizing, int alpha, int beta, int lastR, int lastC, char lastPlayer) {
        if (lastR >= 0 && checkWin(lastR, lastC, lastPlayer)) {
            return lastPlayer == 'O' ? (INF / 2 + depth) : (-INF / 2 - depth);
        }
        if (depth == 0 || moves >= N * N) {
            return evaluateBoard();
        }

        List<int[]> candidates = getCandidateMoves();
        if (candidates.isEmpty()) {
            return evaluateBoard();
        }

        char player = maximizing ? 'O' : 'X';
        if (maximizing) {
            int best = -INF;
            for (int[] move : candidates) {
                int r = move[0], c = move[1];
                board[r][c] = player;
                int score = minimax(depth - 1, false, alpha, beta, r, c, player);
                board[r][c] = ' ';
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
                if (beta <= alpha) break;
            }
            return best;
        }

        int best = INF;
        for (int[] move : candidates) {
            int r = move[0], c = move[1];
            board[r][c] = player;
            int score = minimax(depth - 1, true, alpha, beta, r, c, player);
            board[r][c] = ' ';
            best = Math.min(best, score);
            beta = Math.min(beta, best);
            if (beta <= alpha) break;
        }
        return best;
    }

    //AI: board scoring
    private int evaluateBoard() {
        return evaluateFor('O') - evaluateFor('X');
    }

    //AI: evaluate one player's patterns
    private int evaluateFor(char player) {
        char opp = (player == 'O') ? 'X' : 'O';
        int score = 0;
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                for (int[] d : dirs) {
                    int endR = r + (WIN - 1) * d[0];
                    int endC = c + (WIN - 1) * d[1];
                    if (endR < 0 || endR >= N || endC < 0 || endC >= N) continue;

                    int playerCount = 0;
                    int oppCount = 0;
                    for (int k = 0; k < WIN; k++) {
                        char cell = board[r + k * d[0]][c + k * d[1]];
                        if (cell == player) playerCount++;
                        else if (cell == opp) oppCount++;
                    }
                    if (oppCount == 0) {
                        score += WINDOW_SCORE[playerCount];
                    }
                }
            }
        }
        return score;
    }

    //AI: candidate move generation near existing pieces
    private List<int[]> getCandidateMoves() {
        List<int[]> candidates = new ArrayList<>();
        boolean hasPiece = false;

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (board[r][c] != ' ') {
                    hasPiece = true;
                }
            }
        }

        if (!hasPiece) {
            candidates.add(new int[]{N / 2, N / 2});
            return candidates;
        }

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (board[r][c] != ' ') continue;
                if (hasNeighbor(r, c, 1)) {
                    candidates.add(new int[]{r, c});
                }
            }
        }

        Collections.shuffle(candidates, random);
        candidates.sort(Comparator.comparingInt((int[] m) -> localHeuristic(m[0], m[1])).reversed());
        if (candidates.size() > MAX_CANDIDATES) {
            return new ArrayList<>(candidates.subList(0, MAX_CANDIDATES));
        }
        return candidates;
    }

    //AI: neighborhood filter
    private boolean hasNeighbor(int r, int c, int dist) {
        for (int dr = -dist; dr <= dist; dr++) {
            for (int dc = -dist; dc <= dist; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr < 0 || nr >= N || nc < 0 || nc >= N) continue;
                if (board[nr][nc] != ' ') return true;
            }
        }
        return false;
    }

    //AI: local priority for move ordering
    private int localHeuristic(int r, int c) {
        int score = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int nr = r + dr, nc = c + dc;
                if (nr < 0 || nr >= N || nc < 0 || nc >= N) continue;
                if (board[nr][nc] == 'O') score += 3;
                else if (board[nr][nc] == 'X') score += 2;
            }
        }
        return score;
    }

    private int[] randomMove() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                if (board[r][c] == ' ') {
                    empty.add(new int[]{r, c});
                }
            }
        }
        if (empty.isEmpty()) return null;
        return empty.get(random.nextInt(empty.size()));
    }

    // same win-checking logic as console version
    private boolean checkWin(int r, int c, char p) {
        int[][] dirs = {{0,1},{1,0},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int cnt = 1;
            cnt += countDir(r, c, d[0], d[1], p);
            cnt += countDir(r, c, -d[0], -d[1], p);
            if (cnt >= WIN) return true;
        }
        return false;
    }

    private int countDir(int r, int c, int dr, int dc, char p) {
        int cnt = 0;
        int i = r + dr, j = c + dc;
        while (i >= 0 && i < N && j >= 0 && j < N && board[i][j] == p) {
            cnt++; i += dr; j += dc;
        }
        return cnt;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TiktoktoeGUI::new);
    }
}
