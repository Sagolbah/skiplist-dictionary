# Authenticated Dictionary with Skip Lists

This repository is an implementation of cryptographically-protected data structure
based on article [Efficient Authenticated Dictionaries with Skip Lists and Commutative Hashing](https://www.cs.jhu.edu/~goodrich/cgc/pubs/hashskip.pdf)
written by Michael T. Goodrich and Roberto Tamassia.

All benchmark results can be found in `SkipList_presentation.pptx` file. The benchmark source can be found in `test` directory.
Examples of using also can be found in tests (Note that first tests are for basic functionality, without proof checking).

According to original article, this structure requires commutative hash function, so that `h(x, y) = h(y, x)`. We used
SHA-256 as the main hash function and modified it for commutativeness. See `CommutativeHashing.java` file for details.

Authors:
* Daniil Boger ([Sagolbah](https://github.com/Sagolbah)) - architecture, code, basic functionality test suite
* Ravil Galiev ([Mr-Ravil](https://github.com/Mr-Ravil)) - advanced test suite and benchmarks, debugging
* Andrew Tsutsiev ([AT4-CGSG](https://github.com/AT4-CGSG)) - presentation, benchmarks
