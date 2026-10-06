package com.subramanyam.lifetracker;

import com.subramanyam.lifetracker.ui.Dashboard;

import javax.swing.SwingUtilities;

/**
 * Entry point of the Life Progress Tracker application.
 *
 * Stage 1 goal: just get a window on screen with the basic
 * dashboard layout. No data, no tracking logic yet — that
 * comes in later stages.
 */
public class Main {

    public static void main(String[] args) {
        // Swing UIs should always be built on the "Event Dispatch Thread".
        // invokeLater just schedules our window-creation code to run there.
        SwingUtilities.invokeLater(() -> {
            Dashboard dashboard = new Dashboard();
            dashboard.setVisible(true);
        });
    }
}
