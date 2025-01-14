for i in $(seq 1 15); do
	if [ $i != 12 ]; then
		if [ $i -gt 9 ]; then
			machine=c304-$i
		else
			machine=c304-0$i
		fi
		echo machine=$machine
		echo

		if [ $1 == "downloader" ]; then
			jarpath=downloader/build/libs/downloader-all.jar
			p=" $3"
		else
			bash -c "ssh $machine cp -r '$HOME/Downloads /work/'"
			bash -c "ssh $machine ls /work/"
			echo 
			echo
			jarpath=daemon/build/libs/daemon-all.jar
			p="Downloads"
		fi
		bash -c "ssh $machine java -jar $jarpath -dii $2 -p /work/$p &"
	fi
done