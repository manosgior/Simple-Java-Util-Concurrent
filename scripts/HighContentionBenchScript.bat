@echo off

set THREADS= 1 2 4 6 8 10 12

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java HighContentionBench %%d CLH
)

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java HighContentionBench %%d MCS
)

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java HighContentionBench %%d TTAS
)

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java HighContentionBench %%d Java
)