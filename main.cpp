#include <algorithm>
#include <array>
#include <iostream>
#include <limits>
#include <string>
#include <vector>

enum class Player
{
    None = 0,
    X,
    O
};

static char toChar(Player p)
{
    switch (p)
    {
    case Player::X:
        return 'X';
    case Player::O:
        return 'O';
    default:
        return ' ';
    }
}

class Board
{
public:
    Board() { reset(); }

    void reset()
    {
        cells.fill(Player::None);
    }

    bool makeMove(int index, Player p)
    {
        if (index < 0 || index >= 9) return false;
        if (cells[index] != Player::None) return false;
        cells[index] = p;
        return true;
    }

    void undoMove(int index) { if (index >= 0 && index < 9) cells[index] = Player::None; }

    std::vector<int> availableMoves() const
    {
        std::vector<int> moves;
        for (int i = 0; i < 9; ++i)
            if (cells[i] == Player::None) moves.push_back(i);
        return moves;
    }

    bool isFull() const
    {
        return std::none_of(cells.begin(), cells.end(), [](Player p) { return p == Player::None; });
    }

    Player winner() const
    {
        static const int lines[8][3] = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // cols
            {0, 4, 8}, {2, 4, 6}             // diags
        };

        for (auto &line : lines)
        {
            Player a = cells[line[0]];
            if (a != Player::None && a == cells[line[1]] && a == cells[line[2]])
                return a;
        }
        return Player::None;
    }

    void display() const
    {
        std::cout << "\n";
        for (int r = 0; r < 3; ++r)
        {
            for (int c = 0; c < 3; ++c)
            {
                int i = r * 3 + c;
                char ch = (cells[i] == Player::None) ? char('1' + i) : toChar(cells[i]);
                std::cout << ' ' << ch << ' ';
                if (c < 2) std::cout << '|';
            }
            std::cout << "\n";
            if (r < 2) std::cout << "-----------\n";
        }
        std::cout << "\n";
    }

private:
    std::array<Player, 9> cells;
};

// Minimax-based AI
struct MoveScore { int index; int score; };

MoveScore minimax(Board &board, Player current, Player aiPlayer, int depth = 0)
{
    Player win = board.winner();
    if (win == aiPlayer) return {-1, 10 - depth};
    if (win != Player::None && win != aiPlayer) return {-1, depth - 10};
    if (board.isFull()) return {-1, 0};

    std::vector<MoveScore> moves;
    for (int idx : board.availableMoves())
    {
        board.makeMove(idx, current);
        MoveScore ms = minimax(board, (current == Player::X) ? Player::O : Player::X, aiPlayer, depth + 1);
        ms.index = idx;
        moves.push_back(ms);
        board.undoMove(idx);
    }

    // Choose best depending on whose turn it is
    if (current == aiPlayer)
    {
        // maximize
        return *std::max_element(moves.begin(), moves.end(), [](const MoveScore &a, const MoveScore &b) { return a.score < b.score; });
    }
    else
    {
        // minimize
        return *std::min_element(moves.begin(), moves.end(), [](const MoveScore &a, const MoveScore &b) { return a.score < b.score; });
    }
}

int readIntInRange(int min, int max)
{
    while (true)
    {
        int v;
        if (!(std::cin >> v))
        {
            std::cin.clear();
            std::cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
            std::cout << "Invalid input. Try again: ";
            continue;
        }
        if (v < min || v > max)
        {
            std::cout << "Please enter a number between " << min << " and " << max << ": ";
            continue;
        }
        return v;
    }
}

int main()
{
    std::cout << "Tic-Tac-Toe\n";
    Board board;

    std::cout << "Choose mode:\n1) Player vs Player\n2) Player vs Computer (AI)\nSelect 1 or 2: ";
    int mode = readIntInRange(1, 2);

    Player human1 = Player::X;
    Player human2 = Player::O;
    Player aiPlayer = Player::None;
    Player turn = Player::X;

    if (mode == 2)
    {
        std::cout << "Who goes first?\n1) You (X)\n2) Computer (X)\nSelect 1 or 2: ";
        int first = readIntInRange(1, 2);
        if (first == 1)
        {
            human1 = Player::X;
            aiPlayer = Player::O;
            turn = Player::X;
        }
        else
        {
            human1 = Player::O; // human plays O
            aiPlayer = Player::X;
            turn = Player::X;
        }
    }

    while (true)
    {
        board.display();
        Player w = board.winner();
        if (w != Player::None)
        {
            std::cout << "Player " << toChar(w) << " wins!\n";
            break;
        }
        if (board.isFull())
        {
            std::cout << "It's a draw.\n";
            break;
        }

        if (mode == 1)
        {
            std::cout << "Player " << toChar(turn) << ", enter cell (1-9): ";
            int cell = readIntInRange(1, 9) - 1;
            if (!board.makeMove(cell, turn))
            {
                std::cout << "Cell occupied or invalid. Try again.\n";
                continue;
            }
            turn = (turn == Player::X) ? Player::O : Player::X;
        }
        else // mode == 2
        {
            if (turn == aiPlayer)
            {
                std::cout << "Computer is thinking...\n";
                MoveScore best = minimax(board, aiPlayer, aiPlayer);
                board.makeMove(best.index, aiPlayer);
                std::cout << "Computer plays " << (best.index + 1) << "\n";
                turn = (turn == Player::X) ? Player::O : Player::X;
            }
            else
            {
                std::cout << "Your turn (" << toChar(turn) << "). Enter cell (1-9): ";
                int cell = readIntInRange(1, 9) - 1;
                if (!board.makeMove(cell, turn))
                {
                    std::cout << "Cell occupied or invalid. Try again.\n";
                    continue;
                }
                turn = (turn == Player::X) ? Player::O : Player::X;
            }
        }
    }

    board.display();
    std::cout << "Game over.\n";
    return 0;
}