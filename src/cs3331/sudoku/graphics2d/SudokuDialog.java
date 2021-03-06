package cs3331.sudoku.graphics2d;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.*;
import java.net.URL;

import javax.sound.sampled.*;
import javax.swing.*;

import cs3331.sudoku.model.Board;
import cs3331.sudoku.model.Square;

/**
 * A dialog template for playing simple Sudoku games.
 * You need to write code for three callback methods:
 * newClicked(int), numberClicked(int) and boardClicked(int,int).
 *
 * @author Yoonsik Cheon
 */
@SuppressWarnings("serial")
public class SudokuDialog extends JFrame {

    /** Default dimension of the dialog. */
    protected final static Dimension DEFAULT_SIZE = new Dimension(310, 430);

    protected final static String RES_DIR = "../resources/";

    /** Image directory */
    protected final static String IMAGE_DIR = RES_DIR + "image/";

    /** Audio directory */
    protected final static String AUDIO_DIR = RES_DIR + "audio/";

    /** Sudoku board. */
    protected Board board;

    /** Special panel to display a Sudoku board. */
    protected BoardPanel boardPanel;

    /** Message bar to display various messages. */
    protected JLabel msgBar = new JLabel("");

    /** Create a new dialog. */
    public SudokuDialog() {
        this(DEFAULT_SIZE, "sudoku");
    }
    
    /** Create a new dialog of the given screen dimension. */
    public SudokuDialog(Dimension dim, String title) {
        super(title);
        setSize(dim);
        initVars();
        configureUI();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    protected void initVars() {
        this.board = new Board(9);
        this.boardPanel = new BoardPanel(board, this::boardClicked);
    }

    /**
     * Callback to be invoked when a square of the board is clicked.
     * @param x 0-based row index of the clicked square.
     * @param y 0-based column index of the clicked square.
     */
    protected void boardClicked(int x, int y) {
        //out with the old
        if(boardPanel.selected != null){
            boardPanel.selected.setSelected(false);
        }
        //in with the new
        board.getSquare(x, y).setSelected(true);
        repaint();
    }
    
    /**
     * TODO Implement this
     * Callback to be invoked when a number button is clicked.
     * @param number Clicked number (1-9), or 0 for "X".
     */
    protected void numberClicked(int number) {
        Square s = new Square(boardPanel.selected.getRow(), boardPanel.selected.getCol(), number);
        if(board.isValidMove(s)){
            board.updateBoard(s);
            if(board.isSolved()){
                playSound(AUDIO_DIR + "error.wav");
                int result = JOptionPane.showConfirmDialog(null, "Congrats Buddy. Start a new game?", "Warning", JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION){
                    this.board = new Board();
                    boardPanel.setBoard(this.board);
                    repaint();
                }
                else if(result == JOptionPane.NO_OPTION){
                    System.exit(0);
                }
            }
        }
        else {
            playSound(AUDIO_DIR + "error.wav");
            showMessage("Invalid move. Number clicked: " + number);
        }
        repaint();
    }

    /**
     * Callback to be invoked when a new button is clicked.
     * TODO If the current game is over, start a new game of the given size;
     * otherwise, prompt the user for a confirmation and then proceed
     * accordingly.
     * @param size Requested puzzle size, either 4 or 9.
     */
    protected void newClicked(int size) {
        int result = JOptionPane.showConfirmDialog(null, "Start a new game?", "Warning", JOptionPane.YES_NO_OPTION);
        if(result == JOptionPane.YES_OPTION){
            this.board = new Board(size);
            boardPanel.setBoard(this.board);
            repaint();//from Jframe documentation, tell this to repaint itself.
        }
    }

    /**
     * Display the given string in the message bar.
     * @param msg Message to be displayed.
     */
    protected void showMessage(String msg) {
        msgBar.setText(msg);
    }

    /** Configure the UI. */
    protected void configureUI() {
//        setIconImage(createImageIcon(IMAGE_DIR + "sudoku.png").getImage());
        setLayout(new BorderLayout());

        JPanel controlPanel = makeControlPanel();
        // boarder: top, left, bottom, right
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 0, 16));
        add(controlPanel, BorderLayout.NORTH);

        createBoard();

        createMessageBar();
    }

    protected void createMessageBar() {
        msgBar.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 0));
        add(msgBar, BorderLayout.SOUTH);
    }

    protected void createBoard() {
        JPanel board = new JPanel();
        board.setBorder(BorderFactory.createEmptyBorder(10,16,0,16));
        board.setLayout(new GridLayout(1,1));
        board.add(boardPanel);
        add(board, BorderLayout.CENTER);
    }

    /**
     * Create a control panel consisting of new and number northJPanel.
     */
    protected JPanel makeControlPanel() {
        JPanel newButtons = createNewButtons();

        // northJPanel labeled 1, 2, ..., 9, and X.
//        JComponent numberButtons = createNumberButtons();
        JComponent numberButtons = new JPanel(new FlowLayout());
        createNumberButtons(numberButtons);
        numberButtons.setAlignmentX(LEFT_ALIGNMENT);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
        content.add(newButtons);
        content.add(numberButtons);
        return content;
    }

    /**
     * @param numberButtons The component the number buttons will be created onto
     */
    protected void createNumberButtons(JComponent numberButtons) {
        int maxNumber = board.getSize() + 1;
        for (int i = 1; i <= maxNumber; i++) {
            int number = i % maxNumber;
            JButton button = new JButton(number == 0 ? "X" : String.valueOf(number));
            button.setFocusPainted(false);
            button.setMargin(new Insets(0, 2, 0, 2));
            button.addActionListener(e -> numberClicked(number));
            numberButtons.add(button);
        }
    }

    private JPanel createNewButtons() {
        JPanel newButtons = new JPanel(new FlowLayout());

        JButton new4Button = new JButton("New (4x4)");
        new4Button.setFocusPainted(false);
        new4Button.addActionListener(e -> newClicked(4));
        newButtons.add(new4Button);
        JButton new9Button = new JButton("New (9x9)");
        new9Button.setFocusPainted(false);
        new9Button.addActionListener(e -> newClicked(9));
        newButtons.add(new9Button);
        newButtons.setAlignmentX(LEFT_ALIGNMENT);
        return newButtons;
    }

    /** Create an image icon from the given image file. */
    private ImageIcon createImageIcon(String filename) {
        URL imageUrl = getClass().getResource(IMAGE_DIR + filename);
        if (imageUrl != null) {
            return new ImageIcon(imageUrl);
        }
        return null;
    }

    /**
     * return the base URL, the directory containing SudokuDialog
     */
    private URL getCodeBase() {
        return getClass().getResource("/");
    }

    public void playSound(String fileName) {
        playSound(getCodeBase(), fileName);
    }

    private void playSound(URL url, String fileName) {
        try {
            playSound(new URL(url, fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playSound(URL url) {
        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(in);
            clip.start();
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new SudokuDialog();
    }
}
