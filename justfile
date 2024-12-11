default:
  just --list

run-all:
    tmux \
    split-window "just run diary '-d'; read" \; \
    split-window -h "just run daemon '-p . -dap 8082'; read" \; \
    split-window -v "just run daemon '-p . -dap 8083'; read" \; \

run project:
    gradle run -p {{project}}

run-args project args:
    gradle run -p {{project}} --args='{{args}}'

