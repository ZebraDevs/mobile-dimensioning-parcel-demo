# Mobile Dimensioning Parcel Demo

Sample showcasing Mobile Dimensioning capability on Zebra supported Devices with optional CSV export of the captured parcel info.

## Features

- Written in Kotlin
- DataWedge integration to scan the parcel barcode
- Ability to save on the external storage of the device the image of the captured dimensions
- Information getting tracked and saved under a CSV file after each operation:
  - Parcel ID
  - Date
  - Time
  - Length
  - Width
  - Height
  - Weight
  - Notes

## Disclaimer

Please be aware that this application is distributed as is without any guarantee of additional support or updates.

## Installation

For the installation process, you can check the released apks under this page or if you wan to compile the latest version of the source code you'll have to locally clone this repository and open the project in Android Studio.

## Export of Data

The CSV file and the images getting saved by the app, can be found under this folder:

```text
/sdcard/md-dimensioning-demo
```

## License

[MIT](LICENSE.txt)
