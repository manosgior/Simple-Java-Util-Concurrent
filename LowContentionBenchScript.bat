@echo off

set THREADS= 1 2 4 6 8 10 12

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java LowContentionBench %%d CLH
)

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java LowContentionBench %%d MCS
)

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java LowContentionBench %%d TTAS
)

for %%d in (%THREADS%) do (
	for /l %%x in (1, 1, 10) do java LowContentionBench %%d Java
)