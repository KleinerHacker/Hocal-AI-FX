![Hocal AI logo](assets/logo.png#only-light){ .hocal-logo }
![Hocal AI logo](assets/logo-dark.png#only-dark){ .hocal-logo }

# Hocal AI

Hocal AI is a desktop application for generating images and videos with AI models that run on your
own machine. It drives [ComfyUI](https://docs.comfy.org/) in the background and offers its work as
**flows**: a flow is a ready-made generation pipeline - for example "Text to Image" or "Upscale" -
that you pick and run without building a ComfyUI graph yourself.

!!! note "Early stage"
    The application is at its very beginning. It currently opens the main window with an example
    list of flows; running a flow is not available yet.

## Starting the application

Unpack the distribution archive for your operating system and start the launcher at its root. A
Java 25 runtime has to be installed.

* Windows: `hocal-ui.bat`
* Linux and macOS: `hocal-ui.sh`

The application follows the light or dark appearance of your operating system and shows its texts
in English or German, depending on the language of your system.

## Logging

Log files are written to `.hocal-ai/logs` in your home folder (`hocal-ai.log`).
