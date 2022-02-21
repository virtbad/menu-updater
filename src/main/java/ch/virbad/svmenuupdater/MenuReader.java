package ch.virbad.svmenuupdater;

import ch.wsb.svmenuparser.menu.Menu;
import ch.wsb.svmenuparser.parser.MenuParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains methods to read menu pdfs from various sources.
 */
@Slf4j
public class MenuReader {

    /**
     * Reads the menus from a file.
     * @param file pdf file to read from
     * @return read menus
     */
    public static List<Menu> readFile(File file) {
        log.info("Reading pdf from {}", file.getAbsolutePath());

        if (file.isFile()) {
            try {
                return readPDFs(PDDocument.load(file));
            } catch (Exception e) {
                log.error("Failed to read file as a PDF: {}", file);
                e.printStackTrace();
            }
        } else log.error("Failed to read file as pdf because it is a folder: {}", file);

        return new ArrayList<>();
    }

    /**
     * Reads a folder containing pdf files.
     * @param dir directory to read
     * @return list of menus read
     */
    public static List<Menu> readFolder(File dir) {
        List<PDDocument> documents = new ArrayList<>();
        log.info("Reading pdfs from {}", dir.getAbsolutePath());

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                try {
                    documents.add(PDDocument.load(file));
                } catch (IOException e) {
                    log.error("Failed to read file in folder as a PDF: {}", file);
                }
            }
        } else {
            log.error("Failed to read menus from folder because it is a file: {}", dir);
            return new ArrayList<>();
        }

        return readPDFs(documents.toArray(new PDDocument[0]));
    }

    /**
     * Reads the menus from a given url.
     * @param url url to read from
     * @return list of menus read
     */
    public static List<Menu> readUrl(URL url) {
        log.info("Reading pdf from {}", url);

        try {
            return readPDFs(PDDocument.load(url.openStream()));
        } catch (Exception e) {
            log.error("Failed to read url as a PDF: {}", url);
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Reads the menus from pdfs given to the function.
     * @param pdfs pdf or pdfs to read
     * @return read menus from the pdfs
     */
    public static List<Menu> readPDFs(PDDocument... pdfs) {
        log.info("Reading {} pdfs", pdfs.length);

        try {
            MenuParser parser = new MenuParser();
            for (PDDocument pdf : pdfs) {
                parser.readPDF(pdf);
            }
            return parser.getMenus();
        } catch (Exception e) {
            log.error("Failed to parse menus!");
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
