for i in $(seq 1 15); do
	if [ $i != 12 ]; then
		if [ $i -gt 9 ]; then
			machine=c304-$i
		else
			machine=c304-0$i
		fi
		echo machine=$machine

		if [ $1 == "downloader" ]; then
			jarpath=downloader/build/libs/downloader-all.jar
		else
			jarpath=daemon/build/libs/daemon-all.jar
		fi
		bash -c "ssh $machine java -jar $jarpath -dii $2 -p /work/ $3 &"
	fi
done