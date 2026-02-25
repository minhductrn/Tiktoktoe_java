import java.util.Scanner;

public class tiktoktoe {
    static final int N = 9;
    static final int WIN = 5; // five-in-a-row to win on 9x9
    static char[][] board = new char[N][N];
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        initBoard();
        char player = 'X';
        int moves = 0;

        while (moves < N * N) {
            printBoard();
            System.out.println("Player " + player + " turn. Enter row and column (1-9), e.g. '3 5':");
            int r = -1, c = -1;
            while (true) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    System.out.println("Please enter two numbers: row and column (1-9). Try again:");
                    continue;
                }
                try {
                    r = Integer.parseInt(parts[0]) - 1;
                    c = Integer.parseInt(parts[1]) - 1;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Use numbers 1-9. Try again:");
                    continue;
                }
                if (r < 0 || r >= N || c < 0 || c >= N) {
                    System.out.println("Out of range. Use 1..9 for both row and column. Try again:");
                    continue;
                }
                if (board[r][c] != ' ') {
                    System.out.println("Cell already occupied. Pick another:");
                    continue;
                }
                break;
            }

            board[r][c] = player;
            moves++;

            if (checkWin(r, c, player)) {
                printBoard();
                System.out.println("Player " + player + " wins!");
                return;
            }

            player = (player == 'X') ? 'O' : 'X';
        }

        printBoard();
        System.out.println("It's a draw!");
    }

    static void initBoard() {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) board[i][j] = ' ';
        }
    }

    static void printBoard() {
        System.out.print("   ");
        for (int c = 1; c <= N; c++) System.out.print(c + " ");
        System.out.println();
        System.out.println("  +" + "--".repeat(N) + "+");
        for (int i = 0; i < N; i++) {
            System.out.printf("%2d|", i + 1);
            for (int j = 0; j < N; j++) {
                System.out.print(board[i][j]);
                System.out.print(' ');
            }
            System.out.println("|");
        }
        System.out.println("  +" + "--".repeat(N) + "+");
    }

    // Check only lines passing through (r,c)
    static boolean checkWin(int r, int c, char p) {
        int[][] dirs = {{0,1},{1,0},{1,1},{1,-1}};
        for (int[] d : dirs) {
            int cnt = 1;
            cnt += countDir(r, c, d[0], d[1], p);
            cnt += countDir(r, c, -d[0], -d[1], p);
            if (cnt >= WIN) return true;
        }
        return false;
    }

    static int countDir(int r, int c, int dr, int dc, char p) {
        int cnt = 0;
        int i = r + dr, j = c + dc;
        while (i >= 0 && i < N && j >= 0 && j < N && board[i][j] == p) {
            cnt++; i += dr; j += dc;
        }
        return cnt;
    }
}
