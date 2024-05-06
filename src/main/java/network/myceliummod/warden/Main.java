package network.myceliummod.warden;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.MatchResult;

public class Main {

    public static void main(String[] args) {
        long[] times = new long[100000];
        for (int i = 0; i < times.length; i++) {
            long start = System.nanoTime();
            // TODO add command args
            // TODO Load rules from file?
            // TODO Load rules from args?
            // TODO Disable default rules?
            final DomainRules rules = DomainRules.builtin();
            for (String arg : args) {
                List<MatchResult> matchingFiles = new LinkedList<>();

                boolean result = checkFile(rules, new File(arg), matchingFiles);
                if (i == 0) {
                    if (!result) {
                        System.out.println("No matching files were found.");
                    } else {
                        System.out.println("Found " + matchingFiles.size() + " matching files:");
                        for (MatchResult match : matchingFiles) {
                            System.out.println("File='" + match.file() + "' host='" + match.zoneId().getHost() + "' referrer='" + match.zoneId().getReferrer() + "'.");
                        }
                    }
                }
                long end = System.nanoTime();
                times[i] = end - start;
            }
        }

        long total = 0;
        long best = Long.MAX_VALUE;
        long worst = Long.MIN_VALUE;
        for (int i = 0; i < times.length; i++) {
            if (i == 0) {
                continue;
            }

            long time = times[i];
            if (time < best) {
                best = time;
            }
            if (time > worst) {
                worst = time;
            }
            total += time;
        }

        long avg = total / (times.length - 1);
        long avgMs = avg / 1_000_000;


        System.out.printf("Runs: %d; Average time: %dns (%dms); best: %dns worst: %dns%n", times.length, avg, avgMs, best, worst);
    }

    /**
     * Check a file or directory recursively for files that were downloaded from a given list of domain rules.
     *
     * @param rules  The rules to match against.
     * @param target The target file. If the target is a directory it will be checked recursively.
     * @return True if one or more files checked were downloaded from a site matching the list of domain rules.
     */
    private static boolean checkFile(DomainRules rules, File target, List<MatchResult> matchingFiles) {
        boolean hasMatch = false;
        if (!target.exists()) {
            throw new IllegalArgumentException("The file does not exist! '" + target.getAbsolutePath() + "'");
        }
        else if (target.isFile()) {
            final ZoneIdentifier zoneId = ZoneIdentifier.from(target);
            if (zoneId != null && rules.test(zoneId)) {
                matchingFiles.add(new MatchResult(target, zoneId));
                hasMatch = true;
            }
        }
        else if (target.isDirectory()) {
            for (File subTarget : Objects.requireNonNull(target.listFiles())) {
                hasMatch |= checkFile(rules, subTarget, matchingFiles);
            }
        }
        return hasMatch;
    }

    private record MatchResult(File file, ZoneIdentifier zoneId) {
    }
}
