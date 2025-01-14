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
		else
			p="$3"
			echo "copie de $p dans /work"
			bash -c "ssh $machine cp -r '$p /work/'"
			fnp=`basename $p`
			echo
			jarpath=$4/daemon/build/libs/daemon-all.jar
			mode="Daemon"
		fi

		echo machine=$machine
		echo "Lancement en arrière plan de $mode"
		echo
		bash -c "ssh $machine java -jar $jarpath -dii $2 -p /work/$fnp$p &"
	fi
done
echo nettoyage
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
			p="$3"
		else
			p="$3"
		fi
		echo "suppression en arrière plan de /work/$p"
		bash -c "ssh $machine rm -r /work/$p &"
	fi
done