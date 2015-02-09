# HeuristicsMiner++ [![Build Status](https://travis-ci.org/delas/hmpp.svg)](https://travis-ci.org/delas/hmpp)

This [ProM 5.2](http://www.promtools.org/doku.php?id=prom52) plugin implements the HeuristicsMiner++ plugin.

A complete description of the algorithm is presented in the paper:
* A. Burattin and A. Sperduti. "[Heuristics Miner for Time Intervals](http://andrea.burattin.net/publications/2010-esann)".  In _Proceedings of ESANN_ 2010; Bruges, Belgium; April 28-30, 2010.

## Installation Instructions
To install this plugin the following steps are required:

1. grab the latest version of the plugin from the above link;
2. copy the file hmpp.jar inside the `lib/external/` folder of the current ProM 5.2 installation;
3. replace the file `lib/framework/ProM.jar` with the one contained in the downloaded zip file (a small modification to the framework was required in order to run the plugin);
4. add, at the end of the file `mining.ini` (in the root directory of the current ProM installation), a line with:
```ini
B5=it.processmining.hmpp.HMPP
```
Now the plugin should be installed!

**Important:** please consider that this plugin is just a proof-of-concept with some limitations. For example, the plugin expects that all the activities contains a start and complete events, even if their execution time is the same (in this case, the behavior will be the same of Heuristics Miner).

