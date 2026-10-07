package com.beastmode.ninety;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class WorkoutPlan {
    public static final class Exercise {
        public final String name;
        public final String target;
        public Exercise(String name, String target) {
            this.name = name;
            this.target = target;
        }
    }

    public static final class Mission {
        public final int day;
        public final int week;
        public final int rounds;
        public final String title;
        public final String phase;
        public final String note;
        public final boolean assessment;
        public final boolean rest;
        public final List<Exercise> exercises;

        Mission(int day, int week, int rounds, String title, String phase, String note,
                boolean assessment, boolean rest, List<Exercise> exercises) {
            this.day = day;
            this.week = week;
            this.rounds = rounds;
            this.title = title;
            this.phase = phase;
            this.note = note;
            this.assessment = assessment;
            this.rest = rest;
            this.exercises = exercises;
        }
    }

    private static Exercise e(String n, String t) {
        return new Exercise(n, t);
    }

    private static List<Exercise> ex(Exercise... values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    private static String pick(int phase, String a, String b, String c) {
        return phase == 0 ? a : phase == 1 ? b : c;
    }

    public static Mission forDay(int day) {
        if (day < 1 || day > 90) throw new IllegalArgumentException("day must be 1..90");

        int week = Math.min(13, ((day - 1) / 7) + 1);

        if (day == 1 || day == 29 || day == 57 || day == 90) {
            return new Mission(
                    day, week, 0,
                    day == 90 ? "FINAL BEAST TEST" : "FITNESS CHECKPOINT",
                    day == 90 ? "FINAL TEST" : "ASSESSMENT",
                    "Record clean push-ups, plank time and squats. Warm up first and stop before form breaks.",
                    true, false,
                    ex(e("Clean push-ups", "maximum quality reps"),
                       e("Plank", "maximum quality hold"),
                       e("Bodyweight squats", "maximum quality reps"))
            );
        }

        if (day >= 85) {
            switch (day) {
                case 85:
                    return new Mission(day, week, 1, "UPPER BODY PRIMER", "FINAL WEEK",
                            "Light work only. Save energy for the final test.", false, false,
                            ex(e("Push-ups", "2 × 8–12"), e("Reverse snow angels", "2 × 12"), e("Plank", "2 × 30 sec")));
                case 86:
                    return new Mission(day, week, 1, "LOWER BODY PRIMER", "FINAL WEEK",
                            "Controlled reps only.", false, false,
                            ex(e("Squats", "2 × 15"), e("Reverse lunges", "2 × 8/leg"), e("Glute bridges", "2 × 12")));
                case 87:
                    return new Mission(day, week, 0, "RECOVERY MARCH", "FINAL WEEK",
                            "Keep effort easy.", false, true,
                            ex(e("Easy walk", "20 minutes"), e("Mobility", "10 minutes")));
                case 88:
                    return new Mission(day, week, 1, "CONTROLLED FINAL CIRCUIT", "FINAL WEEK",
                            "Do not chase failure today.", false, false,
                            ex(e("Push-ups", "2 × 8"), e("Squats", "2 × 15"), e("Sit-ups", "2 × 12"), e("Plank", "2 × 30 sec")));
                case 89:
                    return new Mission(day, week, 0, "PRE-TEST REST", "FINAL WEEK",
                            "Rest, hydrate and get good sleep.", false, true, ex());
            }
        }

        int[] roundPattern = {2,3,3,2,3,4,4,2,4,4,5,2};
        int phase = day <= 28 ? 0 : day <= 56 ? 1 : 2;
        int rounds = roundPattern[Math.min(11, week - 1)];
        int dow = (day - 1) % 7;
        String phaseName = phase == 0 ? "FOUNDATION" : phase == 1 ? "BUILD" : "PEAK";
        String note = "Complete each exercise in order. Rest 60–120 seconds after each round. Stop when form breaks.";

        switch (dow) {
            case 0:
                return new Mission(day, week, rounds, "UPPER BODY ASSAULT", phaseName, note, false, false,
                        ex(e("Push-ups", pick(phase,"6–10","10–15","12–20")),
                           e("Pike push-ups", pick(phase,"5–6","6–8","8–10")),
                           e("Reverse snow angels", pick(phase,"12","15","20")),
                           e("Sit-ups or crunches", pick(phase,"10–15","15–20","20–25")),
                           e("Plank", pick(phase,"30 sec","40 sec","50 sec"))));
            case 1:
                return new Mission(day, week, rounds, "LEG STRENGTH", phaseName, note, false, false,
                        ex(e("Squats", pick(phase,"15","20","25")),
                           e("Reverse lunges", pick(phase,"8/leg","10/leg","12/leg")),
                           e("Glute bridges", pick(phase,"15","20","25")),
                           e("Calf raises", pick(phase,"20","25","30")),
                           e("Wall sit", pick(phase,"30 sec","45 sec","60 sec"))));
            case 2:
                return new Mission(day, week, rounds, "CARDIO & CORE", phaseName, note, false, false,
                        ex(e("High knees or marching", pick(phase,"30 sec","40 sec","45 sec")),
                           e("Mountain climbers", pick(phase,"20 sec","30 sec","40 sec")),
                           e("Burpees or step-backs", pick(phase,"4–6","6–8","8–10")),
                           e("Bicycle crunches", pick(phase,"12","16","20")),
                           e("Side plank", pick(phase,"20 sec/side","30 sec/side","40 sec/side"))));
            case 3:
                return new Mission(day, week, 0, "ACTIVE RECOVERY", phaseName,
                        "Keep this day easy. Recovery is part of the program.", false, true,
                        ex(e("Brisk walk", "20–30 min"), e("Gentle mobility", "10 min")));
            case 4:
                return new Mission(day, week, rounds, "FULL BODY GAUNTLET", phaseName, note, false, false,
                        ex(e("Push-ups", pick(phase,"8","12","15")),
                           e("Squats", pick(phase,"15","20","25")),
                           e("Reverse lunges", pick(phase,"8/leg","10/leg","12/leg")),
                           e("Sit-ups", pick(phase,"12","18","22")),
                           e("Plank", pick(phase,"30 sec","45 sec","60 sec"))));
            case 5:
                return new Mission(day, week, rounds, "ENDURANCE MISSION", phaseName, note, false, false,
                        ex(e("Jumping jacks or step jacks", pick(phase,"30 sec","40 sec","50 sec")),
                           e("Push-ups", pick(phase,"6","10","12")),
                           e("Squats", pick(phase,"15","20","25")),
                           e("Mountain climbers", pick(phase,"20 sec","30 sec","40 sec")),
                           e("Bird dogs", pick(phase,"8/side","10/side","12/side"))));
            default:
                return new Mission(day, week, 0, "FULL REST", phaseName,
                        "No strenuous workout today. Recovery counts.", false, true, ex());
        }
    }

    public static String rank(int completed) {
        if (completed >= 75) return "ELITE";
        if (completed >= 50) return "VANGUARD";
        if (completed >= 30) return "RANGER";
        if (completed >= 10) return "SOLDIER";
        return "RECRUIT";
    }
}
