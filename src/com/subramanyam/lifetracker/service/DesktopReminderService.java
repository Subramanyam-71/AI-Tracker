package com.subramanyam.lifetracker.service;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Shows local desktop reminders at 8:00 AM and 7:15 PM while the app is open. */
public class DesktopReminderService {

    private final JFrame parentWindow;
    private final ProgressService progressService;
    private final TelegramBotService telegramBotService;
    private final MotivationService motivationService = new MotivationService();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "life-progress-reminders");
        thread.setDaemon(true);
        return thread;
    });
    private TrayIcon trayIcon;

    public DesktopReminderService(JFrame parentWindow, ProgressService progressService,
                                  TelegramBotService telegramBotService) {
        this.parentWindow = parentWindow;
        this.progressService = progressService;
        this.telegramBotService = telegramBotService;
        setupTrayIcon();
    }

    public void start() {
        scheduleDaily(LocalTime.of(8, 0), () -> showReminder(
                "Morning Motivation", motivationService.getDailyQuote(LocalDate.now())));
        scheduleDaily(LocalTime.of(19, 15), () -> showReminder(
                "Life Progress Update", motivationService.getProgressReminder(progressService, LocalDate.now())
                        + "\n“" + motivationService.getDailyQuote(LocalDate.now()) + "”"));
    }

    private void scheduleDaily(LocalTime time, Runnable reminder) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstRun = LocalDateTime.of(LocalDate.now(), time);
        if (!firstRun.isAfter(now)) {
            firstRun = firstRun.plusDays(1);
        }
        long delayMillis = Duration.between(now, firstRun).toMillis();
        scheduler.scheduleAtFixedRate(reminder, delayMillis, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS);
    }

    private void setupTrayIcon() {
        if (!SystemTray.isSupported()) {
            return;
        }
        try {
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(new Color(76, 110, 245));
            graphics.fillOval(1, 1, 14, 14);
            graphics.dispose();

            trayIcon = new TrayIcon(image, "Life Progress");
            trayIcon.setImageAutoSize(true);
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException exception) {
            trayIcon = null;
        }
    }

    private void showReminder(String title, String message) {
        telegramBotService.sendIfConfigured(title + "\n\n" + message);
        SwingUtilities.invokeLater(() -> {
            if (trayIcon != null) {
                trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
            } else {
                JOptionPane.showMessageDialog(parentWindow, message, title, JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
