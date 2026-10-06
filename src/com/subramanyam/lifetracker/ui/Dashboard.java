package com.subramanyam.lifetracker.ui;

import com.subramanyam.lifetracker.service.ProgressService;
import com.subramanyam.lifetracker.service.DesktopReminderService;
import com.subramanyam.lifetracker.service.TelegramBotService;
import com.subramanyam.lifetracker.service.TelegramSettings;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Main application window. Header + sidebar nav + swappable content panels.
 */
public class Dashboard extends JFrame {

    // Colors kept in one place so the whole app is easy to re-theme later.
    private static final Color BG_COLOR = new Color(247, 248, 250);
    private static final Color SIDEBAR_COLOR = new Color(30, 33, 41);
    private static final Color ACCENT_COLOR = new Color(76, 110, 245);
    private static final Color TEXT_MUTED = new Color(120, 124, 134);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final FadeOverlay fadeOverlay = new FadeOverlay(BG_COLOR);
    private final ProgressService progressService = new ProgressService();
    private ProgressPanel progressPanel;
    private CalendarView calendarView;
    private DesktopReminderService desktopReminderService;
    private final TelegramSettings telegramSettings = new TelegramSettings();
    private final TelegramBotService telegramBotService = new TelegramBotService(telegramSettings);

    public Dashboard() {
        setTitle("Life Progress");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setMinimumSize(new Dimension(700, 500));
        setLocationRelativeTo(null); // center on screen

        setLayout(new BorderLayout());
        add(buildHeader(), BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContentArea(), BorderLayout.CENTER);

        desktopReminderService = new DesktopReminderService(this, progressService, telegramBotService);
        desktopReminderService.start();
    }

    /** Top bar: app name + current month/year, e.g. "September 2026". */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("LIFE PROGRESS");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        String monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        JLabel subtitle = new JLabel(monthYear);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_MUTED);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setBackground(Color.WHITE);
        titleBlock.add(title);
        titleBlock.add(subtitle);

        header.add(titleBlock, BorderLayout.WEST);
        return header;
    }

    /** Left navigation: Dashboard / Calendar / Progress / Settings. */
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        String[] navItems = {"Dashboard", "Calendar", "Progress", "Settings"};
        for (String item : navItems) {
            sidebar.add(buildNavButton(item));
            sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton buildNavButton(String label) {
        JButton button = new JButton(label);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setBackground(SIDEBAR_COLOR);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
            }
        });

        button.addActionListener(e -> {
            if ("Progress".equals(label) && progressPanel != null) {
                progressPanel.refresh();
            }
            cardLayout.show(contentPanel, label);
            fadeOverlay.fadeIn();
        });
        return button;
    }

    /**
     * Stacks two components so they always share the exact same bounds —
     * used to lay the FadeOverlay directly on top of the content panel,
     * regardless of window size.
     */
    private static class StackPanel extends JPanel {
        StackPanel() {
            setLayout(null); // children are sized manually in doLayout(), not by a normal layout manager
        }

        @Override
        public void doLayout() {
            for (Component child : getComponents()) {
                child.setBounds(0, 0, getWidth(), getHeight());
            }
        }
    }

    /** Center area holding the 4 swappable panels, with a fade overlay layered on top. */
    private JPanel buildContentArea() {
        contentPanel.setBackground(BG_COLOR);

        contentPanel.add(new TaskTrackerPanel(progressService), "Dashboard");

        calendarView = new CalendarView(progressService);
        contentPanel.add(calendarView, "Calendar");

        progressPanel = new ProgressPanel(progressService);
        contentPanel.add(progressPanel, "Progress");

        contentPanel.add(new SettingsPanel(telegramSettings, telegramBotService, progressService), "Settings");

        cardLayout.show(contentPanel, "Dashboard");

        StackPanel stack = new StackPanel();
        stack.add(contentPanel); // added first = underlying panel
        stack.add(fadeOverlay);  // added last = frontmost layer, so it remains visible on top
        return stack;
    }

}