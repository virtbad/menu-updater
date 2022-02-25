package ch.virtbad.svmenuupdater;

import ch.wsb.svmenuparser.menu.Menu;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * This class is the main class and also handles the cli things
 */
@Slf4j
public class Main {
    public static void main(String[] args) { new Main(args); }
    private static final String VERSION = "1.1.0";

    private enum Mode {
        FILE,
        DIRECTORY,
        WEB,
        SPECIAL
    }

    private enum SpecialInterval {
        ONCE,
        DAY,
        WEEK,
        MONTH
    }

    public Boolean action = null;

    public String upstreamUrl = null;
    public boolean print = false;

    public String pdfLocation;
    public Mode mode;

    public SpecialInterval special = null;

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
                case SPECIAL -> {
                    if (special == null) {
                        special = SpecialInterval.ONCE;
                        System.out.println("No special flag provided! Using today flag by default.");
                    }

                    menus = new ArrayList<>();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(new Date());

                    loop: while(true) {
                        URL url = createDatedUrl(pdfLocation, calendar.getTime());
                        if (url == null) {
                            log.error("Formatted url seems to be invalid!");
                            break;
                        }

                        if (!doesUrlResourceExist(url)) {
                            log.info("Url does not seem to exist, aborting: {}", url);
                            break;
                        }

                        List<Menu> newMenus = MenuReader.readUrl(url);
                        if (newMenus == null) {
                            log.warn("Could not parse menus from url, moving on: {}", url);
                            continue;
                        }

                        menus.addAll(newMenus);

                        switch (special) {
                            case ONCE -> {
                                break loop;
                            }
                            case DAY -> calendar.add(Calendar.DATE, 1);
                            case WEEK -> calendar.add(Calendar.DATE, 7);
                            case MONTH -> calendar.add(Calendar.MONTH, 1);
                        }
                    }
                }
            }

            if (menus == null || menus.size() == 0) {
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

    public boolean doesUrlResourceExist(URL url) {
        try {
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");
            return huc.getResponseCode() == HTTP_OK;
        } catch (IOException ignored) {}

        return false;
    }

    /**
     * Creates the url string with the current date for the today argument
     * @param entered entered string with date format components
     * @param date date to use to create string
     * @return formatted string
     */
    public URL createDatedUrl(String entered, Date date) {
        Pattern p = Pattern.compile("\\{(\\w+)}");

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

        try {
            return new URL(entered);
        } catch (MalformedURLException e) {
            return null;
        }
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
                    case "-s", "--special" -> {
                        action = true;
                        i++;
                        mode = Mode.SPECIAL;
                        pdfLocation = args[i];
                    }

                    // Special modes
                    case "-t", "--today" -> {
                        special = SpecialInterval.ONCE;
                    }
                    case "-n", "--next" -> {
                        i++;
                        switch (args[i]) {
                            case "day" -> special = SpecialInterval.DAY;
                            case "week" -> special = SpecialInterval.WEEK;
                            case "month" -> special = SpecialInterval.MONTH;
                        }
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
                  --special [special-url] | Loads the pdf from a special timed url. Additional flags required.
                Special flags:
                  --today | Loads only the current pdf using the current time.
                  --next [interval] | Loads all pdfs, until no new one is found. Advances by interval (day, week or month)
                For every argument, a shorthand with the first letter is available.
                
                Example:
                java -jar [jarname] -u http://localhost/menu -s https://restaurant.com/menu/week{yyyy}-{ww}.pdf -n week
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
