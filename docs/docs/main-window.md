# Main window

The main window opens when the application starts. It has a size of 1280 x 800 pixels and can be
resized freely.

## Menu

| Menu | Entry | Shortcut     | Action                |
|------|-------|--------------|-----------------------|
| File | Exit  | `Ctrl` + `Q` | Closes the main window |

On macOS the shortcut is `Cmd` + `Q`.

## Layout

The window below the menu is divided into resizable parts. Drag a divider to change the share of
the neighbouring parts. When the window grows or shrinks, the project side, the generation details
and the history list keep their width; the media with the prompt input takes the difference.

| Part                              | Position                         | Initial share                |
|-----------------------------------|----------------------------------|------------------------------|
| Project side                      | left                             | 30 % of the window width     |
| Tree                              | project side, top                | 50 % of the project side     |
| File list                         | project side, bottom             | 50 % of the project side     |
| Content area                      | right                            | 70 % of the window width     |
| Generation details                | content area, left               | 20 % of the content area     |
| Media with prompt input           | content area, centre             | 60 % of the content area     |
| History list                      | content area, right              | 20 % of the content area     |

The parts are still empty placeholders; their contents follow in later versions.
