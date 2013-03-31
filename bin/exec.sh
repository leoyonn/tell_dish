#!/bin/bash
# run the flow of sanji.

base_dir="./"
bin_dir="${base_dir}bin/"
java_bin="${bin_dir}runjava.sh"
exec_cls="sanji.tools.Executor"
cmd="${java_bin} $exec_cls $*"
$cmd;



