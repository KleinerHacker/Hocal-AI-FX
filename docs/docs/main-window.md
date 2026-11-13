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

The generation details, the image and the history list are still empty placeholders; their contents
follow in later versions.

## Project

The application starts with a new, empty project. The window shows it:

* the folder tree on the left shows the folders of the project, the project itself as the topmost
  folder; behind every name, in gray, stands the number of the direct children of the folder
* the file list below the tree shows the files of the selected folder. Every row has two preview
  images - the original medium first and directly behind it the current medium, the last one created -
  and the name of the file. A file with one medium only shows its image twice. Previews are loaded in
  the background; until an image is there, and for a file that is no image, a placeholder is shown
* the list follows the project: a file that is added to the selected folder appears at once

## Prompt input

Below the image there is the input part:

* a multi-line text box for the prompt, with the send button behind it; the button is active as soon as
  the prompt is not empty, but sending does nothing yet
* above the text box on the left the add button; it opens a dialog that shows the folders of the project
  on the left and the files of the selected folder on the right. Pick a file and confirm with OK to add
  its image; Cancel or `Esc` adds nothing. An added image appears as a small chip with its name and a
  preview of its last medium; the cross on the chip removes it again
* above the text box on the right a selection of the model and below it a bar of buttons for the flow;
  both offer example entries for now and change each other: the flows depend on the chosen model
