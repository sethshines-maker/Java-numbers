import java.util.*;

public class Main {
    static final List<String> PROPS = List.of(
        "EVEN", "ODD", "BUZZ", "DUCK", "PALINDROMIC", "GAPFUL",
        "SPY", "SQUARE", "SUNNY", "JUMPING", "HAPPY", "SAD");

    // Pairs that can't both be present
    static final String[][] EXCLUSIVE = {
        {"EVEN", "ODD"}, {"DUCK", "SPY"}, {"SUNNY", "SQUARE"}, {"SAD", "HAPPY"}
    };
    // Pairs that can't both be absent (every number has exactly one of them)
    static final String[][] COMPLEMENT = {
        {"EVEN", "ODD"}, {"SAD", "HAPPY"}
    };

    static boolean isSquare(long n) {
        long r = (long) Math.sqrt((double) n);
        while (r * r > n) r--;
        while ((r + 1) * (r + 1) <= n) r++;
        return r * r == n;
    }

    static boolean isHappy(long n) {
        Set<Long> seen = new HashSet<>();
        while (n != 1 && seen.add(n)) {
            long sum = 0;
            for (long m = n; m > 0; m /= 10) {
                long d = m % 10;
                sum += d * d;
            }
            n = sum;
        }
        return n == 1;
    }

    static boolean has(long n, String p) {
        switch (p) {
            case "EVEN": return n % 2 == 0;
            case "ODD": return n % 2 != 0;
            case "BUZZ": return n % 7 == 0 || n % 10 == 7;
            case "DUCK": return String.valueOf(n).indexOf('0') >= 0;
            case "PALINDROMIC": {
                String s = String.valueOf(n);
                return new StringBuilder(s).reverse().toString().equals(s);
            }
            case "GAPFUL": {
                if (n < 100) return false;
                String t = String.valueOf(n);
                long d = (t.charAt(0) - '0') * 10 + (t.charAt(t.length() - 1) - '0');
                return n % d == 0;
            }
            case "SPY": {
                long sum = 0, prod = 1;
                for (char c : String.valueOf(n).toCharArray()) {
                    sum += c - '0';
                    prod *= c - '0';
                }
                return sum == prod;
            }
            case "SQUARE": return isSquare(n);
            case "SUNNY": return isSquare(n + 1);
            case "JUMPING": {
                String s = String.valueOf(n);
                for (int i = 1; i < s.length(); i++) {
                    if (Math.abs(s.charAt(i) - s.charAt(i - 1)) != 1) return false;
                }
                return true;
            }
            case "HAPPY": return isHappy(n);
            case "SAD": return !isHappy(n);
            default: return false;
        }
    }

    static String list(long n) {
        StringJoiner sj = new StringJoiner(", ");
        for (String p : PROPS) if (has(n, p)) sj.add(p.toLowerCase());
        return sj.toString();
    }

    static Long parse(String s) {
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    static boolean matches(long n, Set<String> include, Set<String> exclude) {
        for (String p : include) if (!has(n, p)) return false;
        for (String p : exclude) if (has(n, p)) return false;
        return true;
    }

    static void printInstructions() {
        System.out.println("Supported requests:");
        System.out.println("- enter a natural number to know its properties;");
        System.out.println("- enter two natural numbers to obtain the properties of the list:");
        System.out.println("  * the first parameter represents a starting number;");
        System.out.println("  * the second parameter shows how many consecutive numbers are to be printed;");
        System.out.println("- two natural numbers and properties to search for;");
        System.out.println("- a property preceded by minus must not be present in numbers;");
        System.out.println("- separate the parameters with one space;");
        System.out.println("- enter 0 to exit.");
    }

    static void mutuallyExclusive(String a, String b) {
        System.out.printf("%nThe request contains mutually exclusive properties: [%s, %s]%n", a, b);
        System.out.println("There are no numbers with these properties.");
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to Amazing Numbers!\n");
        printInstructions();

        while (true) {
            System.out.print("\nEnter a request: ");
            System.out.flush();
            if (!sc.hasNextLine()) break;
            String line = sc.nextLine().trim();

            if (line.isEmpty()) {
                System.out.println();
                printInstructions();
                continue;
            }

            String[] in = line.split("\\s+");
            Long first = parse(in[0]);

            if (first == null || first < 0 || (first == 0 && in.length > 1)) {
                System.out.println("\nThe first parameter should be a natural number or zero.");
                continue;
            }
            if (first == 0) {
                System.out.println("\nGoodbye!");
                break;
            }
            if (in.length == 1) {
                System.out.printf("%nProperties of %,d%n", first);
                for (String p : PROPS) {
                    System.out.printf("%12s: %b%n", p.toLowerCase(), has(first, p));
                }
                continue;
            }

            Long count = parse(in[1]);
            if (count == null || count < 1) {
                System.out.println("\nThe second parameter should be a natural number.");
                continue;
            }

            if (in.length == 2) {
                System.out.println();
                for (long i = 0; i < count; i++) {
                    System.out.printf("%,16d is %s%n", first + i, list(first + i));
                }
                continue;
            }

            Set<String> include = new LinkedHashSet<>();
            Set<String> exclude = new LinkedHashSet<>();
            List<String> wrong = new ArrayList<>();
            for (int i = 2; i < in.length; i++) {
                String raw = in[i].toUpperCase();
                boolean neg = raw.startsWith("-");
                String p = neg ? raw.substring(1) : raw;
                if (PROPS.contains(p)) {
                    (neg ? exclude : include).add(p);
                } else if (!wrong.contains(raw)) {
                    wrong.add(raw);
                }
            }

            if (!wrong.isEmpty()) {
                if (wrong.size() == 1) {
                    System.out.printf("%nThe property %s is wrong.%n", wrong);
                } else {
                    System.out.printf("%nThe properties %s are wrong.%n", wrong);
                }
                System.out.println("Available properties: " + PROPS);
                continue;
            }

            boolean conflict = false;

            // property and -property
            for (String p : include) {
                if (exclude.contains(p)) {
                    mutuallyExclusive(p, "-" + p);
                    conflict = true;
                    break;
                }
            }
            // both required but incompatible
            if (!conflict) {
                for (String[] pair : EXCLUSIVE) {
                    if (include.contains(pair[0]) && include.contains(pair[1])) {
                        mutuallyExclusive(pair[0], pair[1]);
                        conflict = true;
                        break;
                    }
                }
            }
            // both forbidden but one of them always holds
            if (!conflict) {
                for (String[] pair : COMPLEMENT) {
                    if (exclude.contains(pair[0]) && exclude.contains(pair[1])) {
                        mutuallyExclusive("-" + pair[0], "-" + pair[1]);
                        conflict = true;
                        break;
                    }
                }
            }
            if (conflict) continue;

            System.out.println();
            long n = first;
            for (long found = 0; found < count; n++) {
                if (matches(n, include, exclude)) {
                    System.out.printf("%,16d is %s%n", n, list(n));
                    found++;
                }
            }
        }
    }
}