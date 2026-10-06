package com.subramanyam.lifetracker.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A see-through layer that sits on top of the content area. To create a
 * "fade in" effect when switching tabs, it briefly paints itself as a
 * solid background color, then dissolves to fully transparent over
 * ~200ms — revealing the newly-shown tab underneath as it fades.
 */
public class FadeOverlay extends JComponent {

    private final Color baseColor;
    private float alpha = 0f;

    public FadeOverlay(Color baseColor) {
        this.baseColor = baseColor;
        setOpaque(false); // lets the real content show through once alpha reaches 0
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (alpha <= 0f) {
            return;
        }
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(baseColor);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    /** Starts fully opaque (hiding whatever's underneath) and fades to transparent. */
    public void fadeIn() {
        alpha = 1f;
        repaint();

        int totalSteps = 15;
        int[] currentStep = {0};
        Timer timer = new Timer(14, null);
        timer.addActionListener(e -> {
            currentStep[0]++;
            alpha = 1f - (currentStep[0] / (float) totalSteps);
            repaint();
            if (currentStep[0] >= totalSteps) {
                alpha = 0f;
                repaint();
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
}