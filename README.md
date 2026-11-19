# Book Guide

A Minecraft server documentation plugin delivered as a single book. One command opens the complete handbook, with a clickable table of contents and page navigation.

- Name: Book Guide
- Author: allry_
- Command: `/guide`
- Player permission: `bookguide.use`
- Admin permission: `bookguide.admin`

## Requirements

- Paper 1.21.4 or a compatible version
- Java 21
- Maven 3.9 or newer for building

## Installation

1. Copy `BookGuide-1.0.0.jar` into the server `plugins` folder.
2. Start the server once so `plugins/BookGuide/guide.yml` is created.
3. Edit `guide.yml`.
4. Run `/guide reload` as an admin.

## Commands

| Command | Function | Permission |
|---|---|---|
| `/guide` | Opens the Server Guide book | `bookguide.use` |
| `/guide reload` | Reloads and validates `guide.yml` | `bookguide.admin` |

## Content management

All documentation lives in a single `guide.yml` file. Each section has a list of `pages`, and each page has a list of `lines`.

- Reorder sections by changing their order under `sections`.
- Add a page by adding an item under `pages`.
- Remove a page by deleting the item from `pages`.
- Change commands or descriptions by editing the text lines.
- Text supports MiniMessage and legacy color codes such as `&a` or `&l`.
- The table of contents shows one clickable line per section with its page number. The default seven sections fit on one page.
- Page numbers, section headers, the back-to-contents button, and previous/next buttons are generated automatically.
- The plugin rejects content that is too long or exceeds 100 pages. Fix `guide.yml` and run `/guide reload` again.

Example format:

```yaml
sections:
  - title: "Example Section"
    pages:
      - lines:
          - "<yellow><bold>Page Title</bold></yellow>"
          - "<gray>A short explanation.</gray>"
```

Each item under `pages` becomes one book page. If the content is too long, the plugin splits it into multiple balanced book pages automatically.

### Command parameters

Angle brackets are MiniMessage tags. Escape command parameters with a backslash so they render literally:

```yaml
- '<aqua>/tpa \<player></aqua>'
- '<aqua>/buyclaimblocks \<amount></aqua>'
```

Without the backslash, `<player>` and `<amount>` are treated as tags and can fail validation or disappear from the text.

## License

MIT. See [LICENSE](LICENSE).
