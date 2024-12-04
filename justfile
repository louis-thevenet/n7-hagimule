default:
  just --list

run-all:
    tmux \
    split-window "just run diary '-d'; read" \; \
    split-window -h "just run daemon '-p . -dap 8082'; read" \; \
    split-window -v "just run daemon '-p . -dap 8083'; read" \; \

run project args:
    gradle run -p {{project}} --args='{{args}}'

