# Hagimule

## Run

### Build

You need java 21 and gradle.

`gradle shadowJar` will build the jars with their dependencies.

### Start Diary

`java -jar diary/build/libs/diary-all.jar -p <port>`

### Start Daemons

`java -jar daemon/build/libs/daemon-all.jar -p PATH/TO/DIR/ -dip <diary port> -t <artificial delay between each buffer (milliseconds)>`

### Download files

`java -jar downloader/build/libs/downloader-all.jar -dip <diary_port>`

## script usage

`source ./aliases.sh` : crée un alias pour lancer directement avec le nom des fichiers de l'application.

<!--
`./launch_c304.sh <[daemon|downloader]> <ip diary> <filename> <pathToHagimule>` : permet de lancer sur toutes les machines de la salle C304 le scripte :
    - soit daemon : dans ce cas filename désigne le dossier qu'on fournit au diary en chemin absolue.
    - soit downloader : dans ce cas filename est un fichier disponible dans le diary.
pathToHagimule est le chemin du HOME à hagimule.

Par exemple : `./launch_c304.sh daemon truite Downloads ~/Documents/2A/Intergiciel/hagimule` lance des daemons sur toute les machines de la salle C304
connecté au Diary Préalablement lancé sur truite. Cette fonction est dangereuse  -->

`./testdiff.sh <filename> <dir>` : affiche la différence entre le fichier `filename` dans `/work` et le fichier `filename` dans le dossier de référence `dir`.
Par exemple : `./testdiff.sh file.txt Downloads` lancé dans le Home compare sur toute les machine le fichier `/work/file.txt` et le fichier `~/Downloads/file.txt`.
