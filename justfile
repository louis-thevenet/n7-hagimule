default:
  just --list

build:
    gradle shadowJar

diary:
    just run diary

daemon path args:
    just run-args daemon "-p {{path}} {{args}}"

downloader args:
    just run-args downloader "{{args}}"

run-all:
    tmux \
    split-window -v "just run diary; read" \; \
    split-window -h "sleep 1; just daemon './test_files/machine_a' '-dap 8082'; read" \; \
    split-window -v "sleep 1; just daemon './test_files/machine_b' '-dap 8083'; read" \;

run project:
    java -jar {{project}}/build/libs/{{project}}-all.jar

run-args project args:
    java -jar {{project}}/build/libs/{{project}}-all.jar {{args}}

