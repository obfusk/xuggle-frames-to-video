[]: {{{1

    File        : README.md
    Maintainer  : Felix C. Stegerman <flx@obfusk.net>
    Date        : 2013-04-20

    Copyright   : Copyright (C) 2013  Felix C. Stegerman
    Version     : 0.0.2-SNAPSHOT

[]: }}}1

## TODO

  * test!
  * look at use of reflection?!

## Description
[]: {{{1

  xuggle-frames-to-video (xftv) - turn a stream of frame+timestamp
  into a video - useful for webrtc

  xftv takes output-file, width, height, and fps arguments on the
  command line; then it reads pairs of file names and timestamps from
  STDIN and uses these to create a video.

  NB: timestamps are relative to the beginning of the video and in
  nanoseconds; the output file's extension is used to determine the
  appropriate video codec.

[]: }}}1

## Usage
[]: {{{1

### Build

    $ lein uberjar
    $ export XFTVJ=/path/to/xftv-standalone.jar

### Run

    $ java -jar "$XFTVJ" <out-file> <width> <height> <fps>
    /path/to/first/image
    <first-image-timestamp>
    /path/to/second/image
    <second-image-timestamp>
    ...
    ^D

[]: }}}1

## License
[]: {{{1

  GPLv3 [1].

[]: }}}1

## References
[]: {{{1

  [1] GNU General Public License, version 3
  --- http://www.opensource.org/licenses/GPL-3.0

[]: }}}1

[]: ! ( vim: set tw=70 sw=2 sts=2 et fdm=marker : )
