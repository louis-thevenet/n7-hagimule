# Hagimule

## Run

### Build

You need java 21 and gradle.

`gradle shadowJar` will build the jars with their dependencies.

### Start Diary

`java -jar diary/build/libs/diary-all.jar -p <port>`

### Start Daemons

`java -jar daemon/build/libs/daemon-all.jar -p PATH/TO/DIR/ -dip <diary port>`

### Download files

`java -jar downloader/build/libs/downloader-all.jar -dip <diary_port>`

## script usage RESTE A FAIRE

`./aliases.sh`

`./launch_c304.sh <[daemon|downloader]> <ip diary> <filename>`

Le fichier source doit être dans Downloads pour `testdiff.sh`

`./testdiff.sh <filename>`
