package com.subramanyam.lifetracker.ui;

import javax.swing.*;
import java.awt.*;

public final class AnimationUtils {

    private AnimationUtils() {
        // Utility class
    }

    /**
     * Smooth progress-bar animation.
     */
    public static void animateProgressBar(
            JProgressBar bar,
            int targetValue
    ) {

        int start = bar.getValue();

        if (start == targetValue) {
            return;
        }

        int direction =
                targetValue > start ? 1 : -1;

        Timer timer =
                new Timer(10, null);

        timer.addActionListener(e -> {

            int next =
                    bar.getValue() + direction;

            bar.setValue(next);

            if (next == targetValue) {
                ((Timer) e.getSource()).stop();
            }
        });

        timer.start();
    }

    /**
     * Smoothly changes a component's background color.
     */
    public static void animateBackground(
            JComponent component,
            Color from,
            Color to
    ) {

        int totalSteps = 18;
        int[] currentStep = {0};

        Timer timer =
                new Timer(15, null);

        timer.addActionListener(e -> {

            currentStep[0]++;

            float ratio =
                    currentStep[0] /
                            (float) totalSteps;

            ratio =
                    1f -
                    (float) Math.pow(
                            1f - ratio,
                            3
                    );

            component.setBackground(
                    blend(
                            from,
                            to,
                            ratio
                    )
            );

            if (currentStep[0] >= totalSteps) {

                component.setBackground(to);

                ((Timer) e.getSource()).stop();
            }
        });

        timer.start();
    }

    /**
     * Smooth entrance animation for task rows/cards.
     *
     * The component appears with a small upward movement.
     */
    public static void fadeInComponent(
            JComponent component,
            int delay
    ) {

        final int totalSteps = 18;
        final int[] step = {0};

        component.setVisible(false);

        Timer starter =
                new Timer(delay, null);

        starter.addActionListener(e -> {

            component.setVisible(true);

            Timer animation =
                    new Timer(20, null);

            animation.addActionListener(animationEvent -> {

                step[0]++;

                float progress =
                        step[0] /
                                (float) totalSteps;

                // Smooth ease-out
                float eased =
                        1f -
                        (float) Math.pow(
                                1f - progress,
                                3
                        );

                int offset =
                        (int) (
                                10 *
                                (1f - eased)
                        );

                /*
                 * Swing components don't support
                 * normal opacity directly.
                 *
                 * This gives the component a
                 * smooth fade/slide entrance.
                 */
                component.setBorder(
                        BorderFactory.createEmptyBorder(
                                offset,
                                0,
                                0,
                                0
                        )
                );

                component.repaint();

                if (step[0] >= totalSteps) {

                    component.setBorder(null);

                    ((Timer) animationEvent
                            .getSource())
                            .stop();
                }
            });

            animation.start();

            ((Timer) e.getSource()).stop();
        });

        starter.start();
    }

    /**
     * Small button pulse when a task is completed.
     */
    public static void pulse(
            AbstractButton button
    ) {

        Font baseFont =
                button.getFont();

        float baseSize =
                baseFont.getSize2D();

        int totalSteps = 14;

        int[] currentStep = {0};

        Timer timer =
                new Timer(15, null);

        timer.addActionListener(e -> {

            currentStep[0]++;

            float progress =
                    currentStep[0] /
                            (float) totalSteps;

            float scale =
                    1f +
                    0.20f *
                    (float) Math.sin(
                            Math.PI * progress
                    );

            button.setFont(
                    baseFont.deriveFont(
                            baseSize * scale
                    )
            );

            if (currentStep[0] >= totalSteps) {

                button.setFont(baseFont);

                ((Timer) e.getSource()).stop();
            }
        });

        timer.start();
    }

    /**
     * Blends two colors.
     */
    private static Color blend(
            Color from,
            Color to,
            float ratio
    ) {

        ratio =
                Math.max(
                        0f,
                        Math.min(
                                1f,
                                ratio
                        )
                );

        int red =
                interpolate(
                        from.getRed(),
                        to.getRed(),
                        ratio
                );

        int green =
                interpolate(
                        from.getGreen(),
                        to.getGreen(),
                        ratio
                );

        int blue =
                interpolate(
                        from.getBlue(),
                        to.getBlue(),
                        ratio
                );

        return new Color(
                red,
                green,
                blue
        );
    }

    /**
     * Interpolates a color channel.
     */
    private static int interpolate(
            int start,
            int end,
            float ratio
    ) {

        int value =
                Math.round(
                        start +
                        (end - start) * ratio
                );

        return Math.max(
                0,
                Math.min(
                        255,
                        value
                )
        );
    }
}