/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package ondes.tools;


import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.util.ArrayList;

public class WaveEditor {
    public static final float MAX_LEVEL = 1000;
    public static final int N_HARMONICS=256;

    JFrame mainWindow;
    JPanel ctlPanel;
    ArrayList<JComboBox<String>> waveSels = new ArrayList<>();
    SynthConnection synthConn;

    Waves waves;

    WaveEditor(SynthConnection sc) {
        synthConn = sc;
        synthConn.start(); // todo - capture the window close and call close()
        waves = new Waves(synthConn);
        synthConn.setWaves(waves);
    }

    void save() {
        String fileName = waves.save();
        //out.println("saved to "+fileName); // WaveFile gives a message
        // todo - display in label for 10 sec or so

        WaveFile.loadWaves();
        String[] waveNames = WaveFile.getWaveNames();
        for (JComboBox<String> waveSel : waveSels) {
            String sel = ""+waveSel.getSelectedItem();
            waveSel.removeAllItems();
            for (String waveName : waveNames) {
                waveSel.addItem(waveName);
            }
            for (int i=0; i<waveNames.length; ++i) {
                if (waveNames[i].equalsIgnoreCase(sel)) {
                    waveSel.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    void toFront() {
        //frame.toFront();  // no go in W10
        //frame.requestFocus();
        mainWindow.setExtendedState(JFrame.ICONIFIED);
        mainWindow.setExtendedState(JFrame.NORMAL);
    }

    Integer[] harmonicList = new Integer[N_HARMONICS];
    {
        for (int i=1; i<=N_HARMONICS; ++i) {
            harmonicList[i-1] = i;
        }
    }

    void addWave() {

        JPanel wavePanel = new JPanel();
        wavePanel.setLayout(new BoxLayout(wavePanel,BoxLayout.Y_AXIS));

        JPanel selPanel = new JPanel();
        selPanel.setLayout(new BoxLayout(selPanel, BoxLayout.Y_AXIS));

        // WAVE SELECTION
        String[] waveNames = WaveFile.getWaveNames();
        JComboBox<String> waveSel = new JComboBox<>(waveNames);
        waveSel.setSelectedIndex(0);
        for (int i=0; i<waveNames.length; ++i) {
            if (waveNames[i].equalsIgnoreCase("sine")) {
                waveSel.setSelectedIndex(i);
                break;
            }
        }
        selPanel.add(waveSel);
        waveSels.add(waveSel);

        // HARMONIC SELECTION
        JComboBox<Integer> harmSel = new JComboBox<>(harmonicList);
        harmSel.setSelectedIndex(0);
        selPanel.add(harmSel);
        wavePanel.add(selPanel);

        // LEVEL SLIDER
        JSlider levelSlider = new JSlider(JSlider.VERTICAL,0,
            (int)MAX_LEVEL, (int)MAX_LEVEL);
            //(int)((MAX_LEVEL * 3)/4) );
        wavePanel.add(levelSlider);
        wavePanel.setBorder(new EtchedBorder());

        //  ACTION LISTENERS
        String waveName = (String)waveSel.getSelectedItem();
        Integer harmonic = (Integer) harmSel.getSelectedItem();
        if (harmonic == null) harmonic = 1;
        float levelValue = levelSlider.getValue();
        float level = levelValue / MAX_LEVEL;

        WaveController wc = waves.getWaveController(waveName, harmonic, level);

        waveSel.addActionListener(e -> {
                // Apparently, it may call this actionListener during the save, when it
                // reloads all of the waves from the file list. In that case,
                // it may have emptied the list at this point so the result will be null.
                // There may be a more elegant way to avoid this race condition, but
                // so far this seems to work.
                String name = (String)waveSel.getSelectedItem();
                if (name != null) wc.setWaveName((String)waveSel.getSelectedItem());
            });
        harmSel.addActionListener(e -> {
            Integer h=(Integer)harmSel.getSelectedItem();
            if (h != null) wc.setHarmonic(h);
        });
        levelSlider.addChangeListener(e -> {
            float lv = levelSlider.getValue();
            wc.setLevel(lv/MAX_LEVEL);

        });


        ctlPanel.add(wavePanel);
    }

    void setIcon() {
        ImageIcon icon = new ImageIcon("sine.png");
        mainWindow.setIconImage(icon.getImage());

    }

    void showWindow() {
        mainWindow = new JFrame("wave editor");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setLocationRelativeTo(null);
        setIcon();

        mainWindow.getContentPane().setLayout(new BorderLayout());
        JButton saveButton = new JButton("SAVE");
        saveButton.addActionListener(e -> save());

        JButton addButton = new JButton("Add Wave");
        addButton.addActionListener(e -> {
            addWave(); mainWindow.pack();
        });

        JPanel south = new JPanel();
        south.setLayout(new FlowLayout());
        south.add(addButton);
        south.add(Box.createHorizontalStrut(100));
        south.add(saveButton);

        mainWindow.getContentPane().add(south, BorderLayout.SOUTH);

        ctlPanel = new JPanel();
        ctlPanel.setLayout(new FlowLayout());
        mainWindow.getContentPane().add(ctlPanel, BorderLayout.CENTER);

        for (int i=0; i<2; ++i) addWave();

        //Display the window.
        mainWindow.pack();
        mainWindow.setVisible(true);
        toFront();
    }

    void run() {
        WaveFile.loadWaves();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                showWindow();
            }
        });
    }


    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        new WaveEditor(new SynthConnection(args)).run();

    }
}
