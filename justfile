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
    just build
    tmux \
    split-window -v "just run diary; read" \; \
    split-window -h "sleep 0.5; just daemon './test_files/machine_a' '-dap 4000'; read" \; \
    split-window -v "sleep 0.5; just daemon './test_files/machine_b' '-dap 8000'; read" \;

run project:
    java -jar {{project}}/build/libs/{{project}}-all.jar

run-args project args:
    java -jar {{project}}/build/libs/{{project}}-all.jar {{args}}

