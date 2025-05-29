import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class TicTacToeGUI extends JFrame implements ActionListener {
    static final int SIZE = 3;
    static final char EMPTY = '-';
    static final char PLAYER = 'O';
    static final char AI = 'X';

    JButton[][] buttons = new JButton[SIZE][SIZE];
    char[][] board = new char[SIZE][SIZE];
    JTextArea logArea = new JTextArea(15, 25);
    JPanel analysisPanel = new JPanel(new GridLayout(0, 1));

    public TicTacToeGUI() {
        setTitle("Tic Tac Toe - Alfa Beta Budamalı AI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(SIZE, SIZE));
        initializeBoard();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(new Font(Font.SANS_SERIF, Font.BOLD, 60));
                buttons[i][j].addActionListener(this);
                boardPanel.add(buttons[i][j]);
            }
        }

        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        JScrollPane analysisScroll = new JScrollPane(analysisPanel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(new JLabel("Hamle Analizi"), BorderLayout.NORTH);
        rightPanel.add(analysisScroll, BorderLayout.CENTER);

        // 🆕 Puan Skalası
        JTextArea ratingLegend = new JTextArea(
                "Puan Skalası:\n" +
                        "5/5 - Mükemmel (kazanmaya yaklaştırır)\n" +
                        "4/5 - İyi\n" +
                        "3/5 - Orta (beraberliğe götürebilir)\n" +
                        "2/5 - Riskli\n" +
                        "1/5 - Kötü\n" +
                        "0/5 - Felaket (kaybettirir)"
        );
        ratingLegend.setEditable(false);
        ratingLegend.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        ratingLegend.setBackground(new Color(240, 240, 240));

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(logScroll, BorderLayout.NORTH);
        containerPanel.add(rightPanel, BorderLayout.CENTER);
        containerPanel.add(ratingLegend, BorderLayout.SOUTH);

        add(boardPanel, BorderLayout.CENTER);
        add(containerPanel, BorderLayout.EAST);

        pack();
        setVisible(true);
    }

    void initializeBoard() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                board[i][j] = EMPTY;
    }

    public void actionPerformed(ActionEvent e) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (e.getSource() == buttons[i][j] && board[i][j] == EMPTY) {
                    board[i][j] = PLAYER;
                    buttons[i][j].setText(String.valueOf(PLAYER));
                    if (checkGameOver(PLAYER)) return;

                    int[] move = findBestMove(board);
                    board[move[0]][move[1]] = AI;
                    buttons[move[0]][move[1]].setText(String.valueOf(AI));
                    if (checkGameOver(AI)) return;
                }
            }
        }
    }

    boolean checkGameOver(char player) {
        int score = evaluate(board);
        if (score == 10 && player == AI) {
            JOptionPane.showMessageDialog(this, "AI kazandı!");
            disableBoard();
            return true;
        } else if (score == -10 && player == PLAYER) {
            JOptionPane.showMessageDialog(this, "Oyuncu kazandı!");
            disableBoard();
            return true;
        } else if (!isMovesLeft(board)) {
            JOptionPane.showMessageDialog(this, "Beraberlik!");
            disableBoard();
            return true;
        }
        return false;
    }

    void disableBoard() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                buttons[i][j].setEnabled(false);
    }

    int[] findBestMove(char[][] board) {
        int bestVal = Integer.MIN_VALUE;
        int[] move = {-1, -1};
        logArea.setText("");
        analysisPanel.removeAll();
        logArea.append("AI düşünüyor...\n\n");

        Map<Integer, Integer> scoreToRating = new HashMap<>();
        scoreToRating.put(10, 5);
        scoreToRating.put(5, 4);
        scoreToRating.put(0, 3);
        scoreToRating.put(-5, 1);
        scoreToRating.put(-10, 0);

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == EMPTY) {
                    board[i][j] = AI;
                    int moveVal = minimax(board, 0, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                    board[i][j] = EMPTY;

                    int rating = scoreToRating.getOrDefault(moveVal, 2);

                    JPanel simPanel = new JPanel(new BorderLayout());
                    simPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
                    simPanel.add(new JLabel("Hamle: [" + i + "," + j + "] | Puan: " + rating + "/5"), BorderLayout.NORTH);
                    JTextArea miniBoard = new JTextArea();
                    miniBoard.setEditable(false);
                    miniBoard.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

                    StringBuilder sb = new StringBuilder();
                    for (int x = 0; x < SIZE; x++) {
                        for (int y = 0; y < SIZE; y++) {
                            char c = board[x][y];
                            if (x == i && y == j) c = AI;
                            sb.append(c + " ");
                        }
                        sb.append("\n");
                    }
                    miniBoard.setText(sb.toString());
                    simPanel.add(miniBoard, BorderLayout.CENTER);

                    JLabel reason = new JLabel();
                    if (moveVal < bestVal) {
                        reason.setText("❌ Elendi: mağlubiyete daha yakın");
                        reason.setForeground(Color.RED);
                    } else {
                        reason.setText("👍 Uygun seviye: " + rating + "/5");
                        reason.setForeground(new Color(0, 128, 0));
                    }
                    simPanel.add(reason, BorderLayout.SOUTH);
                    analysisPanel.add(simPanel);

                    if (moveVal > bestVal) {
                        move[0] = i;
                        move[1] = j;
                        bestVal = moveVal;
                    }
                }
            }
        }

        analysisPanel.revalidate();
        analysisPanel.repaint();
        return move;
    }

    int minimax(char[][] board, int depth, boolean isMax, int alpha, int beta) {
        int score = evaluate(board);
        if (score == 10 || score == -10 || !isMovesLeft(board)) return score;

        if (isMax) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = AI;
                        best = Math.max(best, minimax(board, depth + 1, false, alpha, beta));
                        board[i][j] = EMPTY;
                        alpha = Math.max(alpha, best);
                        if (beta <= alpha) return best;
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    if (board[i][j] == EMPTY) {
                        board[i][j] = PLAYER;
                        best = Math.min(best, minimax(board, depth + 1, true, alpha, beta));
                        board[i][j] = EMPTY;
                        beta = Math.min(beta, best);
                        if (beta <= alpha) return best;
                    }
                }
            }
            return best;
        }
    }

    boolean isMovesLeft(char[][] board) {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] == EMPTY) return true;
        return false;
    }

    int evaluate(char[][] b) {
        for (int row = 0; row < SIZE; row++) {
            if (b[row][0] == b[row][1] && b[row][1] == b[row][2]) {
                if (b[row][0] == AI) return 10;
                else if (b[row][0] == PLAYER) return -10;
            }
        }

        for (int col = 0; col < SIZE; col++) {
            if (b[0][col] == b[1][col] && b[1][col] == b[2][col]) {
                if (b[0][col] == AI) return 10;
                else if (b[0][col] == PLAYER) return -10;
            }
        }

        if (b[0][0] == b[1][1] && b[1][1] == b[2][2]) {
            if (b[0][0] == AI) return 10;
            else if (b[0][0] == PLAYER) return -10;
        }

        if (b[0][2] == b[1][1] && b[1][1] == b[2][0]) {
            if (b[0][2] == AI) return 10;
            else if (b[0][2] == PLAYER) return -10;
        }

        return 0;
    }

    public static void main(String[] args) {
        new TicTacToeGUI();
    }
}
