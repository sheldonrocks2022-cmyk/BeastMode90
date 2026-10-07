package com.beastmode.ninety;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.time.LocalDate;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 11, 16);
    private static final int PANEL = Color.rgb(23, 28, 36);
    private static final int PANEL2 = Color.rgb(35, 41, 51);
    private static final int WHITE = Color.rgb(246, 248, 250);
    private static final int MUTED = Color.rgb(157, 166, 180);
    private static final int ORANGE = Color.rgb(255, 103, 66);
    private static final int LIME = Color.rgb(184, 235, 106);

    private SharedPreferences prefs;
    private LinearLayout root;
    private LinearLayout content;
    private int page = 0;
    private int selectedDay = 1;
    private CountDownTimer timer;
    private long remaining = 90_000;
    private TextView timerText;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        prefs = getSharedPreferences("beast_mode_90", MODE_PRIVATE);
        if (!prefs.contains("start_day")) {
            prefs.edit().putLong("start_day", LocalDate.now().toEpochDay()).putInt("difficulty", 2).apply();
        }
        selectedDay = currentDay();

        root = column();
        root.setBackgroundColor(BG);
        setContentView(root);
        buildChrome();
        render();

        if (!prefs.getBoolean("intro_seen", false)) {
            new AlertDialog.Builder(this)
                    .setTitle("BEAST MODE 90")
                    .setMessage("90 days of hard bodyweight training with recovery built in. Start at a level you can perform with clean form. Stop for sharp pain, chest pain, faintness or unusual shortness of breath.")
                    .setPositiveButton("START", (d, w) -> prefs.edit().putBoolean("intro_seen", true).apply())
                    .setCancelable(false)
                    .show();
        }
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout column() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }

    private TextView action(String label, boolean primary, View.OnClickListener click) {
        TextView b = text(label, 14, primary ? BG : WHITE, true);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(16), dp(14), dp(16), dp(14));
        b.setBackground(shape(primary ? ORANGE : PANEL2, 14));
        b.setClickable(true);
        b.setOnClickListener(click);
        return b;
    }

    private LinearLayout panel() {
        LinearLayout p = column();
        p.setPadding(dp(18), dp(18), dp(18), dp(18));
        p.setBackground(shape(PANEL, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(14));
        content.addView(p, lp);
        return p;
    }

    private void buildChrome() {
        LinearLayout header = row();
        header.setPadding(dp(20), dp(18), dp(20), dp(14));

        LinearLayout titles = column();
        titles.addView(text("90-DAY HOME CHALLENGE", 11, ORANGE, true));
        titles.addView(text("BEAST MODE", 29, WHITE, true));
        header.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));

        TextView badge = text("BM", 16, BG, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(shape(LIME, 14));
        header.addView(badge, new LinearLayout.LayoutParams(dp(48), dp(48)));
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        content = column();
        content.setPadding(dp(18), dp(12), dp(18), dp(24));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout nav = row();
        nav.setPadding(dp(6), dp(8), dp(6), dp(8));
        nav.setBackgroundColor(PANEL);

        String[] labels = {"TODAY", "PLAN", "STATS", "SETUP"};
        for (int i = 0; i < labels.length; i++) {
            final int target = i;
            TextView n = text(labels[i], 12, WHITE, true);
            n.setGravity(Gravity.CENTER);
            n.setPadding(0, dp(12), 0, dp(12));
            n.setOnClickListener(v -> {
                page = target;
                render();
            });
            nav.addView(n, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(nav);
    }

    private void render() {
        content.removeAllViews();
        if (page == 0) renderToday();
        else if (page == 1) renderPlan();
        else if (page == 2) renderStats();
        else renderSetup();
    }

    private int currentDay() {
        long start = prefs.getLong("start_day", LocalDate.now().toEpochDay());
        long delta = LocalDate.now().toEpochDay() - start;
        return (int) Math.max(1, Math.min(90, delta + 1));
    }

    private int difficulty() {
        return prefs.getInt("difficulty", 2);
    }

    private String difficultyName() {
        return difficulty() == 0 ? "BUILD" : difficulty() == 1 ? "HARD" : "EXTREME";
    }

    private int scaledRounds(int rounds) {
        if (rounds <= 0) return 0;
        if (difficulty() == 0) return Math.max(1, (int) Math.ceil(rounds * 0.60));
        if (difficulty() == 1) return Math.max(1, (int) Math.ceil(rounds * 0.80));
        return rounds;
    }

    private boolean done(int day) {
        return prefs.getBoolean("done_" + day, false);
    }

    private int completedCount() {
        int c = 0;
        for (int i = 1; i <= 90; i++) if (done(i)) c++;
        return c;
    }

    private void renderToday() {
        WorkoutPlan.Mission m = WorkoutPlan.forDay(selectedDay);

        TextView day = text("DAY " + selectedDay + " / 90", 13, ORANGE, true);
        content.addView(day);
        content.addView(text(m.title, 29, WHITE, true));

        TextView phase = text(m.phase + "  •  WEEK " + m.week + "  •  " + difficultyName(), 12, MUTED, true);
        phase.setPadding(0, dp(4), 0, dp(16));
        content.addView(phase);

        LinearLayout overview = panel();
        overview.addView(text(done(selectedDay) ? "MISSION COMPLETE ✓" : "TODAY'S MISSION", 15, done(selectedDay) ? LIME : WHITE, true));
        overview.addView(text(m.note, 14, MUTED, false));

        if (!m.assessment && !m.rest) {
            TextView rounds = text("ROUNDS: " + scaledRounds(m.rounds), 22, ORANGE, true);
            rounds.setPadding(0, dp(12), 0, 0);
            overview.addView(rounds);
        }

        LinearLayout exercises = panel();
        exercises.addView(text(m.assessment ? "FITNESS TEST" : m.rest ? "RECOVERY" : "EXERCISES", 16, WHITE, true));
        for (WorkoutPlan.Exercise e : m.exercises) {
            LinearLayout line = row();
            line.setPadding(0, dp(13), 0, dp(6));
            TextView left = text(e.name, 15, WHITE, true);
            TextView right = text(e.target, 14, LIME, true);
            right.setGravity(Gravity.END);
            line.addView(left, new LinearLayout.LayoutParams(0, -2, 1));
            line.addView(right);
            exercises.addView(line);
        }

        if (m.assessment) {
            TextView record = action("RECORD FITNESS TEST", true, v -> showAssessmentDialog(selectedDay));
            content.addView(record, new LinearLayout.LayoutParams(-1, -2));
            addGap(10);
        } else if (!m.rest) {
            LinearLayout timerPanel = panel();
            timerPanel.addView(text("REST TIMER", 14, MUTED, true));
            timerText = text(formatTime(remaining), 36, WHITE, true);
            timerPanel.addView(timerText);
            LinearLayout buttons = row();
            TextView start = action(timer == null ? "START 90S" : "PAUSE", true, v -> toggleTimer());
            TextView reset = action("RESET", false, v -> resetTimer());
            LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -2, 1);
            half.setMargins(0, dp(10), dp(6), 0);
            buttons.addView(start, half);
            LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, -2, 1);
            half2.setMargins(dp(6), dp(10), 0, 0);
            buttons.addView(reset, half2);
            timerPanel.addView(buttons);
        }

        TextView complete = action(done(selectedDay) ? "UNDO COMPLETION" : "COMPLETE DAY", !done(selectedDay), v -> {
            prefs.edit().putBoolean("done_" + selectedDay, !done(selectedDay)).apply();
            render();
        });
        content.addView(complete, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout move = row();
        TextView prev = action("‹ PREVIOUS", false, v -> {
            selectedDay = Math.max(1, selectedDay - 1);
            render();
        });
        TextView today = action("TODAY", false, v -> {
            selectedDay = currentDay();
            render();
        });
        TextView next = action("NEXT ›", false, v -> {
            selectedDay = Math.min(90, selectedDay + 1);
            render();
        });
        LinearLayout.LayoutParams third = new LinearLayout.LayoutParams(0, -2, 1);
        third.setMargins(0, dp(10), dp(4), 0);
        move.addView(prev, third);
        LinearLayout.LayoutParams third2 = new LinearLayout.LayoutParams(0, -2, 1);
        third2.setMargins(dp(4), dp(10), dp(4), 0);
        move.addView(today, third2);
        LinearLayout.LayoutParams third3 = new LinearLayout.LayoutParams(0, -2, 1);
        third3.setMargins(dp(4), dp(10), 0, 0);
        move.addView(next, third3);
        content.addView(move);
    }

    private void renderPlan() {
        content.addView(text("90-DAY ROADMAP", 28, WHITE, true));
        TextView sub = text("Tap any mission to open it.", 14, MUTED, false);
        sub.setPadding(0, dp(4), 0, dp(14));
        content.addView(sub);

        for (int d = 1; d <= 90; d++) {
            WorkoutPlan.Mission m = WorkoutPlan.forDay(d);
            LinearLayout p = panel();
            LinearLayout top = row();
            top.addView(text("DAY " + d, 13, done(d) ? LIME : ORANGE, true), new LinearLayout.LayoutParams(0, -2, 1));
            top.addView(text(done(d) ? "DONE ✓" : m.phase, 12, done(d) ? LIME : MUTED, true));
            p.addView(top);
            p.addView(text(m.title, 18, WHITE, true));
            final int pick = d;
            p.setOnClickListener(v -> {
                selectedDay = pick;
                page = 0;
                render();
            });
        }
    }

    private void renderStats() {
        int complete = completedCount();
        content.addView(text("YOUR PROGRESS", 28, WHITE, true));

        LinearLayout score = panel();
        score.addView(text(complete + " / 90 DAYS", 32, ORANGE, true));
        score.addView(text("RANK: " + WorkoutPlan.rank(complete), 18, LIME, true));
        score.addView(text(String.format(Locale.US, "%.0f%% complete", complete * 100.0 / 90.0), 14, MUTED, false));

        int[] tests = {1, 29, 57, 90};
        for (int d : tests) {
            LinearLayout p = panel();
            p.addView(text("DAY " + d + " TEST", 17, WHITE, true));
            int push = prefs.getInt("test_" + d + "_push", -1);
            int plank = prefs.getInt("test_" + d + "_plank", -1);
            int squat = prefs.getInt("test_" + d + "_squat", -1);
            if (push < 0) p.addView(text("Not recorded yet", 14, MUTED, false));
            else {
                p.addView(text("Push-ups: " + push, 14, WHITE, false));
                p.addView(text("Plank: " + plank + " sec", 14, WHITE, false));
                p.addView(text("Squats: " + squat, 14, WHITE, false));
            }
        }
    }

    private void renderSetup() {
        content.addView(text("SETUP", 28, WHITE, true));

        LinearLayout difficultyPanel = panel();
        difficultyPanel.addView(text("DIFFICULTY", 16, WHITE, true));
        difficultyPanel.addView(text("Build = 60% rounds • Hard = 80% • Extreme = 100%", 13, MUTED, false));
        String[] names = {"BUILD", "HARD", "EXTREME"};
        for (int i = 0; i < names.length; i++) {
            final int value = i;
            TextView b = action((difficulty() == i ? "✓ " : "") + names[i], difficulty() == i, v -> {
                prefs.edit().putInt("difficulty", value).apply();
                render();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, dp(10), 0, 0);
            difficultyPanel.addView(b, lp);
        }

        LinearLayout safety = panel();
        safety.addView(text("TRAINING RULES", 16, WHITE, true));
        safety.addView(text("• Warm up before hard work\n• Keep clean form\n• Recovery days count\n• Do not train through sharp pain\n• Eat, hydrate and sleep enough", 14, MUTED, false));

        TextView restart = action("RESET ALL PROGRESS", false, v -> new AlertDialog.Builder(this)
                .setTitle("Reset Beast Mode 90?")
                .setMessage("This deletes all completed days and fitness-test results on this device.")
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("RESET", (d, w) -> {
                    prefs.edit().clear().putLong("start_day", LocalDate.now().toEpochDay()).putInt("difficulty", 2).putBoolean("intro_seen", true).apply();
                    selectedDay = 1;
                    render();
                }).show());
        content.addView(restart, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showAssessmentDialog(int day) {
        LinearLayout box = column();
        box.setPadding(dp(20), dp(4), dp(20), 0);

        EditText push = numberField("Push-ups");
        EditText plank = numberField("Plank seconds");
        EditText squat = numberField("Squats");
        box.addView(push);
        box.addView(plank);
        box.addView(squat);

        new AlertDialog.Builder(this)
                .setTitle("DAY " + day + " FITNESS TEST")
                .setView(box)
                .setNegativeButton("CANCEL", null)
                .setPositiveButton("SAVE", (d, w) -> {
                    prefs.edit()
                            .putInt("test_" + day + "_push", parse(push))
                            .putInt("test_" + day + "_plank", parse(plank))
                            .putInt("test_" + day + "_squat", parse(squat))
                            .putBoolean("done_" + day, true)
                            .apply();
                    render();
                }).show();
    }

    private EditText numberField(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);
        return e;
    }

    private int parse(EditText e) {
        try {
            return Integer.parseInt(e.getText().toString().trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private void toggleTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            render();
            return;
        }

        timer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long ms) {
                remaining = ms;
                if (timerText != null) timerText.setText(formatTime(ms));
            }

            @Override
            public void onFinish() {
                remaining = 90_000;
                timer = null;
                if (timerText != null) timerText.setText("DONE");
            }
        }.start();
        render();
    }

    private void resetTimer() {
        if (timer != null) timer.cancel();
        timer = null;
        remaining = 90_000;
        render();
    }

    private String formatTime(long ms) {
        long seconds = (ms + 999) / 1000;
        return String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60);
    }

    private void addGap(int amount) {
        View v = new View(this);
        content.addView(v, new LinearLayout.LayoutParams(1, dp(amount)));
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
