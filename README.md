# hagimule

## Build

```bash
mvn package

# Start Server
java -cp ./diary/target/daemon-1.0-SNAPSHOT.jar hagimule.App

# Start Daemnon
java -cp ./daemon/target/daemon-1.0-SNAPSHOT.jar hagimule.App

# Run Client
java -cp ./downloader/target/downloader-1.0-SNAPSHOT.jar hagimule.App
```

## Archi

- Un serveur annuaire implémentation HashMap<String, Tuple Class Int \* List<Host>>
- Un client possede des fichiers qui publient sur l'annuaire
  - Il peut aussi télécharger d'autre fichier sur disponible sur le serveur annuaire.
    Le téléchargement doitêtre découpé en plusieurs morceau de ton fichier.

## Améliorations visées

Le client peut vérifier l'authenticité
du fichier grace au checksum donnée avec la liste donnée par l'annuaire des hosts qui le possèdent.

## Install Maven

```
#!/bin/sh

mvn_version=${mvn_version:-3.9.9}
url="https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz"
install_dir="../maven"

mkdir ${install_dir}
curl -fsSL ${url} | tar zx --strip-components=1 -C ${install_dir}
cat << EOF > ../profile.d/maven.sh
#!/bin/sh
export MAVEN_HOME=${install_dir}
export M2_HOME=${install_dir}
export M2=${install_dir}/bin
export PATH=${install_dir}/bin:$PATH
EOF
source ../profile.d/maven.sh
echo maven installed to ${install_dir}
mvn --version
```
