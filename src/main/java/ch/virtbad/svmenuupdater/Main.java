package ch.virtbad.svmenuupdater;

public class Main {
    public static void main(String[] args) { new Main(args); }
    private static final String VERSION = "0.1.0";

    private enum Mode {
        FILE,
        DIRECTORY,
        WEB,
        TODAY
    }

    public Boolean action = null;
    public String upstreamUrl = null;
    public boolean print = false;
    public String pdfLocation = null;
    public Mode mode = null;

    public Main(String[] args) {
        processArguments(args);

        processInputs();
    }

    private void processInputs() {
        if (action == null) {
            System.out.println("Please specify an action to do! Use --help for help.");
            return;
        }

        if (action) {
            
        }
    }

    public void processArguments(String[] args) {
        for (int i = 0; i < args.length; i++) {

            if (args[i].startsWith("-")) {
                switch (args[i]) {
                    // Outputs
                    case "-u", "--url" -> {
                        i++;
                        upstreamUrl = args[i];
                    }
                    case "-p", "--print" -> {
                        print = true;
                    }

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

    public void printVersion() {
        System.out.println("SV Menu Updater version " + VERSION + "\n  Licensed under the MIT License\n  https://github.com/virtbad/menu-updater");
    }

}
