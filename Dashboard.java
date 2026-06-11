import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class Dashboard {

    private final JFrame frame;
    private final JLabel walletABalanceLabel;
    private final JLabel walletBBalanceLabel;
    private final JLabel mempoolLabel;
    private final JLabel difficultyLabel;
    private final JLabel blockHeightLabel;
    private final JLabel rewardLabel;
    private final JLabel statusLabel;
    private final JTextField amountField;
    private final JTextArea logArea;
    private final JComboBox<String> directionCombo;
    private final JComboBox<String> minerCombo;

    private final Node node1;
    private final Node node2;

    public Dashboard(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;

        setLookAndFeel();

        frame = new JFrame("NoobChain Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(18, 20, 26));

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        root.setBackground(new Color(18, 20, 26));

        JPanel topRow = new JPanel(new GridLayout(1, 2, 10, 0));
        topRow.setOpaque(false);

        walletABalanceLabel = new JLabel();
        walletBBalanceLabel = new JLabel();
        topRow.add(makeBalanceCard("Wallet A", walletABalanceLabel, new Color(39, 174, 96)));
        topRow.add(makeBalanceCard("Wallet B", walletBBalanceLabel, new Color(52, 152, 219)));


        JPanel statsBar = new JPanel(new GridLayout(1, 4, 8, 0));
        statsBar.setOpaque(false);

        mempoolLabel     = new JLabel("0", SwingConstants.CENTER);
        difficultyLabel  = new JLabel("0", SwingConstants.CENTER);
        blockHeightLabel = new JLabel("0", SwingConstants.CENTER);
        rewardLabel      = new JLabel("0", SwingConstants.CENTER);

        statsBar.add(makeStatCard("Mempool",    mempoolLabel,     new Color(243, 156, 18)));
        statsBar.add(makeStatCard("Difficulty", difficultyLabel,  new Color(155, 89, 182)));
        statsBar.add(makeStatCard("Height",     blockHeightLabel, new Color(52, 152, 219)));
        statsBar.add(makeStatCard("Reward",     rewardLabel,      new Color(39, 174, 96)));

        JPanel headerPanel = new JPanel(new BorderLayout(0, 8));
        headerPanel.setOpaque(false);
        headerPanel.add(topRow,   BorderLayout.NORTH);
        headerPanel.add(statsBar, BorderLayout.SOUTH);

        //  Log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(new Color(13, 15, 20));
        logArea.setForeground(new Color(200, 210, 220));
        logArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(makeTitledBorder("Activity Log"));
        scroll.setPreferredSize(new Dimension(820, 300));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setOpaque(false);

        amountField = new JTextField(10);
        amountField.setFont(new Font("Consolas", Font.PLAIN, 14));
        amountField.setBackground(new Color(28, 32, 40));
        amountField.setForeground(Color.WHITE);
        amountField.setCaretColor(Color.WHITE);
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 70, 90)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        directionCombo = new JComboBox<>(new String[]{"A → B", "B → A"});
        styleCombo(directionCombo);

        JButton sendBtn     = makeButton("Submit to Mempool", new Color(39, 174, 96));
        JButton mineBtn     = makeButton("Mine Block",        new Color(52, 152, 219));
        JButton validateBtn = makeButton("Validate Chain",    new Color(155, 89, 182));
        JButton clearBtn    = makeButton("Clear Log",         new Color(80, 90, 110));

        row1.add(new JLabel(styledLabel("Amount:")));
        row1.add(amountField);
        row1.add(new JLabel(styledLabel("Direction:")));
        row1.add(directionCombo);
        row1.add(sendBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);

        minerCombo = new JComboBox<>(new String[]{
                "Node-1 (Wallet A) mines",
                "Node-2 (Wallet B) mines"
        });
        styleCombo(minerCombo);

        row2.add(new JLabel(styledLabel("Miner:")));
        row2.add(minerCombo);
        row2.add(mineBtn);
        row2.add(validateBtn);
        row2.add(clearBtn);

        statusLabel = new JLabel("Ready — submit a transaction or mine a block.");
        statusLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(140, 160, 180));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));

        JPanel controlsPanel = new JPanel(new BorderLayout(0, 6));
        controlsPanel.setOpaque(false);
        controlsPanel.setBorder(makeTitledBorder("Controls"));

        JPanel rows = new JPanel(new GridLayout(2, 1, 0, 6));
        rows.setOpaque(false);
        rows.add(row1);
        rows.add(row2);

        controlsPanel.add(rows,        BorderLayout.CENTER);
        controlsPanel.add(statusLabel, BorderLayout.SOUTH);

        // Assemble
        root.add(headerPanel,   BorderLayout.NORTH);
        root.add(scroll,        BorderLayout.CENTER);
        root.add(controlsPanel, BorderLayout.SOUTH);
        frame.add(root, BorderLayout.CENTER);

        // Wire actions
        sendBtn.addActionListener(e -> handleSend());
        mineBtn.addActionListener(e -> handleMine());
        validateBtn.addActionListener(e -> handleValidate());
        clearBtn.addActionListener(e -> logArea.setText(""));
        amountField.addActionListener(e -> handleSend());

        new Timer(1000, e -> refreshAll()).start();

        refreshAll();
        log("INFO", "Dashboard initialized. Network: 2 nodes active.");
        log("INFO", "Node-1 (WalletA) on port 6001 | Node-2 (WalletB) on port 6002");

        frame.pack();
        frame.setMinimumSize(new Dimension(860, 600));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Handlers
    private void handleSend() {
        if (!isReady()) return;
        try {
            // Parse amount as coins → convert to Satoshi's
            float amountCoins = Float.parseFloat(amountField.getText().trim());
            if (amountCoins <= 0f) {
                showError("Amount must be > 0.");
                return;
            }

            long amountSatoshi = (long)(amountCoins * 100_000_000L);

            // Default fee: 0.01 coins = 1_000_000 Satoshi's
            long feeSatoshi = 1_000_000L;

            boolean aToB      = directionCombo.getSelectedIndex() == 0;
            Wallet sender     = aToB ? BlockchainState.walletA : BlockchainState.walletB;
            Wallet recipient  = aToB ? BlockchainState.walletB : BlockchainState.walletA;
            String senderName = aToB ? "A" : "B";
            String recipeName = aToB ? "B" : "A";

            //  sendFunds() submits to BlockchainState.mempool automatically
            Transaction tx = sender.sendFunds(recipient.publicKey, amountSatoshi, feeSatoshi);

            if (tx == null) {
                showError("Insufficient funds in Wallet " + senderName +
                        " | Balance: " + StringUtil.toCoins(sender.getBalance()));
                return;
            }

            log("TX", String.format(
                    "Wallet %s → Wallet %s | %.8f coins | fee: %.8f | mempool: %d",
                    senderName, recipeName,
                    amountSatoshi / 100_000_000.0,
                    feeSatoshi / 100_000_000.0,
                    BlockchainState.mempool.size()
            ));
            setStatus("TX pending in mempool. Click Mine Block to confirm.");
            amountField.setText("");
            refreshAll();

        } catch (NumberFormatException ex) {
            showError("Invalid amount — enter a number like 10 or 25.5");
        }
    }

    private void handleMine() {
        if (!isReady()) return;

        // Pick correct node AND wallet based on combo selection
        boolean node1Selected = minerCombo.getSelectedIndex() == 0;
        Node   miningNode  = node1Selected ? node1  : node2;
        Wallet miningWallet= node1Selected ? BlockchainState.walletA
                : BlockchainState.walletB;
        String minerName   = node1Selected ? "Node-1 (WalletA)" : "Node-2 (WalletB)";

        log("MINE", "Mining started | miner: " + minerName +
                " | mempool: " + BlockchainState.mempool.size() + " TX(s)");

        String prevHash = BlockchainState.getChainTip().hash;

        try {
            //  MiningEngine handles Coinbase + mempool drain + broadcast
            Block mined = MiningEngine.mineNextBlock(prevHash, miningWallet, miningNode);

            log("MINE", String.format(
                    "Block #%d mined | hash: %s... | difficulty: %d",
                    BlockchainState.getHeight(),
                    mined.hash.substring(0, 14),
                    mined.difficulty
            ));

            // Show network sync status after mine
            boolean inSync = node1.getLocalHeight() == node2.getLocalHeight();
            log("NET", "Node-1 height: " + node1.getLocalHeight() +
                    " | Node-2 height: " + node2.getLocalHeight() +
                    (inSync ? " | IN SYNC ✓" : " | OUT OF SYNC ✗"));

            setStatus("Block mined. Difficulty: " + BlockchainState.difficulty);
            refreshAll();

        } catch (Exception ex) {
            showError("Mining failed: " + ex.getMessage());
        }
    }

    private void handleValidate() {
        if (!isReady()) return;
        log("VALIDATE", "Validating " + BlockchainState.blockchain.size() + " blocks...");

        // ChainValidator — not Noob.isChainValid()
        boolean valid = ChainValidator.isChainValid();

        if (valid) {
            log("VALID",   "All " + BlockchainState.blockchain.size() +
                    " blocks passed validation.");
            setStatus("Blockchain is valid ✓");
        } else {
            log("INVALID", "Chain validation FAILED.");
            setStatus("Validation FAILED — check console for details.");
        }
    }

    // UI refresh

    private void refreshAll() {
        //  All state from BlockchainState — not Noob
        String a = BlockchainState.walletA != null
                ? StringUtil.toCoins(BlockchainState.walletA.getBalance()) : "—";
        String b = BlockchainState.walletB != null
                ? StringUtil.toCoins(BlockchainState.walletB.getBalance()) : "—";

        walletABalanceLabel.setText(balanceHtml(a));
        walletBBalanceLabel.setText(balanceHtml(b));

        mempoolLabel.setText(String.valueOf(BlockchainState.mempool.size()));
        difficultyLabel.setText(String.valueOf(BlockchainState.difficulty));
        blockHeightLabel.setText(String.valueOf(BlockchainState.getHeight()));
        rewardLabel.setText(StringUtil.toCoins(BlockchainState.miningReward));
    }

    private boolean isReady() {
        if (BlockchainState.walletA == null || BlockchainState.walletB == null) {
            showError("Wallets not initialized."); return false;
        }
        if (BlockchainState.blockchain == null || BlockchainState.blockchain.isEmpty()) {
            showError("Blockchain empty."); return false;
        }
        if (node1 == null || node2 == null) {
            showError("P2P nodes not initialized."); return false;
        }
        return true;
    }

    // Logging

    private void log(String tag, String msg) {
        logArea.append("[" + tag + "] " + msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void showError(String msg) {
        log("ERROR", msg);
        setStatus("Error: " + msg);
        JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setStatus(String msg) { statusLabel.setText(msg); }

    // Component factories

    private String balanceHtml(String value) {
        return "<html><span style='font-family:Consolas;font-size:24px;" +
                "font-weight:bold;color:#E8F4FD;'>" + value + "</span></html>";
    }

    private String styledLabel(String text) {
        return "<html><span style='color:#8899aa;font-family:Consolas;" +
                "font-size:12px;'>" + text + "</span></html>";
    }

    private JPanel makeBalanceCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(new Color(24, 28, 36));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(45, 52, 65)),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16)
                )
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 12));
        titleLabel.setForeground(new Color(120, 140, 160));
        valueLabel.setText(balanceHtml("—"));
        card.add(titleLabel,  BorderLayout.NORTH);
        card.add(valueLabel,  BorderLayout.CENTER);
        return card;
    }

    private JPanel makeStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(new Color(24, 28, 36));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 52, 65)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Consolas", Font.PLAIN, 11));
        titleLabel.setForeground(new Color(100, 120, 140));
        valueLabel.setFont(new Font("Consolas", Font.BOLD, 22));
        valueLabel.setForeground(accent);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Consolas", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.addMouseListener(new MouseAdapter() {
            final Color base = bg;
            public void mouseEntered(MouseEvent e) { btn.setBackground(base.brighter()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(base); }
        });
        return btn;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Consolas", Font.PLAIN, 13));
        combo.setBackground(new Color(28, 32, 40));
        combo.setForeground(Color.WHITE);
    }

    private TitledBorder makeTitledBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(50, 60, 80)), title
        );
        tb.setTitleFont(new Font("Consolas", Font.PLAIN, 11));
        tb.setTitleColor(new Color(100, 120, 150));
        return tb;
    }

    private void setLookAndFeel() {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
    }
}