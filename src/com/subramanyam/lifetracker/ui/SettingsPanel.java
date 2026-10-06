package com.subramanyam.lifetracker.ui;

import com.subramanyam.lifetracker.service.TelegramBotService;
import com.subramanyam.lifetracker.service.TelegramSettings;
import com.subramanyam.lifetracker.service.MotivationService;
import com.subramanyam.lifetracker.service.ProgressService;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.time.LocalDate;

/** Lets the user connect their own Telegram bot without placing secrets in source code. */
public class SettingsPanel extends JPanel {

    private static final Color BG_COLOR = new Color(247, 248, 250);
    private static final Color BORDER_COLOR = new Color(230, 230, 230);
    private static final Color TEXT_MUTED = new Color(120, 124, 134);

    private final TelegramSettings telegramSettings;
    private final TelegramBotService telegramBotService;
    private final ProgressService progressService;
    private final MotivationService motivationService = new MotivationService();
    private final JPasswordField tokenField = new JPasswordField();
    private final JTextField chatIdField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");

    public SettingsPanel(TelegramSettings telegramSettings, TelegramBotService telegramBotService,
                         ProgressService progressService) {
        this.telegramSettings = telegramSettings;
        this.telegramBotService = telegramBotService;
        this.progressService = progressService;
        setLayout(new GridBagLayout());
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        tokenField.setText(telegramSettings.getBotToken());
        chatIdField.setText(telegramSettings.getChatId());
        add(buildCard());
    }

    private JPanel buildCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setPreferredSize(new Dimension(520, 430));

        JLabel title = new JLabel("Telegram Reminders");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel help = new JLabel("1. Send /start to your bot  •  2. Paste its token  •  3. Find your chat ID");
        help.setFont(new Font("SansSerif", Font.PLAIN, 12));
        help.setForeground(TEXT_MUTED);
        card.add(help);
        card.add(Box.createRigidArea(new Dimension(0, 18)));

        card.add(fieldLabel("Bot Token"));
        card.add(tokenField);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        card.add(fieldLabel("Chat ID"));
        card.add(chatIdField);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setBackground(Color.WHITE);
        JButton save = new JButton("Save Settings");
        JButton findChat = new JButton("Find My Chat ID");
        JButton test = new JButton("Send Test Message");
        buttons.add(save);
        buttons.add(Box.createRigidArea(new Dimension(8, 0)));
        buttons.add(findChat);
        buttons.add(Box.createRigidArea(new Dimension(8, 0)));
        buttons.add(test);
        card.add(buttons);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton sendToday = new JButton("Send Today's Messages Now");
        sendToday.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(sendToday);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_MUTED);
        card.add(statusLabel);

        save.addActionListener(e -> saveSettings());
        findChat.addActionListener(e -> findChatId());
        test.addActionListener(e -> sendTestMessage());
        sendToday.addActionListener(e -> sendTodaysMessages());
        return card;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void saveSettings() {
        telegramSettings.saveBotToken(new String(tokenField.getPassword()));
        telegramSettings.saveChatId(chatIdField.getText());
        setStatus("Settings saved on this computer.", new Color(64, 191, 118));
    }

    private void findChatId() {
        saveSettings();
        setStatus("Looking for your /start message…", TEXT_MUTED);
        new SwingWorker<Optional<String>, Void>() {
            @Override
            protected Optional<String> doInBackground() throws Exception {
                return telegramBotService.findLatestChatId();
            }

            @Override
            protected void done() {
                try {
                    Optional<String> chatId = get();
                    if (chatId.isPresent()) {
                        chatIdField.setText(chatId.get());
                        telegramSettings.saveChatId(chatId.get());
                        setStatus("Chat found. You can now send a test message.", new Color(64, 191, 118));
                    } else {
                        setStatus("No /start message found. Send /start to the bot, then try again.", Color.RED);
                    }
                } catch (Exception exception) {
                    setStatus("Could not connect. Check your token and internet connection.", Color.RED);
                }
            }
        }.execute();
    }

    private void sendTestMessage() {
        saveSettings();
        if (!telegramSettings.isConfigured()) {
            setStatus("Save a token and chat ID first.", Color.RED);
            return;
        }
        setStatus("Sending test message…", TEXT_MUTED);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                telegramBotService.sendMessage(telegramSettings.getChatId(),
                        "Life Progress is connected! Your daily reminders are ready.");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    setStatus("Test sent — check Telegram.", new Color(64, 191, 118));
                } catch (Exception exception) {
                    setStatus("Could not send. Check the token, chat ID, and internet.", Color.RED);
                }
            }
        }.execute();
    }

    private void sendTodaysMessages() {
        saveSettings();
        if (!telegramSettings.isConfigured()) {
            setStatus("Save a token and chat ID first.", Color.RED);
            return;
        }
        setStatus("Sending today's two messages…", TEXT_MUTED);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                LocalDate today = LocalDate.now();
                telegramBotService.sendMessage(telegramSettings.getChatId(),
                        "Morning Motivation\n\n“" + motivationService.getDailyQuote(today) + "”");
                telegramBotService.sendMessage(telegramSettings.getChatId(),
                        "Life Progress Update\n\n" + motivationService.getProgressReminder(progressService, today)
                                + "\n\n“" + motivationService.getDailyQuote(today) + "”");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    setStatus("Today's motivation and progress update were sent.", new Color(64, 191, 118));
                } catch (Exception exception) {
                    setStatus("Could not send. Check the token, chat ID, and internet.", Color.RED);
                }
            }
        }.execute();
    }

    private void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }
}
