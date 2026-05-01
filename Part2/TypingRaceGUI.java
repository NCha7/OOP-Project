import java.awt.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.text.*;

public class TypingRaceGUI
{
    private JFrame frame;
    private ArrayList<Typist> typists = new ArrayList<>();
    private ArrayList<JTextPane> panes = new ArrayList<>();

    private String passage;
    private int passageLength;

    private boolean autocorrect;
    private boolean caffeine;
    private boolean nightShift;

    public void showSetup()
    {
        JFrame setup = new JFrame("Typing Race Setup");
        setup.setSize(500, 400);
        setup.setLayout(new GridLayout(6, 2));

        JComboBox<String> passageBox = new JComboBox<>(
            new String[]{"Short", "Medium", "Long", "Custom"});

        JTextField customField = new JTextField();

        JComboBox<Integer> seats = new JComboBox<>(
            new Integer[]{2,3,4,5,6});

        JCheckBox auto = new JCheckBox("Autocorrect");
        JCheckBox caf = new JCheckBox("Caffeine Mode");
        JCheckBox night = new JCheckBox("Night Shift");

        JButton start = new JButton("Start Race");

        setup.add(new JLabel("Passage:"));
        setup.add(passageBox);
        setup.add(new JLabel("Custom:"));
        setup.add(customField);
        setup.add(new JLabel("Typists:"));
        setup.add(seats);

        setup.add(auto);
        setup.add(caf);
        setup.add(night);

        setup.add(new JLabel());
        setup.add(start);

        start.addActionListener(e -> {
            passage = getPassage(passageBox, customField);
            passageLength = passage.length();

            autocorrect = auto.isSelected();
            caffeine = caf.isSelected();
            nightShift = night.isSelected();

            int count = (int) seats.getSelectedItem();

            createTypists(count);

            setup.dispose();
            startRaceUI();
        });

        setup.setVisible(true);
    }

    private String getPassage(JComboBox<String> box, JTextField custom)
    {
        switch ((String) box.getSelectedItem())
        {
            case "Short":
                return "The quick brown fox jumps over the lazy dog.";
            case "Medium":
                return "Typing races are a fun way to test speed and accuracy.";
            case "Long":
                return "Typing fast and accurately is an essential skill in the modern world where efficiency matters.";
            default:
                return custom.getText();
        }
    }

    private void createTypists(int count)
    {
        typists.clear();

        for (int i = 0; i < count; i++)
        {
            double acc = 0.5 + Math.random() * 0.4;

            if (nightShift) acc -= 0.1;

            typists.add(new Typist((char)('①' + i),
                "Typist " + (i+1), acc));
        }
    }

    private void startRaceUI()
    {
        frame = new JFrame("Typing Race");
        frame.setSize(800, 500);
        frame.setLayout(new GridLayout(typists.size(), 1));

        panes.clear();

        for (Typist t : typists)
        {
            JTextPane pane = new JTextPane();
            pane.setText(passage);
            pane.setEditable(false);

            panes.add(pane);
            frame.add(new JScrollPane(pane));
        }

        frame.setVisible(true);

        runRace();
    }

    private void runRace()
    {
        new Thread(() -> {
            boolean finished = false;
            int turn = 0;

            while (!finished)
            {
                turn++;

                for (int i = 0; i < typists.size(); i++)
                {
                    Typist t = typists.get(i);

                    double acc = t.getAccuracy();

                    if (caffeine && turn <= 10)
                        acc += 0.2;

                    if (caffeine && turn > 10 && Math.random() < 0.1)
                        t.burnOut(3);

                    if (Math.random() < acc)
                        t.typeCharacter();

                    int slide = autocorrect ? 1 : 2;

                    if (Math.random() < 0.2)
                        t.slideBack(slide);

                    updatePane(i, t);

                    if (t.getProgress() >= passageLength)
                        finished = true;
                }

                try { Thread.sleep(150); } catch(Exception e){}
            }

            showWinner();
        }).start();
    }

    private void updatePane(int index, Typist t)
    {
        SwingUtilities.invokeLater(() -> {
            JTextPane pane = panes.get(index);

            int progress = Math.min(t.getProgress(), passage.length());

            String before = passage.substring(0, progress);
            String after = passage.substring(progress);

            pane.setText("");

            try {
                StyledDocument doc = pane.getStyledDocument();

                Style green = pane.addStyle("green", null);
                StyleConstants.setForeground(green, Color.GREEN);

                Style black = pane.addStyle("black", null);
                StyleConstants.setForeground(black, Color.BLACK);

                doc.insertString(doc.getLength(), before, green);
                doc.insertString(doc.getLength(), after, black);

            } catch (Exception e) {}
        });
    }

    private void showWinner()
    {
        Typist winner = null;

        for (Typist t : typists)
        {
            if (t.getProgress() >= passageLength)
            {
                winner = t;
                break;
            }
        }

        JOptionPane.showMessageDialog(frame,
            "🏆 Winner: " + winner.getName());
    }

    public static void main(String[] args)
    {
        new TypingRaceGUI().showSetup();
    }
}
