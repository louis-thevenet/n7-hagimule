for i in "diary" "daemon" "downloader"; do
	alias $i="java -jar /build/libs/$i-all.jar"
done
alias build="./gradlew shadowjar"