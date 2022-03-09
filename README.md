# Menu Updater

This is a CLI tool to update the database of a hosted [menu api](https://github.com/virtbad/menu-api).

## Usage

The CLI in of itself should be self-explanatory. To see the usable command, you can use

```shell
java -jar menu-updater.jar --help
```

## Installation

To use this cli, you can download a built jar from the release section. Then, you can execute it in the directory, like
pointed out in the usage.

To build it from source, run ```./gradlew jar```, and then execute the jar which is located somewhere in the build
directory.

## Functionality

To get then update the Database of a menu api, you should execute this cli on the same host, the api is hosted. If that
is done, this api can use an [endpoint](https://github.com/virtbad/menu-api/blob/main/docs/menu.md#submit-menu) which is
only accessible on the local host. Thus, you should provide an url that points to this endpoint when using
the ```--url``` argument.

This CLI is just a wrapper for the underlying library [SVMenuParser](https://github.com/WhySoBad/SVMenuParser), which
enables to parse the menu PDFs. If you want to build your own application around parsing these PDFs, you can use it
directly.

## Related Projects

* [menu-api](https://github.com/virtbad/menu-api)
* [menu-website](https://github.com/virtbad/menu-website)
* [menu-telegram-bot](https://github.com/virtbad/menu-telegram-bot)
* [menu-cli](https://github.com/virtbad/menu-cli)

## License

Coming Soon.