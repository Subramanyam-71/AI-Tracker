package com.subramanyam.lifetracker.ui;

import com.subramanyam.lifetracker.model.DailyProgress;
import com.subramanyam.lifetracker.model.TaskStatus;
import com.subramanyam.lifetracker.service.ProgressService;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

public class CalendarView extends JPanel {

    private static final Color BG_COLOR = new Color(247, 248, 250);
    private static final Color CARD_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(76, 110, 245);
    private static final Color SELECTED_DATE_COLOR = new Color(230, 234, 250);
    private static final Color TEXT_MUTED = new Color(120, 124, 134);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color COMPLETED_BORDER_COLOR = new Color(64, 191, 118);
    private static final Color PARTIAL_BORDER_COLOR = new Color(255, 196, 87);

    private final ProgressService progressService;
    private final LocalDate today = LocalDate.now();
    private final JLabel selectedDayLabel;
    private final Map<LocalDate, JButton> dayButtons = new HashMap<>();
    private JButton selectedButton;

    public CalendarView(ProgressService progressService) {
        this.progressService = progressService;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        int year = today.getYear();

        // The current day selects itself as each month is built, so this
        // label must exist before the month grid is created.
        selectedDayLabel = new JLabel("Click a day to select it");
        selectedDayLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        selectedDayLabel.setForeground(TEXT_MUTED);
        selectedDayLabel.setBorder(BorderFactory.createEmptyBorder(12, 4, 0, 0));

        JLabel yearLabel = new JLabel(String.valueOf(year));
        yearLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        yearLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(yearLabel, BorderLayout.NORTH);

        JPanel monthsGrid = new JPanel(new GridLayout(4, 3, 16, 16));
        monthsGrid.setBackground(BG_COLOR);
        for (Month month : Month.values()) {
            monthsGrid.add(buildMonthPanel(year, month));
        }

        JScrollPane scrollPane = new JScrollPane(monthsGrid);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(selectedDayLabel, BorderLayout.SOUTH);
    }

    private JPanel buildMonthPanel(int year, Month month) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(CARD_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel monthTitle = new JLabel(month.getDisplayName(TextStyle.FULL, Locale.getDefault()));
        monthTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(monthTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        grid.setBackground(CARD_COLOR);

        for (DayOfWeek day : weekdaysStartingSunday()) {
            JLabel dayHeader = new JLabel(day.getDisplayName(TextStyle.SHORT, Locale.getDefault()), SwingConstants.CENTER);
            dayHeader.setFont(new Font("SansSerif", Font.PLAIN, 10));
            dayHeader.setForeground(TEXT_MUTED);
            grid.add(dayHeader);
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstOfMonth = yearMonth.atDay(1);

        int blankCellsBeforeFirstDay = firstOfMonth.getDayOfWeek().getValue() % 7;
        for (int i = 0; i < blankCellsBeforeFirstDay; i++) {
            grid.add(new JLabel(""));
        }

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            grid.add(buildDayButton(date));
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JButton buildDayButton(LocalDate date) {
        JButton dayButton = new JButton(String.valueOf(date.getDayOfMonth()));
        dayButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        dayButton.setMargin(new Insets(2, 2, 2, 2));
        dayButton.setFocusPainted(false);
        dayButton.setBackground(CARD_COLOR);
        dayButton.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        dayButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dayButtons.put(date, dayButton);
        updateProgressMarker(date, dayButton);

        dayButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (dayButton != selectedButton && !date.equals(today)) {
                    AnimationUtils.animateBackground(dayButton, dayButton.getBackground(), SELECTED_DATE_COLOR);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (dayButton != selectedButton && !date.equals(today)) {
                    AnimationUtils.animateBackground(dayButton, dayButton.getBackground(), CARD_COLOR);
                }
            }
        });

        dayButton.addActionListener(e -> selectDay(dayButton, date));

        // Today always stays blue, even after the user selects another date.
        if (date.equals(today)) {
            dayButton.setBackground(ACCENT_COLOR);
            dayButton.setForeground(Color.WHITE);
            updateSelectedDayLabel(date);
        }
        return dayButton;
    }

    /** Refreshes the green/yellow day borders after task statuses change. */
    public void refreshProgressMarkers() {
        dayButtons.forEach(this::updateProgressMarker);
        repaint();
    }

    private void updateProgressMarker(LocalDate date, JButton button) {
        DailyProgress progress = progressService.getExistingProgress(date);
        Color markerColor = BORDER_COLOR;
        if (progress != null && !progress.getTasks().isEmpty()) {
            long completed = progress.getTasks().stream()
                    .filter(task -> task.getStatus() == TaskStatus.COMPLETED).count();
            long started = progress.getTasks().stream()
                    .filter(task -> task.getStatus() != TaskStatus.NOT_STARTED).count();
            if (completed == progress.getTasks().size()) {
                markerColor = COMPLETED_BORDER_COLOR;
            } else if (started > 0) {
                markerColor = PARTIAL_BORDER_COLOR;
            }
        }
        button.setBorder(BorderFactory.createLineBorder(markerColor));
    }

    private void selectDay(JButton clickedButton, LocalDate date) {
        if (selectedButton != null && selectedButton != clickedButton) {
            selectedButton.setBackground(CARD_COLOR);
            selectedButton.setForeground(Color.BLACK);
        }

        // The current day remains solid blue. Other selected dates use a
        // softer highlight so they are easy to distinguish from today.
        if (!date.equals(today)) {
            clickedButton.setBackground(SELECTED_DATE_COLOR);
            clickedButton.setForeground(Color.BLACK);
            selectedButton = clickedButton;
        } else {
            selectedButton = null;
        }

        updateSelectedDayLabel(date);
    }

    private void updateSelectedDayLabel(LocalDate date) {
        selectedDayLabel.setText("Selected: " + date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
                + " " + date.getDayOfMonth() + ", " + date.getYear());
    }

    private DayOfWeek[] weekdaysStartingSunday() {
        return new DayOfWeek[] {
                DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
        };
    }
}