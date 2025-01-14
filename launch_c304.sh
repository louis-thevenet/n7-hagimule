for i in $(seq 1 15); do
	if [ $i != 12 ]; then
		if [ $i -gt 9 ]; then
			machine=c304-$i
		else
			machine=c304-0$i
		fi

		if [ $1 == "downloader" ]; then
			jarpath=$4/downloader/build/libs/downloader-all.jar
			p=" $3"
			mode="Downloader"
			path="/work/ $p"
		else
			p="$3"
			fnp=`basename $p`
			echo "copie de $fnp dans /work"
			bash -c "ssh $machine cp -r '$p /work/$fnp'"
			echo
			jarpath=$4/daemon/build/libs/daemon-all.jar
			mode="Daemon"
			path="/work/$fnp"
		fi

		echo machine=$machine
		echo "Lancement en arrière plan de $mode"
		echo
		bash -c "ssh $machine java -jar $jarpath -dii $2 -p $path &"
	fi
done
