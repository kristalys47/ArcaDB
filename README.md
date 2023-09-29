# **ArcaDB**

 Modern enterprises rely on data management systems to collect, store, and analyze vast amounts of data  related with their operations. Nowadays, clusters and hardware accelerators (e.g., GPUs, TPUs) have become a necessity  to scale with the data processing demands in many applications related to social media, bioinformatics, surveillance systems, remote sensing, and medical informatics. Given this new scenario,  the architecture of  data analytics engines must evolve to take advantage of these new technological trends. In this paper, we present ArcaDB: a disaggregated query engine that leverages container technology to place operators at compute nodes that fit their performance profile. In ArcaDB, a query plan is dispatched to worker nodes that have different compute characteristics. Each operator is annotated with the preferred type of compute node for execution, and  ArcaDB ensures that the operator gets picked up by the appropriate workers. We have implemented a prototype version of ArcaDB using Java, Python, and Docker containers. We have also completed a preliminary performance study of this prototype, using image and scientific data. This study shows that ArcaDB can speedup query performance by a factor of 3.5x in comparison with a shared-nothing, symmetric arrangement.

 Kristalys Ruiz Rohena & Dr. Manuel Rodriguez Martinez


Shield: [![CC BY-NC-SA 4.0][cc-by-nc-sa-shield]][cc-by-nc-sa]

This work is licensed under a
[Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License][cc-by-nc-sa].

[![CC BY-NC-SA 4.0][cc-by-nc-sa-image]][cc-by-nc-sa]

[cc-by-nc-sa]: http://creativecommons.org/licenses/by-nc-sa/4.0/
[cc-by-nc-sa-image]: https://licensebuttons.net/l/by-nc-sa/4.0/88x31.png
[cc-by-nc-sa-shield]: https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-lightgrey.svg
