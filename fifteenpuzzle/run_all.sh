tests_passed=0
tests_failed=()

for i in {1..40}; do
    echo "Running test ${i}:"
    start=$(date +%s)
    java Solver.java board$(printf "%02d" $i).txt testSol.txt &
    solver_pid=$!
    timeout=30
    while ps -p $solver_pid > /dev/null && [[ $timeout -gt 0 ]]; do
        sleep 1 # Wait for 1 second while the current test is still running
        ((timeout--))
    done
    if ps -p $solver_pid > /dev/null; then
        echo "Test ${i} took longer than 30 seconds. Stopping..."
        kill -9 $solver_pid > /dev/null 2>&1 # Kill the solver process forcefully
        tests_failed+=("$i")
        continue
    fi
    end=$(date +%s)
    runtime=$((end-start))
    ((tests_passed++))
done

echo "Passed ${tests_passed} tests out of 40."

echo "Failed tests:"
for test in "${tests_failed[@]}"; do
    echo "$test"
done

