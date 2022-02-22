package ch.virtbad.svmenuupdater;

import ch.wsb.svmenuparser.menu.Menu;
import ch.wsb.svmenuparser.menu.MenuPrice;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to submit a bunch of menus to an upstream api.
 */
@Slf4j
public class MenuSubmitter {

    private HttpClient client;
    private Gson gson;
    private String url;

    /**
     * Creates a menu submitter.
     * @param url url of the upstream api (should be localhost because probably the api is limited to localhost only submissions)
     */
    public MenuSubmitter(String url) {
        client = HttpClient.newHttpClient();
        gson = new Gson();
        this.url = url;
    }

    /**
     * Submits a list of menus to the api.
     * @param menus menus to submit
     */
    public void submit(List<Menu> menus) {
        log.info("Submitting {} menus.", menus.size());
        for (Menu menu : menus) {
            submit(menu);
        }
    }

    /**
     * Submits a menu to the api.
     * @param menu menu to submit
     */
    public void submit(Menu menu) {
        log.info("Submitting Menu: {}", menu.getTitle());
        try {
            HttpResponse<Void> response = client.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(new RequestMenu(menu))))
                            .build(),
                    HttpResponse.BodyHandlers.discarding());

            if (response.statusCode() != 200) log.error("Failed to submit menu with error: {}", response.statusCode());

        } catch (IOException | InterruptedException e) {
            log.error("Failed to submit Menu!");
            e.printStackTrace();
        }
    }

    /**
     * This class and its subclasses are to establish the structure the api requires.
     */
    private static class RequestMenu {
        private String title;
        private String description;
        private long date;
        private int channel;
        private int label;
        private List<RequestPrice> prices;

        public RequestMenu(Menu menu) {
            this.title = menu.getTitle();
            this.description = menu.getDescription();
            this.date = menu.getDate().getTime();
            this.channel = menu.getMenuGroup();
            this.label = menu.getLabel() == null ? 0 : menu.getLabel().ordinal() + 1;

            this.prices = new ArrayList<>();
            for (MenuPrice menuPrice : menu.getPrice()) {
                this.prices.add(new RequestPrice(menuPrice));
            }
        }

        private static class RequestPrice {

            public RequestPrice(MenuPrice price) {
                this.tag = price.getGroup();
                this.price = Float.parseFloat(price.getPrice());
            }

            private String tag;
            private float price;
        }
    }

}
