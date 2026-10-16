package com.crowdfund.service;

import com.crowdfund.db.CampaignDAO;
import com.crowdfund.exception.DataAccessException;
import com.crowdfund.model.Campaign;

import javax.swing.SwingUtilities;
import java.util.List;

/**
 * A background thread that keeps the dashboard totals fresh.
 *
 * <p>Every few seconds it reads all campaigns from the database, adds up the
 * totals, and then hands the result back to the Swing UI thread using
 * {@link SwingUtilities#invokeLater}. Swing components may only be touched on
 * the event-dispatch thread, so this is the correct way to update the screen
 * from a background thread.</p>
 */
public class CampaignMonitor {

    /** The UI implements this to receive fresh totals. */
    public interface Listener {
        void onTotals(double totalRaised, double totalGoal, int campaignCount);
    }

    private final CampaignDAO campaignDAO = new CampaignDAO();
    private final Listener listener;
    private Thread thread;
    private volatile boolean running = true;

    public CampaignMonitor(Listener listener) {
        this.listener = listener;
    }

    public void start() {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    try {
                        List<Campaign> campaigns = campaignDAO.findAll();
                        double raised = 0;
                        double goal = 0;
                        for (Campaign campaign : campaigns) {
                            raised += campaign.getRaisedAmount();
                            goal += campaign.getGoalAmount();
                        }
                        final double totalRaised = raised;
                        final double totalGoal = goal;
                        final int count = campaigns.size();

                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                listener.onTotals(totalRaised, totalGoal, count);
                            }
                        });

                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (DataAccessException e) {
                        // Database not reachable right now; try again next round.
                        try {
                            Thread.sleep(4000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("campaign-monitor");
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }
}
