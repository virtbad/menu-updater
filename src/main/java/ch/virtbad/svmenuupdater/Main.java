package ch.virtbad.svmenuupdater;

import ch.wsb.svmenuparser.menu.Menu;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is the main class and also handles the cli things
 */
@Slf4j
public class Main {
    public static void main(String[] args) { new Main(args); }
    private static final String VERSION = "1.0.0";

    private enum Mode {
        FILE,
        DIRECTORY,
        WEB,
        TODAY
    }

    public Boolean action = null;

    public String upstreamUrl = null;
    public boolean print = false;

    public String pdfLocation;
    public Mode mode;

    public Main(String[] args) {
        processArguments(args);

        processInputs();
    }

    /**
     * Processes the parsed arguments as inputs and runs the reading and submitting
     */
    private void processInputs() {
        if (action == null) {
            System.out.println("Please specify a mode of operation! Use --help for help.");
            return;
        }

        if (action) {
            if (!print && upstreamUrl == null) {
                System.out.println("Please specify an output method! Use --help for help.");
                return;
            }

            System.out.println("Initiating Parsing sequence!\n");

            List<Menu> menus = null;

            switch (mode) {
                case FILE -> menus = MenuReader.readFile(new File(pdfLocation));
                case DIRECTORY -> menus = MenuReader.readFolder(new File(pdfLocation));
                case WEB -> {
                    try {
                        menus = MenuReader.readUrl(new URL(pdfLocation));
                    } catch (MalformedURLException e) {
                        log.error("Invalid web url provided!");
                    }
                }
                case TODAY -> {
                    String url = createTodayUrlString(pdfLocation);

                    try {
                        menus = MenuReader.readUrl(new URL(url));
                    } catch (MalformedURLException e) {
                        log.error("Formatted url seems to be invalid: {}", url);
                    }
                }
            }

            if (menus == null) {
                System.err.println("\nParsing menus did not work!");
                System.exit(1);
            }

            if (print) {
                System.out.printf("\nPrinting out %d serialized menus:\n", menus.size());

                for (Menu menu : menus) {
                    System.out.printf("  Serialized menu on %s: %s - %s\n", new SimpleDateFormat("dd.MM.yy").format(menu.getDate()), menu.getTitle(), menu.getDescription());
                }
            } else {
                if (!new MenuSubmitter(upstreamUrl).submit(menus)) {
                    System.err.println("\nNot all menus were uploaded!");
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Creates the url string with the current date for the today argument
     * @param entered entered string with date format components
     * @return formatted string
     */
    public String createTodayUrlString(String entered) {
        Pattern p = Pattern.compile("\\{(\\w+)}");
        Date date = new Date();

        Matcher m = p.matcher(entered);
        while (m.find()) {
            String replacement = "";
            if (m.group(1).equals("ww")) { // Create custom week number not based off of the week year
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setFirstDayOfWeek(GregorianCalendar.MONDAY);
                calendar.setMinimalDaysInFirstWeek(4);
                calendar.setTime(date);

                replacement = "" + calendar.get(GregorianCalendar.WEEK_OF_YEAR);
                if (replacement.length() == 1) replacement = "0" + replacement;

            } else replacement = new SimpleDateFormat(m.group(1)).format(date);

            entered = entered.replaceFirst(Pattern.quote(m.group(0)), replacement);
        }

        return entered;
    }

    /**
     * Processes the arguments given to the jar on execution
     * @param args args given
     */
    public void processArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {

            if (args[i].startsWith("-")) {
                switch (args[i]) {
                    // Outputs
                    case "-u", "--url" -> {
                        i++;
                        upstreamUrl = args[i];
                    }
                    case "-p", "--print" -> print = true;

                    // Modes
                    case "-f", "--file" -> {
                        action = true;

                        i++;
                        mode = Mode.FILE;
                        pdfLocation = args[i];
                    }
                    case "-d", "--directory" -> {
                        action = true;

                        i++;
                        mode = Mode.DIRECTORY;
                        pdfLocation = args[i];
                    }
                    case "-w", "--web" -> {
                        action = true;

                        i++;
                        mode = Mode.WEB;
                        pdfLocation = args[i];
                    }
                    case "-t", "--today" -> {
                        action = true;
                        i++;
                        mode = Mode.TODAY;
                        pdfLocation = args[i];
                    }

                    // Information
                    case "-h", "--help" -> {
                        if (action != null && action) break;

                        action = false;
                        printHelp();
                        return;
                    }
                    case "-v", "--version" -> {
                        if (action != null && action) break;

                        action = false;
                        printVersion();
                        return;
                    }
                }
            }
        }
    }

    /**
     * Prints the help message to the standard out
     */
    public void printHelp() {
        System.out.println(
                """
                
                SV Menu Updater
                ---------------
                Information Commands:
                  --help | Displays this page
                  --version | Displays the version of this program
                Output arguments:
                  --url [upstream-url] | Sets the url of the endpoint to upload menus
                  --print | Performs a dry run by only printing menus to console
                Mode arguments:
                  --file [file-path] | Loads the menu pdf from the given file
                  --directory [dir-path] | Loads the menu pdfs from the given directory
                  --web [pdf-url] | Loads the menu pdf from the given url
                  --today [special-url] | Loads the pdf from an url, with time things inserted in curly brackets
                For every argument, a shorthand with the first letter is available.
                
                Example:
                java -jar [jarname] -u http://localhost/menu -t https://restaurant.com/menu/week{yyyy}-{ww}.pdf
                """
        );
    }

    /**
     * Prints the version to the standard out
     */
    public void printVersion() {
        System.out.println("SV Menu Updater version " + VERSION + "\n  Licensed under the MIT License\n  https://github.com/virtbad/menu-updater");
    }

}
