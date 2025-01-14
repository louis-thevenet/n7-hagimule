for i in $(seq 1 15); do
	if [ $i != 12 ]; then
		if [ $i -gt 9 ]; then
			machine=c304-$i
		else
			machine=c304-0$i
		fi
		echo machine=$machine

		bash -c "ssh $machine diff /work/$1 $2/$1"
	fi
done