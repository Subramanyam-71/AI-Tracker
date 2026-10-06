package com.subramanyam.lifetracker.ui;

import com.subramanyam.lifetracker.model.DailyProgress;
import com.subramanyam.lifetracker.model.Task;
import com.subramanyam.lifetracker.model.TaskStatus;
import com.subramanyam.lifetracker.service.AIProgressService;
import com.subramanyam.lifetracker.service.MotivationService;
import com.subramanyam.lifetracker.service.ProgressService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Stage 3: Daily task tracking.
 *
 * Shows today's tracked items with a status button.
 * Clicking a status button cycles:
 * Not Started -> In Progress -> Completed.
 *
 * Supports:
 * - Adding tasks
 * - Editing task names
 * - Removing tasks
 * - Updating task status
 * - Saving notes
 */
public class TaskTrackerPanel extends JPanel {

    private static final Color BG_COLOR = new Color(247, 248, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_MUTED = new Color(120, 124, 134);

    private static final Color NOT_STARTED_COLOR = new Color(235, 236, 240);
    private static final Color IN_PROGRESS_COLOR = new Color(255, 196, 87);
    private static final Color COMPLETED_COLOR = new Color(64, 191, 118);

    private static final Color INSIGHT_BG_COLOR = new Color(238, 240, 255);
    private static final Color INSIGHT_BORDER_COLOR = new Color(210, 214, 250);

    private final ProgressService progressService;
    private final MotivationService motivationService = new MotivationService();
    private final AIProgressService aiProgressService = new AIProgressService();

    private final JTextField noteField = new JTextField();
    private final JTextField newTaskField = new JTextField();

    private final JPanel taskListPanel = new JPanel();
    private final JTextArea insightArea = new JTextArea();

    private DailyProgress todayProgress;

    public TaskTrackerPanel(ProgressService progressService) {
        this.progressService = progressService;

        setLayout(new BorderLayout(0, 16));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        LocalDate today = LocalDate.now();
        todayProgress = loadTodayProgress(today);

        add(buildHeading(today), BorderLayout.NORTH);
        add(buildTaskListScrollPane(todayProgress), BorderLayout.CENTER);
        add(buildBottomSection(todayProgress), BorderLayout.SOUTH);
    }

    private DailyProgress loadTodayProgress(LocalDate today) {
        return progressService.getProgressFor(today);
    }

    private JPanel buildHeading(LocalDate today) {
        JPanel headingPanel = new JPanel();
        headingPanel.setLayout(new BoxLayout(headingPanel, BoxLayout.Y_AXIS));
        headingPanel.setBackground(BG_COLOR);

        String formatted =
                today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"));

        JLabel heading = new JLabel("Today's Goals — " + formatted);
        heading.setFont(new Font("SansSerif", Font.BOLD, 20));

        JLabel quote =
                new JLabel("“" + motivationService.getDailyQuote(today) + "”");
        quote.setFont(new Font("SansSerif", Font.ITALIC, 13));
        quote.setForeground(TEXT_MUTED);

        headingPanel.add(heading);
        headingPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headingPanel.add(quote);

        return headingPanel;
    }

    private JScrollPane buildTaskListScrollPane(DailyProgress todayProgress) {
        taskListPanel.setLayout(
                new BoxLayout(taskListPanel, BoxLayout.Y_AXIS)
        );

        taskListPanel.setBackground(BG_COLOR);

        for (Task task : todayProgress.getTasks()) {
            addTaskRowToList(task);
        }

        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    private void addTaskRowToList(Task task) {
        taskListPanel.add(buildTaskRow(task));
        taskListPanel.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private void rebuildTaskList() {
        taskListPanel.removeAll();

        for (Task task : todayProgress.getTasks()) {
            addTaskRowToList(task);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel buildTaskRow(Task task) {

        JPanel row = new JPanel(new BorderLayout());

        row.setBackground(CARD_COLOR);

        row.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 48)
        );

        row.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR),
                        BorderFactory.createEmptyBorder(
                                8, 16, 8, 16
                        )
                )
        );

        // -----------------------------
        // TASK NAME
        // -----------------------------

        JLabel nameLabel = new JLabel(task.getName());
        nameLabel.setFont(
                new Font("SansSerif", Font.PLAIN, 14)
        );

        // -----------------------------
        // STATUS BUTTON
        // -----------------------------

        JButton statusButton = new JButton();

        statusButton.setFocusPainted(false);
        statusButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        statusButton.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        statusButton.setPreferredSize(
                new Dimension(120, 30)
        );

        applyStatusStyle(
                statusButton,
                task.getStatus(),
                false
        );

        statusButton.addActionListener(e -> {

            task.setStatus(
                    task.getStatus().next()
            );

            applyStatusStyle(
                    statusButton,
                    task.getStatus(),
                    true
            );

            if (task.getStatus() ==
                    TaskStatus.COMPLETED) {

                AnimationUtils.pulse(statusButton);
            }

            progressService.save();
            refreshInsight();
        });

        // -----------------------------
        // EDIT BUTTON
        // -----------------------------

        JButton editButton = new JButton("Edit");

        editButton.setFocusPainted(false);
        editButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        editButton.setFont(
                new Font("SansSerif", Font.BOLD, 12)
        );

        editButton.setToolTipText(
                "Edit task name"
        );

        editButton.addActionListener(e -> {

            String newName = JOptionPane.showInputDialog(
                    this,
                    "Edit task name:",
                    task.getName()
            );

            if (newName != null) {

                String trimmedName = newName.trim();

                if (!trimmedName.isEmpty() &&
                        !trimmedName.equals(task.getName())) {

                    task.setName(trimmedName);

                    progressService.save();

                    rebuildTaskList();

                    refreshInsight();
                }
            }
        });

        // -----------------------------
        // REMOVE BUTTON
        // -----------------------------

        JButton removeButton =
                new JButton("\u2715");

        removeButton.setFocusPainted(false);
        removeButton.setBorderPainted(false);
        removeButton.setContentAreaFilled(false);

        removeButton.setForeground(
                new Color(190, 60, 60)
        );

        removeButton.setFont(
                new Font("SansSerif", Font.BOLD, 13)
        );

        removeButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        removeButton.setToolTipText(
                "Remove this task"
        );

        removeButton.addActionListener(e -> {

            todayProgress.getTasks().remove(task);

            rebuildTaskList();

            progressService.save();

            refreshInsight();
        });

        // -----------------------------
        // RIGHT SIDE BUTTONS
        // -----------------------------

        JPanel rightSide =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.RIGHT,
                                8,
                                0
                        )
                );

        rightSide.setBackground(CARD_COLOR);

        rightSide.add(statusButton);
        rightSide.add(editButton);
        rightSide.add(removeButton);

        // -----------------------------
        // ROW
        // -----------------------------

        row.add(
                nameLabel,
                BorderLayout.WEST
        );

        row.add(
                rightSide,
                BorderLayout.EAST
        );

        return row;
    }

    private void applyStatusStyle(
            JButton button,
            TaskStatus status,
            boolean animate
    ) {

        button.setText(status.getLabel());

        Color targetBackground;
        Color foreground;

        switch (status) {

            case COMPLETED -> {

                targetBackground =
                        COMPLETED_COLOR;

                foreground = Color.WHITE;
            }

            case IN_PROGRESS -> {

                targetBackground =
                        IN_PROGRESS_COLOR;

                foreground = Color.WHITE;
            }

            default -> {

                targetBackground =
                        NOT_STARTED_COLOR;

                foreground = Color.DARK_GRAY;
            }
        }

        button.setForeground(foreground);

        if (animate) {

            AnimationUtils.animateBackground(
                    button,
                    button.getBackground(),
                    targetBackground
            );

        } else {

            button.setBackground(
                    targetBackground
            );
        }
    }

    private JPanel buildBottomSection(
            DailyProgress todayProgress
    ) {

        JPanel bottom = new JPanel();

        bottom.setLayout(
                new BoxLayout(
                        bottom,
                        BoxLayout.Y_AXIS
                )
        );

        bottom.setBackground(BG_COLOR);

        bottom.add(buildInsightSection());

        bottom.add(
                Box.createRigidArea(
                        new Dimension(0, 12)
                )
        );

        bottom.add(
                buildAddTaskRow(todayProgress)
        );

        bottom.add(
                Box.createRigidArea(
                        new Dimension(0, 12)
                )
        );

        bottom.add(
                buildNoteSection(todayProgress)
        );

        return bottom;
    }

    private JPanel buildInsightSection() {

        JPanel section =
                new JPanel(
                        new BorderLayout(10, 0)
                );

        section.setBackground(
                INSIGHT_BG_COLOR
        );

        section.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                INSIGHT_BORDER_COLOR
                        ),
                        BorderFactory.createEmptyBorder(
                                12, 14, 12, 14
                        )
                )
        );

        JLabel icon =
                new JLabel("\uD83E\uDD16");

        icon.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        20
                )
        );

        icon.setVerticalAlignment(
                SwingConstants.TOP
        );

        insightArea.setEditable(false);
        insightArea.setLineWrap(true);
        insightArea.setWrapStyleWord(true);
        insightArea.setOpaque(false);

        insightArea.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        refreshInsight();

        section.add(
                icon,
                BorderLayout.WEST
        );

        section.add(
                insightArea,
                BorderLayout.CENTER
        );

        return section;
    }

    private void refreshInsight() {

        insightArea.setText(
                aiProgressService.generateAnalysis(
                        todayProgress
                )
        );
    }

    private JPanel buildAddTaskRow(
            DailyProgress todayProgress
    ) {

        JPanel row =
                new JPanel(
                        new BorderLayout(8, 0)
                );

        row.setBackground(BG_COLOR);

        newTaskField.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        newTaskField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        BorderFactory.createEmptyBorder(
                                8, 10, 8, 10
                        )
                )
        );

        JButton addButton =
                new JButton("+ Add Task");

        addButton.setFocusPainted(false);
        addButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        addButton.setFont(
                new Font(
                        "SansSerif",
                        Font.BOLD,
                        12
                )
        );

        Runnable addNewTask = () -> {

            String name =
                    newTaskField.getText().trim();

            if (name.isEmpty()) {
                return;
            }

            Task newTask =
                    new Task(name);

            todayProgress.getTasks()
                    .add(newTask);

            rebuildTaskList();

            newTaskField.setText("");

            progressService.save();

            refreshInsight();
        };

        addButton.addActionListener(
                e -> addNewTask.run()
        );

        newTaskField.addActionListener(
                e -> addNewTask.run()
        );

        row.add(
                newTaskField,
                BorderLayout.CENTER
        );

        row.add(
                addButton,
                BorderLayout.EAST
        );
        AnimationUtils.fadeInComponent(
        row,
        taskListPanel.getComponentCount() * 40
);
        return row;
    }

    private JPanel buildNoteSection(
            DailyProgress todayProgress
    ) {

        JPanel section =
                new JPanel(
                        new BorderLayout(0, 6)
                );

        section.setBackground(BG_COLOR);

        section.setBorder(
                BorderFactory.createEmptyBorder(
                        12, 0, 0, 0
                )
        );

        JLabel label =
                new JLabel("Today's Note");

        label.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        12
                )
        );

        label.setForeground(TEXT_MUTED);

        noteField.setText(
                todayProgress.getNote()
        );

        noteField.setFont(
                new Font(
                        "SansSerif",
                        Font.PLAIN,
                        13
                )
        );

        noteField.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                BORDER_COLOR
                        ),
                        BorderFactory.createEmptyBorder(
                                8, 10, 8, 10
                        )
                )
        );

        noteField.addFocusListener(
                new java.awt.event.FocusAdapter() {

                    public void focusLost(
                            java.awt.event.FocusEvent e
                    ) {

                        todayProgress.setNote(
                                noteField.getText()
                        );

                        progressService.save();
                    }
                }
        );

        section.add(
                label,
                BorderLayout.NORTH
        );

        section.add(
                noteField,
                BorderLayout.CENTER
        );

        return section;
    }
}