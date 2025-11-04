package source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SConsole {

    public static class ConsoleDimensions {

        public int width;
        public int height;

        public ConsoleDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    public static ConsoleDimensions getConsoleDimensions() {
        // 1. Environment Variables
        String columns = System.getenv("COLUMNS");
        String lines = System.getenv("LINES");

        if (columns != null && lines != null) {
            try {
                return new ConsoleDimensions(
                    Integer.parseInt(columns),
                    Integer.parseInt(lines)
                );
            } catch (NumberFormatException e) {
                // Environment variables not valid numbers, try tput
            }
        }

        // 2. tput (Unix-like)
        String osName = System.getProperty("os.name").toLowerCase();
        if (
            osName.contains("nix") ||
            osName.contains("nux") ||
            osName.contains("mac")
        ) {
            try {
                int width = executeTput("cols");
                int height = executeTput("lines");

                if (width > 0 && height > 0) {
                    return new ConsoleDimensions(width, height);
                }
            } catch (IOException | InterruptedException e) {
                // tput failed, use defaults
            }
        }

        // 3. Windows
        if (osName.contains("win")) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(
                    "mode",
                    "con"
                );
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
                );
                String line;
                int width = -1;
                int height = -1;

                while ((line = reader.readLine()) != null) {
                    if (line.contains("Columns:")) {
                        Pattern pattern = Pattern.compile("Columns:\\s+(\\d+)");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            width = Integer.parseInt(matcher.group(1));
                        }
                    } else if (line.contains("Lines:")) {
                        Pattern pattern = Pattern.compile("Lines:\\s+(\\d+)");
                        Matcher matcher = pattern.matcher(line);
                        if (matcher.find()) {
                            height = Integer.parseInt(matcher.group(1));
                        }
                    }
                }

                process.waitFor();

                do {
                    if (process.exitValue() != 0) {
                        break;
                    }

                    if (width > 0 && height > 0) {
                        return new ConsoleDimensions(width, height);
                    }
                } while (false);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 3. Defaults
        return new ConsoleDimensions(100, 40); // Common default values
    }

    private static int executeTput(String command)
        throws IOException, InterruptedException {
        Process process = new ProcessBuilder(
            "bash",
            "-c",
            "tput " + command + " 2> /dev/tty"
        ).start();
        process.waitFor();

        if (process.exitValue() == 0) {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String output = reader.readLine();
            if (output != null) {
                try {
                    return Integer.parseInt(output);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }
}
