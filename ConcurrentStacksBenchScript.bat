@echo off

set THREADS= 1 2 4 6 8 10 12

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Simple
)

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 10 5
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 10 10
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 10 20
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 10 30
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 20 5
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 20 10
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 20 20
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 20 30
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 30 5
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 30 10
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 30 20
	for /l %%x in (1, 1, 10) do java ConcurrentStackBench %%d Elimination 30 30
)