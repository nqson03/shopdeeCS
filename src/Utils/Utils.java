package Utils;

import java.util.InputMismatchException;
import java.util.Optional;
import java.util.Scanner;

public final class Utils {
    private static final Scanner sc = new Scanner(System.in);

    private Utils() {
    }

    public static double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }

    public static String promptInput(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public static Optional<Integer> promptIntInput(String prompt) {
        System.out.print(prompt);
        try {
            int res = sc.nextInt();
            sc.nextLine();
            return Optional.of(res);
        } catch (InputMismatchException e) {
            sc.nextLine();
            return Optional.empty();
        }
    }

    public static Optional<Double> promptDoubleInput(String prompt) {
        System.out.print(prompt);
        try {
            double res = sc.nextInt();
            sc.nextLine();
            return Optional.of(res);
        } catch (InputMismatchException e) {
            sc.nextLine();
            return Optional.empty();
        }
    }
}
