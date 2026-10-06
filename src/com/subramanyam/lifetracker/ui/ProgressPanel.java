package com.subramanyam.lifetracker.ui;

import com.subramanyam.lifetracker.model.TaskStatus;
import com.subramanyam.lifetracker.service.ProgressService;

import javax.swing.*;
import java.awt.*;
import java.time.YearMonth;

/** Stage 4: a live summary of the user's tracked work for the current month. */
public class ProgressPanel extends JPanel {

    private static final Color BG_COLOR = new Color(247, 248, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color ACCENT_COLOR = new Color(76, 110, 245);
    private static final Color COMPLETED_COLOR = new Color(64, 191, 118);
    private static final Color IN_PROGRESS_COLOR = new Color(255, 196, 87);
    private static final Color TEXT_MUTED = new Color(120, 124, 134);

    private final ProgressService progressService;
    private final JLabel percentageLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();
    private final JProgressBar overallBar = new JProgressBar(0, 100);
    private final JPanel summaryCards = new JPanel(new GridLayout(1, 3, 12, 0));
    private final JPanel goalBars = new JPanel();

    public ProgressPanel(ProgressService progressService) {
        this.progressService = progressService;
        setLayout(new BorderLayout(0, 18));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        refresh();
    }

    private JPanel buildHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_COLOR);

        JLabel heading = new JLabel("Monthly Progress");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        panel.add(heading);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));

        detailLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detailLabel.setForeground(TEXT_MUTED);
        panel.add(detailLabel);
        return panel;
    }

    private JScrollPane buildContent() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG_COLOR);

        JPanel overallCard = createCard(new BorderLayout(12, 0));
        percentageLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        percentageLabel.setForeground(ACCENT_COLOR);
        overallCard.add(percentageLabel, BorderLayout.WEST);

        overallBar.setStringPainted(true);
        overallBar.setForeground(ACCENT_COLOR);
        overallBar.setBackground(new Color(235, 236, 240));
        overallBar.setBorderPainted(false);
        overallCard.add(overallBar, BorderLayout.CENTER);
        overallCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        content.add(overallCard);
        content.add(Box.createRigidArea(new Dimension(0, 16)));

        summaryCards.setBackground(BG_COLOR);
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 92));
        content.add(summaryCards);
        content.add(Box.createRigidArea(new Dimension(0, 22)));

        JLabel goalsHeading = new JLabel("Goal Breakdown");
        goalsHeading.setFont(new Font("SansSerif", Font.BOLD, 16));
        goalsHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(goalsHeading);
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        goalBars.setLayout(new BoxLayout(goalBars, BoxLayout.Y_AXIS));
        goalBars.setBackground(BG_COLOR);
        goalBars.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(goalBars);
        content.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    /** Call this after changing tasks or when the user opens the Progress page. */
    public void refresh() {
        YearMonth month = YearMonth.now();
        int total = progressService.getMonthlyTaskTotal(month);
        int completed = progressService.getMonthlyTaskCount(month, TaskStatus.COMPLETED);
        int inProgress = progressService.getMonthlyTaskCount(month, TaskStatus.IN_PROGRESS);
        int notStarted = progressService.getMonthlyTaskCount(month, TaskStatus.NOT_STARTED);
        int percentage = total == 0 ? 0 : completed * 100 / total;

        detailLabel.setText(month.getMonth().name().charAt(0) + month.getMonth().name().substring(1).toLowerCase()
                + " " + month.getYear() + " • Updated from your tracked days");
        percentageLabel.setText(percentage + "%");
        AnimationUtils.animateProgressBar(overallBar, percentage);
        overallBar.setString(completed + " of " + total + " tasks completed");

        summaryCards.removeAll();
        summaryCards.add(createSummaryCard("Completed", String.valueOf(completed), COMPLETED_COLOR));
        summaryCards.add(createSummaryCard("In Progress", String.valueOf(inProgress), IN_PROGRESS_COLOR));
        summaryCards.add(createSummaryCard("Not Started", String.valueOf(notStarted), TEXT_MUTED));

        goalBars.removeAll();
        for (String goal : progressService.getMonthlyGoalNames(month)) {
            int goalTotal = progressService.getMonthlyTaskTotalForGoal(month, goal);
            int goalCompleted = progressService.getMonthlyTaskCountForGoal(month, goal, TaskStatus.COMPLETED);
            goalBars.add(createGoalRow(goal, goalCompleted, goalTotal));
            goalBars.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        revalidate();
        repaint();
    }

    private JPanel createSummaryCard(String title, String value, Color valueColor) {
        JPanel card = createCard(new BorderLayout());
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(valueColor);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_MUTED);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createGoalRow(String goal, int completed, int total) {
        JPanel row = createCard(new BorderLayout(12, 0));
        JLabel name = new JLabel(goal);
        name.setFont(new Font("SansSerif", Font.PLAIN, 14));
        row.add(name, BorderLayout.WEST);

        int percentage = total == 0 ? 0 : completed * 100 / total;
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(true);
        bar.setString(completed + "/" + total + " completed");
        bar.setForeground(COMPLETED_COLOR);
        bar.setBackground(new Color(235, 236, 240));
        bar.setBorderPainted(false);
        row.add(bar, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        AnimationUtils.animateProgressBar(bar, percentage);
        return row;
    }

    private JPanel createCard(LayoutManager layout) {
        JPanel card = new JPanel(layout);
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        return card;
    }
}